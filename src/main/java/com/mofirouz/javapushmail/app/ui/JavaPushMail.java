package com.mofirouz.javapushmail.app.ui;

import com.mofirouz.javapushmail.JavaPushMailAccount;
import com.mofirouz.javapushmail.app.JavaPushMailAccountsManager;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Mo Firouz 
 * @since 2/10/11
 */
public class JavaPushMail {

    private JavaPushMailFrame frame;
    private JavaPushMailAccountsManager manager;
    public static String NOTIFICATION_ICON;
    public static File NOTIFICATION_ICON_FILE;
    

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JavaPushMail jpm = new JavaPushMail();
                jpm.init();
            }
        });
    }

    public JavaPushMail() {
        NOTIFICATION_ICON = getTempImagePath((new ImageIcon(this.getClass().getResource("email48x48.png"))).getImage());
        NOTIFICATION_ICON_FILE = new File(NOTIFICATION_ICON);
    }
    
    public void init() {
        initFrame();
        initManager();
        frame.init(manager);
        frame.showMe(!frame.isUsingPerferences());
        
        manager.readAccounts("credentials.credentials");
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

    protected static String getTempImagePath(Image icon) {
        if (icon == null)
            return null;

        File iconFile = new File(".");

        try {
            iconFile = File.createTempFile("temp", ".png");
            BufferedImage bimg = new BufferedImage(icon.getWidth(null), icon.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bimg.createGraphics();
            g2d.drawImage(icon, 0, 0, null);
            g2d.dispose();
            ImageIO.write(bimg, "png", iconFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        iconFile.deleteOnExit();

        return iconFile.getAbsolutePath();
    }
}
