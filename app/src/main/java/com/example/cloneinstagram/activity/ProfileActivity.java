package com.example.cloneinstagram.activity;



import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.example.cloneinstagram.R;
import com.example.cloneinstagram.adapter.AdapterGrid;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.helper.UserFirebase;
import com.example.cloneinstagram.model.Post;
import com.example.cloneinstagram.model.User;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private boolean wasStopped;

    private int postsCount, followersCount, followingCount;
    private List<String> followingList = new ArrayList<>();
    private List<String> photosUrl = new ArrayList<>();
    private boolean isFollowing;

    private TextView selectedTextFollowingCount, selectedTextFollowersCount,selectedTextPostsCount, selectedTextDisplayQuote, selectedTextDisplayEmail, textViewNoPosts;
    private CircleImageView selectedProfileFragmentCircleImageView;
    private Button buttonFollow;

    private ProgressBar selectedProfileProgressBar;
    private GridView selectedProfileGridView;
    private AdapterGrid adapterGrid;

    private DatabaseReference selectedUserRef;
    private DatabaseReference postsRef;
    private ValueEventListener selectedUserListener;

    private User selectedUser, loggedUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        // Get Firebase Data

        loadInterface();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int gridWidth =  screenWidth/3 ;

        selectedProfileGridView.setColumnWidth(gridWidth);
        selectedProfileProgressBar.setVisibility(View.VISIBLE);

        getDataNLoadViewInterface();
    }

    private void getDataNLoadViewInterface(){

        //Retrieving Selected User Data

        Bundle userBundle = getIntent().getExtras();
        if(userBundle!=null && userBundle.containsKey("user")){

            selectedUser = (User) userBundle.getSerializable("user");
            loggedUser = MainActivity.loggedUser;


            setValueEventListeners();
            loadViewInterface();
            setupClickListeners();
        }
    }

    private void loadImageGrid(){
        initiateImageLoader();
        selectedProfileProgressBar.setVisibility(View.GONE);

        if(adapterGrid==null) {
            adapterGrid = new AdapterGrid(getApplicationContext(), R.layout.adapter_grid, photosUrl);
            selectedProfileGridView.setAdapter(adapterGrid);
        }

        adapterGrid.notifyDataSetChanged();

    }

    // #########################         Interface Referencing       ##############################

    private void loadInterface(){

        selectedTextFollowersCount = findViewById(R.id.textFollowersCount);
        selectedTextFollowingCount = findViewById(R.id.textFollowingCount);
        selectedTextPostsCount = findViewById(R.id.textPostsCount);
        selectedTextDisplayQuote = findViewById(R.id.textDisplayQuote);
        selectedTextDisplayEmail = findViewById(R.id.textDisplayEmail);

        buttonFollow = findViewById(R.id.buttonProfileAction);


        textViewNoPosts = findViewById(R.id.textViewNoPosts);
        selectedProfileGridView = findViewById(R.id.profileGridView);
        selectedProfileProgressBar = findViewById(R.id.profileProgressBar);

        selectedProfileFragmentCircleImageView = findViewById(R.id.profileFragmentCircleImageView);
    }

    private void loadViewInterface(){

        postsCount = selectedUser.getPostsCount();
        followingCount = selectedUser.getFollowingCount();
        followersCount = selectedUser.getFollowersCount();

        // #######################         Setting Texts         ##########################

        selectedTextPostsCount.setText(String.valueOf(postsCount));
        selectedTextFollowingCount.setText(String.valueOf(followingCount));
        selectedTextFollowersCount.setText(String.valueOf(followersCount));

        getSupportActionBar().setTitle(selectedUser.getDisplayName());
        selectedTextDisplayEmail.setText(selectedUser.getEmail());
        String quote  = selectedUser.getQuote();

        if(quote!=null && !quote.isEmpty()) {
            selectedTextDisplayQuote.setText(quote);
        }else {
            String text = '"' + selectedUser.getDisplayName() + '"';
            selectedTextDisplayQuote.setText(text);
        }

        // #######################         Setting Image         ##########################

        String profImage = selectedUser.getxPhoto();

        if(profImage!=null && !profImage.isEmpty()) {

            Uri profImageURL = Uri.parse(profImage);
            Glide.with(this).load(profImageURL).into(selectedProfileFragmentCircleImageView);

        }else {
            selectedProfileFragmentCircleImageView.setImageResource(R.drawable.padrao);
        }
    }

    private void updateFollowersCount(int followersCount, int myFollowingCount){

        try {
            if(!isFollowing){
                followersCount++;
                myFollowingCount++;

            }else {
                --followersCount;
                --myFollowingCount;
            }

            selectedUser.updateFollowersCount(followersCount);
            loggedUser.updateFollowingCount(myFollowingCount);

            saveFollower();

        }catch (Exception e){
            e.printStackTrace();
            throwToast(e.getMessage(), true);
        }
    }

    private void saveFollower(){

        try {
            if(!isFollowing){

                loggedUser.addInFollowingList(selectedUser);
                selectedUser.addInFollowersList(loggedUser);
                setFeed();

                isFollowing = true;

            }else {

                selectedUser.removeInFollowersList(loggedUser);
                loggedUser.removeInFollowingList(selectedUser);

                isFollowing = false;

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        followingState(isFollowing);
    }


    public void getFollowersCountToUpdate(String userId){
        UserFirebase.getUserDataSingleValue(
                userId, "followersCount",
                valueEventListener(UserFirebase.GET_FOLLOWERS_COUNT));
    }

    public void getFollowingList(String userId) {
        UserFirebase.getUserDataSingleValue(
                userId,"followingList",
                valueEventListener(UserFirebase.GET_FOLLOWING_LIST));
    }

    private void followingState(boolean isFollowing){

        if(isFollowing){
            buttonFollow.setText(getResources().getString(R.string.text_unfollow));

        }else {

            buttonFollow.setText(getResources().getString(R.string.text_follow));

        }
    }

    private void setFeed() {

        new Handler().post(new Runnable() {
            @Override
            public void run() {

                DatabaseReference feedRef =
                        ConfigurateFirebase.getFireDBRef()
                                .child("feed")
                                .child(selectedUser.getUserID());

                feedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            try {

                                DatabaseReference myFeedRef =
                                        ConfigurateFirebase.getFireDBRef()
                                                .child("feed")
                                                .child(UserFirebase.getCurrentUserID());

                                HashMap myFeedMap = new HashMap();

                                int i = 0;

                                for (DataSnapshot feedData : dataSnapshot.getChildren()) {
                                    i++;


                                    HashMap feedMap = (HashMap) feedData.getValue();

                                    String postUserId = (String) feedMap.get("userId");
                                    String postKey = (String) feedMap.get("postKey");


                                    if (selectedUser.getUserID().equals(postUserId)) {

                                        HashMap feed = new HashMap();

                                        feed.put("userId", postUserId);
                                        feed.put("postKey", postKey);


                                        myFeedMap.put(postKey, feed);
                                    }

                                    if (i == dataSnapshot.getChildrenCount()) {

                                        myFeedRef.updateChildren(myFeedMap);
                                    }
                                }

                            } catch (Exception e) {
                                throwToast(e.getMessage(), true);
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    private void setValueEventListeners(){

        DatabaseReference usersRef = ConfigurateFirebase.getFireDBRef().child("users");
        selectedUserRef = usersRef.child(selectedUser.getUserID());

        postsRef = ConfigurateFirebase.getFireDBRef().child("posts").child(selectedUser.getUserID());

        // SINGLE VALUE LISTENER
        UserFirebase.getUserData(UserFirebase.getCurrentUserID(),
                valueEventListener(UserFirebase.GET_LOGGED_USER_DATA));
        getFollowingList(UserFirebase.getCurrentUserID());

        // REAL-TIME LISTENER
        postsRef.addValueEventListener(valueEventListener(UserFirebase.GET_POSTS_DATA));
        selectedUserListener = selectedUserRef
                .addValueEventListener(valueEventListener(UserFirebase.GET_USER_DATA));
    }

    // #########################          Activity Cycle         ###############################

    @Override
    protected void onStart() {
        super.onStart();
        if(wasStopped) {
            getDataNLoadViewInterface();
            wasStopped = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(selectedUserListener!=null && selectedUserRef !=null) {
            selectedUserRef.removeEventListener(selectedUserListener);
        }
        postsRef.removeEventListener(valueEventListener(UserFirebase.GET_POSTS_DATA));

        wasStopped = true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    // #########################          CLICK LISTENERS         ###############################

    private void setupClickListeners(){

        buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(loggedUser!=null) {
                    getFollowersCountToUpdate(selectedUser.getUserID());
                }
            }
        });
    }

    // #########################               HELPERS             ###############################

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

    private ValueEventListener valueEventListener(final int requestCode){
        return  new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                switch (requestCode) {

                    case UserFirebase.GET_USER_DATA:
                        if (dataSnapshot.exists()) {
                            selectedUser = dataSnapshot.getValue(User.class);
                            loadViewInterface();
                        }
                        break;

                    case UserFirebase.GET_FOLLOWING_LIST:
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot usersKeyData : dataSnapshot.getChildren()) {
                                String followingID = usersKeyData.getKey();
                                followingList.add(followingID);
                            }
                            isFollowing = followingList.contains(selectedUser.getUserID());
                            followingState(isFollowing);
                        } else
                            followingState(false);
                        break;

                    case UserFirebase.GET_FOLLOWERS_COUNT:
                        if (dataSnapshot.exists()) {
                            followersCount = dataSnapshot.getValue(Integer.class);
                            updateFollowersCount(followersCount, loggedUser.getFollowingCount());
                        }
                        break;

                    case UserFirebase.GET_POSTS_DATA:
                        if (dataSnapshot.exists()) {

                            if (textViewNoPosts.getVisibility() == View.VISIBLE)
                                textViewNoPosts.setVisibility(View.GONE);

                            photosUrl.clear();

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                Post post = data.getValue(Post.class);
                                if (post.getPostPhoto() != null && !post.getPostPhoto().isEmpty())
                                    photosUrl.add(post.getPostPhoto());
                            }

                            loadImageGrid();

                        } else {

                            selectedProfileProgressBar.setVisibility(View.GONE);
                            textViewNoPosts.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    private void initiateImageLoader(){
        ImageLoaderConfiguration config =
                new ImageLoaderConfiguration
                        .Builder(this)
                        .build();
        ImageLoader.getInstance().init(config);
    }
}
