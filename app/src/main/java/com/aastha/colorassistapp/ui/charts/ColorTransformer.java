package com.aastha.colorassistapp.ui.charts;

import android.graphics.Color;

/**
 * ColorTransformer applies colorblindness simulation filters to RGB colors
 * Based on Brettel, Viénot, and Mollon (1997) model
 */
public class ColorTransformer {

    /**
     * Protanopia (Red-blind): Missing long-wavelength cones (L cones)
     * Person sees reds as black, confuses red and green
     */
    public static int applyProtanopia(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        // Transformation matrix for protanopia
        // Based on: Hunt et al. 1995 cone response to D65 light
        float L = (float) (0.567 * r + 0.433 * g);
        float M = (float) (0.558 * r + 0.442 * g);
        float S = (float) (b);

        // Transform to visible spectrum
        int newR = (int) Math.min(255, Math.max(0, 0.299 * L + 0.701 * M));
        int newG = (int) Math.min(255, Math.max(0, 0.169 * L + 0.831 * M));
        int newB = (int) Math.min(255, Math.max(0, S));

        return Color.argb(a, newR, newG, newB);
    }

    /**
     * Deuteranopia (Green-blind): Missing medium-wavelength cones (M cones)
     * Person sees greens as black, confuses red and green
     */
    public static int applyDeuteranopia(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        // Transformation matrix for deuteranopia
        float L = (float) (r);
        float M = (float) (0.625 * r + 0.375 * g);
        float S = (float) (b);

        // Transform to visible spectrum
        int newR = (int) Math.min(255, Math.max(0, 0.700 * L + 0.300 * M));
        int newG = (int) Math.min(255, Math.max(0, 0.700 * L + 0.300 * M));
        int newB = (int) Math.min(255, Math.max(0, S));

        return Color.argb(a, newR, newG, newB);
    }

    /**
     * Tritanopia (Blue-blind): Missing short-wavelength cones (S cones)
     * Person sees blues and yellows differently, confuses blue and yellow
     */
    public static int applyTritanopia(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        // Transformation matrix for tritanopia
        float L = (float) (r);
        float M = (float) (g);
        float S = (float) (0.949 * b + 0.051 * r);

        // Transform to visible spectrum
        int newR = (int) Math.min(255, Math.max(0, L));
        int newG = (int) Math.min(255, Math.max(0, 0.475 * M + 0.525 * S));
        int newB = (int) Math.min(255, Math.max(0, 0.183 * M + 0.817 * S));

        return Color.argb(a, newR, newG, newB);
    }

    /**
     * Simpler Protanopia simulation (alternative)
     * Reduces red channel intensity, shifts colors toward blue-yellow spectrum
     */
    public static int applyProtanopiaSimple(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        // Simulate by reducing red sensitivity and enhancing blue-yellow
        int newR = (int) (r * 0.56);
        int newG = (int) (g * 0.58);
        int newB = (int) (b);

        return Color.argb(a, newR, newG, newB);
    }

    /**
     * Simpler Deuteranopia simulation (alternative)
     * Reduces green channel intensity
     */
    public static int applyDeuteranopiaSimple(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        // Simulate by reducing green sensitivity
        int newR = (int) (r * 0.625);
        int newG = (int) (g * 0.375);
        int newB = (int) (b);

        return Color.argb(a, newR, newG, newB);
    }

    /**
     * Simpler Tritanopia simulation (alternative)
     * Shifts blue-yellow perception
     */
    public static int applyTritanopiaSimple(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        // Simulate by shifting blue perception
        int newR = (int) (r * 0.95);
        int newG = (int) (g * 0.475);
        int newB = (int) (b * 0.817);

        return Color.argb(a, newR, newG, newB);
    }

    /**
     * Apply the appropriate colorblindness transformation based on mode
     */
    public static int transformColor(int color, Mode mode) {
        switch (mode) {
            case DEUTERANOPIA:
                return applyDeuteranopia(color);
            case PROTANOPIA:
                return applyProtanopia(color);
            case TRITANOPIA:
                return applyTritanopia(color);
            case NONE:
            default:
                return color;
        }
    }

    /**
     * Colorblindness mode enumeration
     */
    public enum Mode {
        NONE,
        PROTANOPIA,      // Red-blind
        DEUTERANOPIA,    // Green-blind
        TRITANOPIA       // Blue-blind
    }
}