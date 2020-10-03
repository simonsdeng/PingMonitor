package me.simondeng.ping;

import me.simondeng.ping.service.PingListener;
import me.simondeng.ping.service.PingService;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Locale;

public class PingMonitor implements PingListener {
	private static final BufferedImage GREEN_ICON;
	private static final BufferedImage YELLOW_ICON;
	private static final BufferedImage RED_ICON;

	static {
		GREEN_ICON = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		YELLOW_ICON = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		RED_ICON = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		final Graphics green = GREEN_ICON.getGraphics();
		green.setClip(new Ellipse2D.Float(0, 0, 16, 16));
		green.drawImage(GREEN_ICON, 0, 0, 16, 16, null);
		green.setColor(Color.GREEN);
		green.fillRect(0, 0, 16, 16);


		final Graphics yellow = YELLOW_ICON.getGraphics();
		yellow.setClip(new Ellipse2D.Float(0, 0, 16, 16));
		yellow.drawImage(GREEN_ICON, 0, 0, 16, 16, null);
		yellow.setColor(Color.YELLOW);
		yellow.fillRect(0, 0, 16, 16);

		final Graphics red = RED_ICON.getGraphics();
		red.setClip(new Ellipse2D.Float(0, 0, 16, 16));
		red.drawImage(GREEN_ICON, 0, 0, 16, 16, null);
		red.setColor(Color.RED);
		red.fillRect(0, 0, 16, 16);
	}

	private PingService service;
	private TrayIcon trayIcon;

	public PingMonitor(String address) {
		service = new PingService(this, address);
		trayIcon = createTrayIcon();
	}

	public static void main(String[] array) throws IOException, AWTException {
		new PingMonitor(array.length > 0 ? array[0] : null).run();
	}

	public void run() throws IOException, AWTException {
		service.start();
		SystemTray.getSystemTray().add(trayIcon);
	}

	private TrayIcon createTrayIcon() {
		String exit = "exit";
		if (Locale.getDefault().equals(new Locale("pt", "BR"))) {
			exit = "Sair";
		}
		final TrayIcon trayIcon = new TrayIcon(RED_ICON);
		final PopupMenu popupMenu = new PopupMenu();
		final MenuItem menuItem = new MenuItem(exit);
		menuItem.addActionListener(e -> onExit());
		popupMenu.add(menuItem);
		trayIcon.setPopupMenu(popupMenu);
		return trayIcon;
	}

	@Override
	public void pingSuccess(String line, int ping) {
		if (ping < 500) {
			trayIcon.setImage(GREEN_ICON);
		} else {
			trayIcon.setImage(YELLOW_ICON);
		}
		trayIcon.setToolTip(line);
	}

	private Integer numberOfFailure = 0;

	@Override
	public void pingFailure(String addr, String message) {
		trayIcon.setImage(RED_ICON);
		trayIcon.setToolTip(message);
		numberOfFailure++;
		if (numberOfFailure > 3) {
			trayIcon.displayMessage(addr, message, TrayIcon.MessageType.WARNING);
			numberOfFailure = 0;
		}

	}

	public void onExit() {
		service.stop();
		System.exit(0);
	}
}
