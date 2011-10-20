package com.mofirouz.javapushmail.app;

import com.mofirouz.javapushmail.JavaPushMailAccount;
import com.mofirouz.notifier.SystemNotification;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author Mo Firouz
 * @since 16/10/11
 */
public abstract class JavaPushMailAccountsManager {

    private ArrayList<JavaPushMailAccount> accounts = new ArrayList<JavaPushMailAccount>();
    private ArrayList<JavaPushMailNotifier> notifiers = new ArrayList<JavaPushMailNotifier>();
    private SystemNotification sysnot;
    private static boolean connected = false;

    public JavaPushMailAccountsManager() {
        sysnot = new SystemNotification();
    }

    public synchronized void addAccount(final boolean testSettings, final String name, final String server, final int port, final boolean useSSL, final String username, final String password) {
        final JavaPushMailAccount mail = new JavaPushMailAccount(name, server, port, useSSL) {

            @Override
            public void onDisconnect(Exception e) {
                connected = false;
                handleDisconnect(e, testSettings);
            }

            @Override
            public void onConnect() {
                connected = true;
                if (!testSettings) {
                    accounts.add(this);
                    notifiers.add(new JavaPushMailNotifier(this, sysnot));
                }
                handleConnect(this, testSettings);
            }
        };
        mail.setCredentials(username, password);
        startMailDaemon(mail);
    }

    public void reconnectAllDisconnected() {

        for (JavaPushMailAccount mail : accounts) {
            startMailDaemon(mail);
        }
    }

    public JavaPushMailAccount getAccount(int i) {
        return accounts.get(i);
    }

    public JavaPushMailNotifier getNotifier(int i) {
        return notifiers.get(i);
    }

    public int countAccounts() {
        return accounts.size();
    }

    public SystemNotification getSystemNotification() {
        return sysnot;
    }

    public boolean saveAccountsToFile() {
        //TODO:
        return false;
    }

    public boolean loadAccountsFromFile(File f) {
        //TODO:
        return false;
    }

    public static boolean isConnected() {
        return connected;
    }

    public abstract void handleDisconnect(Exception ex, boolean testSettings);

    public abstract void handleConnect(JavaPushMailAccount mail, boolean testSettings);

    private void startMailDaemon(JavaPushMailAccount mail) {
        Thread t = new Thread(mail);
        t.setName("JPM-" + mail.getAccountName());
        t.start();
    }
    
    @Deprecated
    public void readAccounts(String filepath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filepath));
            String str;
            while ((str = in.readLine()) != null) {
                if (!str.startsWith("##")) {
                    String[] line = str.split(",");
                    addAccount(false, line[0].replaceAll("\"", "").trim(), line[1].replaceAll("\"", "").trim(), Integer.parseInt(line[2].replaceAll("\"", "").trim()), Boolean.getBoolean(line[3].replaceAll("\"", "").trim()), line[4].replaceAll("\"", "").trim(), line[5].replaceAll("\"", "").trim());
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }
}
