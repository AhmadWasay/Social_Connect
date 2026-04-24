package com.example.socialconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post currentPost = postList.get(position);

        holder.postTextContent.setText(currentPost.getTextContent());

        // Initial state for async loading
        holder.postAuthorName.setText("Loading...");
        holder.postAuthorAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);

        // Fetch user data from Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentPost.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String realName = documentSnapshot.getString("name");
                        holder.postAuthorName.setText(realName != null ? realName : "Unknown User");

                        String avatarUrl = documentSnapshot.getString("profileImageUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(avatarUrl)
                                    .placeholder(android.R.drawable.ic_menu_myplaces)
                                    .circleCrop()
                                    .into(holder.postAuthorAvatar);
                        }
                    } else {
                        // SAFETY NET 1: The document is missing from the database
                        holder.postAuthorName.setText("Unknown User");
                    }
                })
                .addOnFailureListener(e -> {
                    // SAFETY NET 2: The internet dropped or Firebase blocked the request
                    holder.postAuthorName.setText("Unknown User");
                });

        // Handle the Timestamp
        if (currentPost.getTimestamp() != null) {
            long timeInMillis = currentPost.getTimestamp().toDate().getTime();
            String timeAgo = (String) android.text.format.DateUtils.getRelativeTimeSpanString(timeInMillis);
            holder.postTimestamp.setText(timeAgo);
        } else {
            holder.postTimestamp.setText("Just now");
        }

        // Handle the optional Post Image
        if (currentPost.getImageUrl() != null && !currentPost.getImageUrl().isEmpty()) {
            holder.postImageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(currentPost.getImageUrl()).into(holder.postImageView);
        } else {
            holder.postImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postAuthorName, postTimestamp, postTextContent;
        ImageView postImageView, postAuthorAvatar;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            postAuthorName = itemView.findViewById(R.id.postAuthorName);
            postTimestamp = itemView.findViewById(R.id.postTimestamp);
            postTextContent = itemView.findViewById(R.id.postTextContent);
            postImageView = itemView.findViewById(R.id.postImageView);
            postAuthorAvatar = itemView.findViewById(R.id.postAuthorAvatar);
        }
    }
}
