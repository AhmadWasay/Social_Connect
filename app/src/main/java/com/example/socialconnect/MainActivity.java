package com.example.socialconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.socialconnect.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        // Initialize Cloudinary (Only needs to happen once when the app opens)
        try {
            java.util.Map<String, String> config = new java.util.HashMap<>();
            config.put("cloud_name", "dgwbepfkk"); // <-- Paste your cloud name
            com.cloudinary.android.MediaManager.init(this, config);
        } catch (Exception e) {
            // MediaManager throws an exception if initialized twice, we just catch and ignore it
        }

        // Ask for Notification Permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home){
                replaceFragment(new HomeFragment());
            }
            else if (item.getItemId() == R.id.nav_profile){
                replaceFragment(new ProfileFragment());
            }
            else if (item.getItemId() == R.id.nav_settings){
                replaceFragment(new SettingsFragment());
            }

            return true;
        });
    }
}