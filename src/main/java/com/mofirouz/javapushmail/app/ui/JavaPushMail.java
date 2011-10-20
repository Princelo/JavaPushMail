package com.mofirouz.javapushmail.app.ui;

import com.mofirouz.javapushmail.JavaPushMailAccount;
import com.mofirouz.javapushmail.app.JavaPushMailAccountsManager;

/**
 *
 * @author Mo Firouz 
 * @since 2/10/11
 */
public class JavaPushMail {

    JavaPushMailFrame frame;
    JavaPushMailAccountsManager manager;

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JavaPushMail jpm = new JavaPushMail();
                jpm.init();
            }
        });
    }

    public JavaPushMail() {
    }

    @SuppressWarnings("deprecation")
	public void init() {
        initFrame();
        initManager();
        frame.init(manager);
        frame.showMe(!frame.isUsingPerferences());

        manager.readAccounts("credentials.credentials");
        //manager.addAccount(false, "Java Mo", "mail.webfaction.com", Integer.parseInt("993"), true, "mofirouz_java", "mofirouzjava");
    }

    private void initManager() {
        manager = new JavaPushMailAccountsManager() {

            @Override
            public void handleConnect(JavaPushMailAccount mail, boolean testSettings) {
                frame.onConnectCallback();
            }

            @Override
            public void handleDisconnect(Exception ex, boolean testSettings) {
                if (testSettings) {
                    return;
                }

                frame.onDisconnectCallback(ex);
            }
        };
    }

    private void initFrame() {
        String os = System.getProperty("os.name");
        if (os.contains("Mac"))
            frame = new JavaPushMailFrameX();
        else
            frame = new JavaPushMailFrame();
    }
}
