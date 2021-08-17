package com.example.cloneinstagram.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.fragment.FeedFragment;
import com.example.cloneinstagram.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class UserFirebase {

    public static final int GET_LOGGED_USER_DATA = -1;
    public static final int GET_USER_DATA = 0;
    public static final int GET_SINGLE_DATA = 1;
    public static final int GET_POSTS_DATA = 2;
    public static final int GET_FOLLOWERS_LIST = 3;
    public static final int GET_FOLLOWERS_COUNT = 4;
    public static final int GET_FOLLOWING_LIST = 5;
    public static final int GET_FOLLOWING_COUNT = 6;
    public static final int GET_FEED = 7;

    public static String getCurrentUserID() {

        return getMyUser().getUid();
    }

    public static FirebaseUser getMyUser() {

        return ConfigurateFirebase.getFirebaseAuth().getCurrentUser();
    }

    public static boolean updateUserProfImage(Uri profileImage) {

        try {
            FirebaseUser user = getMyUser();

            UserProfileChangeRequest userChangeRequest =
                    new UserProfileChangeRequest.Builder()
                            .setPhotoUri(profileImage)
                            .build();


            user.updateProfile(userChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (!task.isSuccessful()) {
                        Log.i("UserProfile", "erro ao atualizar a imagem de perfil \n" + task.getException().getMessage());
                    }
                }
            });
            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }

    public static boolean updateUserProfName(String profileName) {

        try {
            FirebaseUser user = getMyUser();

            UserProfileChangeRequest userChangeRequest =
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName(profileName)
                            .build();


            user.updateProfile(userChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (!task.isSuccessful()) {
                        Log.i("UserProfile", "erro ao atualizar o nome de perfil \n" + task.getException().getMessage());
                    }
                }
            });
            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }

    public static void getUserData(String userId, ValueEventListener listener) {

        DatabaseReference usersRef = ConfigurateFirebase.getFireDBRef().child("users");
        DatabaseReference userRef = usersRef.child(userId);
        userRef.addListenerForSingleValueEvent(listener);

    }

    public static void getUserDataSingleValue(String userId, String child, ValueEventListener listener) {
        /*
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()){
                Class valueClass = dataSnapshot.getValue().getClass();
                dataValue = (String.valueOf(dataSnapshot.getValue(valueClass)));
            }
        }
         */
        DatabaseReference usersRef = ConfigurateFirebase.getFireDBRef().child("users");
        DatabaseReference dataValueRef = usersRef.child(userId).child(child);
        dataValueRef.addListenerForSingleValueEvent(listener);
    }


}
