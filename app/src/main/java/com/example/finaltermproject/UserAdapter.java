package com.example.finaltermproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onSelect(User user);
        void onRename(User user);
        void onDelete(User user);
    }

    private List<User> userList;
    private final OnUserActionListener listener;

    public UserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false); // match your XML name
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameText.setText(user.getUsername());

        holder.selectButton.setOnClickListener(v -> listener.onSelect(user));
        holder.renameButton.setOnClickListener(v -> listener.onRename(user));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateData(List<User> newUsers) {
        this.userList = newUsers;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        Button selectButton;
        ImageButton renameButton, deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.textUsername);
            selectButton = itemView.findViewById(R.id.btnSelect);
            renameButton = itemView.findViewById(R.id.btnRename);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }
}
