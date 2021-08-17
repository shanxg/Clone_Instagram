package com.example.cloneinstagram.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import androidx.appcompat.widget.SearchView;

import com.example.cloneinstagram.R;
import com.example.cloneinstagram.activity.ProfileActivity;
import com.example.cloneinstagram.adapter.AdapterUsers;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.helper.RecyclerItemClickListener;
import com.example.cloneinstagram.helper.UserFirebase;
import com.example.cloneinstagram.model.User;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

public class SearchFragment extends Fragment {

    private List<User> usersList = new ArrayList<>();

    private SearchView searchView;
    private RecyclerView recyclerSearch;

    private AdapterUsers adapterUsers;

    private DatabaseReference usersQueryRef;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchView = view.findViewById(R.id.searchView);
        recyclerSearch = view.findViewById(R.id.recyclerSearch);

        configurateDatabase();
        setQueryConfig();
        setRecyclerSearch();

        return view;
    }

    private void configurateDatabase(){
        DatabaseReference dbRef = ConfigurateFirebase.getFireDBRef();
        usersQueryRef = dbRef.child("users");
    }

    private void setQueryConfig (){
        searchView.setQueryHint("Search Users");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(newText.isEmpty()){
                    usersList.clear();
                    adapterUsers.notifyDataSetChanged();
                }else {
                    queryUsers(newText.toUpperCase());
                }
                return true;
            }
        });
    }

    private void queryUsers(String queryText){

        if(queryText.length()>1){

            Query query = usersQueryRef.orderByChild("nameQ")
                    .startAt(queryText)
                    .endAt(queryText+"\uf8ff");

            query.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            usersList.clear();

                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                User user  = data.getValue(User.class);
                                if(!user.getUserID().contains(UserFirebase.getCurrentUserID())) {
                                    usersList.add(user);
                                }
                            }

                            adapterUsers.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }
    }

    private void setRecyclerSearch(){

        adapterUsers = new AdapterUsers(getActivity(), usersList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerSearch.setLayoutManager(layoutManager);
        recyclerSearch.setHasFixedSize(true);
        recyclerSearch.setAdapter(adapterUsers);

        adapterUsers.notifyDataSetChanged();

        recyclerSearch.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerSearch,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                Intent userProfileIntent = new Intent(getActivity(), ProfileActivity.class);
                                userProfileIntent.putExtra("user", usersList.get(position) );
                                startActivity(userProfileIntent);

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }));
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
