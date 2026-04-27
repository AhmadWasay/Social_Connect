package com.example.socialconnect;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    ShapeableImageView profileImageView;
    EditText nameEditText, bioEditText;
    Button saveProfileButton;
    Uri selectedImageUri;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    ListenerRegistration profileListener;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri o) {
                    if (o != null){
                        selectedImageUri = o;
                        profileImageView.setImageURI(o);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        nameEditText = view.findViewById(R.id.nameEditText);
        bioEditText = view.findViewById(R.id.bioEditText);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        if (fAuth.getCurrentUser() != null) {
            userId = fAuth.getCurrentUser().getUid();
            loadProfileDataFromFirebase();
        }

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });

        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileDataToFirebase();
            }
        });

        return view;
    }

    private void saveProfileDataToFirebase() {
        String name = nameEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();

        if (name.isEmpty()){
            Toast.makeText(getContext(), "Name is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }
        
        saveProfileButton.setEnabled(false);
        saveProfileButton.setText("Saving...");

        if(selectedImageUri != null){
            com.cloudinary.android.MediaManager.get().upload(selectedImageUri)
                    .unsigned("lxmihjih")
                    .callback(new com.cloudinary.android.callback.UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, java.util.Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            saveTextDataToFirestore(name, bio, imageUrl);
                        }

                        @Override
                        public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                            saveProfileButton.setEnabled(true);
                            saveProfileButton.setText("Save Profile");
                        }

                        @Override
                        public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                    }).dispatch();
        } else {
            saveTextDataToFirestore(name, bio, null);
        }
    }

    private void saveTextDataToFirestore(String name, String bio, String imageUrl){
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("bio", bio);
        if(imageUrl != null){
            userProfile.put("profileImageUrl", imageUrl);
        }
        
        DocumentReference documentReference = fStore.collection("users").document(userId);
        
        documentReference.set(userProfile, SetOptions.merge()).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
            saveProfileButton.setEnabled(true);
            saveProfileButton.setText("Save Profile");
            selectedImageUri = null; // Clear selection after success
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error saving profile", Toast.LENGTH_SHORT).show();
            saveProfileButton.setEnabled(true);
            saveProfileButton.setText("Save Profile");
        });
    }

    private void loadProfileDataFromFirebase() {
        DocumentReference documentReference = fStore.collection("users").document(userId);
        profileListener = documentReference.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                if (e.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    Log.d("ProfileDebug", "Client is offline");
                } else {
                    Log.e("ProfileDebug", "Error loading profile data: ", e);
                }
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String bio = documentSnapshot.getString("bio");
                String imageUrl = documentSnapshot.getString("profileImageUrl");
                if (isAdded() && getContext() != null) {
                    nameEditText.setText(name);
                    bioEditText.setText(bio);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(getContext())
                             .load(imageUrl)
                             .placeholder(android.R.drawable.ic_menu_camera)
                             .into(profileImageView);
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (profileListener != null) {
            profileListener.remove();
        }
    }
}
