package com.example.cloneinstagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cloneinstagram.R;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUserID, inputUserPW;
    private TextView buttonCreateAccount;
    private Button buttonLogin;
    private ProgressBar progressBar;

    private FirebaseAuth authRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRef = ConfigurateFirebase.getFirebaseAuth();

        if(authRef.getCurrentUser()!=null){

            openMainActivity();

        }else {
            setContentView(R.layout.activity_login);

            loadInterface();

            buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openRegisterActivity();
                }
            });

            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    validateText();
                }
            });
        }
    }

    private void loadInterface(){

        inputUserID = findViewById(R.id.inputUserID);
        inputUserPW = findViewById(R.id.inputUserPW);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.textButtonCreateAccount);
        progressBar = findViewById(R.id.progressLoginBar);
    }

    private void validateText(){
        String userID, userPW;


        userID = inputUserID.getText().toString();
        userPW = inputUserPW.getText().toString();

        if (userID!=null && !userID.isEmpty()) {
            if(userPW!=null && !userPW.isEmpty()) {

                progressBar.setVisibility(View.VISIBLE);
                authenticateUser(userID, userPW);

            }else {throwToast("Password empty: \nInsert your password.", true);}
        }else {throwToast("Email empty: \nInsert your email.", true);}

    }

    public void authenticateUser(String userID, String userPW){

        authRef.signInWithEmailAndPassword
                    (userID, userPW)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    openMainActivity();
                                    progressBar.setVisibility(View.GONE);

                                }else {
                                    String exception;

                                    try { throw task.getException(); }
                                    catch (FirebaseAuthInvalidUserException e)
                                    { exception = e.getMessage(); }
                                    catch (FirebaseAuthInvalidCredentialsException e)
                                    { exception = e.getMessage(); }
                                    catch (Exception e){ exception = e.getMessage(); }

                                    throwToast(exception, true);
                                }
                            }
                        });
    }

   /** ##############################     OPEN ACTIVITIES     ################################# **/
    private void openRegisterActivity(){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    private void openMainActivity(){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

   /** ##############################        UTILITIES        ################################# **/
    private void throwToast(String msgText, boolean longToast) {


        if (longToast) {

            Toast.makeText(this,
                    msgText,
                    Toast.LENGTH_LONG).show();

        } else {

            Toast.makeText(this,
                    msgText,
                    Toast.LENGTH_SHORT).show();

        }
    }
}




