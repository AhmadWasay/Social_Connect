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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
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

    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerLayout;

    // --- PAGINATION VARIABLES ---
    private DocumentSnapshot lastVisible; // The "Cursor" (remembers the last post we saw)
    private boolean isLoading = false;    // Prevents spam-clicking the database
    private boolean hasMorePosts = true;  // Tells us when we hit the absolute bottom
    private final int PAGE_LIMIT = 5;     // How many posts to load at a time

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

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);

        // Start the loading animation!
        if (shimmerLayout != null) {
            shimmerLayout.startShimmer();
        }

        // Listen for the Pull-to-Refresh gesture
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                hasMorePosts = true;       // Reset endless scrolling
                lastVisible = null;        // Reset the cursor
                loadInitialPosts();        // Fetch fresh data!
            });
        }

        // Fetch the initial posts
        loadInitialPosts();

        // --- ENDLESS SCROLL LISTENER ---
        postRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // dy > 0 means the user is scrolling down
                if (dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                        // If the user has scrolled to the bottom of the current list...
                        if (!isLoading && hasMorePosts) {
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                loadMorePosts(); // Fetch the next chunk!
                            }
                        }
                    }
                }
            }
        });

        addPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreatePostActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadInitialPosts() {
        isLoading = true;

        fStore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_LIMIT)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // STOP THE SHIMMER ANIMATION AND HIDE IT
                    if (shimmerLayout != null) {
                        shimmerLayout.stopShimmer();
                        shimmerLayout.setVisibility(View.GONE);
                    }

                    // SHOW THE ACTUAL FEED
                    postRecyclerView.setVisibility(View.VISIBLE);

                    // STOP THE PULL-TO-REFRESH SPINNER
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    if (!queryDocumentSnapshots.isEmpty()) {
                        postList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Post post = doc.toObject(Post.class);
                            postList.add(post);
                        }
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        postAdapter.notifyDataSetChanged();
                    } else {
                        hasMorePosts = false;
                    }
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (shimmerLayout != null) {
                        shimmerLayout.stopShimmer();
                        shimmerLayout.setVisibility(View.GONE);
                    }
                    Log.e("HomeFragment", "Initial load failed", e);
                });
    }

    private void loadMorePosts() {
        // If we are already loading, or if we reached the bottom of the database, stop here!
        if (isLoading || !hasMorePosts || lastVisible == null) {
            return;
        }

        isLoading = true;

        fStore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible) // Start exactly where we left off!
                .limit(PAGE_LIMIT)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        // Add the NEW posts to the EXISTING list
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Post post = doc.toObject(Post.class);
                            postList.add(post);
                        }

                        // Update the cursor to the new bottom
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                        postAdapter.notifyDataSetChanged();

                        // If we asked for 5 posts and got less than 5, we hit the end of the database
                        if (queryDocumentSnapshots.size() < PAGE_LIMIT) {
                            hasMorePosts = false;
                        }
                    } else {
                        hasMorePosts = false; // Database is completely empty
                    }
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                    Log.e("HomeFragment", "Load more failed", e);
                });
    }
}
