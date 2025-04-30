package com.example.myapplication.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.StatusController;
import com.example.myapplication.model.Users;
import com.example.myapplication.view.ChatActivity;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewholder> {
    private Context context;
    private StatusController statusController;
    ArrayList<Users> usersArrayList;

    public UserAdapter(Context context, ArrayList<Users> usersArrayList) {
        this.usersArrayList = usersArrayList;
        this.context = context;
        this.statusController = new StatusController();
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Users users = usersArrayList.get(position);
        holder.username.setText(users.getFullname());
        //Gán nội dung (text) cho TextView có id là userstatus bằng với giá trị status từ đối tượng users.
        String readableStatus = statusController.checkOnline(users.getStatus());
        holder.userstatus.setText(readableStatus);


        // Kiểm tra và hiển thị tin nhắn cuối cùng nếu có
        String lastMsg = users.getLastMessage();
        if (lastMsg != null && !lastMsg.isEmpty()) {
            holder.lastMessage.setText(lastMsg);
        } else {
            holder.lastMessage.setText("Chưa có tin nhắn");
        }

        // Kiểm tra và hiển thị thời gian tin nhắn cuối cùng nếu có
        String lastMsgTime = users.getLastMessageTime();
        if (lastMsgTime != null && !lastMsgTime.isEmpty()) {
            holder.messageTime.setText(lastMsgTime);
            holder.messageTime.setVisibility(View.VISIBLE);
        } else {
            holder.messageTime.setText("");
            holder.messageTime.setVisibility(View.GONE);
        }

        // Ẩn trạng thái tin nhắn nếu không có tin nhắn
        holder.messageStatus.setVisibility(lastMsg != null && !lastMsg.isEmpty() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", users.getFullname());
                intent.putExtra("uid", users.getUserId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView username;
        TextView userstatus;
        TextView lastMessage;
        TextView messageTime;
        ImageView messageStatus;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageStatus = itemView.findViewById(R.id.messageStatus);
        }
    }
}
