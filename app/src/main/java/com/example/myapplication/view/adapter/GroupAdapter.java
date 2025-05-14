package com.example.myapplication.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.LastMessageController;
import com.example.myapplication.model.Group;
import com.example.myapplication.service.IOnLatestMessageCallback;
import com.example.myapplication.service.impl.GetLatestMessage;
import com.example.myapplication.view.GroupChatActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groupList;
    private OnItemClickListener onItemClickListener;
    private LastMessageController lastMessageController;

    public GroupAdapter(Context context, List<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
        this.lastMessageController = new LastMessageController(new GetLatestMessage());
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
        String senderUID = FirebaseAuth.getInstance().getUid();
        String roomId = group.getGroupId();
        lastMessageController.checkUserInGroup(senderUID, roomId, new IOnLatestMessageCallback() {
            @Override
            public void onCallback(String result) {
                if (result == null) {
                    Log.e("CheckUserInGroup", "Firebase returned null for user check.");
                    return;
                }

                if (result.equals("true")) {
                    if (holder == null || group == null) {
                        Log.e("LoadLastMessage", "ViewHolder or Group object is null.");
                        return;
                    }

                    lastMessageController.loadGroupLatestMessage(roomId, new IOnLatestMessageCallback() {
                        @Override
                        public void onCallback(String roomId) {}

                        @Override
                        public void onMessageReceived(String message, long timestamp, String type) {
                            if (holder == null || group == null) {
                                Log.e("LoadLastMessage", "ViewHolder or Group object is null.");
                                return;
                            }

                            // Kiểm tra dữ liệu đầu vào
                            if (message == null) {
                                message = "Không có tin nhắn";
                            }

                            if (type == null) {
                                type = "text";
                            }

                            String displayMessage;

                            switch (type) {
                                case "image":
                                    displayMessage = "Đã gửi 1 hình ảnh";
                                    break;
                                case "file":
                                    displayMessage = "Đã gửi 1 tệp file";
                                    break;
                                case "text":
                                default:
                                    displayMessage = message;
                                    break;
                            }

                            holder.lastMessage.setText(displayMessage);
                            group.setLastMessage(displayMessage);

                            // Format và hiển thị thời gian
                            String time = formatTimestamp(timestamp);
                            if (time == null || time.isEmpty()) {
                                time = "Thời gian không xác định";
                            }
                            holder.lastMessageTime.setText(time);
                            holder.lastMessageTime.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("LoadLastMessage", "Error loading last message: " + error);
                            if (holder != null) {
                                holder.lastMessage.setText("Chưa có tin nhắn");
                                holder.lastMessageTime.setText("");
                            }
                        }
                    });

                } else {
                    Log.d("CheckUserInGroup", "User is NOT in the group.");
                    if (holder != null) {
                        holder.lastMessage.setText("Bạn không thuộc nhóm này.");
                        holder.lastMessageTime.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onMessageReceived(String message, long timestamp, String type) {}

            @Override
            public void onError(String error) {
                Log.e("CheckUserInGroup", "Error checking user in group: " + error);
                if (holder != null) {
                    holder.lastMessage.setText("Chưa có tin nhắn");
                    holder.lastMessageTime.setVisibility(View.GONE);
                }
            }
        });


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

    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        return sdf.format(date);
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
        TextView lastMessage;
        TextView lastMessageTime;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);
            timeCreateGr = itemView.findViewById(R.id.timeCreateGr);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            lastMessageTime = itemView.findViewById(R.id.messageTime);
        }
    }
}