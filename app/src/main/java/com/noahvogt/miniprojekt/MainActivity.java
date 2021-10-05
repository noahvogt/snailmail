package com.noahvogt.miniprojekt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.noahvogt.miniprojekt.DataBase.Message;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Data;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.snackbar.Snackbar;
import com.noahvogt.miniprojekt.data.CustomAdapter;
import com.noahvogt.miniprojekt.data.EmailViewModel;
import com.noahvogt.miniprojekt.data.MailFunctions;
import com.noahvogt.miniprojekt.workers.DownloadWorker;
import com.noahvogt.miniprojekt.ui.show.MessageShowFragment;
import com.noahvogt.miniprojekt.data.BooleanTypeAdapter;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;



import static com.noahvogt.miniprojekt.R.id.drawer_layout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CustomAdapter.SelectedMessage, PopupMenu.OnMenuItemClickListener {

    private AppBarConfiguration mAppBarConfiguration;

    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;
    public static final String emailData = "Email";
    public static final String passwordData = "Password";
    public static final String nameData = "Name";
    public static EmailViewModel mEmailViewModel;
    public static RecyclerView recyclerView;

    private AlertDialog dialog;
    private EditText newemail_name, newemail_email, newemail_password; /* may not be private */

    SharedPreferences preferences, mailServerCredentials;

    /* leave descriptor empty */
    public MainActivity() {}


    public String getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            showToast("not null");
            for(Fragment fragment : fragments){
                showToast(fragment.toString());
                if(fragment.isVisible())
                    showToast("found visible fragment");
                    return "is gallery";
            }
        } else {
            showToast("null");}
            return null;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* define button listeners */

        Button add_email_button = (Button) findViewById(R.id.addEmailButton);
        add_email_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewEmailDialog();
            }
        });

        /*creates accountmanager by clicking on profil */
        View accountView = findViewById(R.id.accountView);
        accountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewEmailDialog();
            }
        });

        /* invoke preferences */
        preferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

        mailServerCredentials = getSharedPreferences("Credentials", Context.MODE_PRIVATE);

        /* invoke toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* invoke drawer */
        DrawerLayout drawer = findViewById(drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        /*
         Passing each menu ID as a set of Ids because each
         menu should be considered as top level destinations.
        */
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_archive, R.id.nav_spam)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        /* Lookup the recyclerview in activity layout */
        recyclerView = findViewById(R.id.recyclerView);

        final CustomAdapter adapter = new CustomAdapter(new CustomAdapter.EmailDiff(),this);



        /* Attach the adapter to the recyclerview to populate items */
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        /* get Inbox Messages in Recyclerviewer at begining is overwritten by Fragments but has to stay*/
        mEmailViewModel = new ViewModelProvider(this).get(EmailViewModel.class);
        mEmailViewModel.getInboxMessage().observe(this, messages -> {
            /* Update the cached copy of the messages in the adapter*/
            adapter.submitList(messages);
        });


        Button settingButton = findViewById(R.id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });


        /* Start email Writer*/
        FloatingActionButton message_create_button = findViewById(R.id.messageButton);
        message_create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialog = MessageCreateFragment.newInstance();
                dialog.show(getSupportFragmentManager(), "tag");

            }
        });

        /* start python instance right on startup */
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }


        /* gets the data from the Email writer and adds it to the Database */
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, MessageCreateFragment.replyIntent);

            /* Creates class for the Date when Email is written */
            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("dd.MM.yy");
            System.out.println(dNow);

         //   if (requestCode == NEW_WORD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
                Message word = new Message(
                        MessageCreateFragment.replyIntent.getStringExtra(MessageCreateFragment.EXTRA_TO),
                        null,
                        null,
                        MessageCreateFragment.replyIntent.getStringExtra(MessageCreateFragment.EXTRA_FROM),
                        ft.format(dNow),
                        MessageCreateFragment.replyIntent.getStringExtra(MessageCreateFragment.EXTRA_SUBJECT),
                        MessageCreateFragment.replyIntent.getStringExtra(MessageCreateFragment.EXTRA_MESSAGE),
                        "Draft",false);
                mEmailViewModel.insert(word);
        }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    /* better leave empty to avoid any listener disambiguity */
    public void onClick(View view) { }

    public void changeMailServerSettingsDialog(String name, String email, String password) {
        /* define View window */
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View changeMailServerSettingsView = getLayoutInflater().inflate(R.layout.mail_credentials_customizer, null);

        EditText incomingServerObject = (EditText) changeMailServerSettingsView.findViewById(R.id.custom_mail_server_incoming_server_text);
        EditText outgoingServerObject = (EditText) changeMailServerSettingsView.findViewById(R.id.custom_mail_server_outgoing_server_text);
        EditText incomingPortObject = (EditText) changeMailServerSettingsView.findViewById(R.id.custom_mail_server_incoming_port_text);
        EditText outgoingPortObject = (EditText) changeMailServerSettingsView.findViewById(R.id.custom_mail_server_outgoing_port_text);
        EditText serverUsernameObject = (EditText) changeMailServerSettingsView.findViewById(R.id.custom_mail_server_username_text);
        EditText passwordObject = (EditText) changeMailServerSettingsView.findViewById(R.id.custom_mail_server_password_text);

        /* set assumed input in corresponding fields */
        incomingServerObject.setText(MailFunctions.getImapHostFromEmail(email));
        outgoingServerObject.setText(MailFunctions.getSmtpHostFromEmail(email));
        incomingPortObject.setText("993");
        outgoingPortObject.setText("587");
        serverUsernameObject.setText(email);
        passwordObject.setText(password);

        /* TODO: add save and cancel button functionality */

        /* open View window */
        dialogBuilder.setView(changeMailServerSettingsView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    public void askForChangeMailServerSettingsDialog(String name, String email, String password) {
        /* define View window */
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        /* open View window */
        dialogBuilder.setTitle("failed to connect :(");
        dialogBuilder
                .setMessage("Do you want to further customize your mail server settings?")
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        /*if this button is clicked, close the whole fragment */
                        changeMailServerSettingsDialog(name, email, password);
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        /* if this button is clicked, close the hole fragment */
                        dialog.dismiss();
                    }
                });
        dialog = dialogBuilder.create();
        dialog.show();
    }

    public static MailServerCredentials newMailServerCredentials;
    public static SharedPreferences.Editor credentialsEditor;

    public void createNewEmailDialog(){
        /* define View window */
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View emailPopupView = getLayoutInflater().inflate(R.layout.popup, null);

        /* init text field variables */
        newemail_name = emailPopupView.findViewById(R.id.popup_material_name_asking_text);
        newemail_email = emailPopupView.findViewById(R.id.popup_material_email_asking_text);
        newemail_password = emailPopupView.findViewById(R.id.popup_material_password_asking_text);

        /* init button variables */
        Button newemail_save_button = (Button) emailPopupView.findViewById(R.id.saveButton);
        /* may not be private */
        Button newemail_cancel_button = (Button) emailPopupView.findViewById(R.id.cancelButton);


        /* open View window */
        dialogBuilder.setView(emailPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        SharedPreferences.Editor preferencesEditor = preferences.edit();
        credentialsEditor = mailServerCredentials.edit();

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }



        /* store user input */
        newemail_save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* store user input */
                String name = newemail_name.getText().toString();
                String email = newemail_email.getText().toString();
                String password = newemail_password.getText().toString();

                Data.Builder builder = new Data.Builder();
                builder.putString(emailData, email)
                        .putString(passwordData, password)
                        .putString(nameData, name);

                if (!MailFunctions.validateEmail(newemail_email) | !MailFunctions.validateName(newemail_name) |
                        !MailFunctions.validatePassword(newemail_password)) {
                    return;
                }
                /* connect to mail server */
                showToast("Probe Connection ...");
                if (MailFunctions.canConnect(MailFunctions.getImapHostFromEmail(email), email, password) == Boolean.TRUE) {
                    showToast("was able to connect");


                    /*
                    Intent intent = new Intent(getBaseContext(), DownloadWorker.class);
                    intent.putExtra("Email", email);
                    intent.putExtra("Password", password);
                    startActivity(intent);

                     */
                    //startActivityForResult(intent, MainActivity.NEW_WORD_ACTIVITY_REQUEST_CODE);

                    /* TODO: replace legacy strings down below completely with serialized settings json string */
                    preferencesEditor.putString("name", name);
                    preferencesEditor.putString("email", email);
                    preferencesEditor.putString("password", password);
                    preferencesEditor.apply();

                    Gson gson = new Gson();

                    /* read login credentials from SharedPreferences */
                    SharedPreferences initialCredentialsReader = getSharedPreferences(
                            "Credentials", Context.MODE_PRIVATE);
                    String initialReadJsonData = initialCredentialsReader.getString("data", "");
                    Type credentialsType = new TypeToken<ArrayList<MailServerCredentials>>(){}.getType();
                    ArrayList<MailServerCredentials> allUsersCredentials = gson.fromJson(initialReadJsonData, credentialsType);

                    /* check for unique email */
                    boolean newEmailIsUnique = true;
                    try {
                        for (int i = 0; i < allUsersCredentials.size(); i++) {
                            if (allUsersCredentials.get(i).getUsername().equals(email)) {
                                newEmailIsUnique = false;
                                break;
                            }
                        }
                    } catch (NullPointerException e) {
                        System.out.println("creating new arraylist for user credentials, as it seems to be empty");
                        allUsersCredentials = new ArrayList<>();
                    }

                    /* add new email account if the email hasn't been entered before */
                    if (newEmailIsUnique) {
                        allUsersCredentials.add(new MailServerCredentials(name, email, password,
                                MailFunctions.getImapHostFromEmail(email), MailFunctions.getSmtpHostFromEmail(email), 993,
                                587, ""));
                        credentialsEditor.putString("data", gson.toJson(allUsersCredentials, credentialsType));
                        credentialsEditor.putString("currentUser", email);
                        credentialsEditor.apply();
                        showToast("Success: added new email account");
                    } else {
                        showToast("Error: cannot add the same email twice");
                }
            } else {
                    askForChangeMailServerSettingsDialog(name, email, password);
            }
        }});

    newemail_cancel_button.setOnClickListener(v -> dialog.dismiss());
 }


    /* show debug output in  specific view */
    private void showSnackbar(View View, String text) {
        Snackbar.make(View, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    /* like showSnackbar(), but global and uglier */
    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void selectedMessage(Message messages, EmailViewModel emailViewModel) {
        DialogFragment dialog = MessageShowFragment.newInstance(messages, mEmailViewModel);
        dialog.show(getSupportFragmentManager(), "tag");

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:

                return true;
        }
        return false;
    }
}

