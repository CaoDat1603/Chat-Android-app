package com.example.myapplication.service;

public interface IOnLatestMessageCallback {
    void onCallback(String roomId);
    void onMessageReceived(String message, long timestamp, String type);
    void onError(String error);

}
