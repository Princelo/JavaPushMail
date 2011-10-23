package com.mofirouz.javapushmail.app.ui;

import com.mofirouz.javapushmail.app.JavaPushMailAccountsManager;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.mail.MessagingException;
import javax.swing.*;

/**
 *
 * @author Mo Firouz
 * @since 2/10/11
 */
public class JavaPushMailFrame {

    protected JavaPushMailAccountsManager manager;
    protected JFrame frame;
    protected JMenuBar menu;
    protected static Image dockIcon;
    private JButton connectBut;

    static {
        dockIcon = (Toolkit.getDefaultToolkit().createImage(JavaPushMailFrame.class.getResource("/dock.png")));
    }

    public JavaPushMailFrame() {
        dockIcon = (new ImageIcon(this.getClass().getResource("dock.png"))).getImage();
    }

    public void init(JavaPushMailAccountsManager manager) {
        this.manager = manager;
        buildTrayPopup();
        frame = new JFrame("Push Mail Configuration Wizard");
        frame.add(buildPanels());
        buildFrame();
        loadPreferences();
        initKeyboardHook();
    }

    private void buildFrame() {
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setIconImage(dockIcon);
        fixMenuBar();
        frame.pack();
    }

    private JPanel buildPanels() {
        JPanel main = new JPanel();

        connectBut = new JButton("Reconnect");
        connectBut.setEnabled(!isUsingPerferences());
        connectBut.setEnabled(false); // temp
        connectBut.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                connectBut.setEnabled(false);
                manager.reconnectAllDisconnected();
            }
        });

        main.add(connectBut);
        //TODO: create panels here!

        return main;
    }

    private void buildTrayPopup() {
        MenuItem exit = new MenuItem("Exit Notifier");
        exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                quitApplication(false);
            }
        });
        MenuItem settings = new MenuItem("Settings");
        settings.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(true);
            }
        });

        PopupMenu popmenu = new PopupMenu();
        popmenu.add(settings);
        popmenu.add(exit);

        manager.getSystemNotification().setPopupMenu(popmenu);
    }

    protected void fixMenuBar() {
    }

    public void showMe(boolean go) {
        frame.setVisible(go);
    }

    public void quitApplication(boolean save) {
        if (save) {
            manager.saveAccountsToFile();
            savePreferences();
        }
        System.exit(0);
    }

    public void onConnectCallback() {
        connectBut.setEnabled(false);
    }

    public void onDisconnectCallback(Exception ex) {
        connectBut.setEnabled(true);

        if (ex instanceof MessagingException) {
            String error = "";
            error += "You have been disconnected. Please reconnect manually.\n";
            error += "\t\tError: " + ex.toString();
            error += "\nPlease check the followings:";
            error += "\n- You have Internet Connectivity";
            error += "\n- You have entered the details correctly";
            error += "\n- Your mail server is operational";
            error += "\n- Your mail server supports IDLE and Push notifications";
            JOptionPane.showMessageDialog(frame, error);
        }
        ex.printStackTrace();
    }

    private void initKeyboardHook() {
    }

    // should only be called upon pressing Save button on the frame,
    // or when the app is closing by the main frame, not the system tray.
    protected boolean savePreferences() {
        return false;
    }

    // should be called automatically at startup, if perferences exists.
    protected boolean loadPreferences() {
        return false;
    }

    public boolean isUsingPerferences() {
        // checks whether perefences exits, and if so, does not show the initial frame at start up.
        boolean res = false;

        return res;
    }
}
