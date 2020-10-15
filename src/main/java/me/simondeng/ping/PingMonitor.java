package me.simondeng.ping;

import javafx.application.Platform;
import me.simondeng.ping.service.PingListener;
import me.simondeng.ping.service.PingService;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class PingMonitor implements PingListener {
    private static final BufferedImage GREEN_ICON;
    private static final BufferedImage YELLOW_ICON;
    private static final BufferedImage RED_ICON;
    private final static String newline = "\n";
    private JTextArea jTextArea;
    private Integer numberOfFailure = 0;
    private PingService service;
    private TrayIcon trayIcon;
    private JFrame frame = new JFrame("Ping Monitor");

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


    public PingMonitor(String address) {
        service = new PingService(this, address);

    }

    public static void main(String[] array) throws IOException, AWTException {
        new PingMonitor(array.length > 0 ? array[0] : null).run();
    }

    public void run() throws IOException, AWTException {
        service.start();
        if (SystemTray.isSupported()) {
            trayIcon = createTrayIcon();
            SystemTray.getSystemTray().add(trayIcon);
        }

    }

    private TrayIcon createTrayIcon() {
        final TrayIcon trayIcon = new TrayIcon(RED_ICON);
        final PopupMenu popupMenu = new PopupMenu();

        final MenuItem menuItemView = new MenuItem(getTextView());
        menuItemView.addActionListener(e -> onView());
        popupMenu.add(menuItemView);

        final MenuItem menuItemExit = new MenuItem(getTextExit());
        menuItemExit.addActionListener(e -> onExit());
        popupMenu.add(menuItemExit);

        trayIcon.setPopupMenu(popupMenu);
        trayIcon.addActionListener(event -> onView());

        return trayIcon;
    }

    @Override
    public void pingSuccess(String line, int pingTimeout) {
        onUpdateView(line, Color.GREEN);
        onUpdateTrayIcon(line, pingTimeout);
    }

    @Override
    public void pingFailure(String addr, String line) {
        onUpdateView(line, Color.RED);
        onUpdateTrayIcon(line, -1);
        numberOfFailure++;
        if (numberOfFailure > 5 && new File("displayMessage.on").exists()) {
            trayIcon.displayMessage(addr, line, TrayIcon.MessageType.WARNING);
            numberOfFailure = 0;
        }
    }

    public String getTextExit() {
        String textExit = "Exit";
        if (Locale.getDefault().equals(new Locale("pt", "BR"))) {
            textExit = "Sair";
        }
        return textExit;
    }

    public String getTextView() {
        String textView = "View";
        if (Locale.getDefault().equals(new Locale("pt", "BR"))) {
            textView = "Visualizar";
        }
        return textView;
    }

    public void onUpdateTrayIcon(String text, int pingTimeout) {
        if (trayIcon != null) {
            trayIcon.setToolTip(text);
            if (pingTimeout == -1) {
                trayIcon.setImage(RED_ICON);
            } else if (pingTimeout < 500) {
                trayIcon.setImage(GREEN_ICON);
            } else {
                trayIcon.setImage(YELLOW_ICON);
            }
        }
    }

    public void onUpdateView(String text, Color backgroundColor) {
        if (jTextArea != null) {
            jTextArea.append(text + newline);
            jTextArea.setBackground(backgroundColor);
        }
    }

    public void onExit() {
        service.stop();
        System.exit(0);
    }

    public void onView() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jTextArea = new JTextArea(20, 40);
        jTextArea.setEditable(false);
        jTextArea.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) jTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(jTextArea);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

    }
}
