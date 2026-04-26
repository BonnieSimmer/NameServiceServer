package com.assignment;

import java.util.regex.Pattern;

public class IPValidator {
    // 25[0-5]          -> 250-255
    // 2[0-4][0-9]      -> 200-249
    // [01]?[0-9][0-9]? -> 0-199
    private static final String IPV4_PATTERN =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern pattern = Pattern.compile(IPV4_PATTERN);

    public static boolean isValid(String ip) {
        if (ip == null) return false;
        return pattern.matcher(ip).matches();
    }
}

