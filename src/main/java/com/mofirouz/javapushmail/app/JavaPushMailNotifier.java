package com.mofirouz.javapushmail.app;

import com.mofirouz.notifier.SystemNotification;
import com.mofirouz.javapushmail.JavaPushMailAccount;
import com.mofirouz.javapushmail.JavaPushMailLogger;
import java.io.IOException;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

/**
 *
 * @author Mo Firouz
 * @since 2/10/11
 */
public class JavaPushMailNotifier {

    private MessageCountListener messageCountListener;
    private MessageChangedListener messageChangedListener;
    private JavaPushMailAccount mail;
    private SystemNotification sysnot;

    public JavaPushMailNotifier(JavaPushMailAccount mail, SystemNotification sysnot) {
        this.mail = mail;
        this.sysnot = sysnot;
        initialiseListeners();
        addListeners();
    }

    private void addListeners() {
        mail.setMessageCounterListerer(messageCountListener);
        mail.setMessageChangedListerer(messageChangedListener);
    }

    private void initialiseListeners() {
        messageCountListener = new MessageCountListener() {

            public void messagesAdded(final MessageCountEvent e) {
                try {
                    JavaPushMailLogger.info("Message Added: " + e.getMessages()[0].getSubject());
                    showNotification(e.getMessages()[0]);
                } catch (MessagingException ex) {
                    //JavaPushMailLogger.error("Error showing notification for new added message", ex);
                    //showPlainNotification();
                } catch (Exception ex) {
                    //JavaPushMailLogger.error("Error showing notification for new added message", ex);
                    //showPlainNotification();
                }
            }

            public void messagesRemoved(MessageCountEvent e) {
                try {
                    JavaPushMailLogger.info("Message Removed: " + e.getMessages()[0].getSubject());
                    sysnot.hideNotification(getNotificationID(e.getMessages()[0]));
                } catch (MessagingException ex) {
                    //JavaPushMailLogger.error("Error showing notification for removed message", ex);
                    //showPlainNotification();
                } catch (Exception ex) {
                    //JavaPushMailLogger.error("Error showing notification for removed message", ex);
                    //showPlainNotification();
                }
            }
        };
        messageChangedListener = new MessageChangedListener() {

            public void messageChanged(MessageChangedEvent e) {
                try {
                    //JavaPushMailLogger.info("Message Changed: " + e.getMessage().getSubject());
                    if (e.getMessage().isSet(Flag.SEEN))
                        sysnot.hideNotification(getNotificationID(e.getMessage()));
                } catch (MessagingException ex) {
                    //JavaPushMailLogger.error("Error showing notification for changed message", ex);
                    //showPlainNotification();
                } catch (Exception ex) {
                    //JavaPushMailLogger.error("Error showing notification for changed message", ex);
                    //showPlainNotification();
                }
            }
        };
    }

    private void showNotification(Message message) throws MessagingException {
        if (message == null) {
            showPlainNotification();
            return;
        }

        String[] mess = new String[2];

        String from = message.getFrom()[0].toString();
        if (from.contains("<") && from.contains(">"))
            from = from.substring(0, from.indexOf("<"));
        String subject = message.getSubject().trim();
        String content = "";
        try {
            if (message.getContentType().startsWith("text/plain")) {
                content = message.getContent().toString();
                mess[1] = content;
                if(content.length() > 40)
                    mess[1] = content.substring(0, 40).trim() + "...";
            }
        } catch (IOException e) {
        }
        
        String title = getNotificationTitle(from,subject);
        mess[0] = getNotificationFirstLine(from, subject, content);
        mess[1] = getNotificationSecondLine(from, subject, content);;
        
        sysnot.showNotification(getNotificationID(message), false, title, mess);
    }
    
    private String getNotificationTitle(String from, String subject) {
        int mode = 1; // replace this with preferences...
            
        if (mode == 0) {
            return mail.getAccountName();
        } else if (mode == 1) {
            return from + " (" + mail.getAccountName() + ")";
        } else if (mode == 2) {
            return subject;
        }
        
        return "";
    }
    
    private String getNotificationFirstLine(String from, String subject, String content) {
        int mode = 2; // replace this with preferences...
        
        if (mode == 0) {
            return mail.getAccountName();
        } else if (mode == 1) {
            return from + " (" + mail.getAccountName() + ")";
        } else if (mode == 2) {
            return subject;
        } else if (mode == 3) {
            return content;
        }
        
        return "";
    }
    
    private String getNotificationSecondLine(String from, String subject, String content) {
        int mode = 3; // replace this with preferences...
        
        if (mode == 0) {
            return mail.getAccountName();
        } else if (mode == 1) {
            return from + " (" + mail.getAccountName() + ")";
        } else if (mode == 2) {
            return subject;
        } else if (mode == 3) {
            return content;
        }
        
        return "";
    }

    private void showPlainNotification() {
        String[] mess = new String[2];
        mess[0] = "You have new mail!";
        mess[1] = "";
        sysnot.showNotification(getNotificationID(null), false, mail.getAccountName(), mess);
    }
    
    private String getNotificationID(Message message) {
        if (message == null)
            return mail.getUsername() + "-" + "plain";
        
        return mail.getUsername() + "-" + message.getMessageNumber();
    }
}
