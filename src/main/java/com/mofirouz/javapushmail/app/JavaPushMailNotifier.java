package com.mofirouz.javapushmail.app;

import com.mofirouz.notifier.SystemNotification;
import com.mofirouz.javapushmail.JavaPushMailAccount;
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
        //mail.setMessageChangedListerer(messageChangedListener);
    }

    private void initialiseListeners() {
        messageCountListener = new MessageCountListener() {

            @Override
            public void messagesAdded(final MessageCountEvent e) {
                try {
                    System.err.println("Message Added: " + e.getMessages()[0].getSubject());
                    showNotification(e.getMessages()[0]);
                } catch (MessagingException ex) {
                }
            }

            @Override
            public void messagesRemoved(MessageCountEvent e) {
                try {
                    System.err.println("Message Removed: " + e.getMessages()[0].getSubject());
                } catch (MessagingException ex) {
                    
                }
            }
        };
        messageChangedListener = new MessageChangedListener() {

            @Override
            public void messageChanged(MessageChangedEvent e) {
                try {
                    System.err.println("Message Changed: " + e.getMessage().getSubject());
                } catch (MessagingException ex) {
                }
            }
        };
    }

    private void showNotification(Message message) throws MessagingException {

        String title = mail.getAccountName();
        String[] mess = new String[2];

        String from = message.getFrom()[0].toString();
        if (from.contains("<") && from.contains(">"))
            from = from.substring(0, from.indexOf("<"));

        mess[0] = from;
        mess[1] = message.getSubject();
        sysnot.showNotification(false, title, mess);
    }
}
