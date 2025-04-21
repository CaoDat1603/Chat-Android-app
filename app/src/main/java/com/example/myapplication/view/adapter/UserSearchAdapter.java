package com.example.myapplication.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Users;

import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private Context context;
    private List<Users> userList;
    private OnUserSelectedListener listener;

    public UserSearchAdapter(Context context, List<Users> userList, OnUserSelectedListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.user_search_item, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        Users user = userList.get(position);
        holder.usernameTextView.setText(user.getFullname());

        holder.selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                listener.onUserSelected(user);
            } else {
                listener.onUserSelected(user); // Remove the user from the selected list
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public interface OnUserSelectedListener {
        void onUserSelected(Users user);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView;
        CheckBox selectCheckBox;

        public UserViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username);
            selectCheckBox = itemView.findViewById(R.id.selectUser);
        }
    }
}
