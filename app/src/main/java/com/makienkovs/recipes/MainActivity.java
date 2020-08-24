package com.makienkovs.recipes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email_recover);
        passwordField = findViewById(R.id.password);
    }

    public void click(View v) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        if (v.getId() == R.id.registration) {
            registration(email, password);
        } else if (v.getId() == R.id.signin) {
            signin(email, password);
        }
    }

    private void signin(String email, String password) {
        if (validationEmailFailed(email) || validationPasswordFailed(password)) return;
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    updateUI();
                }
            } else {
                Snackbar
                        .make(findViewById(R.id.registration_layout), R.string.auth_failed, Snackbar.LENGTH_SHORT)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show();
            }
        });
    }

    private void registration(String email, String password) {
        if (validationEmailFailed(email)) {
            Snackbar
                    .make(findViewById(R.id.registration_layout), R.string.err_email, Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show();
            return;
        }
        if (validationPasswordFailed(password)) {
            Snackbar
                    .make(findViewById(R.id.registration_layout), R.string.err_password, Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Snackbar
                        .make(findViewById(R.id.registration_layout), R.string.reg_ok, Snackbar.LENGTH_SHORT)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show();
            } else {
                Snackbar
                        .make(findViewById(R.id.registration_layout), R.string.reg_failed, Snackbar.LENGTH_SHORT)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show();
            }
        });
    }

    private boolean validationEmailFailed(String email) {
        return email == null || !email.contains("@") || !email.contains(".");
    }

    private boolean validationPasswordFailed(String password) {
        return password == null || password.length() < 6 || password.length() > 20;
    }

    //When initializing your Activity, check to see if the user is currently signed in.
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI();
        }
    }

    private void updateUI() {
        Intent intent = new Intent(this, ContentActivity.class);
        startActivity(intent);
    }

    public void forgot(View view) {
        @SuppressLint("InflateParams") final View recoverLayout = getLayoutInflater().inflate(R.layout.recover, null);
        final EditText editText = recoverLayout.findViewById(R.id.email_recover);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.recover))
                .setMessage(getString(R.string.email_to_recover))
                .setCancelable(false)
                .setView(recoverLayout)
                .setNegativeButton(getText(R.string.cancel), null)
                .setPositiveButton(getText(R.string.Ok), (dialog, which) -> {
                    String email = editText.getText().toString();
                    if (validationEmailFailed(email)) {
                        Snackbar
                                .make(findViewById(R.id.registration_layout), R.string.err_email, Snackbar.LENGTH_SHORT)
                                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                                .show();
                    } else {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Snackbar
                                                .make(findViewById(R.id.registration_layout), R.string.email_sent, Snackbar.LENGTH_SHORT)
                                                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                                                .show();
                                    } else {
                                        Snackbar
                                                .make(findViewById(R.id.registration_layout), R.string.email_not_sent, Snackbar.LENGTH_SHORT)
                                                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                                                .show();
                                    }
                                });
                    }
                })
                .create()
                .show();
    }
}