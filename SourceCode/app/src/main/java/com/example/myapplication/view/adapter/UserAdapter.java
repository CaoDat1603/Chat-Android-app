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
import com.example.myapplication.controller.LastMessageController;
import com.example.myapplication.controller.StatusController;
import com.example.myapplication.model.Users;
import com.example.myapplication.service.IOnLatestMessageCallback;
import com.example.myapplication.service.impl.GetLatestMessage;
import com.example.myapplication.view.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewholder> {
    private Context context;
    private StatusController statusController;
    private LastMessageController lastMessageController;
    ArrayList<Users> usersArrayList;

    public UserAdapter(Context context, ArrayList<Users> usersArrayList) {
        this.usersArrayList = usersArrayList;
        this.context = context;
        this.statusController = new StatusController();
        this.lastMessageController = new LastMessageController(new GetLatestMessage());
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
        String readableStatus = statusController.checkOnline(users.getStatus());
        holder.userstatus.setText(readableStatus);

        String senderUID = FirebaseAuth.getInstance().getUid();
        String receiverUID = users.getUserId();

        lastMessageController.loadLatestMessage(senderUID, receiverUID, new IOnLatestMessageCallback() {
            @Override
            public void onMessageReceived(String message, long timestamp, String type) {
                String displayMessage;

                if (type == null || type.equals("text")) {
                    displayMessage = message;
                } else {
                    switch (type) {
                        case "image":
                            displayMessage = "Đã gửi 1 hình ảnh";
                            break;
                        case "file":
                            displayMessage = "Đã gửi 1 tệp file";
                            break;
                        default:
                            displayMessage = "Đã gửi 1 tệp";
                            break;
                    }
                }

                holder.lastMessage.setText(displayMessage);
                users.setLastMessage(displayMessage);

                //Format và hiển thị thời gian
                String time = formatTimestamp(timestamp);
                holder.messageTime.setText(time);
                holder.messageTime.setVisibility(View.VISIBLE);

                //Hiển thị biểu tượng trạng thái nếu có tin nhắn
                holder.messageStatus.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String error) {
                String lastMsg = users.getLastMessage();
                if (lastMsg != null && !lastMsg.isEmpty()) {
                    holder.lastMessage.setText(lastMsg);
                    holder.messageStatus.setVisibility(View.VISIBLE);
                } else {
                    holder.lastMessage.setText("Chưa có tin nhắn");
                    holder.messageStatus.setVisibility(View.GONE);
                }

                holder.messageTime.setText("");
                holder.messageTime.setVisibility(View.GONE);
            }

            @Override
            public void onCallback(String roomId) {

            }
        });

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

    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        return sdf.format(date);
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
