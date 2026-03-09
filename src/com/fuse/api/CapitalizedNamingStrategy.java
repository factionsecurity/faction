package com.fuse.api;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;

public class CapitalizedNamingStrategy extends PropertyNamingStrategies.NamingBase {
    @Override
    public String translate(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}