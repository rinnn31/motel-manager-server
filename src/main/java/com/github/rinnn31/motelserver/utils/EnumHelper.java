package com.github.rinnn31.motelserver.utils;

public class EnumHelper {
    public static <E extends Enum<E>> E fromString(Class<E> enumClass, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static <E extends Enum<E>> E fromString(Class<E> enumClass, String value, E defaultValue) {
        E result = fromString(enumClass, value);
        return result != null ? result : defaultValue;
    }
}
