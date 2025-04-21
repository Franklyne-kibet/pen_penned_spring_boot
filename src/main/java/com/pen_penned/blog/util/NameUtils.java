package com.pen_penned.blog.util;

import org.springframework.util.StringUtils;

public class NameUtils {

    /**
     * Splits a full name into first and last name components.
     * If the full name is empty or null, uses the email prefix as fallback.
     *
     * @param fullName The full name to split, can be null
     * @param email    The email to use as fallback if name is empty
     * @return String array where index 0 is firstName and index 1 is lastName (may be empty)
     */
    public static String[] splitName(String fullName, String email) {
        // Use name if available, otherwise use email prefix
        String nameToSplit = StringUtils.hasText(fullName)
                ? fullName
                : email.split("@")[0];

        String[] result = new String[2];
        String[] nameParts = nameToSplit.trim().split(" ", 2);

        // First name is always the first part
        result[0] = nameParts[0];

        // Last name is the second part if available, otherwise empty string
        result[1] = (nameParts.length > 1 && StringUtils.hasText(nameParts[1]))
                ? nameParts[1]
                : "";

        return result;
    }
}
