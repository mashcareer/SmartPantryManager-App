package com.example.smartpantry.utils;

import java.util.Locale;

public final class IngredientNormalizer {
    private IngredientNormalizer() {}

    public static String normalize(String value) {
        if (value == null) return "";
        String s = value.toLowerCase(Locale.ROOT).trim();
        s = s.replaceAll("[^a-z0-9\\s]", " ");
        s = s.replaceAll("\\s+", " ");
        if (s.endsWith("ies") && s.length() > 3) return s.substring(0, s.length() - 3) + "y";
        if (s.endsWith("oes") && s.length() > 3) return s.substring(0, s.length() - 2);
        if (s.endsWith("es") && s.length() > 3) return s.substring(0, s.length() - 2);
        if (s.endsWith("s") && !s.endsWith("ss") && s.length() > 2) return s.substring(0, s.length() - 1);
        return s;
    }
}
