package com.example.myapplication.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.msgModel;
import com.example.myapplication.view.ShowImageMessageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class messagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final ArrayList<msgModel> messagesAdapterArrayList;
    private final int ITEM_SEND = 1;
    private final int ITEM_RECEIVE = 2;

    public messagesAdapter(Context context, ArrayList<msgModel> messagesAdapterArrayList) {
        this.context = context;
        this.messagesAdapterArrayList = messagesAdapterArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_layout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        msgModel message = messagesAdapterArrayList.get(position);

        if (holder instanceof SenderViewHolder) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            bindMessage(viewHolder, message);
        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            bindMessage(viewHolder, message, position);
        }
    }

    @Override
    public int getItemCount() {
        return messagesAdapterArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        msgModel message = messagesAdapterArrayList.get(position);
        return FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderid()) ? ITEM_SEND : ITEM_RECEIVE;
    }

    private void bindMessage(SenderViewHolder viewHolder, msgModel message) {
        viewHolder.msgtxt.setVisibility(View.GONE);
        viewHolder.imageView.setVisibility(View.GONE);
        viewHolder.fileLayout.setVisibility(View.GONE);

        if (message.getType().equals("text")) {
            viewHolder.msgtxt.setVisibility(View.VISIBLE);
            viewHolder.msgtxt.setText(message.getMessage());
        } else if (message.getType().equals("image")) {
            viewHolder.imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(message.getMessage()).into(viewHolder.imageView);
            viewHolder.itemView.setOnClickListener(v -> downloadFile(message.getMessage()));

            // Gắn sự kiện mở ShowImageActivity khi nhấn vào hình ảnh
            viewHolder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(viewHolder.itemView.getContext(), ShowImageMessageActivity.class);
                intent.putExtra("image_url", message.getMessage()); // Truyền URL hình ảnh
                viewHolder.itemView.getContext().startActivity(intent);
            });
        } else if (message.getType().equals("file")) {
            viewHolder.fileLayout.setVisibility(View.VISIBLE);
            viewHolder.fileName.setText(message.getFileName());
            viewHolder.itemView.setOnClickListener(v -> downloadFile(message.getMessage()));
        }
    }

    private void bindMessage(ReceiverViewHolder viewHolder, msgModel message, int position) {
        viewHolder.msgtxt.setVisibility(View.GONE);
        viewHolder.imageView.setVisibility(View.GONE);
        viewHolder.fileLayout.setVisibility(View.GONE);
        viewHolder.notedlayout.setVisibility(View.GONE);



        if (message.isGroupMessage()) {
            viewHolder.pro.setVisibility(View.GONE);

            // Hiển thị tên người gửi nếu cần
            if ((position == 0 || !message.getSenderid().equals(messagesAdapterArrayList.get(position - 1).getSenderid()))) {
                viewHolder.notedlayout.setVisibility(View.VISIBLE);
                // Lấy tên người gửi từ Firebase
                getSenderNamebyID(message.getSenderid(), new SenderNameCallback() {
                    @Override
                    public void onNameReceived(String senderName) {
                        viewHolder.noted.setText(senderName);
                    }
                });
                viewHolder.pronone.setVisibility(View.GONE);
            }

            if (position == messagesAdapterArrayList.size() - 1 ||
                    !message.getSenderid().equals(messagesAdapterArrayList.get(position + 1).getSenderid())) {
                viewHolder.pro.setVisibility(View.VISIBLE);
            }

            if (message.getType().equals("text")) {
                viewHolder.pronone.setVisibility(View.VISIBLE);
                viewHolder.msgtxt.setVisibility(View.VISIBLE);
                viewHolder.msgtxt.setText(message.getMessage());
            } else if (message.getType().equals("image")) {
                viewHolder.pronone.setVisibility(View.VISIBLE);
                viewHolder.imageView.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into(viewHolder.imageView);

                // Gắn sự kiện mở ShowImageActivity khi nhấn vào hình ảnh
                viewHolder.imageView.setOnClickListener(v -> {
                    Intent intent = new Intent(viewHolder.itemView.getContext(), ShowImageMessageActivity.class);
                    intent.putExtra("image_url", message.getMessage()); // Truyền URL hình ảnh
                    viewHolder.itemView.getContext().startActivity(intent);
                });
            } else if (message.getType().equals("file")) {
                viewHolder.pronone.setVisibility(View.VISIBLE);
                viewHolder.fileLayout.setVisibility(View.VISIBLE);
                viewHolder.fileName.setText(message.getFileName());
                viewHolder.itemView.setOnClickListener(v -> downloadFile(message.getMessage()));
            }
        }
        else  {
            if (message.getType().equals("text")) {
                viewHolder.pro.setVisibility(View.VISIBLE);
                viewHolder.msgtxt.setVisibility(View.VISIBLE);
                viewHolder.msgtxt.setText(message.getMessage());
            } else if (message.getType().equals("image")) {
                viewHolder.pro.setVisibility(View.VISIBLE);
                viewHolder.imageView.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into(viewHolder.imageView);

                // Gắn sự kiện mở ShowImageActivity khi nhấn vào hình ảnh
                viewHolder.imageView.setOnClickListener(v -> {
                    Intent intent = new Intent(viewHolder.itemView.getContext(), ShowImageMessageActivity.class);
                    intent.putExtra("image_url", message.getMessage()); // Truyền URL hình ảnh
                    viewHolder.itemView.getContext().startActivity(intent);
                });
            } else if (message.getType().equals("file")) {
                viewHolder.pro.setVisibility(View.VISIBLE);
                viewHolder.fileLayout.setVisibility(View.VISIBLE);
                viewHolder.fileName.setText(message.getFileName());
                viewHolder.itemView.setOnClickListener(v -> downloadFile(message.getMessage()));
            }
        }
    }

    private void downloadFile(String fileLinks) {
        String[] links = fileLinks.split(",");
        for (String link : links) {
            String cleanLink = link.trim();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(cleanLink));
            context.startActivity(intent);
        }
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView msgtxt;
        ImageView imageView;
        TextView fileName;
        View fileLayout;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            msgtxt = itemView.findViewById(R.id.sendertextset);
            imageView = itemView.findViewById(R.id.senderImageView);
            fileLayout = itemView.findViewById(R.id.fileLayout);
            fileName = itemView.findViewById(R.id.fileName);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        View pro;
        View notedlayout;
        View pronone;
        TextView msgtxt;
        ImageView imageView;
        TextView fileName;
        View fileLayout;
        TextView noted;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            pro = itemView.findViewById(R.id.pro);
            msgtxt = itemView.findViewById(R.id.recivertextset);
            imageView = itemView.findViewById(R.id.receiverImageView);
            fileLayout = itemView.findViewById(R.id.fileLayout);
            fileName = itemView.findViewById(R.id.fileName);
            noted = itemView.findViewById(R.id.noted);
            pronone = itemView.findViewById(R.id.pronone);
            notedlayout = itemView.findViewById(R.id.notedlayout);
        }
    }

    private void getSenderNamebyID(String senderID, final SenderNameCallback callback) {
        // Giả sử bạn có một Firebase reference tới "user" nơi lưu thông tin người dùng
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("user").child(senderID);

        usersRef.child("fullname").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String senderName = dataSnapshot.getValue(String.class);  // Lấy tên người gửi
                    callback.onNameReceived(senderName);  // Gọi callback với tên người gửi
                } else {
                    callback.onNameReceived("Unknown User");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý khi có lỗi
                callback.onNameReceived("Unknown User");
            }
        });
    }

    // Định nghĩa interface callback để trả về tên người gửi
    public interface SenderNameCallback {
        void onNameReceived(String senderName);
    }
}