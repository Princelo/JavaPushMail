package com.mofirouz.javapushmail.app.ui;

import com.mofirouz.javapushmail.JavaPushMailAccount;
import com.mofirouz.javapushmail.app.JavaPushMailAccountsManager;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 *
 * @author Mo Firouz 
 * @since 2/10/11
 */
public class JavaPushMail {

	static {
		// set "production" logging to WARN+ by default
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.WARN);

		// to enable "testing" output to console use:
        // 'java -Djavapushmail.debug=INFO -jar javapushmail.jar <params>'
        String s = System.getProperty("javapushmail.debug");
        if (s != null) {
            root.setLevel(Level.INFO);
        }
	}

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
