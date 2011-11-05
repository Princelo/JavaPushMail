package com.mofirouz.javapushmail.app.ui;

import com.mofirouz.javapushmail.JavaPushMailAccount;
import com.mofirouz.javapushmail.app.JavaPushMailAccountsManager;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.mail.MessagingException;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Mo Firouz
 * @since 2/10/11
 */
public class JavaPushMailFrame {

    protected JavaPushMailAccountsManager manager;
    protected JFrame frame;
    protected JTabbedPane tabbedPanel;
    protected JTable accountsTable;
    protected JMenuBar menu;
    protected Image dockIcon;
    private JavaPushMailAccountSettingsPanel settingsPanel;
    private NewAccountDialog accountDialog;

    public JavaPushMailFrame() {
        dockIcon = Toolkit.getDefaultToolkit().createImage(getClass().getResource("dock.png"));//(new ImageIcon(this.getClass().getResource("dock.png"))).getImage();
        accountDialog = new NewAccountDialog(frame, true);
    }

    public void init(JavaPushMailAccountsManager manager) {
        this.manager = manager;
        buildTrayPopup();
        frame = new JFrame("Push Mail Configuration Wizard");
        buildPanels();
        buildPopup();
        buildTabs();
        buildFrame();
        loadPreferences();
        initKeyboardHook();
    }

    private void buildFrame() {
        frame.add(tabbedPanel);
        frame.setResizable(false);
        frame.setIconImage(dockIcon);
        frame.getRootPane().setDefaultButton(settingsPanel.getHideButton());
        fixMenuBar();
        frame.pack();
    }

    private void buildTabs() {
        tabbedPanel = new JTabbedPane();
        tabbedPanel.add(settingsPanel);
    }
    
    private void buildPanels() {
        settingsPanel = new JavaPushMailAccountSettingsPanel();
        settingsPanel.getNewAccountButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                accountDialog.dispose();
                accountDialog.setVisible(true);
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
        configTable();
        accountsTable.getModel().addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent tme) {
                updateModel(tme.getFirstRow());
            }
        });
    }

    private void configTable() {
        for (int column = 0; column < accountsTable.getColumnCount(); column++) {
            JTableHeader header = accountsTable.getTableHeader();
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
            renderer.setHorizontalAlignment(JLabel.CENTER);
            accountsTable.getColumnModel().getColumn(column).setHeaderRenderer(renderer);

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            accountsTable.getColumnModel().getColumn(column).setCellRenderer(centerRenderer);
        }
    }

    private void buildPopup() {
        class TablePopup extends JPopupMenu {

            int row;

            public void setSelectedRow(int i) {
                row = i;
            }
        }

        final TablePopup tablePopup = new TablePopup();
        JMenuItem deleteItem = new JMenuItem("Remove");
        deleteItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                tablePopup.setVisible(false);
                
                setWaitingState(true);
                SwingWorker worker = new SwingWorker<String, Object>() {

                    @Override
                    public String doInBackground() {
                        manager.removeAccount(tablePopup.row);
                        return "";
                    }

                    @Override
                    protected void done() {
                        setWaitingState(false);
                    }
                };
                worker.execute();
            }
        });

        final JMenuItem dis_connectItem = new JMenuItem("Connect");
        dis_connectItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                tablePopup.setVisible(false);
                setWaitingState(true);

                SwingWorker worker = new SwingWorker<String, Object>() {

                    @Override
                    public String doInBackground() {
                        if (manager.getAccount(tablePopup.row).isConnected())
                            manager.getAccount(tablePopup.row).disconnect();
                        else
                            manager.getAccount(tablePopup.row).connect();
                        return "";
                    }

                    @Override
                    protected void done() {
                    }
                };
                worker.execute();
            }
        });

        tablePopup.add(dis_connectItem);
        tablePopup.addSeparator();
        tablePopup.add(deleteItem);

        accountsTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (!accountsTable.isEnabled())
                    return;

                if (me.getButton() == MouseEvent.BUTTON3) {
                    tablePopup.setLocation(me.getLocationOnScreen());
                    if (accountsTable.rowAtPoint(me.getPoint()) != -1) {
                        tablePopup.setSelectedRow(accountsTable.rowAtPoint(me.getPoint()));

                        dis_connectItem.setText("Connect");
                        if (manager.getAccount(tablePopup.row).isConnected())
                            dis_connectItem.setText("Disconnect");

                        tablePopup.setVisible(true);
                    }
                } else {
                    tablePopup.setVisible(false);
                }
            }
        });
    }

    private void buildTrayPopup() {
        MenuItem exit = new MenuItem("Exit Notifier");
        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                quitApplication(false);
            }
        });
        MenuItem settings = new MenuItem("Settings");
        settings.addActionListener(new ActionListener() {

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

    private void initKeyboardHook() {
        //TODO:
    }

    private void updateModel(int rowChanged) {
        //TODO:
        //setWaitingState(true);
    }

    public void setWaitingState(boolean show) {
        accountsTable.setEnabled(!show);
        settingsPanel.getNewAccountButton().setEnabled(!show);
        accountsTable.clearSelection();
        settingsPanel.getWorkingLabel().setVisible(show);
    }

    public void showMe(boolean go) {
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(go);
    }

    public void quitApplication(boolean save) {
        frame.dispose();
        
        manager.disconnectAllAccounts();
        if (save && accountsTable.isEnabled()) {
            manager.saveAccounts();
            savePreferences();
        }
        System.exit(0);
    }

    public void onErrorCallback(Exception ex) {
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

    public synchronized void updateOnModelChange() {
        refreshFrame();
    }

    public synchronized void updateOnStateChange() {
        refreshFrame();
        setWaitingState(false);
    }

    public synchronized void refreshFrame() {
        if (accountsTable == null)
            return;

        DefaultTableModel model = (DefaultTableModel) accountsTable.getModel();
        model.setRowCount(0);

        for (int i = 0; i < manager.countAccounts(); i++) {
            JavaPushMailAccount mail = manager.getAccount(i);
            Vector data = new Vector();
            data.add(mail.getAccountName());
            data.add(mail.getServerAddress());
            data.add(mail.getServerPort());
            data.add(mail.isSSL());
            data.add(mail.getUsername());
            data.add(JavaPushMailAccountSettingsPanel.PASSWORD_FIELD);
            model.addRow(data);
        }
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
