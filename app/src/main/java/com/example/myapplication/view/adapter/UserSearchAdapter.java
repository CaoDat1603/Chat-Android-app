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
    private boolean isSelectList; // Xác định đây có phải là RecyclerView của người đã chọn không

    public UserSearchAdapter(Context context, List<Users> userList, OnUserSelectedListener listener, boolean isSelectList) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
        this.isSelectList = isSelectList; // Gán giá trị cho biến này
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

        // Nếu đây là danh sách người đã chọn, checkbox phải được chọn
        // Nếu không phải là danh sách đã chọn, checkbox không cần chọn
        holder.selectCheckBox.setChecked(isSelectList);

        // Đặt sự kiện thay đổi trạng thái checkbox
        holder.selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                listener.onUserSelected(user, true); // Gọi phương thức khi chọn
            } else {
                listener.onUserDeselected(user, false); // Gọi phương thức khi bỏ chọn
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public interface OnUserSelectedListener {
        void onUserSelected(Users user, boolean isSelected); // Khi chọn người dùng
        void onUserDeselected(Users user, boolean isSelected); // Khi bỏ chọn người dùng
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
