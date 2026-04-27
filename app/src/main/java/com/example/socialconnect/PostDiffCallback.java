package com.example.socialconnect;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

public class PostDiffCallback extends DiffUtil.Callback {

    private final List<Post> oldList;
    private final List<Post> newList;

    public PostDiffCallback(List<Post> oldList, List<Post> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    // 1. Are these the exact same post? (Check the unique ID)
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getPostId().equals(newList.get(newItemPosition).getPostId());
    }

    // 2. If it is the same post, did the data inside it change?
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Post oldPost = oldList.get(oldItemPosition);
        Post newPost = newList.get(newItemPosition);

        // Check if the text is the same
        boolean textSame = false;
        if (oldPost.getTextContent() != null && newPost.getTextContent() != null) {
            textSame = oldPost.getTextContent().equals(newPost.getTextContent());
        }

        // Check if the amount of likes changed
        boolean likesSame = oldPost.getLikes().size() == newPost.getLikes().size();

        // If the text and the likes are exactly the same, no need to redraw this card!
        return textSame && likesSame;
    }
}
