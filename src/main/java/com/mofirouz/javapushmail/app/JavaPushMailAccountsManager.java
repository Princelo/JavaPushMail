package com.mofirouz.javapushmail.app;

import com.mofirouz.javapushmail.JavaPushMailAccount;
import com.mofirouz.javapushmail.app.ui.JavaPushMail;
import com.mofirouz.notifier.SystemNotification;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Mo Firouz
 * @since 16/10/11
 */
public abstract class JavaPushMailAccountsManager {

    private static final String ENCRYPTION_KEY = "mofirouz6874134567$££";
    private static final String ACCOUNT_FILE = "accounts.jpm";
    private ArrayList<JavaPushMailAccount> accounts = new ArrayList<JavaPushMailAccount>();
    private ArrayList<JavaPushMailNotifier> notifiers = new ArrayList<JavaPushMailNotifier>();
    private SystemNotification sysnot;
    private static boolean connected = false;

    public JavaPushMailAccountsManager() {
        sysnot = new SystemNotification();
        sysnot.setIcon(JavaPushMail.NOTIFICATION_ICON_FILE);
        loadAccounts();
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
                    saveAccounts();
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

    public ArrayList<JavaPushMailAccount> getAccounts() {
        return accounts;
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

    public boolean saveAccounts() {
        try {
            File saveFile = new File(ACCOUNT_FILE);
            if (saveFile.exists())
                saveFile.delete();
            FileOutputStream outputStream = new FileOutputStream(saveFile, false);
            SecretKeySpec key = new SecretKeySpec(new DESKeySpec(ENCRYPTION_KEY.getBytes()).getKey(), "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            CipherOutputStream encryptedOutput = new CipherOutputStream(outputStream, cipher);
            String saveData = "";

            for (JavaPushMailAccount mail : accounts) {
                saveData += "\"" + mail.getAccountName() + "\",\"" + mail.getServerAddress() + "\"," + mail.getServerPort() + ",\"" + mail.isSSL() + "\",\"" + mail.getUsername() + "\",\"" + mail.getPassword() + "\"";
                saveData += "\n";
            }
            encryptedOutput.write(saveData.getBytes());
            encryptedOutput.flush();
            encryptedOutput.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadAccounts() {
        try {
            File file = new File(ACCOUNT_FILE);
            if (!file.exists())
                return false;
            String data = decryptSave(file);
            Scanner sc = new Scanner(data);

            while (sc.hasNextLine()) {
                String str = sc.nextLine();
                String[] line = str.split(",");
                addAccount(false, line[0].replaceAll("\"", "").trim(), line[1].replaceAll("\"", "").trim(), Integer.parseInt(line[2].replaceAll("\"", "").trim()), Boolean.getBoolean(line[3].replaceAll("\"", "").trim()), line[4].replaceAll("\"", "").trim(), line[5].replaceAll("\"", "").trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String decryptSave(File file) {
        String bufferData;
        ArrayList<Integer> ali = new ArrayList<Integer>();
        byte[] by;

        int intRead = 0;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            SecretKeySpec key = new SecretKeySpec(new DESKeySpec(ENCRYPTION_KEY.getBytes()).getKey(), "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            CipherInputStream dec = new CipherInputStream(inputStream, cipher); //Decrypted input...

            while (intRead != -1) {
                intRead = dec.read();
                if (intRead != -1) {
                    ali.add(intRead);
                }
            }
            by = new byte[ali.size()];
            for (int i = 0; i < ali.size(); i++) {
                by[i] = (Byte.decode("" + (Integer) ali.get(i)));
            }
            bufferData = new String(by);
            return bufferData;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
