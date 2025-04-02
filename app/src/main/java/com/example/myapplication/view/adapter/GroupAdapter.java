package com.example.myapplication.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Group;
import com.example.myapplication.view.GroupChatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groupList;
    private OnItemClickListener onItemClickListener;

    public GroupAdapter(Context context, List<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    // Khởi tạo GroupAdapter với listener
    public GroupAdapter(Context context, List<Group> groupList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.groupList = groupList;
        this.onItemClickListener = onItemClickListener;
    }

    // Phương thức setOnItemClickListener
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(Group group);
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.groupNameTextView.setText(group.getGroupName());

        // Lấy thời gian từ group
        long createdAt = group.getCreatedAt();

        // Chuyển đổi thời gian sang định dạng mong muốn
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(createdAt));

        // Hiển thị thời gian đã định dạng
        holder.timeCreateGr.setText(formattedDate);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(group);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupName", group.getGroupName());
                intent.putExtra("groupId", group.getGroupId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    // Phương thức này sẽ cập nhật lại danh sách nhóm trong Adapter
    public void updateGroupList(List<Group> newGroupList) {
        if (newGroupList != null) {
            groupList.clear();  // Xóa bỏ các nhóm cũ
            groupList.addAll(newGroupList);  // Thêm nhóm mới vào danh sách
            notifyDataSetChanged();  // Cập nhật RecyclerView
        }
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;
        TextView timeCreateGr;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);
            timeCreateGr = itemView.findViewById(R.id.timeCreateGr);
        }
    }
}
