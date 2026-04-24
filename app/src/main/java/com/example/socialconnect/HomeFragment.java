package com.example.socialconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView postRecyclerView;
    PostAdapter postAdapter;
    List<Post> postList;
    FirebaseFirestore fStore;
    FloatingActionButton addPostFab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postRecyclerView = view.findViewById(R.id.postsRecyclerView);
        addPostFab = view.findViewById(R.id.addPostFab);

        postRecyclerView.setHasFixedSize(true);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fStore = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        postRecyclerView.setAdapter(postAdapter);

        loadPostsFromFirebase();

        addPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreatePostActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadPostsFromFirebase(){
        fStore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) ->{
                    if (error != null){
                        Log.e("HomeFragment", "Failed to fetch posts.", error);
                        Toast.makeText(getContext(), "Error loading feed.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null){
                        postList.clear();

                        for (QueryDocumentSnapshot doc : value){
                            Post post = doc.toObject(Post.class);
                            postList.add(post);
                        }

                        postAdapter.notifyDataSetChanged();
                    }
                });
    }
}