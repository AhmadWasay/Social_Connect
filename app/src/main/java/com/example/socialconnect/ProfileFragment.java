package com.example.socialconnect;

import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
//import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    ShapeableImageView profileImageView;
    EditText nameEditText, bioEditText;
    Button saveProfileButton;
    Uri selectedImageUri;

    FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        nameEditText = view.findViewById(R.id.nameEditText);
        bioEditText = view.findViewById(R.id.bioEditText);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });

        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();
                String bio = bioEditText.getText().toString().trim();

                if (name.isEmpty()){
                    Toast.makeText(getContext(), "Name is mandatory", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getContext(), "Everything on track", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}