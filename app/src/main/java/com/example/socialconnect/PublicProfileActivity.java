package com.example.socialconnect;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PublicProfileActivity extends AppCompatActivity {

    private String targetUserId;
    private ShapeableImageView publicProfileAvatar;
    private TextView publicProfileName, publicProfileBio;
    private ImageButton btnBack;

    private RecyclerView publicProfileRecyclerView;
    private PostAdapter postAdapter; // RECYCLING YOUR ADAPTER!
    private List<Post> postList;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        // 1. Get the User ID passed from the Feed
        targetUserId = getIntent().getStringExtra("USER_ID");

        if (targetUserId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Initialize Views
        publicProfileAvatar = findViewById(R.id.publicProfileAvatar);
        publicProfileName = findViewById(R.id.publicProfileName);
        publicProfileBio = findViewById(R.id.publicProfileBio);
        btnBack = findViewById(R.id.btnBack);
        publicProfileRecyclerView = findViewById(R.id.publicProfileRecyclerView);

        btnBack.setOnClickListener(v -> finish());

        // 3. Initialize Firebase & RecyclerView
        fStore = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();

        // This is the magic: We just reuse your existing PostAdapter!
        postAdapter = new PostAdapter(this, postList);
        publicProfileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        publicProfileRecyclerView.setAdapter(postAdapter);

        // 4. Fetch the data
        loadUserProfile();
        loadUserPosts();
    }

    private void loadUserProfile() {
        fStore.collection("users").document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String bio = documentSnapshot.getString("bio");
                        String avatarUrl = documentSnapshot.getString("profileImageUrl");

                        publicProfileName.setText(name != null ? name : "Unknown User");
                        publicProfileBio.setText(bio != null ? bio : "No bio available.");

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this).load(avatarUrl).circleCrop().into(publicProfileAvatar);
                        }
                    }
                });
    }

    private void loadUserPosts() {
        // Query Firebase for ONLY posts where the userId matches this specific person
        fStore.collection("posts")
                .whereEqualTo("userId", targetUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        postList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Post post = doc.toObject(Post.class);
                            postList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }
}
