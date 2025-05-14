package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.GroupController;
import com.example.myapplication.model.Users;
import com.example.myapplication.view.adapter.UserSearchAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AddMemeberActivity extends AppCompatActivity {

    private GroupController groupController;
    private UserSearchAdapter searchAdapter, selectedAdapter;
    private EditText searchUsersEditText;
    private RecyclerView searchRecyclerView, selectedRecyclerView;
    private List<Users> groupMembers = new ArrayList<>();
    private List<Users> allUsers; // Danh sách tất cả người dùng
    private List<Users> filteredUsers; // Danh sách người dùng sau khi tìm kiếm
    private List<Users> selectedUsers; // Danh sách người dùng đã chọn
    private String groupId;
    private Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_memeber);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        groupId = getIntent().getStringExtra("groupId");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("members")) {
            groupMembers = (List<Users>) intent.getSerializableExtra("members");
        } else {
            Log.e("AddMemberActivity", "Không nhận được danh sách thành viên");
            groupMembers = new ArrayList<>(); // hoặc xử lý khác
        }

        searchUsersEditText = findViewById(R.id.searchUsers);
        searchRecyclerView = findViewById(R.id.searchRecyclerView);
        selectedRecyclerView = findViewById(R.id.selectedRecyclerView);

        ImageButton turnback = findViewById(R.id.turnback);
        // Turn back
        turnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        allUsers = new ArrayList<>();
        filteredUsers = new ArrayList<>();
        selectedUsers = new ArrayList<>();

        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Khởi tạo adapter cho RecyclerView tìm kiếm (isSelectList = false)
        searchAdapter = new UserSearchAdapter(this, filteredUsers, new UserSearchAdapter.OnUserSelectedListener() {
            @Override
            public void onUserSelected(Users user, boolean isSelected) {
                if (isSelected) {
                    selectedUsers.add(user);
                    updateFilteredUsers(user, false);
                }
                updateSelectedRecyclerView();
            }

            @Override
            public void onUserDeselected(Users user, boolean isSelected) {
                if (!isSelected) {

                }
                updateSelectedRecyclerView();  // Cập nhật lại RecyclerView đã chọn
            }

        }, false); // false vì đây là danh sách người dùng tìm kiếm

        // Khởi tạo adapter cho RecyclerView đã chọn (isSelectList = true)
        selectedAdapter = new UserSearchAdapter(this, selectedUsers, new UserSearchAdapter.OnUserSelectedListener() {
            @Override
            public void onUserSelected(Users user, boolean isSelected) {
                if (isSelected) {

                }
                updateSelectedRecyclerView();
            }

            @Override
            public void onUserDeselected(Users user, boolean isSelected) {
                if (!isSelected) {
                    selectedUsers.remove(user);
                    updateFilteredUsers(user, true);
                }
                updateSelectedRecyclerView();
            }
        }, true); // true vì đây là danh sách người dùng đã chọn

        // Cài đặt RecyclerView
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.setAdapter(searchAdapter);

        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedRecyclerView.setAdapter(selectedAdapter);

        groupController = new GroupController(this);
        groupController.filterUserToAdd(allUsers, groupMembers, users);

        findViewById(R.id.addButton).setOnClickListener(v -> {
            if (selectedUsers.isEmpty()) {
                showErrorMessage("Bạn chưa chọn thành viên cho nhóm.");
                return;
            }
            groupController.addMember(selectedUsers, groupId);
        });

        searchUsersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterUserList(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    public void filterUserList(String query) {
        filteredUsers.clear();
        for (Users user : allUsers) {
            if (query.isEmpty() || user.getFullname().toLowerCase().contains(query.toLowerCase())) {
                if (!selectedUsers.contains(user)) {
                    filteredUsers.add(user);
                }
            }
        }
        // Kiểm tra xem RecyclerView có đang tính toán layout không trước khi gọi notifyDataSetChanged()
        if (!searchRecyclerView.isComputingLayout()) {
            searchAdapter.notifyDataSetChanged();
        } else {
            // Trì hoãn việc gọi notifyDataSetChanged() cho đến khi RecyclerView hoàn tất tính toán
            searchRecyclerView.post(() -> searchAdapter.notifyDataSetChanged());
        }
    }

    private void updateSelectedRecyclerView() {
        // Kiểm tra nếu RecyclerView không đang tính toán layout
        if (!selectedRecyclerView.isComputingLayout()) {
            selectedAdapter.notifyDataSetChanged();
        } else {
            // Trì hoãn việc gọi notifyDataSetChanged() nếu RecyclerView đang tính toán layout
            selectedRecyclerView.post(() -> selectedAdapter.notifyDataSetChanged());
        }
    }

    private void updateFilteredUsers(Users user, boolean isAdding) {
        if (isAdding) {
            filteredUsers.add(user);
        } else {
            filteredUsers.remove(user);
        }
        // Kiểm tra nếu RecyclerView không đang tính toán layout
        if (!searchRecyclerView.isComputingLayout()) {
            searchAdapter.notifyDataSetChanged();
        } else {
            // Trì hoãn việc gọi notifyDataSetChanged() nếu RecyclerView đang tính toán layout
            searchRecyclerView.post(() -> searchAdapter.notifyDataSetChanged());
        }
    }

    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onAddMember() {
        Toast.makeText(this, "Nhóm đã được tạo thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ManageMemberActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
        finish();
    }
}