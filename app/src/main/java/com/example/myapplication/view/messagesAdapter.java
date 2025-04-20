package com.example.myapplication.view;

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
import com.google.firebase.auth.FirebaseAuth;
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
            bindMessage(viewHolder, message);
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
        viewHolder.fileLayout.setVisibility(View.GONE); // Ẩn layout file mặc định

        if (message.getType().equals("text")) {
            viewHolder.pro.setVisibility(View.VISIBLE);
            viewHolder.msgtxt.setVisibility(View.VISIBLE);
            viewHolder.msgtxt.setText(message.getMessage());
        } else if (message.getType().equals("image")) {
            viewHolder.pro.setVisibility(View.GONE);
            viewHolder.imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(message.getMessage()).into(viewHolder.imageView);
            viewHolder.itemView.setOnClickListener(v -> downloadFile(message.getMessage()));
        } else if (message.getType().equals("file")) {
            viewHolder.pro.setVisibility(View.VISIBLE);
            viewHolder.fileLayout.setVisibility(View.VISIBLE);
            viewHolder.fileName.setText(message.getFileName()); // Hiển thị tên file
            viewHolder.itemView.setOnClickListener(v -> downloadFile(message.getMessage()));
        }
    }

    private void bindMessage(ReceiverViewHolder viewHolder, msgModel message) {
        viewHolder.msgtxt.setVisibility(View.GONE);
        viewHolder.imageView.setVisibility(View.GONE);
        viewHolder.fileLayout.setVisibility(View.GONE); // Ẩn layout file mặc định

        if (message.getType().equals("text")) {
            viewHolder.pro.setVisibility(View.VISIBLE);
            viewHolder.msgtxt.setVisibility(View.VISIBLE);
            viewHolder.msgtxt.setText(message.getMessage());
        } else if (message.getType().equals("image")) {
            viewHolder.pro.setVisibility(View.GONE);
            viewHolder.imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(message.getMessage()).into(viewHolder.imageView);
            viewHolder.itemView.setOnClickListener(v -> downloadFile(message.getMessage()));
        } else if (message.getType().equals("file")) {
            viewHolder.pro.setVisibility(View.VISIBLE);
            viewHolder.fileLayout.setVisibility(View.VISIBLE);
            viewHolder.fileName.setText(message.getFileName()); // Hiển thị tên file
            viewHolder.itemView.setOnClickListener(v -> downloadFile(message.getMessage()));
        }
    }

    private void downloadFile(String fileLinks) {
        String[] links = fileLinks.split(","); // Giả sử các liên kết được ngăn cách bằng dấu phẩy
        for (String link : links) {
            String cleanLink = link.trim();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(cleanLink));
            context.startActivity(intent);
        }
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        View pro;
        TextView msgtxt;
        ImageView imageView;
        TextView fileName; // Thêm TextView để hiển thị tên file
        View fileLayout; // Layout chứa tên file

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            pro = itemView.findViewById(R.id.pro);
            msgtxt = itemView.findViewById(R.id.sendertextset);
            imageView = itemView.findViewById(R.id.senderImageView);
            fileLayout = itemView.findViewById(R.id.fileLayout); // Layout cho file
            fileName = itemView.findViewById(R.id.fileName); // ID cho TextView hiển thị tên file
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        View pro;
        TextView msgtxt;
        ImageView imageView;
        TextView fileName; // Thêm TextView để hiển thị tên file
        View fileLayout; // Layout chứa tên file

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            pro = itemView.findViewById(R.id.pro);
            msgtxt = itemView.findViewById(R.id.recivertextset);
            imageView = itemView.findViewById(R.id.receiverImageView);
            fileLayout = itemView.findViewById(R.id.fileLayout); // Layout cho file
            fileName = itemView.findViewById(R.id.fileName); // ID cho TextView hiển thị tên file
        }
    }
}