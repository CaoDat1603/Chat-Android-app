package com.example.myapplication.service;

public interface IOnLatestMessageCallback {
    void onMessageReceived(String message, long timestamp, String type);
    void onError(String error);
}
