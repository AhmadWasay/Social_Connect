package com.example.socialconnect;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etPostContent;
    private ImageView ivPostImagePreview;
    private Button btnPublishPost;
    private ImageButton btnCancelPost;
    private LinearLayout btnAddImage;

    private Uri selectedImageUri;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    // The native image picker we used for the profile picture!
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivPostImagePreview.setImageURI(uri);
                    ivPostImagePreview.setVisibility(View.VISIBLE); // Un-hide the image view
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // 1. Initialize Views
        etPostContent = findViewById(R.id.etPostContent);
        ivPostImagePreview = findViewById(R.id.ivPostImagePreview);
        btnPublishPost = findViewById(R.id.btnPublishPost);
        btnCancelPost = findViewById(R.id.btnCancelPost);
        btnAddImage = findViewById(R.id.btnAddImage);

        // 2. Initialize Firebase
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // 3. Click Listeners
        btnCancelPost.setOnClickListener(v -> finish()); // Closes this screen immediately

        btnAddImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnPublishPost.setOnClickListener(v -> publishPost());
    }

    private void publishPost() {
        String content = etPostContent.getText().toString().trim();

        if (content.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Cannot publish an empty post!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button so they don't click it twice while it's loading
        btnPublishPost.setEnabled(false);
        btnPublishPost.setText("Posting...");

        if (fAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to post.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = fAuth.getCurrentUser().getUid();

        // Generate a random unique ID for this specific post
        String postId = UUID.randomUUID().toString();

        /* NOTE: If the user selected an image, you would upload it to Firebase Storage here,
           get the download URL, and THEN save to Firestore (just like the profile picture).
           Since you are sorting out Storage billing with the client, we will just save the text for now! */

        saveToFirestore(postId, currentUserId, content, null);
    }

    private void saveToFirestore(String postId, String userId, String content, String imageUrl) {
        // Create the Post data object
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("postId", postId);
        postMap.put("userId", userId);
        postMap.put("textContent", content);
        postMap.put("imageUrl", imageUrl);

        // FieldValue.serverTimestamp() guarantees the exact time is recorded by Google's servers
        postMap.put("timestamp", FieldValue.serverTimestamp());

        // Save it to the "posts" collection
        fStore.collection("posts").document(postId)
                .set(postMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreatePostActivity.this, "Posted!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the screen and return to the Feed!
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreatePostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPublishPost.setEnabled(true);
                    btnPublishPost.setText("Post");
                });
    }
}