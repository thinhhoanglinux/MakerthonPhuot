package com.example.tkkil.phuot.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.tkkil.phuot.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordActivity extends AppCompatActivity {
    Button btnChangePwd;
    private FirebaseAuth mAuth;
    private EditText edtOldPwd, edtNewPwd, edtRePwd;
    private RelativeLayout main;
    private ProgressDialog mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        mAuth = FirebaseAuth.getInstance();
        mLoading = new ProgressDialog(this, R.style.MyDialogTheme);
        mLoading.setTitle("Loading");
        mLoading.setMessage("Please wait...");
        init();
        initToolbar();
    }

    private boolean Validate() {
        boolean isValidate = true;
        if (TextUtils.isEmpty(edtOldPwd.getText().toString().trim())) {
            edtOldPwd.setError("Please enter Old Password!");
            isValidate = false;
        }
        if (TextUtils.isEmpty(edtNewPwd.getText().toString().trim())) {
            edtNewPwd.setError("Please enter New Password!");
            isValidate = false;
        }
        if (TextUtils.isEmpty(edtRePwd.getText().toString().trim())) {
            edtRePwd.setError("Please enter Re Password!");
            isValidate = false;
        }
        return isValidate;
    }

    private void init() {
        main = findViewById(R.id.main);
        edtOldPwd = findViewById(R.id.edtOldPwd);
        edtNewPwd = findViewById(R.id.edtNewPwd);
        edtRePwd = findViewById(R.id.edtRePwd);
        btnChangePwd = findViewById(R.id.btnChangePwd);

        btnChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Validate()) {
                    return;
                }
                onChangePwd();
            }
        });
    }

    private void onChangePwd() {
        mLoading.show();
        mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(), edtOldPwd.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (edtNewPwd.getText().toString().trim().equals(edtRePwd.getText().toString().trim())) {
                                mAuth.getCurrentUser().updatePassword(edtRePwd.getText().toString().trim())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Snackbar.make(main, "Success", Snackbar.LENGTH_SHORT).show();
                                                    mLoading.dismiss();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(main, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                mLoading.dismiss();
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(main, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        mLoading.dismiss();
                    }
                });

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
