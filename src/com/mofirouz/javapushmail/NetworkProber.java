package com.mofirouz.javapushmail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mo Firouz
 * @since 16/10/11
 */
public abstract class NetworkProber {

    private final static int TIME_OUT = 3000; // wait 3secs for every ping
    private final static int SLEEP_TIME = 5000; // wait 5secs between each ping
    private String host;
    private int failureCount = 0;
    protected Timer timer;

    public NetworkProber(String host) {
        this.host = host;
    }

    private boolean probe() {
        boolean status = false;
        try {
            status = InetAddress.getByName(host).isReachable(TIME_OUT);
            if (status == false)
                failureCount++;
            else
                failureCount = 0;
        } catch (Exception ex) {
            failureCount++;
        }

        return status;
    }

    private void periodicProber() {
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                onNetworkChange(probe());
            }
        };
        stop();

        timer = new Timer(true);
        timer.scheduleAtFixedRate(task, Calendar.getInstance().getTime(), SLEEP_TIME);
    }

    public void start() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                periodicProber();
            }
        };
        Thread t = new Thread(r, "NetworkProberStarter");
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

    public int getFailureCount() {
        return failureCount;
    }

    public abstract void onNetworkChange(boolean change);
}
