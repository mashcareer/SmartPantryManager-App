package com.example.smartpantry.utils;

import java.util.Locale;

public final class UnitConverter {
    private UnitConverter() {}

    public static boolean areCompatible(String from, String to) {
        return dimension(from) != null && dimension(from).equals(dimension(to));
    }

    public static double convert(double quantity, String from, String to) {
        if (!areCompatible(from, to)) throw new IllegalArgumentException("Incompatible units");
        if (from.equalsIgnoreCase(to)) return quantity;
        String dim = dimension(from);
        double base = toBase(quantity, from, dim);
        return fromBase(base, to, dim);
    }

    private static String dimension(String unit) {
        if (unit == null) return null;
        switch (unit.toLowerCase(Locale.ROOT).trim()) {
            case "pieces": return "count";
            case "grams":
            case "kg": return "mass";
            case "ml":
            case "litres":
            case "cups":
            case "tablespoons":
            case "teaspoons": return "volume";
            default: return null;
        }
    }

    private static double toBase(double q, String unit, String dim) {
        String u = unit.toLowerCase(Locale.ROOT).trim();
        if ("count".equals(dim)) return q;
        if ("mass".equals(dim)) return u.equals("kg") ? q * 1000.0 : q;
        switch (u) {
            case "litres": return q * 1000.0;
            case "cups": return q * 240.0;
            case "tablespoons": return q * 15.0;
            case "teaspoons": return q * 5.0;
            default: return q;
        }
    }

    private static double fromBase(double q, String unit, String dim) {
        String u = unit.toLowerCase(Locale.ROOT).trim();
        if ("count".equals(dim)) return q;
        if ("mass".equals(dim)) return u.equals("kg") ? q / 1000.0 : q;
        switch (u) {
            case "litres": return q / 1000.0;
            case "cups": return q / 240.0;
            case "tablespoons": return q / 15.0;
            case "teaspoons": return q / 5.0;
            default: return q;
        }
    }
}
