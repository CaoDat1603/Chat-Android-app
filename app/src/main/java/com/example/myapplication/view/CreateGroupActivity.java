package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.GroupController;
import com.example.myapplication.model.Users;
import com.example.myapplication.view.adapter.UserSearchAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {

    private GroupController groupController;
    private UserSearchAdapter userAdapter;
    private EditText groupNameEditText, searchUsersEditText;
    private RecyclerView userRecyclerView;
    private List<Users> userList; // Danh sách người dùng
    private List<Users> filteredUserList; // Danh sách người dùng lọc từ tìm kiếm
    private List<Users> selectedUsers; // Danh sách người dùng đã chọn

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        // Khởi tạo các thành phần giao diện
        groupNameEditText = findViewById(R.id.groupName);
        searchUsersEditText = findViewById(R.id.searchUsers);
        userRecyclerView = findViewById(R.id.userRecyclerView);

        // Khởi tạo danh sách người dùng và danh sách đã chọn
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        selectedUsers = new ArrayList<>();

        // adminId là ID của người dùng hiện tại
        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Khởi tạo UserSearchAdapter
        userAdapter = new UserSearchAdapter(this, filteredUserList, new UserSearchAdapter.OnUserSelectedListener() {
            @Override
            public void onUserSelected(Users user) {
                // Thêm hoặc loại bỏ người dùng vào/ra danh sách đã chọn
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                } else {
                    selectedUsers.add(user);
                }
            }
        });

        ImageButton turnback = findViewById(R.id.turnback);

        // Turn back
        turnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Cài đặt RecyclerView để hiển thị danh sách người dùng
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userRecyclerView.setAdapter(userAdapter);

        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("user");

        // Lấy dữ liệu người dùng từ Firebase và cập nhật adapter
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);

                    // Chỉ thêm những người dùng KHÁC với user hiện tại vào danh sách
                    if (!user.getUserId().equals(adminId)) {
                        userList.add(user);
                    }
                }
                filterUserList(""); // Lọc lại danh sách khi dữ liệu thay đổi
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showErrorMessage("Lỗi khi tải dữ liệu người dùng.");
            }
        });

        // Khởi tạo GroupController với Context hiện tại (CreateGroupActivity)
        groupController = new GroupController(this);

        // Xử lý sự kiện khi người dùng nhấn nút tạo nhóm
        findViewById(R.id.createButton).setOnClickListener(v -> {
            String groupName = groupNameEditText.getText().toString();

            // Kiểm tra xem có người dùng được chọn hay không
            if (selectedUsers.isEmpty()) {
                showErrorMessage("Bạn chưa chọn thành viên cho nhóm.");
                return;
            }

            // Gọi GroupController để tạo nhóm
            groupController.createGroup(groupName, selectedUsers, adminId);
        });

        // Thêm TextWatcher để theo dõi thay đổi trong trường tìm kiếm người dùng
        searchUsersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Lọc danh sách người dùng khi có thay đổi trong ô tìm kiếm
                filterUserList(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void filterUserList(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            for (Users user : userList) {
                if (user.getFullname().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onGroupCreated() {
        Toast.makeText(this, "Nhóm đã được tạo thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CreateGroupActivity.this, MainActivityGroup.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Đảm bảo MainActivityGroup cũ bị đóng
        startActivity(intent);
        finish();
    }
}