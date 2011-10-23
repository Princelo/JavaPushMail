package com.mofirouz.javapushmail.app.ui;

import com.mofirouz.javapushmail.JavaPushMailAccount;
import com.mofirouz.javapushmail.app.JavaPushMailAccountsManager;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.mail.MessagingException;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Mo Firouz
 * @since 2/10/11
 */
public class JavaPushMailFrame {

    protected JavaPushMailAccountsManager manager;
    protected JFrame frame;
    protected JTable accountsTable;
    protected JMenuBar menu;
    protected static Image dockIcon;
    private JavaPushMailAccountSettingsPanel settingsPanel;

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
        buildPanels();
        buildFrame();
        loadPreferences();
        initKeyboardHook();
    }

    private void buildFrame() {
        frame.add(settingsPanel);
        frame.setResizable(false);
        frame.setIconImage(dockIcon);
        fixMenuBar();
        frame.pack();
    }

    private void buildPanels() {
        settingsPanel = new JavaPushMailAccountSettingsPanel();

        settingsPanel.getConnectButton().setEnabled(!isUsingPerferences());
        settingsPanel.getConnectButton().setEnabled(false); // temp
        settingsPanel.getConnectButton().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                settingsPanel.getConnectButton().setEnabled(false);
                manager.reconnectAllDisconnected();
            }
        });

        settingsPanel.getQuitButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                quitApplication(true);
            }
        });

        settingsPanel.getHideButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                manager.saveAccounts();
                frame.dispose();
            }
        });

        accountsTable = settingsPanel.getAccountTable();
        accountsTable.getModel().addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent tme) {
                System.out.println("Table changed!");
            }
        });
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
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(go);
    }

    public void quitApplication(boolean save) {
        if (save) {
            manager.saveAccounts();
            savePreferences();
        }
        System.exit(0);
    }

    public void onConnectCallback() {
        settingsPanel.getConnectButton().setEnabled(false);
        refreshFrame();
    }

    public void onDisconnectCallback(Exception ex) {
        settingsPanel.getConnectButton().setEnabled(true);
        refreshFrame();
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

    public void refreshFrame() {
        if (accountsTable == null)
            return;

        DefaultTableModel model = (DefaultTableModel) accountsTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++)
            model.removeRow(i);
        
        for (JavaPushMailAccount mail : manager.getAccounts()) {
            Vector data = new Vector();
            data.add(mail.getAccountName());
            data.add(mail.getServerAddress());
            data.add(mail.getServerPort());
            data.add(mail.isSSL());
            data.add(mail.getUsername());
            data.add(JavaPushMailAccountSettingsPanel.PASSWORD_FIELD);
            data.add(Boolean.TRUE);
            model.addRow(data);
        }
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
