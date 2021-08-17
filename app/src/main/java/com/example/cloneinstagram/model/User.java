package com.example.cloneinstagram.model;



import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable  {

    private String name, nameQ, displayName, userID, email, pw, xPhoto, quote;


    private int followingCount;
    private int followersCount;
    private int postsCount;


    public User() {
    }

    public User(String name, String email, String pw) {
        this.name = name;
        this.email = email;
        this.pw = pw;
        setNameQ(name);
    }

    public void save() {
        DatabaseReference dbRef = ConfigurateFirebase.getFireDBRef();
        DatabaseReference userRef = dbRef.child("users").child(getUserID());

        this.startCounts();

        userRef.setValue(this);
    }

    public boolean update() {

        DatabaseReference fireDB = ConfigurateFirebase.getFireDBRef();
        DatabaseReference userRef = fireDB.child("users").child(getUserID());

        Task task = userRef.updateChildren(userMap());

        return task.isSuccessful();
    }

    @Exclude
    private Map<String, Object> userMap() {

        HashMap<String, Object> userMap = new HashMap<>();

        userMap.put("quote", getQuote());
        userMap.put("name", getName());
        userMap.put("nameQ", getNameQ());
        userMap.put("displayName", getDisplayName());
        userMap.put("xPhoto", getxPhoto());

        return userMap;
    }

    public void addInFollowersList(User followerUser){

            String followerID = followerUser.getUserID();

            Follower follower = new Follower();
            follower.setUserId(followerID);
            follower.setUserName(followerUser.getName());
            follower.setUserXPhoto(followerUser.getxPhoto());

            DatabaseReference selectedUserRef =
                    ConfigurateFirebase.getFireDBRef()
                            .child("users")
                            .child(this.getUserID());

            DatabaseReference followersRef = selectedUserRef.child("followersList");

            followersRef.child(followerID)
                            .setValue(follower);

    }

    public void removeInFollowersList(User followerUser){

        String followerID = followerUser.getUserID();

        DatabaseReference selectedUserRef =
                ConfigurateFirebase.getFireDBRef()
                        .child("users")
                        .child(this.getUserID());

        DatabaseReference followersRef = selectedUserRef.child("followersList");

        followersRef.child(followerID)
                .removeValue();
    }

    public void addInFollowingList(User followingUser){

            String followingId = followingUser.getUserID();

            Follower following = new Follower();
            following.setUserId(followingId);
            following.setUserName(followingUser.getName());
            following.setUserXPhoto(followingUser.getxPhoto());

            DatabaseReference userRef =
                    ConfigurateFirebase.getFireDBRef()
                            .child("users")
                            .child(this.getUserID());

            DatabaseReference followingRef = userRef.child("followingList");

            followingRef.child(followingId)
                            .setValue(following);
    }

    public void removeInFollowingList(User followingUser){

        String followingId = followingUser.getUserID();

        DatabaseReference userRef =
                ConfigurateFirebase.getFireDBRef()
                        .child("users")
                        .child(this.getUserID());

        DatabaseReference followingRef = userRef.child("followingList");

        followingRef.child(followingId)
                .removeValue();

    }

    public int getFollowingCount()
    {        return followingCount;    }

    public void setFollowingCount(int followingCount){
        this.followingCount = followingCount;
    }

    public boolean updateFollowingCount(int followingCount){
        this.followingCount = followingCount;

        DatabaseReference userRef = ConfigurateFirebase.getFireDBRef()
                .child(this.userID).child("followingCount");

        try {
            userRef.setValue(followingCount);
            return true;
        }catch (Exception e  ) {
            e.printStackTrace();
            return false;
        }
    }


    public int getFollowersCount()
    {        return followersCount;    }

    public void setFollowersCount(int followersCount){
        this.followersCount = followersCount;
    }
    public boolean updateFollowersCount(int followersCount){
        this.followersCount = followersCount;

        DatabaseReference userRef = ConfigurateFirebase.getFireDBRef()
                .child(this.userID).child("followersCount");

        try {
            userRef.setValue(followersCount);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    public int getPostsCount()
    {        return postsCount;    }

    public void setPostsCount(int postsCount)
    {        this.postsCount = postsCount;    }


    public String getDisplayName() {        return displayName;    }
    public void setDisplayName(String displayName) {        this.displayName = displayName;    }

    public String getNameQ() {
        return nameQ;
    }
    public void setNameQ(String name) {
        this.nameQ = name.toUpperCase();
    }

    public String getQuote() {
        return quote;
    }
    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {        this.name = name;    }

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getPw() {
        return pw;
    }
    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getxPhoto() {
        return xPhoto;
    }
    public void setxPhoto(String xPhoto) {
        this.xPhoto = xPhoto;
    }

    private void startCounts(){
        this.postsCount = 0;
        this.followersCount = 0;
        this.followingCount = 0;

    }

}
