package com.mofirouz.javapushmail.app.ui;

import javax.swing.*;
import com.apple.eawt.*;

/**
 *
 * @author Mo Firouz
 * @since 15/10/11
 */
public class JavaPushMailFrameX extends JavaPushMailFrame {

    private Application app;

    public JavaPushMailFrameX() {
        super();
        initAppleFrame();
        setAppleHandlers();
    }

    private void initAppleFrame() {
        try {
            app = new Application();
            com.apple.resources.MacOSXResourceBundle.clearCache();
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            app.setDockIconImage(super.dockIcon);
            app.setEnabledAboutMenu(false);
            app.setEnabledPreferencesMenu(true);
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
    }

    @Override
    protected void fixMenuBar() {
        menu = new JMenuBar();
        apple.laf.ScreenMenuBar screenmenu = new apple.laf.ScreenMenuBar(menu);
        frame.setMenuBar(screenmenu);
    }

    private void setAppleHandlers() {
        app.addApplicationListener(new ApplicationListener() {

            @Override
            public void handleAbout(ApplicationEvent e) {
                e.setHandled(true);
            }

            @Override
            public void handleOpenApplication(ApplicationEvent e) {
                e.setHandled(true);
            }

            @Override
            public void handleOpenFile(ApplicationEvent e) {
                e.setHandled(true);
            }

            @Override
            public void handlePreferences(ApplicationEvent e) {
                e.setHandled(true);
                showMe(true);
            }

            @Override
            public void handlePrintFile(ApplicationEvent e) {
                e.setHandled(true);
            }

            @Override
            public void handleQuit(ApplicationEvent e) {
                e.setHandled(false);
                quitApplication(true);
            }
            @Override
            public void handleReOpenApplication(ApplicationEvent e) {
                e.setHandled(true);
                frame.setVisible(true);
                frame.repaint();
            }
        });
    }
}
