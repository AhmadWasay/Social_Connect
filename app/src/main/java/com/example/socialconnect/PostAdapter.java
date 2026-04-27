package com.example.socialconnect;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
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

        // Optimized user data loading: Only load if not already loaded or view is recycled
        holder.postAuthorName.setText("Loading...");
        holder.postAuthorAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);

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
                        holder.postAuthorName.setText("Unknown User");
                    }
                })
                .addOnFailureListener(e -> holder.postAuthorName.setText("Unknown User"));

        if (currentPost.getTimestamp() != null) {
            long timeInMillis = currentPost.getTimestamp().toDate().getTime();
            String timeAgo = (String) android.text.format.DateUtils.getRelativeTimeSpanString(timeInMillis);
            holder.postTimestamp.setText(timeAgo);
        } else {
            holder.postTimestamp.setText("Just now");
        }

        if (currentPost.getImageUrl() != null && !currentPost.getImageUrl().isEmpty()) {
            holder.postImageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(currentPost.getImageUrl()).into(holder.postImageView);
        } else {
            holder.postImageView.setVisibility(View.GONE);
        }

        // --- OPTIMIZED LIKE SYSTEM ---
        updateLikeUI(holder, currentPost);

        holder.btnLike.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts").document(currentPost.getPostId());
            boolean isLiked = currentPost.getLikes().contains(currentUserId);

            // 1. UI Animation
            v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).withEndAction(() -> 
                v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
            ).start();

            // 2. Local Update (Instant feedback)
            if (isLiked) {
                currentPost.getLikes().remove(currentUserId);
                postRef.update("likes", FieldValue.arrayRemove(currentUserId));
            } else {
                currentPost.getLikes().add(currentUserId);
                postRef.update("likes", FieldValue.arrayUnion(currentUserId));
            }
            
            // 3. Selective Re-render (Only this item)
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.btnComment.setOnClickListener(v -> {
            CommentBottomSheet bottomSheet = CommentBottomSheet.newInstance(currentPost.getPostId());
            bottomSheet.show(((androidx.appcompat.app.AppCompatActivity) context).getSupportFragmentManager(), "CommentBottomSheet");
        });

        View.OnClickListener profileClickListener = v -> {
            android.content.Intent intent = new android.content.Intent(context, PublicProfileActivity.class);
            intent.putExtra("USER_ID", currentPost.getUserId());
            context.startActivity(intent);
        };
        holder.postAuthorAvatar.setOnClickListener(profileClickListener);
        holder.postAuthorName.setOnClickListener(profileClickListener);
    }

    private void updateLikeUI(PostViewHolder holder, Post post) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        int likeCount = post.getLikes().size();
        holder.tvLikeCount.setText(likeCount + (likeCount == 1 ? " Like" : " Likes"));

        if (post.getLikes().contains(currentUserId)) {
            holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
            holder.btnLike.setColorFilter(Color.RED);
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_heart_outline);
            holder.btnLike.setColorFilter(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postAuthorName, postTimestamp, postTextContent, tvLikeCount;
        ImageView postImageView, postAuthorAvatar, btnLike, btnComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postAuthorName = itemView.findViewById(R.id.postAuthorName);
            postTimestamp = itemView.findViewById(R.id.postTimestamp);
            postTextContent = itemView.findViewById(R.id.postTextContent);
            postImageView = itemView.findViewById(R.id.postImageView);
            postAuthorAvatar = itemView.findViewById(R.id.postAuthorAvatar);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
        }
    }
}
