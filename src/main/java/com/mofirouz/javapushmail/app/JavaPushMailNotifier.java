package com.mofirouz.javapushmail.app;

import com.mofirouz.notifier.SystemNotification;
import com.mofirouz.javapushmail.JavaPushMailAccount;
import com.mofirouz.javapushmail.JavaPushMailLogger;
import java.io.IOException;
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
                    JavaPushMailLogger.error("Error showing notification for new added message", ex);
                    //showPlainNotification();
                } catch (Exception ex) {
                    JavaPushMailLogger.error("Error showing notification for new added message", ex);
                    //showPlainNotification();
                }
            }

            public void messagesRemoved(MessageCountEvent e) {
                try {
                    JavaPushMailLogger.info("Message Removed: " + e.getMessages()[0].getSubject());
                    sysnot.hideNotification(getNotificationID(e.getMessages()[0]));
                } catch (MessagingException ex) {
                    JavaPushMailLogger.error("Error showing notification for removed message", ex);
                    //showPlainNotification();
                } catch (Exception ex) {
                    JavaPushMailLogger.error("Error showing notification for removed message", ex);
                    //showPlainNotification();
                }
            }
        };
        messageChangedListener = new MessageChangedListener() {

            public void messageChanged(MessageChangedEvent e) {
                try {
                    JavaPushMailLogger.info("Message Changed: " + e.getMessage().getSubject());
                    sysnot.hideNotification(getNotificationID(e.getMessage()));
                } catch (MessagingException ex) {
                    JavaPushMailLogger.error("Error showing notification for changed message", ex);
                    //showPlainNotification();
                } catch (Exception ex) {
                    JavaPushMailLogger.error("Error showing notification for changed message", ex);
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

        String title = from + " (" + mail.getAccountName() + ")";//mail.getAccountName();
        mess[0] = message.getSubject().trim();
        mess[1] = "";
        try {
            if (message.getContentType().startsWith("text/plain")) {
                String content = message.getContent().toString();
                mess[1] = content;
                if(content.length() > 40)
                    mess[1] = content.substring(0, 40).trim() + "...";
            }
        } catch (IOException e) {
        }
        sysnot.showNotification(getNotificationID(message), false, title, mess);
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
