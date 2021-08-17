package com.example.cloneinstagram.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfigurateFirebase {

    private static FirebaseAuth auth;
    private static DatabaseReference fireDBRef;
    private static StorageReference storage;

    public static DatabaseReference getFireDBRef(){

        if(fireDBRef==null){
            fireDBRef = FirebaseDatabase.getInstance().getReference();
        }

        return fireDBRef;
    }


    public static FirebaseAuth getFirebaseAuth(){

        if(auth==null){
            auth = FirebaseAuth.getInstance();
        }

        return auth;
    }

    public static StorageReference getStorage(){

        if(storage == null){
            storage = FirebaseStorage.getInstance().getReference();
        }
        return storage;
    }

}
