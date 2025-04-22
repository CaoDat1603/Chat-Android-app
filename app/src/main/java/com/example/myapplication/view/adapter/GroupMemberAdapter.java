package com.example.myapplication.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Users;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
    private Context context;
    private List<Users> groupMembers;
    private String adminId;
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onDeleteMember(Users user);
        void onChangeAdmin(Users user);
    }

    public GroupMemberAdapter(Context context, List<Users> groupMembers,  String adminId, OnMemberActionListener listener) {
        this.context = context;
        this.groupMembers = groupMembers;
        this.adminId = adminId;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.group_member_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Users user = groupMembers.get(position);
        holder.username.setText(user.getFullname());

        // Check if the user is admin
        if (user.getUserId().equals(adminId)) {
            holder.isAdmin.setVisibility(View.VISIBLE);
            holder.changeAdmin.setVisibility(View.GONE);
            holder.deleteMember.setVisibility(View.GONE);
        } else {
            holder.isAdmin.setVisibility(View.GONE);
            holder.changeAdmin.setVisibility(View.VISIBLE);
            holder.deleteMember.setVisibility(View.VISIBLE);
        }

        holder.deleteMember.setOnClickListener(v -> listener.onDeleteMember(user));
        holder.changeAdmin.setOnClickListener(v -> listener.onChangeAdmin(user));
    }

    @Override
    public int getItemCount() {
        return groupMembers.size();
    }

    public void updateMemberList(List<Users> newList) {
        groupMembers = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageButton deleteMember, changeAdmin, isAdmin;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            deleteMember = itemView.findViewById(R.id.deletemember);
            changeAdmin = itemView.findViewById(R.id.changeadmin);
            isAdmin = itemView.findViewById(R.id.isadmin);
        }
    }
}