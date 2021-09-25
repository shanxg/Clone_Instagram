package com.example.cloneinstagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.cloneinstagram.R;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.fragment.FeedFragment;

import com.example.cloneinstagram.fragment.ProfileFragment;
import com.example.cloneinstagram.fragment.SearchFragment;
import com.example.cloneinstagram.helper.BitmapHelper;
import com.example.cloneinstagram.helper.SystemPermissions;
import com.example.cloneinstagram.helper.UserFirebase;
import com.example.cloneinstagram.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;


public class MainActivity extends AppCompatActivity {

    public static User loggedUser;

    public static final int SELECT_CAMERA = 100;
    public static final int SELECT_GALLERY = 200;

    public static final String[] needPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private int itemID;
    private Menu menu;

    private FirebaseAuth authRef;

    private DatabaseReference userRef;
    private ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        toolbar.setTitle("Clone Instagram");
        setSupportActionBar(toolbar);

        authRef = ConfigurateFirebase.getFirebaseAuth();
        if(authRef.getCurrentUser()==null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        UserFirebase.getUserData(UserFirebase.getCurrentUserID(),
                valueEventListener(UserFirebase.GET_LOGGED_USER_DATA));
        //  STARTS ACTIVITY AFTER LOGGED USER IS SET

    }

    private void configurateDatabase(){
        userListener =  valueEventListener(UserFirebase.GET_USER_DATA);

        userRef = ConfigurateFirebase.getFireDBRef().child("users").child(UserFirebase.getCurrentUserID());
        userRef.addValueEventListener(userListener);
    }

    private void startActivity(){
        configurateBottomNavView();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.viewPager, new FeedFragment()).commit();

    }


    private void configurateBottomNavView(){
        BottomNavigationViewEx bottomNavView = findViewById(R.id.bottomNavView);
        bottomNavView.enableAnimation(true);
        bottomNavView.setTextVisibility(true);
        bottomNavView.enableItemShiftingMode(false);
        bottomNavView.enableShiftingMode(false);

        enableNavigator(bottomNavView);

    }

    private void enableNavigator(BottomNavigationViewEx viewEx){

        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemID = item.getItemId();

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                modifyToolbar(itemID);

                switch (itemID){
                    case R.id.menu_home:
                        fragmentTransaction.replace(R.id.viewPager, new FeedFragment()).commit();
                        return true;

                    case R.id.menu_search:
                        fragmentTransaction.replace(R.id.viewPager, new SearchFragment()).commit();
                        return true;

                    case R.id.menu_post:
                        SystemPermissions.validatePermissions(needPermissions, MainActivity.this, 1);

                        return true;

                    case R.id.menu_profile:
                        fragmentTransaction.replace(R.id.viewPager, new ProfileFragment()).commit();
                        return true;
                }
                return false;
            }
        });

    }

    public void modifyToolbar(int item){
        clearToolbar(item);

        switch (item){
            case R.id.menu_home:
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                break;

            case R.id.menu_search:
                getSupportActionBar().setTitle("Search Users");
                break;

            case R.id.menu_post:
                getSupportActionBar().setTitle("New Post");
                break;

            case R.id.menu_profile:
                getSupportActionBar().setTitle(authRef.getCurrentUser().getDisplayName());
                break;
        }
    }

    private void clearToolbar(int item){
        MenuInflater mainMenuInflater = getMenuInflater();

        if (item != R.id.menu_post) {

            itemID = item;

            if (menu.findItem(R.id.menuOpenGallery) != null
                    && menu.findItem(R.id.menuOpenCamera) != null) {

                menu.removeItem(R.id.menuOpenGallery);
                menu.removeItem(R.id.menuOpenCamera);
            }
            if (menu.findItem(R.id.menuSignOut) == null
                    && menu.findItem(R.id.menuSettings) == null) {

                mainMenuInflater.inflate(R.menu.menu_main, menu);
            }
        }else {
            if (menu.findItem(R.id.menuSignOut) != null
                    && menu.findItem(R.id.menuSettings) != null) {

                menu.removeItem(R.id.menuSettings);
                menu.removeItem(R.id.menuSignOut);
            }

            if (menu.findItem(R.id.menuOpenGallery) == null
                    && menu.findItem(R.id.menuOpenCamera) == null) {

                mainMenuInflater.inflate(R.menu.menu_new_post, menu);
            }
        }
    }




    /** ##############################    ACTIVITY PROCESSES    ################################# **/

    @Override
    protected void onStart() {
        super.onStart();
        configurateDatabase();
    }


    @Override
    protected void onStop() {
        super.onStop();
        userRef.removeEventListener(userListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mainMenuInflater = getMenuInflater();
        mainMenuInflater.inflate(R.menu.menu_main, menu);
        this.menu = menu;


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSettings:
                startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                break;
            case R.id.menuSignOut:
                signOut();
                throwToast("Signed out", false);
                break;
            case R.id.menuOpenCamera:
                openCamera();
                break;

            case R.id.menuOpenGallery:
                openGallery();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        modifyToolbar(itemID);

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

                    BitmapHelper.getInstance().setBitmap(image);
                    openFilterActivity();

                }
            }catch (Exception e){
                e.printStackTrace();

            }
        }
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
        builder.setMessage("To keep using the App, you need to accept the requested permissions.");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /** ##############################     OPEN ACTIVITIES     ################################# **/

    private void signOut(){

        try {
            authRef.signOut();
        }catch (Exception e){
            throwToast(e.getMessage(), true);
        }

        if(authRef.getCurrentUser() == null){
            startActivity( new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

    }

    private void openCamera(){

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intentCamera.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intentCamera, SELECT_CAMERA);

    }

    private void  openGallery(){

        Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intentGallery.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intentGallery, SELECT_GALLERY);

    }

    private void openFilterActivity(){
        Intent iFA = new Intent(this, FiltersActivity.class);
        startActivity(iFA);
        finish();
    }

    /** ##############################        UTILITIES        ################################# **/

    private void throwToast(String msgText, boolean lengthLong){

        if(lengthLong) {

            Toast.makeText(this,
                    msgText,
                    Toast.LENGTH_LONG).show();

        }else {

            Toast.makeText(this,
                    msgText,
                    Toast.LENGTH_SHORT).show();

        }
    }

    private ValueEventListener valueEventListener (final int requestCode){
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                switch (requestCode){
                    case UserFirebase.GET_LOGGED_USER_DATA:
                        loggedUser = dataSnapshot.getValue(User.class);
                        if(loggedUser!=null)
                            startActivity();
                        break;
                    case UserFirebase.GET_USER_DATA:
                        loggedUser = dataSnapshot.getValue(User.class);
                        break;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }
}

