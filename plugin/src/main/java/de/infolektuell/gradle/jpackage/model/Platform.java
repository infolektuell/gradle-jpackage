package de.infolektuell.gradle.jpackage.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Platform {
    public static Boolean isWindows(String osName) {
        final Pattern pattern = Pattern.compile("windows", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(osName);
        return matcher.find();
    }

    public static Boolean isMac(String osName) {
        final Pattern pattern = Pattern.compile("mac", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(osName);
        return matcher.find();
    }
}
