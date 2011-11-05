package com.mofirouz.javapushmail;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.SortTerm;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

/**
 *
 * @author Mo Firouz
 * @since 2/10/11
 */
public abstract class JavaPushMailAccount implements Runnable {

    public final static int READ_ONLY_FOLDER = Folder.READ_ONLY;
    public final static int READ_WRITE_FOLDER = Folder.READ_WRITE;
    private boolean connected = false;
    private boolean usePush = true;
    private String accountName;
    private String serverAddress;
    private String username;
    private String password;
    private int serverPort;
    private boolean useSSL;
    private IMAPStore server;
    private Session session;
    private IMAPFolder folder;
    private MessageCountListener messageCountListener, externalCountListener;
    private MessageChangedListener messageChangedListener, externalChangedListener;
    private NetworkProber prober;
    private MailPoller poller;
    private Thread pushThread;

    public JavaPushMailAccount(String accountName, String serverAddress, int serverPort, boolean useSSL) {
        this.accountName = accountName;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.useSSL = useSSL;
    }

    public void setCredentials(String u, String p) {
        this.username = u;
        this.password = p;
    }

    public void run() {
        this.initConnection();
    }

    public void connect() {
        try {
            server.connect(serverAddress, serverPort, username, password);
            selectFolder("");
            connected = true;
            prober.start();
            if (!usePush)
                poller.start(accountName);
            System.err.println("Fully Connected: " + accountName);
            onConnect();
        } catch (MessagingException ex) {
            connected = false;
            folder = null;
            messageChangedListener = null;
            messageCountListener = null;
            onError(ex);
        } catch (IllegalStateException ex) {
        }
    }

    public void setMessageChangedListerer(MessageChangedListener listener) {
        removeListener(externalChangedListener);
        externalChangedListener = listener;
        addListener(externalChangedListener);
    }

    public void setMessageCounterListerer(MessageCountListener listener) {
        removeListener(externalCountListener);
        externalCountListener = listener;
        addListener(externalCountListener);
    }

    public void disconnect() {
        if (!connected || server == null || !server.isConnected())
            return;

        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    closeFolder();
                    server.close();
                    prober.stop();
                    poller.stop();
                    connected = false;
                    onDisconnect();
                } catch (Exception e) {
                    onError(e);
                }
            }
        });
        t.start();
    }

    private void initConnection() {
        prober = new NetworkProber(serverAddress, accountName) {

            @Override
            public void onNetworkChange(boolean change) {
                connected = true;
                if (getPingFailureCount() >= 2 || getSessionFailureCount() != 0) {
                    connected = false;
                    prober.stop();
                    if (!usePush)
                        poller.stop();
                    connect();
                }
            }
        };

        poller = new MailPoller(folder) {

            @Override
            public void onNewMessage() {
                try {
                    if (externalCountListener != null) {
                        externalCountListener.messagesAdded(new MessageCountEvent(folder, MessageCountEvent.ADDED, false, getNewMessages()));
                        messageCountListener.messagesAdded(new MessageCountEvent(folder, MessageCountEvent.ADDED, false, getNewMessages()));
                    }
                } catch (Exception e) {
                    onError(e);
                }

            }
        };

        Properties props = System.getProperties();

        //props.put("mail.debug", "true");

        String imapProtocol = "imap";
        if (useSSL) {
            imapProtocol = "imaps";
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.imap.socketFactory.fallback", "false");
        }
        props.setProperty("mail.store.protocol", imapProtocol);
        session = Session.getDefaultInstance(props, null);
        try {
            server = (IMAPStore) session.getStore("imaps");
            connect();
        } catch (MessagingException ex) {
            onError(ex);
        }
    }

    private void selectFolder(String folderName) {
        try {
            closeFolder();
            if (folderName.equalsIgnoreCase("")) {
                folder = (IMAPFolder) server.getFolder("INBOX");
            } else {
                folder = (IMAPFolder) server.getFolder(folderName);
            }
            openFolder();
        } catch (MessagingException ex) {
            onError(ex);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    private void openFolder() throws MessagingException {
        if (folder == null)
            return;

        folder.open(Folder.READ_ONLY);
        folder.setSubscribed(true);
        removeAllListenersFromFolder();
        addAllListenersFromFolder();
        poller.setFolder(folder);
        usePush();
    }

    private void closeFolder() throws MessagingException {
        if (folder == null)
            return;

        removeAllListenersFromFolder();
        folder.setSubscribed(false);
        folder.close(false);
        folder = null;
    }

    private void usePush() {
        if (folder == null)
            return;

        Runnable r = new Runnable() {

            public void run() {
                try {
                    if (usePush)
                        folder.idle(false);
                } catch (Exception e) {
                    System.err.println("Push Error: " + accountName);
                    e.printStackTrace();
                    connect();
                    usePush = false;
                }
            }
        };
        pushThread = new Thread(r, "Push-" + accountName);
        pushThread.setDaemon(true);
        pushThread.start();
    }

    private void removeAllListenersFromFolder() {
        removeListener(externalChangedListener);
        removeListener(externalCountListener);
    }

    private void removeListener(EventListener listener) {
        if (listener == null || folder == null)
            return;

        if (listener instanceof MessageChangedListener) {
            folder.removeMessageChangedListener((MessageChangedListener) listener);
        } else if (listener instanceof MessageCountListener) {
            folder.removeMessageCountListener((MessageCountListener) listener);
        }
    }

    private void addAllListenersFromFolder() {
        addListener(externalCountListener);
        addListener(externalChangedListener);
    }

    private void addListener(EventListener listener) {
        if (listener == null || folder == null)
            return;

        if (listener instanceof MessageChangedListener) {
            folder.addMessageChangedListener((MessageChangedListener) listener);
        } else if (listener instanceof MessageCountListener) {
            folder.addMessageCountListener((MessageCountListener) listener);
        }

        addInternalListeners(listener);

    }

    private void addInternalListeners(EventListener listener) {
        if (listener == null || folder == null)
            return;

        if (listener instanceof MessageChangedListener && messageChangedListener == null) {
            messageChangedListener = new MessageChangedListener() {

                public void messageChanged(MessageChangedEvent mce) {
                    usePush();
                }
            };
            folder.addMessageChangedListener(messageChangedListener);
        } else if (listener instanceof MessageCountListener && messageCountListener == null) {
            messageCountListener = new MessageCountListener() {

                public void messagesAdded(MessageCountEvent mce) {
                    usePush();
                }

                public void messagesRemoved(MessageCountEvent mce) {
                    usePush();
                }
            };
            folder.addMessageCountListener(messageCountListener);
        }
    }

    public Message[] getNewMessages() throws MessagingException {
        ArrayList<Message> mess = new ArrayList<Message>();

        Message[] allmess = folder.getSortedMessages(new SortTerm[]{SortTerm.ARRIVAL, SortTerm.DATE});

        for (int i = 0; i < poller.getDiffCount(); i++) {
            if (allmess[i].isSet(Flags.Flag.SEEN) == false)
                mess.add(allmess[i]);
        }

        Message[] messages = new Message[mess.size()];
        for (int i = 0; i < mess.size(); i++)
            messages[i] = mess.get(i);

        return messages;
    }

    public Message[] getMessages() throws MessagingException {
        return folder.getMessages();
    }

    public String getAccountName() {
        return accountName;
    }

    public String getPassword() {
        return password;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public boolean isSSL() {
        return useSSL;
    }

    public String getUsername() {
        return username;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isSessionValid() {
        return server.isConnected();
    }

    public abstract void onError(Exception e);

    public abstract void onDisconnect();

    public abstract void onConnect();
}
