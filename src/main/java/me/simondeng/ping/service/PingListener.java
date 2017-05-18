package me.simondeng.ping.service;

public interface PingListener {
	void pingSuccess(int ping);
	void pingFailure(String message);
}
