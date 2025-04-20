package com.example.myapplication.controller;

import com.example.myapplication.model.Users;
import java.util.ArrayList;

public class UserController {
    private ArrayList<Users> usersList;

    public UserController() {
        usersList = new ArrayList<>();
    }

    public ArrayList<Users> getUsers() {
        // Có thể gọi Firebase hoặc database để lấy dữ liệu
        return usersList;
    }

    public void addUser(Users user) {
        usersList.add(user);
    }
}