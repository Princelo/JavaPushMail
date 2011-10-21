package com.mofirouz.javapushmail;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.SortTerm;

import java.util.EventListener;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Mo Firouz
 * @since 2/10/11
 */
@Slf4j
public abstract class JavaPushMailAccount implements Runnable {

    public final static int READ_ONLY_FOLDER = Folder.READ_ONLY;
    public final static int READ_WRITE_FOLDER = Folder.READ_WRITE;
    private boolean connected = false;
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

    @Override
    public void run() {
        this.initConnection();
    }

    private void initConnection() {
        prober = new NetworkProber(serverAddress) {

            @Override
            public void onNetworkChange(boolean change) {
                connected = true;
                if (getPingFailureCount() >= 2 || getSessionFailureCount() != 0) {
                    log.error(accountName + " Ping fail: " + getPingFailureCount() + " | Session fail: " + getSessionFailureCount());
                    prober.stop();
                    poller.stop();
                    connected = false;
                    reconnect();
                }
            }
        };
        
        poller = new MailPoller(folder) {
			@Override
			public void onNewMessage() {
				try {
				if (externalCountListener != null)
					externalCountListener.messagesAdded(new MessageCountEvent(folder, MessageCountEvent.ADDED, false, getNewMessages()));
				} catch (Exception e) {
					log.debug("Error: ", e);
				}
				
			}
		};

        Properties props = System.getProperties();
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
            reconnect();
        } catch (MessagingException ex) {
            onDisconnect(ex);
        }
    }

    public void reconnect() {
        try {
            server.connect(serverAddress, serverPort, username, password);
            connected = true;
            prober.start();
            selectFolder("");
            poller.start(accountName);
            log.info("{}: Fully Connected!", accountName);
            onConnect();
        } catch (MessagingException ex) {
            connected = false;
            folder = null;
            messageChangedListener = null;
            messageCountListener = null;
            onDisconnect(ex);
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

    private void selectFolder(String folderName) {
        try {
            closeFolder();
            if (folderName.equalsIgnoreCase("")) {
                folder = (IMAPFolder) server.getFolder("INBOX"); //server.getDefaultFolder();
            } else {
                folder = (IMAPFolder) server.getFolder(folderName);
            }
            openFolder();
            addInternalListeners(messageChangedListener);
            addInternalListeners(messageCountListener);
        } catch (MessagingException ex) {
            onDisconnect(ex);
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
        usePush();
    }

    private void closeFolder() throws MessagingException {
        if (folder == null) {
            return;
        }

        removeAllListenersFromFolder();
        folder.setSubscribed(false);
        folder.close(false);
        folder = null;
    }

    private void usePush() {
        if (folder == null)
            return;
        
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    folder.idle(false);
                } catch (Exception e) {
                }
            }
        };
        pushThread = new Thread(r, "Push-" + accountName);
        pushThread.setDaemon(true);
        pushThread.start();
    }
    
    private void removeAllListenersFromFolder() {
        removeListener(messageChangedListener);
        removeListener(messageCountListener);
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
        addListener(messageChangedListener);
        addListener(messageCountListener);
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

                @Override
                public void messageChanged(MessageChangedEvent mce) {
                    usePush();
                }
            };
            folder.addMessageChangedListener(messageChangedListener);
        } else if (listener instanceof MessageCountListener && messageCountListener == null) {
            messageCountListener = new MessageCountListener() {

                @Override
                public void messagesAdded(MessageCountEvent mce) {
                    usePush();
                }

                @Override
                public void messagesRemoved(MessageCountEvent mce) {
                    usePush();
                }
            };
            folder.addMessageCountListener(messageCountListener);
        }
    }

    public Message[] getNewMessages() throws MessagingException {
    	Message[] mess = new Message[folder.getNewMessageCount()];
    	
    	Message[] allmess = folder.getSortedMessages(new SortTerm[] {SortTerm.ARRIVAL, SortTerm.DATE});
    	
    	for (int i = 0; i < folder.getNewMessageCount(); i++)
    		mess[i] = allmess[i];
    	
    	return mess;
    }
    
    public Message[] getMessages() throws MessagingException {
        return folder.getMessages();
    }

    public String getAccountName() {
        return accountName;
    }

    public boolean isConnected() {
        return connected;
    }
    
    public boolean isSessionValid() {
    	return server.isConnected();
    }

    public abstract void onDisconnect(Exception e);

    public abstract void onConnect();
}
