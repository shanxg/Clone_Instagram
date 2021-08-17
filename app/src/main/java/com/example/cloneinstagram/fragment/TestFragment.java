package com.example.cloneinstagram.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cloneinstagram.R;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.helper.UserFirebase;
import com.example.cloneinstagram.model.Post;
import com.example.cloneinstagram.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends Fragment {

    private TextView textDataValue, textUserData;
    private Button buttonUserData, buttonDataValue;


    public TestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        loadInterface(view);
        setClickListeners();



        return view;
    }

    private void loadInterface(View v){
        textDataValue = v.findViewById(R.id.textDataValue);
        textUserData = v.findViewById(R.id.textUserData);
        buttonUserData = v.findViewById(R.id.buttonUserData);
        buttonDataValue = v.findViewById(R.id.buttonDataValue);
    }

    private void getSingleDataValue(DataSnapshot dataValue){

        Class valueClass = dataValue.getValue().getClass();
        String stringValue = (String.valueOf(dataValue.getValue(valueClass)));

        /*
        String textData =
                "[DISPLAY NAME]"
                        + "\n" +"Display Name: "+ stringValue
                        + "\n" + "User ID: " + UserFirebase.getCurrentUserID();
        */

        DatabaseReference dbRef =
                FirebaseDatabase.getInstance()
                        .getReferenceFromUrl("https://clone-instagram-6d675-default-rtdb.firebaseio.com/feed/yFgRm5SfEtY7pzoRLUiUOBijdC73/-MdJcF1U1-qaXEzbdHBX");

        String textData = dbRef.getParent().getParent().getKey();

        textDataValue.setText(textData);
        buttonDataValue.setEnabled(true);

    }

    private void getUserData(DataSnapshot userData){

        User user = userData.getValue(User.class);

        if(user!=null) {
            String text =
                    "[USER]"
                            + "\n" + "Name: " + user.getDisplayName()
                            + "\n" + "Followers Count: " + user.getFollowersCount()
                            + "\n" + "User ID: " + user.getUserID();
            textUserData.setText(text);
        }else {
            textUserData.setText("NULL USER");
        }
        buttonUserData.setEnabled(true);
    }

    /** #########################         CLICK LISTENERS        ############################### **/
    private void setClickListeners(){

        buttonUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserFirebase.getUserData(UserFirebase.getCurrentUserID(),
                        valueEventListener(UserFirebase.GET_USER_DATA) );
                buttonUserData.setEnabled(false);
            }
        });

        buttonDataValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserFirebase.getUserDataSingleValue(
                        UserFirebase.getCurrentUserID(),
                        "displayName",
                        valueEventListener(UserFirebase.GET_SINGLE_DATA));
                buttonDataValue.setEnabled(false);
            }
        });
    }


    /** ##############################        UTILITIES        ################################# **/
    private void throwToast(String msgText, boolean lenghLong){

        if(lenghLong) {

            Toast.makeText(getActivity(),
                    msgText,
                    Toast.LENGTH_LONG).show();

        }else {

            Toast.makeText(getActivity(),
                    msgText,
                    Toast.LENGTH_SHORT).show();

        }
    }

    private ValueEventListener valueEventListener (final int requestCode){

        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                switch (requestCode){
                    case UserFirebase.GET_SINGLE_DATA:
                        if(dataSnapshot.exists())
                            getSingleDataValue(dataSnapshot);

                        break;
                    case UserFirebase.GET_USER_DATA:
                        if(dataSnapshot.exists())
                            getUserData(dataSnapshot);

                        break;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

}