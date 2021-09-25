package com.example.cloneinstagram.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cloneinstagram.R;
import com.example.cloneinstagram.activity.MainActivity;
import com.example.cloneinstagram.activity.MyProfileActivity;
import com.example.cloneinstagram.adapter.AdapterGrid;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.helper.UserFirebase;
import com.example.cloneinstagram.model.Post;
import com.example.cloneinstagram.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private List<String> photosUrl = new ArrayList<>();

    private TextView textFollowingCount, textFollowersCount, textPostsCount, textDisplayQuote, textDisplayEmail, textViewNoPosts;
    private CircleImageView profileFragmentCircleImageView;

    private Button buttonEditProfile;

    private ProgressBar profileProgressBar;
    private GridView profileGridView;
    private AdapterGrid adapterGrid;

    private DatabaseReference myPostsRef;

    private User loggedUser;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        loadInterface(view);
        initiateImageLoader();
        setClickListeners();

        loggedUser = MainActivity.loggedUser;
        if (loggedUser != null)
            loadViewInterface();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int gridWidth =  screenWidth/3 ;

        profileGridView.setColumnWidth(gridWidth);

        profileProgressBar.setVisibility(View.VISIBLE);

        return view;
    }

    private void loadInterface(View view) {
        textFollowersCount = view.findViewById(R.id.textFollowersCount);
        textFollowingCount = view.findViewById(R.id.textFollowingCount);
        textPostsCount = view.findViewById(R.id.textPostsCount);

        textDisplayQuote = view.findViewById(R.id.textDisplayQuote);
        textDisplayEmail = view.findViewById(R.id.textDisplayEmail);

        buttonEditProfile = view.findViewById(R.id.buttonProfileAction);

        profileGridView = view.findViewById(R.id.profileGridView);
        textViewNoPosts = view.findViewById(R.id.textViewNoPosts);

        profileProgressBar = view.findViewById(R.id.profileProgressBar);
        profileFragmentCircleImageView = view.findViewById(R.id.profileFragmentCircleImageView);
    }

    private void loadViewInterface() {

        // #######################         Setting Texts         ##########################

        textDisplayEmail.setText(loggedUser.getEmail());

        String quote = loggedUser.getQuote();
        if (quote != null && !quote.isEmpty()) {
            textDisplayQuote.setText(quote);
        } else {
            String text = '"' + loggedUser.getDisplayName() + '"';
            textDisplayQuote.setText(text);
        }


        String posts = "" + loggedUser.getPostsCount();
        String following = "" + loggedUser.getFollowingCount();
        String followers = "" + loggedUser.getFollowersCount();

        textPostsCount.setText(posts);
        textFollowingCount.setText(following);
        textFollowersCount.setText(followers);

        // #######################         Setting Image         ##########################

        Uri profImageURL = UserFirebase.getMyUser().getPhotoUrl();

        if (profImageURL != null) {
            Glide.with(getActivity()).load(profImageURL).into(profileFragmentCircleImageView);
        } else {
            profileFragmentCircleImageView.setImageResource(R.drawable.padrao);
        }
    }

    private void loadImageGrid(){
        profileProgressBar.setVisibility(View.GONE);

        if(adapterGrid==null) {
            adapterGrid = new AdapterGrid(getActivity(), R.layout.adapter_grid, photosUrl);
            profileGridView.setAdapter(adapterGrid);
        }

        adapterGrid.notifyDataSetChanged();

    }
    /** #########################         CLICK LISTENERS        ############################### **/

    private  void setClickListeners(){

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MyProfileActivity.class));
            }
        });

    }

    /** ############################     ACTIVITY LIFE-CYCLE    ############################### **/

    private void configurateDatabase() {
        String userId = loggedUser.getUserID();

        myPostsRef = ConfigurateFirebase.getFireDBRef().child("posts").child(userId);
        myPostsRef.addValueEventListener(valueEventListener());

        UserFirebase.getUserData(loggedUser.getUserID(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    loggedUser = MainActivity.loggedUser = dataSnapshot.getValue(User.class);
                    if (loggedUser != null)
                        loadViewInterface();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        configurateDatabase();
    }

    @Override
    public void onStop() {
        super.onStop();

        myPostsRef.removeEventListener(valueEventListener());
    }

    /** ##############################        UTILITIES        ################################# **/

    private ValueEventListener valueEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

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
                    textViewNoPosts.setVisibility(View.VISIBLE);
                    profileProgressBar.setVisibility(View.GONE);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    private void initiateImageLoader(){
        ImageLoaderConfiguration config =
                new ImageLoaderConfiguration
                        .Builder(getActivity())
                        .build();
        ImageLoader.getInstance().init(config);

    }
}



