package com.release.qquimz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final Context context;
    private List<User> friends;
    private boolean isAddFriendEnabled;
    private OnAddFriendListener addFriendListener;

    public FriendAdapter(List<User> friends, Context context, boolean isAddFriendEnabled) {
        this.friends = new ArrayList<>(friends);
        this.context = context;
        this.isAddFriendEnabled = isAddFriendEnabled;
    }

    public void setOnAddFriendListener(OnAddFriendListener listener) {
        this.addFriendListener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User user = friends.get(position);

        // id를 표시하도록 수정
        holder.tokenTextView.setText(user.getNickname());
        holder.pointTextView.setText("Points: " + user.getPoint());

        if (user.getPoint() < 500) {
            holder.rankingIcon.setImageResource(R.drawable.bronze);
        } else if (user.getPoint() < 1000) {
            holder.rankingIcon.setImageResource(R.drawable.silver);
        } else if (user.getPoint() < 2000) {
            holder.rankingIcon.setImageResource(R.drawable.gold);
        } else if (user.getPoint() < 5000) {
            holder.rankingIcon.setImageResource(R.drawable.ruby);
        } else {
            holder.rankingIcon.setImageResource(R.drawable.pioneer);
        }

        if (isAddFriendEnabled) {
            holder.addFriendButton.setVisibility(View.VISIBLE);
            holder.addFriendButton.setOnClickListener(v -> {
                if (addFriendListener != null) {
                    addFriendListener.onAddFriend(user);
                }
                holder.addFriendButton.setVisibility(View.GONE);
            });
        } else {
            holder.addFriendButton.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void setData(List<User> newFriends) {
        friends.clear();
        friends.addAll(newFriends);
        Collections.sort(friends, Comparator.comparingInt(User::getPoint).reversed());
        notifyDataSetChanged();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tokenTextView;
        TextView pointTextView;
        ImageView rankingIcon;
        ImageButton addFriendButton;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tokenTextView = itemView.findViewById(R.id.friend_token);
            pointTextView = itemView.findViewById(R.id.friend_point);
            rankingIcon = itemView.findViewById(R.id.ranking_icon);
            addFriendButton = itemView.findViewById(R.id.btn_add_friend);
        }
    }

    public interface OnAddFriendListener {
        void onAddFriend(User user);
    }
}
