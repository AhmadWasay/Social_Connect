package com.example.socialconnect;

import android.content.Context;
import android.text.format.DateUtils;
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

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment currentComment = commentList.get(position);

        holder.commentTextContent.setText(currentComment.getCommentText());
        holder.commentAuthorName.setText("Loading...");

        // Fetch the user's name and picture
        FirebaseFirestore.getInstance().collection("users").document(currentComment.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String realName = documentSnapshot.getString("name");
                        holder.commentAuthorName.setText(realName != null ? realName : "Unknown User");

                        String avatarUrl = documentSnapshot.getString("profileImageUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(context).load(avatarUrl).circleCrop().into(holder.commentAuthorAvatar);
                        }
                    } else {
                        holder.commentAuthorName.setText("Unknown User");
                    }
                });

        // Timestamp
        if (currentComment.getTimestamp() != null) {
            long timeInMillis = currentComment.getTimestamp().toDate().getTime();
            String timeAgo = (String) DateUtils.getRelativeTimeSpanString(timeInMillis);
            holder.commentTimestamp.setText(timeAgo);
        } else {
            holder.commentTimestamp.setText("Just now");
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentAuthorName, commentTimestamp, commentTextContent;
        ImageView commentAuthorAvatar;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentAuthorName = itemView.findViewById(R.id.commentAuthorName);
            commentTimestamp = itemView.findViewById(R.id.commentTimestamp);
            commentTextContent = itemView.findViewById(R.id.commentTextContent);
            commentAuthorAvatar = itemView.findViewById(R.id.commentAuthorAvatar);
        }
    }
}
