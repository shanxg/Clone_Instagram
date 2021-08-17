package com.example.cloneinstagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cloneinstagram.R;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.fragment.FeedFragment;
import com.example.cloneinstagram.helper.BitmapHelper;
import com.example.cloneinstagram.helper.DateUtil;
import com.example.cloneinstagram.helper.UserFirebase;
import com.example.cloneinstagram.model.Post;
import com.example.cloneinstagram.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;


import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {

    private String userID;

    private User loggedUser;

    private StorageReference myStorage;

    private ImageView postImageView;
    private TextInputEditText inputPostTitle, inputPostDescription;

    private MenuItem menuItem;

    private Bitmap image;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        toolbar.setTitle("Publish");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postImageView = findViewById(R.id.postImageView);
        inputPostTitle = findViewById(R.id.inputPostTitle);
        inputPostDescription = findViewById(R.id.inputPostDescription);

        userID = UserFirebase.getCurrentUserID();

        loggedUser = MainActivity.loggedUser;

        getMyBundleData();
        configurateFirebase();
    }

    public void getMyBundleData() {

        image = BitmapHelper.getInstance().getBitmap();

        if (image != null) {
            postImageView.setImageBitmap(image);
        }
    }

    private void configurateFirebase() {
        myStorage = ConfigurateFirebase.getStorage();

    }

    private void validateText() {
        String postTitle, postDescription;

        postTitle = inputPostTitle.getText().toString();
        postDescription = inputPostDescription.getText().toString();


        if (postTitle != null && !postTitle.isEmpty()) {

            Post post = new Post();

            post.setPostTitle(postTitle);
            if (postDescription != null) post.setPostDescrition(postDescription);

            uploadImage(post);

        } else {
            throwToast("Title empty:\n Insert a title.", true);
        }
    }

    private void uploadImage(final Post post) {

        // SAVING IMAGE IN  FIREBASE

        UUID randomImageName = UUID.randomUUID();
        StorageReference myImageRef =
                myStorage.child("images")
                        .child(userID)
                        .child("posts")
                        .child(randomImageName + ".jpeg");

        if (image != null) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            UploadTask uploadTask = myImageRef.putBytes(imageData);

            uploadTask
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            throwToast("Upload error:\n" + e.getMessage(), true);

                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri imageUrl = taskSnapshot.getDownloadUrl();

                            Post finalPost = post;
                            finalPost.setPostPhoto(imageUrl.toString());
                            finalPost.setPostDate(DateUtil.actualDate());

                            registerPost(finalPost);
                        }
                    });
        } else throwToast("Empty image data", true);

    }

    private void registerPost(final Post finalPost) {

        User postUser = new User();
        postUser.setDisplayName(loggedUser.getDisplayName());
        postUser.setUserID(loggedUser.getUserID());
        postUser.setxPhoto(loggedUser.getxPhoto());
        if(loggedUser.getQuote()!=null)
            postUser.setQuote(loggedUser.getQuote());

        boolean isSaved = finalPost.save(postUser);

        if(!isSaved){
            throwToast("Upload error", true);
        }else {
            throwToast("Post salvo com sucesso", false);
            BitmapHelper.getInstance().clear();
            incrementPostsCount();
        }

        menuItem.setEnabled(true);
    }

    private void incrementPostsCount(){
        UserFirebase.getUserDataSingleValue(
                userID, "postsCount",
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            int postsCount = dataSnapshot.getValue(Integer.class);
                            postsCount++;

                            dataSnapshot.getRef().setValue(postsCount);

                            goMainActivity();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /** ##############################     OPEN ACTIVITIES    ################################# **/

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        ActivityCompat.finishAffinity(this);
    }

    /** #############################    ACTIVITY PROCESSES    ################################ **/



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_post, menu);

        menuItem = menu.findItem(R.id.menuGoForward);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuGoForward) {
            validateText();
            menuItem.setEnabled(false);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** ##############################        UTILITIES       ################################# **/

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
