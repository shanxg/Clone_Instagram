package com.example.cloneinstagram.model;

import com.example.cloneinstagram.activity.MainActivity;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post implements Serializable {

    public static final int POST_LIKE_LIST = -10;
    public static final int POST_LIKE_COUNT = -11;

    public static final int POST_COMMENTS_REF = -12;
    public static final int POST_COMMENTS_LIST = -13;

    private String postPhoto, postTitle, postDescription, postDate, postKey;
    private int likeCount, commentsCount;
    private User postUser;

    public Post() {
    }


    public boolean save(User postUser) {

        boolean isDone;

        DatabaseReference dbRef = ConfigurateFirebase.getFireDBRef();
        DatabaseReference postRef =
                dbRef.child("posts")
                        .child(postUser.getUserID())
                        .push();

        this.setPostKey(postRef.getKey());
        this.setPostUser(postUser);
        this.startCounters();

        try {
            postRef.setValue(this);
            isDone = true;

        } catch (Exception e) {
            e.printStackTrace();
            isDone = false;
        }

        if (isDone)
            updateFeed();

        return isDone;
    }

    private void updateFeed() {
        DatabaseReference followersRef =
                ConfigurateFirebase.getFireDBRef()
                        .child("users")
                        .child(postUser.getUserID())
                        .child("followersList");

        Query query = followersRef.orderByChild("userId");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> followersIdList = new ArrayList<>();

                if (dataSnapshot.exists()) {

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String userId = data.getKey();
                        followersIdList.add(userId);
                    }

                    DatabaseReference feedRef =
                            ConfigurateFirebase.getFireDBRef()
                                    .child("feed");

                    DatabaseReference myFeedRef =
                            ConfigurateFirebase.getFireDBRef()
                                    .child("feed")
                                    .child(postUser.getUserID())
                                    .push();

                    Map feed = new HashMap();
                    feed.put("/userId", postUser.getUserID());
                    feed.put("/postKey", getPostKey());

                    for (String followerId : followersIdList) {

                        feedRef.child(followerId).push();
                        feedRef.updateChildren(feed);
                    }
                    myFeedRef.updateChildren(feed);

                }else {

                    DatabaseReference myFeedRef =
                            ConfigurateFirebase.getFireDBRef()
                                    .child("feed")
                                    .child(postUser.getUserID())
                                    .push();

                    Map feed = new HashMap();
                    feed.put("/userId", postUser.getUserID());
                    feed.put("/postKey", getPostKey());

                    myFeedRef.updateChildren(feed);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void startCounters() {
        this.likeCount = 0;
        this.commentsCount = 0;
    }

    public User getPostUser() {
        return postUser;
    }

    public void setPostUser(User postUser) {
        this.postUser = postUser;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }


    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }


    public String getPostPhoto() {
        return postPhoto;
    }

    public void setPostPhoto(String postPhoto) {
        this.postPhoto = postPhoto;
    }


    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }


    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean updateLikedList(boolean isIncreasing){

        boolean isComplete;

        String postUserId = this.postUser.getUserID();

        DatabaseReference postRef = ConfigurateFirebase.getFireDBRef()
                                        .child("posts")
                                            .child(postUserId)
                                                .child(this.postKey);

        Follower follower = new Follower();
        follower.setUserId(MainActivity.loggedUser.getUserID());
        follower.setUserXPhoto(MainActivity.loggedUser.getxPhoto());
        follower.setUserName(MainActivity.loggedUser.getDisplayName());

        DatabaseReference likedListRef = postRef.child("likedList").child(follower.userId);

        if(isIncreasing) {

            try {
                likedListRef.setValue(follower);
                isComplete = true;

            } catch (Exception e) {
                e.printStackTrace();
                isComplete = false;
            }

        }else {

            try {
                likedListRef.removeValue();
                isComplete = true;

            } catch (Exception e) {
                e.printStackTrace();
                isComplete = false;
            }

        }

        if (isComplete)
            updateLikeCount(isIncreasing);

        return isComplete;
    }

    private void updateLikeCount(final boolean isIncreased){
        DatabaseReference postRef =
                ConfigurateFirebase.getFireDBRef()
                        .child("posts")
                        .child(postUser.getUserID())
                        .child(this.getPostKey());

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("likedList")) {
                    int likeCount = (int) dataSnapshot.child("likedList").getChildrenCount();

                    if(isIncreased) {

                        if (!dataSnapshot.child("likedList").hasChild(MainActivity.loggedUser.getUserID()))
                            likeCount++;

                    }else {

                        if (dataSnapshot.child("likedList").hasChild(MainActivity.loggedUser.getUserID()))
                            likeCount--;
                    }

                    dataSnapshot.child("likeCount").getRef().setValue(likeCount);
                    setLikeCount(likeCount);
                }else {

                    int likeCount = dataSnapshot.child("likeCount").getValue(Integer.class);

                    if(isIncreased) {
                        likeCount++;
                    }else {
                        likeCount--;
                    }

                    dataSnapshot.getRef().child("likeCount").setValue(likeCount);
                    setLikeCount(likeCount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public boolean saveComment(CommentMessage comment){

        boolean isSaved;

        DatabaseReference postRef =
                ConfigurateFirebase.getFireDBRef()
                                        .child("posts")
                                            .child(this.postUser.getUserID())
                                                .child(this.getPostKey());
        DatabaseReference commentsRef = postRef.child("comments").push();

        try {
            commentsRef.setValue(comment);
            isSaved = true;

        }catch (Exception e){
            e.printStackTrace();
            isSaved = false;
        }
        if(isSaved)
            incrementCommentsCount();

        return isSaved;
    }

    private void incrementCommentsCount(){

    }
}
