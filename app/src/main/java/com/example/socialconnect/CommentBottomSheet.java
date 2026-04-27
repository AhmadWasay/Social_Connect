package com.example.socialconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private String postId; // We need to know WHICH post we are commenting on
    private RecyclerView commentsRecyclerView;
    private EditText etCommentInput;
    private ImageButton btnSendComment;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private FirebaseFirestore fStore;

    // This is the proper Android way to pass data into a Fragment
    public static CommentBottomSheet newInstance(String postId) {
        CommentBottomSheet fragment = new CommentBottomSheet();
        Bundle args = new Bundle();
        args.putString("POST_ID", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_comments, container, false);

        // Retrieve the Post ID
        if (getArguments() != null) {
            postId = getArguments().getString("POST_ID");
        }

        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        etCommentInput = view.findViewById(R.id.etCommentInput);
        btnSendComment = view.findViewById(R.id.btnSendComment);

        fStore = FirebaseFirestore.getInstance();
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), commentList);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(commentAdapter);

        loadComments();

        btnSendComment.setOnClickListener(v -> postComment());

        return view;
    }

    private void loadComments() {
        // Notice the path! posts -> specific post -> comments
        fStore.collection("posts").document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING) // Oldest at top, newest at bottom
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        commentList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Comment comment = doc.toObject(Comment.class);
                            commentList.add(comment);
                        }
                        commentAdapter.notifyDataSetChanged();

                        // Scroll to the very bottom automatically when a new comment loads
                        if (!commentList.isEmpty()) {
                            commentsRecyclerView.scrollToPosition(commentList.size() - 1);
                        }
                    }
                });
    }

    private void postComment() {
        String commentText = etCommentInput.getText().toString().trim();
        if (commentText.isEmpty()) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String commentId = UUID.randomUUID().toString();

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("commentId", commentId);
        commentMap.put("userId", currentUserId);
        commentMap.put("commentText", commentText);
        commentMap.put("timestamp", FieldValue.serverTimestamp());

        // Save to the subcollection!
        fStore.collection("posts").document(postId).collection("comments").document(commentId)
                .set(commentMap)
                .addOnSuccessListener(aVoid -> {
                    etCommentInput.setText(""); // Clear the text box
                })
                .addOnFailureListener(e -> {
                    // PRINT THE EXACT ERROR FROM FIREBASE
                    android.util.Log.e("CommentError", "Firebase rejected comment: ", e);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
