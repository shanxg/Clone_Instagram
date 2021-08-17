package com.example.cloneinstagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cloneinstagram.R;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.helper.UserFirebase;
import com.example.cloneinstagram.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth authRef;


    private EditText editRegUserID, editRegUserName, editRegUserPW;
    private Button buttonRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loadInterface();
        authRef = ConfigurateFirebase.getFirebaseAuth();

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { validateText();

            }
        });
    }

    private void loadInterface(){

        editRegUserID = findViewById(R.id.editRegUserID);
        editRegUserName = findViewById(R.id.editRegUserName);
        editRegUserPW = findViewById(R.id.editRegUserPW);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressRegBar);
    }

    private void validateText(){
        String userName, userEmail, userPW;

        userName = editRegUserName.getText().toString();
        userEmail = editRegUserID.getText().toString();
        userPW = editRegUserPW.getText().toString();


        if(userName!=null && !userName.isEmpty()){
            if (userEmail!=null && !userEmail.isEmpty()) {
                if(userPW!=null && !userPW.isEmpty()) {

                    User user = new User(userName, userEmail, userPW);
                    registerUser(user);

                }else {throwToast("Password empty:\n Insert your password.", true);}
            }else {throwToast("Email empty:\n Insert your email.", true);}
        }else {throwToast("Name empty:\n Insert your name.", true);}
    }

    private void registerUser(final User user){

        progressBar.setVisibility(View.VISIBLE);

        authRef.createUserWithEmailAndPassword(
                    user.getEmail(), user.getPw())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        user.setUserID(task.getResult().getUser().getUid());

                                        try {

                                            UserFirebase.updateUserProfName(user.getName());
                                            user.setDisplayName(user.getName());
                                            user.save();



                                            throwToast("User registered succesfully.", true);
                                            progressBar.setVisibility(View.GONE);
                                            openMainActivity();

                                        }catch (Exception e){
                                            e.printStackTrace();
                                            throwToast(e.getMessage(), true);
                                        }

                                    } else {

                                        progressBar.setVisibility(View.GONE);
                                        String exception;

                                        try {  throw task.getException();  }
                                        catch (FirebaseAuthWeakPasswordException e)
                                        { exception = e.getMessage(); }
                                        catch (FirebaseAuthInvalidCredentialsException e)
                                        { exception = e.getMessage(); }
                                        catch (FirebaseAuthUserCollisionException e)
                                        { exception = e.getMessage(); }
                                        catch(Exception e){ exception = e.getMessage(); }

                                        throwToast(exception, true);
                                    }
                            }
                        });
    }

    /** ##############################     OPEN ACTIVITIES     ################################# **/
    private   void openMainActivity(){
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