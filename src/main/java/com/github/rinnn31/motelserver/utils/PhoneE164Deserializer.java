package com.github.rinnn31.motelserver.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.google.i18n.phonenumbers.*;

import java.io.IOException;

public class PhoneE164Deserializer extends JsonDeserializer<String> {

    private static final PhoneNumberUtil util = PhoneNumberUtil.getInstance();

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        String raw = p.getText();

        try {
            Phonenumber.PhoneNumber number = util.parse(raw, "VN");

            if (!util.isValidNumber(number)) {
                return null;
            }

            return util.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);

        } catch (Exception e) {
            return null;
        }
    }
}