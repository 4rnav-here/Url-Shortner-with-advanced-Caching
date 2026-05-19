package com.urlShortner.project.util;

import java.security.SecureRandom;

public class Base62Generator {
    private static final String BASE62 =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final SecureRandom random = new SecureRandom();

    public static String generateShortCode(int length) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {

            int index = random.nextInt(BASE62.length());

            sb.append(BASE62.charAt(index));
        }

        return sb.toString();
    }
}
