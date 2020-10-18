package me.simondeng.ping.service;

public interface PingListener {
    void pingSuccess(String line, int ping);

    void pingFailure(String addr, String line);
}
