package me.simondeng.ping.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingService {
	public static final String ADDR = "8.8.8.8";
	private static final boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");
	private static final boolean IS_ANDROID = System.getProperty("java.vendor.url").equals("http://www.android.com/");

	private List<PingListener> listeners;
	private ProcessBuilder procBuilder;
	private volatile Process proc;

	public PingService() {
		listeners = new ArrayList<>();
		procBuilder = getProcessBuilder();
	}

	public PingService(PingListener listener) {
		this();
		addListener(listener);
	}

	private static ProcessBuilder getProcessBuilder() {
		List<String> cmd = new ArrayList<>();
		if (IS_WINDOWS) {
			cmd.add("ping");
			cmd.add("-t");
		} else if (IS_ANDROID) {
			cmd.add("/system/bin/ping");
		} else {
			cmd.add("ping");
		}

		cmd.add(ADDR);
		return new ProcessBuilder(cmd);
	}

	public void addListener(PingListener listener) {
		listeners.add(listener);
	}

	public boolean removeListener(PingListener listener) {
		return listeners.remove(listener);
	}

	public void start() throws IOException {
		if (proc != null) throw new IllegalStateException();
		proc = procBuilder.start();

		new Thread(() -> {
			final Scanner scanner = new Scanner(proc.getInputStream());
			scanner.nextLine();
			if (IS_WINDOWS) scanner.nextLine();

			final Pattern pattern = Pattern.compile("time=(\\d+(\\.\\d+)?) ?ms");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);

				if (matcher.find()) {
					final int ping = (int) Math.round(Double.parseDouble(matcher.group(1)));
					for (PingListener listener : listeners) {
						listener.pingSuccess(ping);
					}
				} else {
					for (PingListener listener : listeners) {
						listener.pingFailure(line);
					}
				}
			}
		}).start();
	}

	public void stop() {
		if (proc == null) throw new IllegalStateException();

		proc.destroy();
		proc = null;
	}
}
