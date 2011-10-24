package com.mofirouz.javapushmail;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Mo Firouz
 * @since 16/10/11
 */
public abstract class NetworkProber {

    private final static int TIME_OUT = 3000; // wait 3secs for every ping
    private final static int SLEEP_TIME = 5000; // wait 5secs between each ping
    private JavaPushMailAccount mail;
    private String host;
    private String name = "NetworkProber";
    private int pingFailureCount = 0;
    private int sessionFailureCount = 0;
    protected Timer timer;

    public NetworkProber(String host, String accountName) {
        this.host = host;
        this.name = "NetworkProper-" + accountName;
    }

    public NetworkProber(JavaPushMailAccount mail, String host) {
        this.host = host;
        this.mail = mail;
        this.name = "NetworkProper-" + mail.getAccountName();
    }

    private boolean probe() {
        boolean status = false;
        try {
            status = InetAddress.getByName(host).isReachable(TIME_OUT);
            if (status)
            	pingFailureCount = 0;
            else
            	pingFailureCount++;
        } catch (Exception ex) {
            pingFailureCount++;
        }
        
        return status;
    }

    private boolean probeWithSessionCheck() {
        boolean status = probe();
        if (status) {
        	if (mail.isSessionValid()) {
        		sessionFailureCount = 0;
        		return true;
        	} else {
        		sessionFailureCount++;
        		return false;
        	}
        }
        return false;
    }
    
    private void periodicProber() {
        TimerTask task = new TimerTask() {
        	
            @Override
            public void run() {
            	if (mail == null)
            		onNetworkChange(probe());
            	else
            		onNetworkChange(probeWithSessionCheck());
            }
        };
        stop();
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(task, Calendar.getInstance().getTime(), SLEEP_TIME);
    }

    public void start() {
        Runnable r = new Runnable() {
            public void run() {
                periodicProber();
            }
        };
        sessionFailureCount = 0;
        pingFailureCount = 0;
        Thread t = new Thread(r, name);
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

    public int getPingFailureCount() {
        return pingFailureCount;
    }
    
    public int getSessionFailureCount() {
    	return sessionFailureCount;
    }

    public abstract void onNetworkChange(boolean change);
}
