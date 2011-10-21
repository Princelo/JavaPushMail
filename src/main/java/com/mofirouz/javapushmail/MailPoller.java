package com.mofirouz.javapushmail;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.mail.imap.IMAPFolder;

/**
 *
 * @author Mo Firouz
 * @since 20/10/11
 */
public abstract class MailPoller {

    private final static int SLEEP_TIME = 1000; //300000; // check mail every 5 min
    private IMAPFolder folder;
    protected Timer timer;

    public MailPoller(IMAPFolder folder) {
        this.folder = folder;
    }

    private boolean poll() {
        try {
            return folder.hasNewMessages();
        } catch (Exception ex) {
        	return false;
        }
    }
    
    private void periodicPoller() {
        TimerTask task = new TimerTask() {
        	
            @Override
            public void run() {
            	if (poll())
            		onNewMessage();
            }
        };
        stop();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(task, Calendar.getInstance().getTime(), SLEEP_TIME);
    }

    public void start(String name) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                periodicPoller();
            }
        };
        Thread t = new Thread(r, "MailPoller-" + name);
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        if (timer == null)
            return;

        timer.cancel();
        timer.purge();
        timer = null;
    }

    public abstract void onNewMessage();
}
