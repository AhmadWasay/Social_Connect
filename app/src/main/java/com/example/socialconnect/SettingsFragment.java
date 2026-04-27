package com.example.socialconnect;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    private MaterialButton logoutButton, deleteAccountButton;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        logoutButton = view.findViewById(R.id.logoutButton);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountDialog();
            }
        });

        return view;
    }

    private void performLogout() {
        fAuth.signOut();
        Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is permanent and will delete all your data.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUserAccount();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUserAccount() {
        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // 1. Wipe Firestore document first
            fStore.collection("users").document(userId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // 2. Delete Auth record
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Account Deleted", Toast.LENGTH_SHORT).show();
                                        navigateToLogin();
                                    } else {
                                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        // Note: If delete fails due to "requires-recent-login", user might need to re-login.
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error deleting profile data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void navigateToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
