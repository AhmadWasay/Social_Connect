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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etPostContent;
    private ImageView ivPostImagePreview;
    private Button btnPublishPost;

    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private Uri selectedImageUri = null;

    // THE MODERN ANDROID PHOTO PICKER
    private final ActivityResultLauncher<String> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivPostImagePreview.setImageURI(uri);
                    ivPostImagePreview.setVisibility(View.VISIBLE); // Show the preview!
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
        ImageButton btnCancelPost = findViewById(R.id.btnCancelPost);
        
        // Find the specific button and the container
        LinearLayout btnAddImageContainer = findViewById(R.id.btnAddImage);
        Button btnAddPhoto = findViewById(R.id.btnAddPhoto);

        // 2. Initialize Firebase
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // 3. Click Listeners
        btnCancelPost.setOnClickListener(v -> finish());

        // Attach listener to BOTH the container and the button to be safe
        View.OnClickListener pickPhotoListener = v -> photoPickerLauncher.launch("image/*");
        btnAddImageContainer.setOnClickListener(pickPhotoListener);
        btnAddPhoto.setOnClickListener(pickPhotoListener);

        btnPublishPost.setOnClickListener(v -> publishPost());
    }

    private void publishPost() {
        String content = etPostContent.getText().toString().trim();

        if (content.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Cannot publish an empty post!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to post.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lock the button to prevent double-posting
        btnPublishPost.setEnabled(false);
        btnPublishPost.setText("Uploading...");

        if (selectedImageUri != null) {
            // STEP 1: UPLOAD THE IMAGE TO CLOUDINARY
            try {
                com.cloudinary.android.MediaManager.get().upload(selectedImageUri)
                        .unsigned("lxmihjih") // <--- PASTE YOUR CLOUDINARY PRESET HERE
                        .callback(new com.cloudinary.android.callback.UploadCallback() {
                            @Override
                            public void onStart(String requestId) {}

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {}

                            @Override
                            public void onSuccess(String requestId, java.util.Map resultData) {
                                // Image uploaded successfully! Get the secure URL.
                                String imageUrl = (String) resultData.get("secure_url");
                                saveToFirestore(content, imageUrl);
                            }

                            @Override
                            public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(CreatePostActivity.this, "Image Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                                    btnPublishPost.setEnabled(true);
                                    btnPublishPost.setText("Post");
                                });
                            }

                            @Override
                            public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                        }).dispatch();
            } catch (Exception e) {
                Toast.makeText(this, "Upload setup error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnPublishPost.setEnabled(true);
                btnPublishPost.setText("Post");
            }
        } else {
            // No image selected, just save the text directly
            saveToFirestore(content, "");
        }
    }

    // STEP 2: SAVE THE URL AND TEXT TO FIRESTORE
    private void saveToFirestore(String content, String imageUrl) {
        if (fAuth.getCurrentUser() == null) return;
        
        String currentUserId = fAuth.getCurrentUser().getUid();
        String postId = UUID.randomUUID().toString();

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("postId", postId);
        postMap.put("userId", currentUserId);
        postMap.put("textContent", content);
        postMap.put("imageUrl", imageUrl); // Now includes the Cloudinary link!
        postMap.put("timestamp", FieldValue.serverTimestamp());

        // CRITICAL: Initialize the empty likes array so the Like button doesn't crash!
        postMap.put("likes", new ArrayList<>());

        fStore.collection("posts").document(postId)
                .set(postMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreatePostActivity.this, "Posted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreatePostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPublishPost.setEnabled(true);
                    btnPublishPost.setText("Post");
                });
    }
}
