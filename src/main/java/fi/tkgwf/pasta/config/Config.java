package fi.tkgwf.pasta.config;

import java.util.HashMap;
import java.util.Map;

public class Config {

    private static final String ENV_PREFIX = "PASTA_";
    private static final Map<String, String> CONFIG = new HashMap<>();

    static {
        defaultConfig();
        System.getenv().forEach((key, value) -> {
            if (key.startsWith(ENV_PREFIX)) {
                CONFIG.put(key.substring(ENV_PREFIX.length()).toLowerCase(), value);
            }
        });
    }

    private static void defaultConfig() {
        CONFIG.put("http_port", "8080");
        CONFIG.put("redis_host", "redis");
        CONFIG.put("max_size", "1048576"); // 1 MB
        CONFIG.put("min_expiry", "60"); // 1 minute
        CONFIG.put("max_expiry", "31536000"); // 1 year
        CONFIG.put("default_expiry", "2592000"); // 30 days
    }

    public static String get(String key) {
        return CONFIG.get(key);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NullPointerException | NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(get(key));
        } catch (NullPointerException | NumberFormatException ex) {
            return defaultValue;
        }
    }
}
