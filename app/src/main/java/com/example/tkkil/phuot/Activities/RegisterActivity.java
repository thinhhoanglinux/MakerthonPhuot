package com.example.tkkil.phuot.Activities;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tkkil.phuot.Models.User;
import com.example.tkkil.phuot.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtEmail, edtPwd, edtUser, edtFull;
    TextView txtvBack;
    Button btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private ProgressDialog mLoading;
    private RelativeLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        mLoading = new ProgressDialog(this, R.style.MyDialogTheme);
        mLoading.setTitle("Loading");
        mLoading.setMessage("Please wait...");
        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRegister:
                if (!validate()) {
                    return;
                }
                Register();
                break;
            case R.id.txtvBack:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_default, R.anim.slide_left_to_right_out);
    }

    private void Register() {
        mLoading.show();
        mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString().trim(), edtPwd.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser mUser = mAuth.getCurrentUser();
                            if(mUser!=null){
                                mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Snackbar.make(main, "Please check email for verification!", Snackbar.LENGTH_SHORT).show();
                                        mLoading.dismiss();
                                        User user = new User(edtFull.getText().toString().trim(), edtUser.getText().toString().trim(), mAuth.getCurrentUser().getEmail(),
                                                mAuth.getCurrentUser().getUid(), "", "", "", "", null);
                                        mRef.child("Users/" + mAuth.getCurrentUser().getUid() + "/").setValue(user);
                                    }
                                });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mLoading.dismiss();
                        Snackbar.make(main, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validate() {
        boolean isValidate = true;
        if (TextUtils.isEmpty(edtUser.getText().toString().trim())) {
            edtUser.setError("Please enter Username!");
            isValidate = false;
        }
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

    private void init() {
        edtUser = findViewById(R.id.edtUser);
        edtEmail = findViewById(R.id.edtEmail);
        edtPwd = findViewById(R.id.edtPwd);
        edtFull = findViewById(R.id.edtFull);
        txtvBack = findViewById(R.id.txtvBack);
        btnRegister = findViewById(R.id.btnRegister);
        main = findViewById(R.id.main);

        btnRegister.setOnClickListener(this);
        txtvBack.setOnClickListener(this);
    }
}
