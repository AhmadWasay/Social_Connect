package com.example.socialconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    ShapeableImageView profileImageView;
    EditText nameEditText, bioEditText;
    Button saveProfileButton, logoutButton;
    Uri selectedImageUri;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    String userId;
    ListenerRegistration profileListener;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(
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

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    mGetContent.launch("image/*");
                } else {
                    Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        nameEditText = view.findViewById(R.id.nameEditText);
        bioEditText = view.findViewById(R.id.bioEditText);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (fAuth.getCurrentUser() != null) {
            userId = fAuth.getCurrentUser().getUid();
            loadProfileDataFromFirebase();
        }

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndOpenGallery();
            }
        });

        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileDataToFirebase();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.signOut();
                Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        return view;
    }

    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                mGetContent.launch("image/*");
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                mGetContent.launch("image/*");
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void saveProfileDataToFirebase() {
        String name = nameEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();

        if (name.isEmpty()){
            Toast.makeText(getContext(), "Name is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "Saving Profile", Toast.LENGTH_SHORT).show();

        if(selectedImageUri != null){
            StorageReference fileRef = storageReference.child("users/" + userId + "/profile.jpg");
            fileRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveTextDataToFirestore(name, bio, imageUrl);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to upload Image", Toast.LENGTH_SHORT).show();
            });
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
        documentReference.set(userProfile).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Profile saved globally!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error saving profile", Toast.LENGTH_SHORT).show();
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
                    if (imageUrl != null) {
                        Glide.with(getContext()).load(imageUrl).placeholder(android.R.drawable.ic_menu_camera).into(profileImageView);
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
