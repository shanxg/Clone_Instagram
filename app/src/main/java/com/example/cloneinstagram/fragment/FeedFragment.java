package com.example.cloneinstagram.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cloneinstagram.R;
import com.example.cloneinstagram.adapter.AdapterFeed;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.helper.UserFirebase;
import com.example.cloneinstagram.model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static Activity activity;


    private int childCount;

    private AdapterFeed adapterFeed;
    private RecyclerView recyclerFeed;
    private List<Post> postList;
    private List<Map> feedRefList;
    private List<String> followingIdList;

    private DatabaseReference dbRef;
    private DatabaseReference feedRef;



    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        activity = getActivity();
        postList = new ArrayList<>();
        followingIdList = new ArrayList<>();
        feedRefList = new ArrayList<>();

        loadInterface(view);

        adapterFeed = new AdapterFeed(getActivity(), postList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerFeed.setLayoutManager(layoutManager);
        recyclerFeed.setHasFixedSize(true);
        recyclerFeed.setAdapter(adapterFeed);

        return view;
    }
    private void getFeedNSetDbRef(){
        dbRef = ConfigurateFirebase.getFireDBRef();
        feedRef = dbRef.child("feed").child(UserFirebase.getCurrentUserID());


        ValueEventListener eventListener = valueEventListener(UserFirebase.GET_FEED);
        feedRef.addValueEventListener(eventListener);
    }

    private void loadInterface(View v){
        recyclerFeed = v.findViewById(R.id.recyclerFeed);
    }

    /** ##############################    ACTIVITY PROCESSES   ################################# **/

    @Override
    public void onStart() {
        super.onStart();
        getFeedNSetDbRef();
    }

    @Override
    public void onStop() {
        super.onStop();
        AdapterFeed.cancelListeners();

        ValueEventListener eventListener = valueEventListener(UserFirebase.GET_FEED);
        feedRef.removeEventListener(eventListener);
    }

    /** ##############################        UTILITIES        ################################# **/

   public static void throwToast(String msgText, boolean lengthLong){

       if(lengthLong) {

           Toast.makeText(activity,
                   msgText,
                   Toast.LENGTH_LONG).show();

       }else {

           Toast.makeText(activity,
                   msgText,
                   Toast.LENGTH_SHORT).show();

       }
   }

    private ValueEventListener valueEventListener (final int requestCode){
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                switch (requestCode){

                    case UserFirebase.GET_POSTS_DATA:
                        if(dataSnapshot.exists()){

                            Post post = dataSnapshot.getValue(Post.class);
                            postList.add(post);

                            if (postList.size()==feedRefList.size()){
                                orderPosts();
                            }
                        }
                        break;

                    case UserFirebase.GET_FEED:
                        if (dataSnapshot.exists()) {

                            feedRefList.clear();
                            childCount = (int) dataSnapshot.getChildrenCount();

                            try {
                                if (feedRefList.size() == 0) {
                                    for (DataSnapshot feedData : dataSnapshot.getChildren()) {
                                        HashMap feedMap = (HashMap) feedData.getValue();

                                        feedRefList.add(feedMap);
                                    }

                                    if (feedRefList.size() == childCount) {
                                        getPostList();

                                    }
                                }

                            }catch (Exception e ){
                                throwToast(e.getMessage(), true);
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    private void orderPosts(){

        List<Post> postListHolder = new ArrayList<>(postList);

       for (Post post : postListHolder) {
           for (Map feedMap : feedRefList)
               if (feedMap.containsValue(post.getPostKey())) {
                   int indexOfPost = feedRefList.indexOf(feedMap);
                   postList.remove(indexOfPost);
                   postList.add(indexOfPost, post);
               }
       }
       adapterFeed.notifyDataSetChanged();
    }

    private void getPostList() {

       postList.clear();
       if (postList.size() == 0) {
           Collections.reverse(feedRefList);

           for (Map feedMap : feedRefList) {
               String postKey = (String) feedMap.get("postKey");
               String userId = (String) feedMap.get("userId");

               DatabaseReference postsRef = dbRef.child("posts").child(userId).child(postKey);
               postsRef.addListenerForSingleValueEvent(valueEventListener(UserFirebase.GET_POSTS_DATA));
           }
       }
    }
}