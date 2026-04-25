package com.github.rinnn31.motelserver.utils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class FormatHelper {
    public static String formatPhoneNumberToE164(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            var parsedNumber = phoneUtil.parse(phoneNumber, "VN");
            if (!phoneUtil.isValidNumber(parsedNumber)) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ");
            }
            return phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (Exception e) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ", e);
        }
    }
}
