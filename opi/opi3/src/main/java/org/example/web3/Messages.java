package org.example.web3;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Загрузка строк из файлов локализации.
 */
public final class Messages {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "locale.messages",
            Locale.forLanguageTag("ru")
    );

    private Messages() {
    }

    public static String get(String key) {
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
