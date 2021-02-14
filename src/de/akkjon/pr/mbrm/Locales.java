package de.akkjon.pr.mbrm;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Locales {
    private static final Locale locale = Locale.getDefault();
    private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle", getLocale());

    /**
     * getter for {@link #locale}
     *
     * @return {@link Locale}
     */
    private static Locale getLocale() {
        return locale;
    }

    /**
     * gets a string from the {@link ResourceBundle} {@link #messages}
     *
     * @param key {@link String}
     * @return {@link String}
     */
    public static String getString(String key) {
        return messages.getString(key);
    }

    /**
     * gets a string from the {@link ResourceBundle} {@link #messages}
     *
     * @param key    {@link String}
     * @param params parameters for the placeholders in {@link String Strings} from the {@link ResourceBundle} {@link #messages}
     * @return {@link String}
     */
    public static String getString(String key, Object... params) {
        return MessageFormat.format(messages.getString(key), params);
    }
}