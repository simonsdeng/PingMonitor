package me.simondeng.ping;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import me.simondeng.ping.service.PingService;
import me.simondeng.ping.service.PingListener;

public class PingMonitor implements PingListener {
	private static final BufferedImage GREEN_ICON;
	private static final BufferedImage YELLOW_ICON;
	private static final BufferedImage RED_ICON;
	static {
		GREEN_ICON = new BufferedImage(16, 16, 6);
		YELLOW_ICON = new BufferedImage(16, 16, 6);
		RED_ICON = new BufferedImage(16, 16, 6);

		final Graphics green = GREEN_ICON.getGraphics();
		green.setColor(Color.GREEN);
		green.fillRect(0, 0, 16, 16);

		final Graphics yellow = YELLOW_ICON.getGraphics();
		yellow.setColor(Color.YELLOW);
		yellow.fillRect(0, 0, 16, 16);

		final Graphics red = RED_ICON.getGraphics();
		red.setColor(Color.RED);
		red.fillRect(0, 0, 16, 16);
	}

	private PingService service;
	private TrayIcon trayIcon;

	public PingMonitor() {
		service = new PingService(this);
		trayIcon = createTrayIcon();
	}

	public static void main(String[] array) throws IOException, AWTException {
		new PingMonitor().run();
	}

	public void run() throws IOException, AWTException {
		service.start();
		SystemTray.getSystemTray().add(trayIcon);
	}

	private TrayIcon createTrayIcon() {
		final TrayIcon trayIcon = new TrayIcon(RED_ICON);
		final PopupMenu popupMenu = new PopupMenu();
		final MenuItem menuItem = new MenuItem("Exit");
		menuItem.addActionListener(e -> onExit());
		popupMenu.add(menuItem);
		trayIcon.setPopupMenu(popupMenu);
		return trayIcon;
	}

	@Override
	public void pingSuccess(int ping) {
		if (ping < 500) {
			trayIcon.setImage(GREEN_ICON);
		} else {
			trayIcon.setImage(YELLOW_ICON);
		}
		trayIcon.setToolTip(ping + "ms");
	}

	@Override
	public void pingFailure(String message) {
		trayIcon.setImage(RED_ICON);
		trayIcon.setToolTip(message);
	}

	public void onExit() {
		service.stop();
		System.exit(0);
	}
}
