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
import com.example.cloneinstagram.model.CommentMessage;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.CommentsViewHolder> {

    private Context context;
    private List<CommentMessage> commentsList;

    public AdapterComments(Context context, List<CommentMessage> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }

    class CommentsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView commentProfileCircleImageView;
        TextView commentTextUserName, commentTextMessage;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            commentProfileCircleImageView = itemView.findViewById(R.id.commentProfileCircleImageView);
            commentTextUserName = itemView.findViewById(R.id.commentTextUserName);
            commentTextMessage = itemView.findViewById(R.id.commentTextMessage);

        }
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(context)
                .inflate(R.layout.adapter_comments, parent, false);
        return new CommentsViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {

        CommentMessage comment = commentsList.get(position);

        String userName = comment.getUserName().toLowerCase()+": ";
        holder.commentTextUserName.setText(userName);
        holder.commentTextMessage.setText(comment.getMessage());

        if(comment.getUserxPhoto()!=null && !comment.getUserxPhoto().isEmpty())
            Glide.with(context).load(Uri.parse(comment.getUserxPhoto())).into(holder.commentProfileCircleImageView);
        else
            holder.commentProfileCircleImageView.setImageResource(R.drawable.padrao);

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }


}
