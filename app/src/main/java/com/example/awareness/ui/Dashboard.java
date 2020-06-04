package com.example.awareness.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.awareness.Constants;
import com.example.awareness.Module;
import com.example.awareness.R;
import com.example.awareness.ui.learningactivity.LearningActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.awareness.Constants.User;
import static com.example.awareness.ui.learningactivity.LearningActivity.learningAdapter;
import static com.example.awareness.ui.learningactivity.LearningActivity.modules;
import static com.example.awareness.ui.learningactivity.LearningActivity.progressBar;

public class Dashboard extends AppCompatActivity {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    SharedPreferences preferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    public void certificate(MenuItem item) {
    }

    public void FormStudents(MenuItem item) {
        Intent intent = new Intent(this, Form.class);
        intent.putExtra("mode", Constants.Forms.FORM_STUDENTS);
        startActivity(intent);
    }

    public void FormKit(MenuItem item) {
        Intent intent = new Intent(this, Form.class);
        intent.putExtra("mode", Constants.Forms.FORM_KIT);
        startActivity(intent);
    }

    public void about(MenuItem item) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }
    public void aboutngo(MenuItem item) {
        Intent intent = new Intent(this, AboutNgo.class);
        startActivity(intent);
    }

    public void logout(MenuItem item) {
        final AlertDialog.Builder logout = new AlertDialog.Builder(this);

        View layout = getLayoutInflater().inflate(R.layout.logout_layout, null, false);
        final EditText entryName = layout.findViewById(R.id.entry_name);
        logout.setView(layout);
        logout.setCancelable(true);

        final AlertDialog logoutDialog = logout.create();


        layout.findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entryName.getText().toString().equals(Constants.name_all)) {
//                    preferences.getAll().clear();
                    SharedPreferences preferences =getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    startActivity(new Intent(Dashboard.this, LoginActivity.class));
                    finish();
                    startActivity(new Intent(Dashboard.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(Dashboard.this, "गलत नाम डाला जा रहा है", Toast.LENGTH_SHORT).show();
                    entryName.getText().clear();
                }
            }
        });

        layout.findViewById(R.id.btn_logout_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDialog.cancel();

            }
        });
        logoutDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        TextView welcomeText = findViewById(R.id.welcome);
        SharedPreferences preferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String name = preferences.getString(User.USER_NAME,null);

        if(name !=null){
            welcomeText.setText("स्वागत है " + name);
        }

        String userId = preferences.getString(User.USER_CONTACT_NUMBER,null);
        if(userId != null) {
            firestore.collection("users").document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {
                                    User.accessModule = Objects.requireNonNull(document.getLong(User.ACCESS_MODULE)).intValue();
                                    User.accessQuestion = Objects.requireNonNull(document.getLong(User.ACCESS_QUESTION)).intValue();
                                    User.progressLink = document.getBoolean(User.PROGRESS_LINK);
                                    User.progressPdf = document.getBoolean(User.PROGRESS_PDF);
                                }
                            }
                        }
                    });

            firestore.collection("modules").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        modules.clear();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Map<String, Object> attachments = document.getData();

                            modules.add(new Module(Integer.parseInt(document.getId()), document.getString("Topic"), attachments));
                        }
                        Collections.sort(modules, new SortbyModuleNumber());
                        try {
                            learningAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        } catch (Exception e) {
                            Log.e("TAGG", e.toString());
                        }

                    } else {
                        Log.e("TAGG", "Error getting modules", task.getException());
                    }
                }
            });
        }

    }

    static class SortbyModuleNumber implements Comparator<Module> {

        @Override
        public int compare(Module a, Module b) {
            return a.getModuleNumber() - b.getModuleNumber();
        }
    }


    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void learningSection(View view) {
        startActivity(new Intent(this, LearningActivity.class));
    }

    public void Formstudents(View view) {
        Intent intent = new Intent(this, Form.class);
        intent.putExtra("mode", Constants.Forms.FORM_STUDENTS);
        startActivity(intent);
    }

    public void FormKit(View view) {
        Intent intent = new Intent(this, Form.class);
        intent.putExtra("mode", Constants.Forms.FORM_KIT);
        startActivity(intent);
    }
}
