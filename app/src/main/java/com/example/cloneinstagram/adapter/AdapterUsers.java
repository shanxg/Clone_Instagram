package com.example.cloneinstagram.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cloneinstagram.R;
import com.example.cloneinstagram.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.UserViewHolder> {

    private List<User> usersList;
    private Context context;

    public AdapterUsers(Context context, List<User> usersList) {
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_users, parent, false);

        return new UserViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        User user  = usersList.get(position);

        holder.textUserName.setText(user.getDisplayName());
        holder.textUserEmail.setText(user.getEmail());

        if(user.getxPhoto()!=null && !user.getxPhoto().equals("")){
            Uri imageURL = Uri.parse(user.getxPhoto());
            Glide.with(context).load(imageURL).into(holder.profileCircleImageView);
        }else {
            holder.profileCircleImageView.setImageResource(R.drawable.padrao);
        }

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        TextView textUserName, textUserEmail;
        CircleImageView profileCircleImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            textUserName = itemView.findViewById(R.id.textUserName);
            textUserEmail = itemView.findViewById(R.id.textUserEmail);
            profileCircleImageView = itemView.findViewById(R.id.profileCircleImageView);
        }
    }
}
