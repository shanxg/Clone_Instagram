package com.example.cloneinstagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cloneinstagram.R;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.helper.SystemPermissions;
import com.example.cloneinstagram.helper.UserFirebase;
import com.example.cloneinstagram.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.cloneinstagram.activity.MainActivity.SELECT_CAMERA;
import static com.example.cloneinstagram.activity.MainActivity.SELECT_GALLERY;
import static com.example.cloneinstagram.activity.MainActivity.needPermissions;

public class MyProfileActivity extends AppCompatActivity {

    private int vis = 1;

    private TextView buttonChangeImage;
    private TextInputEditText editUserName, editUserQuote, editUserEmail;
    private CircleImageView editActivityProfileCircleImageView;
    private Button buttonSaveChanges;
    private ImageButton buttonOpenCamera, buttonOpenGallery;
    private LinearLayout movLayout;
    private ConstraintLayout myProfileLayout;

    private StorageReference myStorage;
    private FirebaseUser myUser;
    private User loggedUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        toolbar.setTitle("Edit Profile");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        SystemPermissions.validatePermissions(needPermissions, this, 1);

        loadInterface();
        myProfileLayoutClickListener();

        myStorage = ConfigurateFirebase.getStorage();
        myUser = UserFirebase.getMyUser();


        if(myUser!=null){

            editUserName.setText(myUser.getDisplayName());
            editUserEmail.setText(myUser.getEmail());

            DatabaseReference dbRef = ConfigurateFirebase.getFireDBRef();
            DatabaseReference userRef  = dbRef.child("users").child(UserFirebase.getCurrentUserID());

            userRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            loggedUser = dataSnapshot.getValue(User.class);

                            String quote = loggedUser.getQuote();

                            if(quote!=null){
                                editUserQuote.setText(quote);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

            if(myUser.getPhotoUrl() != null) {

                Uri profImage = myUser.getPhotoUrl();

                    Glide.with(MyProfileActivity.this)
                            .load(profImage)
                            .into(editActivityProfileCircleImageView);

            }else
                editActivityProfileCircleImageView.setImageResource(R.drawable.padrao);

        }else
            editUserQuote.setText("logged user data is null");
    }

    private void loadInterface(){

        movLayout = findViewById(R.id.movLayout);

        buttonOpenCamera = findViewById(R.id.buttonOpenCamera);
        buttonOpenGallery = findViewById(R.id.buttonOpenGallery);
        buttonChangeImage = findViewById(R.id.buttonChangeImage);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);

        editUserName = findViewById(R.id.editUserName);
        editUserQuote = findViewById(R.id.editUserQuote);
        editUserEmail = findViewById(R.id.editUserEmail);
        editUserEmail.setFocusable(false);

        editActivityProfileCircleImageView = findViewById(R.id.editActivityProfileCircleImageView);
    }

    private  void openImageResources(){

        if (vis == 1) {
            movLayout.setVisibility(View.VISIBLE);
            vis = 0;
            movLayoutClickListener();
        } else {
            movLayout.setVisibility(View.GONE);
            vis = 1;
            movLayoutClickListener();
            myProfileLayout.setOnClickListener(null);
        }

    }

    private  void closeImageResources(){

        vis = 0;
        openImageResources();

    }

    private void saveProfileData(){

        String updatedName = editUserName.getText().toString();

        if (!updatedName.isEmpty()) {

            UserFirebase.updateUserProfName(updatedName);
            loggedUser.setDisplayName(updatedName);

            String newQuote = editUserQuote.getText().toString();

            if( !newQuote.isEmpty()) {
                loggedUser.setQuote(newQuote);
            }

            loggedUser.update();

            finish();
        }
        else
            {throwToast("Empty User Name", true);}
    }

    public void updateProfileImage(Uri imageUrl){

        if ( UserFirebase.updateUserProfImage(imageUrl) ){

            throwToast("Upload succesful:\n Profile image changed", false);
            loggedUser.setxPhoto(imageUrl.toString());
            loggedUser.update();
        }
    }

    /** ##############################    CLICK LISTENERS    ################################# **/

    private void myProfileLayoutClickListener(){

        movLayoutClickListener();

        buttonOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECT_GALLERY);
                }
            }
        });

        buttonOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECT_CAMERA);
                }
            }
        });

        buttonSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveProfileData();


            }
        });

        buttonChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openImageResources();

            }
        });

    }

    private void movLayoutClickListener(){

        if(vis == 0) {
            myProfileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeImageResources();
                }
            });
        }else {
            myProfileLayout = findViewById(R.id.myProfileLayout);
        }
    }

    /** ##############################    ACTIVITY PROCESS    ################################# **/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        openImageResources();

        if(resultCode == RESULT_OK){
            Bitmap image = null;

            try {

                switch (requestCode){
                    case SELECT_CAMERA:
                        image = (Bitmap) data.getExtras().get("data");

                        break;
                    case SELECT_GALLERY:
                        Uri selectedImage = data.getData();
                        image = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);

                        break;

                }

                if(image != null){

                    editActivityProfileCircleImageView.setImageBitmap( image );

                    StorageReference myImageRef =
                            myStorage.child("images")
                                    .child(UserFirebase.getCurrentUserID())
                                    .child("profile")
                                    .child( UserFirebase.getCurrentUserID() + ".jpeg");


                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] imageData = baos.toByteArray();

                    UploadTask uploadTask = myImageRef.putBytes(imageData);

                    uploadTask
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    throwToast("Upload error:\n"+e.getMessage(),true);

                                }
                            })
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    Uri imageUrl = taskSnapshot.getDownloadUrl();
                                    updateProfileImage(imageUrl);
                                }
                            });
                }

            }catch (Exception e){
                e.printStackTrace();

            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissionResult: grantResults){
            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                permisionValidationAlert();
            }
        }
    }

    private void permisionValidationAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions denied:");
        builder.setMessage("To keep using the App, you need to accept the Requested permissions.");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SystemPermissions.validatePermissions(needPermissions, MyProfileActivity.this, 1);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
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