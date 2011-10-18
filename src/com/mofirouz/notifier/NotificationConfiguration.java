package com.mofirouz.notifier;

import ch.swingfx.twinkle.style.AbstractNotificationStyle;
import ch.swingfx.twinkle.style.theme.LightDefaultNotification;
import ch.swingfx.twinkle.window.Positions;

/**
 *
 * @author Mo Firouz
 * @since 9/10/11
 */
public class NotificationConfiguration {
    private AbstractNotificationStyle style;
    private Positions position;
    
    public NotificationConfiguration() {
    }
    
    public AbstractNotificationStyle getStyle() {
        return style;
    }
    
    public Positions getPosition() {
        return position;
    }

    public void setPosition(Positions position) {
        this.position = position;
    }

    public void setStyle(AbstractNotificationStyle style) {
        this.style = style;
    }
    
    public static NotificationConfiguration getDefaultConfiguration() {
        NotificationConfiguration config = new NotificationConfiguration();
        config.style = new LightDefaultNotification();
        config.position = Positions.SOUTH_EAST;
        return config;
    }
    
}
