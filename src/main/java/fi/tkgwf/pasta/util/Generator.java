package fi.tkgwf.pasta.util;

import java.security.SecureRandom;

public class Generator {

    private static final String VALID_ID_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate a random ID of the specified length
     *
     * @param length
     * @return The generated ID
     */
    public static String generateID(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Invalid ID length: " + length);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(VALID_ID_CHARS.charAt(RANDOM.nextInt(VALID_ID_CHARS.length())));
        }
        return sb.toString();
    }
}
