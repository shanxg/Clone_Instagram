package com.example.cloneinstagram.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.cloneinstagram.R;
import com.example.cloneinstagram.activity.MainActivity;
import com.example.cloneinstagram.config.ConfigurateFirebase;
import com.example.cloneinstagram.fragment.FeedFragment;
import com.example.cloneinstagram.model.CommentMessage;
import com.example.cloneinstagram.model.Post;
import com.example.cloneinstagram.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;


import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFeed extends RecyclerView.Adapter<AdapterFeed.FeedViewHolder> {

    private Context context;
    private List<Post> postList;
    private List<String> hasComments = new ArrayList<>();

    private static List<ValueEventListener> staticPostListenerList = new ArrayList<>();
    private static List<FeedViewHolder> staticHolderList = new ArrayList<>();


    public AdapterFeed(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {

        Post post;
        AdapterComments adapterComments;
        List<CommentMessage> commentList = new ArrayList<>();
        DatabaseReference commentsRef, likedListRef;

        CircleImageView postProfileCircleImageView;
        ImageView postImageView, btnCommentsIcon;
        TextView textCommentsCount, textLikesCount, textLikeButton, postTextUserQuote, postTextUserName, textPostTitleName, textPostTitle, textPostDescription, textPostDate;
        LinearLayout buttonComment,  movScrollView;
        RecyclerView recyclerPostComments;
        Button btnCloseComments;
        LikeButton buttonLike;
        FloatingActionButton fab;
        TextInputEditText inputCommentText;

        FeedViewHolder(@NonNull View itemView) {
            super(itemView);

            postTextUserQuote = itemView.findViewById(R.id.postTextUserQuote);
            postTextUserName = itemView.findViewById(R.id.postTextUserName);

            postProfileCircleImageView = itemView.findViewById(R.id.postProfileCircleImageView);
            postImageView = itemView.findViewById(R.id.postImageView);

            textPostTitleName = itemView.findViewById(R.id.textPostTitleName);
            textPostTitle = itemView.findViewById(R.id.textPostTitle);
            textPostDescription = itemView.findViewById(R.id.textPostDescription);

            buttonComment = itemView.findViewById(R.id.buttonComment);
            btnCommentsIcon = itemView.findViewById(R.id.btnCommentsIcon);
            textLikeButton = itemView.findViewById(R.id.textLikeButton);
            buttonLike = itemView.findViewById(R.id.buttonLike);

            textPostDate = itemView.findViewById(R.id.textPostDate);
            textLikesCount = itemView.findViewById(R.id.textLikesCount);
            textCommentsCount = itemView.findViewById(R.id.textCommentsCount);

            recyclerPostComments = itemView.findViewById(R.id.recyclerPostComments);
            movScrollView = itemView.findViewById(R.id.movScrollView);
            btnCloseComments = itemView.findViewById(R.id.btnCloseComments);

            inputCommentText = itemView.findViewById(R.id.inputCommentText);
            fab = itemView.findViewById(R.id.fabSendMessage);
        }
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_feed,parent, false);
        return new FeedViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull final FeedViewHolder holder, int position) {



        holder.movScrollView.setVisibility(View.GONE);
        holder.movScrollView.setEnabled(false);

        holder.post = postList.get(position);
        final Post post = holder.post;
        final User postUser = post.getPostUser();


        // ##########################      POST USER DATA       ###########################

        if(postUser!=null) {
            holder.postTextUserName.setText(postUser.getDisplayName());
            holder.postTextUserQuote.setText(postUser.getQuote());

            holder.textPostTitleName.setText(postUser.getDisplayName().toLowerCase());

            if(postUser.getxPhoto()!=null && !postUser.getxPhoto().isEmpty()){
                Uri profileImageUrl = Uri.parse(postUser.getxPhoto());
                Glide.with(context).load(profileImageUrl).into(holder.postProfileCircleImageView);
            }else
                holder.postProfileCircleImageView.setImageResource(R.drawable.padrao);
        }

        // ##########################      IMAGE LOADING       ###########################

        if(post.getPostPhoto()!=null && !post.getPostPhoto().isEmpty()){
            Uri postImageUrl = Uri.parse(post.getPostPhoto());
            Glide.with(context).load(postImageUrl).into(holder.postImageView);
        }else
            holder.postImageView.setImageResource(R.drawable.padrao);

        // ##########################      POST DATA       ###########################

        holder.textPostTitle.setText(post.getPostTitle().toUpperCase());
        holder.textPostDescription.setText(post.getPostDescription());

        String dateText = "posted " + post.getPostDate();
        holder.textPostDate.setText(dateText);

        String likeCountText = post.getLikeCount()+" likes";
        holder.textLikesCount.setText(likeCountText);

        String commentsCountText = post.getCommentsCount()+" comments";
        holder.textCommentsCount.setText(commentsCountText);

        // ##########################      CHECK STATES      ############################

        checkLikedList(holder);
        checkCommentedList(holder);


        // ##########################      CLICK LISTENERS      ###########################

        holder.buttonLike.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                boolean complete = post.updateLikedList(true);

                if (!complete)
                    likeButton.setLiked(false);
                else
                    setLikesListener(holder);

                updateLikeButtonText(holder);

            }

            @Override
            public void unLiked(LikeButton likeButton) {

                boolean complete = post.updateLikedList(false);

                if (!complete)
                    likeButton.setLiked(true);
                else
                    setLikesListener(holder);

                updateLikeButtonText(holder);

            }
        });


        holder.buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setAdapterComments(holder);
                setCommentsListener(holder);

                holder.movScrollView.setEnabled(true);
                holder.movScrollView.setVisibility(View.VISIBLE);

            }
        });

        holder.btnCloseComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removeCommentsListeners(holder);

                holder.movScrollView.setVisibility(View.GONE);
                holder.movScrollView.setEnabled(false);
            }
        });

        holder.fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                validateText(holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


    /** ############################     UTILITIES FUNCTIONS     ############################## **/


    private void validateText(FeedViewHolder holder){

        Post post = holder.post;

        TextInputEditText inputCommentText = holder.inputCommentText;
        String commentText = inputCommentText.getText().toString();

        if(!commentText.isEmpty()){

            CommentMessage comment = new CommentMessage();
            comment.setMessage(commentText);
            comment.setUserId(MainActivity.loggedUser.getUserID());
            comment.setUserName(MainActivity.loggedUser.getDisplayName());
            if(MainActivity.loggedUser.getxPhoto()!=null && !MainActivity.loggedUser.getxPhoto().isEmpty())
                comment.setUserxPhoto(MainActivity.loggedUser.getxPhoto());

            if(post.saveComment(comment)) {
                holder.inputCommentText.setText("");
                holder.btnCommentsIcon.setImageResource((R.drawable.ic_chat_black_24dp));
                if(!hasComments.contains(post.getPostKey()))
                    hasComments.add(post.getPostKey());
            }


        }else
            FeedFragment.throwToast("WRITE SOMETHING FIRST!", true);

    }


    private void setAdapterComments(FeedViewHolder holder){

        RecyclerView recyclerViewComments = holder.recyclerPostComments;

        holder.adapterComments = new AdapterComments(context, holder.commentList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);

        recyclerViewComments.setLayoutManager(layoutManager);
        recyclerViewComments.setHasFixedSize(true);
        recyclerViewComments.setAdapter(holder.adapterComments);

        holder.adapterComments.notifyDataSetChanged();
    }

    private void checkCommentedList(FeedViewHolder holder){

        Post post = holder.post;

        DatabaseReference postRef =
                ConfigurateFirebase.getFireDBRef()
                        .child("posts")
                        .child(post.getPostUser().getUserID())
                        .child(post.getPostKey());

        holder.commentsRef = postRef.child("comments");

        holder.commentsRef.addListenerForSingleValueEvent(valueEventListener(Post.POST_COMMENTS_LIST, holder));

    }

    private void checkLikedList(FeedViewHolder holder) {

        Post post = holder.post;

        String postUserId = post.getPostUser().getUserID();

        DatabaseReference postRef = ConfigurateFirebase.getFireDBRef()
                .child("posts")
                .child(postUserId)
                .child(post.getPostKey());

        holder.likedListRef = postRef.child("likedList");

        holder.likedListRef.addListenerForSingleValueEvent(valueEventListener(Post.POST_LIKE_LIST, holder));
    }

    private void setLikesListener(FeedViewHolder holder){

        ValueEventListener likesListener = valueEventListener(Post.POST_LIKE_COUNT, holder);


        if(!holder.buttonLike.isLiked()){

            holder.likedListRef.removeEventListener(likesListener);

            // ##################      UPDATE STATIC DATA       ###################

            if(staticPostListenerList.contains(likesListener)){

                int indexOfListener = staticPostListenerList.indexOf(likesListener);

                staticPostListenerList.remove(likesListener);
                staticHolderList.remove(indexOfListener);

            }

        }else {

            holder.likedListRef.addValueEventListener(likesListener);

            // ##################      UPDATE STATIC DATA       ###################

            if(!staticPostListenerList.contains(likesListener)){

                staticPostListenerList.add(likesListener);
                staticHolderList.add(holder);
            }
        }
    }

    private void setCommentsListener(FeedViewHolder holder){

        Post post = holder.post;

        DatabaseReference postRef =
                ConfigurateFirebase.getFireDBRef()
                        .child("posts")
                        .child(post.getPostUser().getUserID())
                        .child(post.getPostKey());

        holder.commentsRef = postRef.child("comments");

        ValueEventListener commentsValueEventListener = valueEventListener(Post.POST_COMMENTS_REF, holder);

        holder.commentsRef.addValueEventListener(commentsValueEventListener);

        // ##########################      UPDATE STATIC DATA       ###########################

        if (!staticPostListenerList.contains(commentsValueEventListener)) {

            staticPostListenerList.add(commentsValueEventListener);
            staticHolderList.add(holder);
        }
    }

    private void removeCommentsListeners(FeedViewHolder holder){

        Post post = holder.post;
        ValueEventListener commentsValueEventListener = valueEventListener(Post.POST_COMMENTS_REF, holder);

        if(!hasComments.contains(post.getPostKey())) {

            holder.commentsRef.removeEventListener(commentsValueEventListener);

            // ##########################      UPDATE STATIC DATA       ###########################

            if (staticPostListenerList.contains(commentsValueEventListener)){

                int indexOfListener = staticPostListenerList.indexOf(commentsValueEventListener);

                staticPostListenerList.remove(indexOfListener);
                staticHolderList.remove(indexOfListener);

            }
        }
    }


    private ValueEventListener valueEventListener(final int requestCode, final FeedViewHolder holder){ //, Post post

        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                switch (requestCode){

                    case Post.POST_LIKE_LIST:
                        String loggedUserId = MainActivity.loggedUser.getUserID();

                        holder.buttonLike.setLiked(dataSnapshot.hasChild(loggedUserId));

                        if (dataSnapshot.hasChild(loggedUserId))
                            setLikesListener(holder);

                        updateLikeButtonText(holder);

                        String textlikesCount = dataSnapshot.getChildrenCount()+" likes";
                        holder.textLikesCount.setText(textlikesCount);

                        break;

                    case Post.POST_LIKE_COUNT:

                        textlikesCount = dataSnapshot.getChildrenCount()+" likes";
                        holder.textLikesCount.setText(textlikesCount);

                        break;

                    case Post.POST_COMMENTS_REF:
                        if(dataSnapshot.exists()){
                            holder.commentList.clear();
                            for(DataSnapshot commentsData: dataSnapshot.getChildren()){
                                CommentMessage comment = commentsData.getValue(CommentMessage.class);

                                holder.commentList.add(comment);
                            }
                            String commentsCount = dataSnapshot.getChildrenCount()+" comments";
                            holder.textCommentsCount.setText(commentsCount);
                            if(holder.adapterComments!=null)
                                holder.adapterComments.notifyDataSetChanged();
                        }
                        break;

                    case Post.POST_COMMENTS_LIST:
                        if(dataSnapshot.exists()) {
                            String textCommentsCount = dataSnapshot.getChildrenCount() + " comments";
                            holder.textCommentsCount.setText(textCommentsCount);

                            for(DataSnapshot data : dataSnapshot.getChildren()) {
                                if (data.hasChild("userId")) {
                                    String userId = data.child("userId").getValue(String.class);

                                    if (userId.equals(MainActivity.loggedUser.getUserID())) {
                                        holder.btnCommentsIcon.setImageResource((R.drawable.ic_chat_black_24dp));

                                        String postKey = dataSnapshot.getRef().getParent().getKey();
                                        if(!hasComments.contains(postKey))
                                            hasComments.add(postKey);

                                        setCommentsListener(holder);
                                        break;
                                    }
                                }
                            }

                        }
                        break;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    private void updateLikeButtonText(FeedViewHolder holder){
        if(holder.buttonLike.isLiked()){
            holder.textLikeButton.setText("UNLIKE");
        }else{
            holder.textLikeButton.setText("LIKE");
        }

    }

    public static void cancelListeners(){

        if(staticHolderList != null && staticPostListenerList != null  ) {

            for (FeedViewHolder holder : staticHolderList) {

                DatabaseReference commentsRef = holder.commentsRef;
                DatabaseReference likedListRef = holder.likedListRef;

                if (commentsRef != null)
                    commentsRef.removeEventListener(staticPostListenerList.get(staticHolderList.indexOf(holder)));

                if (likedListRef != null)
                    likedListRef.removeEventListener(staticPostListenerList.get(staticHolderList.indexOf(holder)));
            }
        }
        staticHolderList.clear();
        staticPostListenerList.clear();
    }

}
