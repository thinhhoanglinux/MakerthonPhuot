package com.example.tkkil.phuot.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tkkil.phuot.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    TextView txtvForgot, txtvRegister;
    Button btnLogin;
    private EditText edtEmail, edtPwd;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoading;
    private RelativeLayout main;
    //Dialog
    private EditText edtEmailDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mLoading = new ProgressDialog(this, R.style.MyDialogTheme);
        mLoading.setTitle("Loading");
        mLoading.setMessage("Please wait...");
        init();

        edtEmail.setText("hoangquocthinh134@gmail.com");
        edtPwd.setText("111111");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                if (!validate()) {
                    return;
                }
                Login();
                break;
            case R.id.txtvRegister:
                startActivity(new Intent(this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_right_to_left_in, R.anim.slide_default);
                break;
            case R.id.txtvForgot:
                forGotDialog();
                break;
            default:
                break;
        }
    }

    private void forGotDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        ab.setTitle("Send A Password Reset Email");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_send_reset_password, null);
        ab.setView(view);
        edtEmailDialog = view.findViewById(R.id.edtEmailDialog);
        ab.setPositiveButton("Send", null);
        ab.setNegativeButton("Cancel", null);

        final AlertDialog alertDialog = ab.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                Button Positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!validateDialog()) {
                            return;
                        }
                        mAuth.sendPasswordResetEmail(edtEmailDialog.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Snackbar.make(main, "Email sent. Please check email!", Snackbar.LENGTH_SHORT).show();
                                            alertDialog.dismiss();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(main, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void Login() {
        mLoading.show();
        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString().trim(), edtPwd.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(main, "Success", Snackbar.LENGTH_SHORT).show();
                            mLoading.dismiss();
                        } else {
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

    private boolean validate() {
        boolean isValidate = true;
        if (TextUtils.isEmpty(edtEmail.getText().toString().trim())) {
            edtEmail.setError("Please enter Email!");
            isValidate = false;
        }
        if (TextUtils.isEmpty(edtPwd.getText().toString().trim())) {
            edtPwd.setError("Please enter Password!");
            isValidate = false;
        } else {
            if (edtPwd.getText().toString().trim().length() < 6) {
                edtPwd.setError("Password is too short");
                isValidate = false;
            }
        }
        return isValidate;
    }

    private boolean validateDialog() {
        boolean isValidate = true;
        if (TextUtils.isEmpty(edtEmailDialog.getText().toString().trim())) {
            edtEmailDialog.setError("Please enter Email!");
            isValidate = false;
        }
        return isValidate;
    }

    private void init() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPwd = findViewById(R.id.edtPwd);
        txtvForgot = findViewById(R.id.txtvForgot);
        txtvRegister = findViewById(R.id.txtvRegister);
        btnLogin = findViewById(R.id.btnLogin);
        main = findViewById(R.id.main);

        btnLogin.setOnClickListener(this);
        txtvForgot.setOnClickListener(this);
        txtvRegister.setOnClickListener(this);
    }
}
