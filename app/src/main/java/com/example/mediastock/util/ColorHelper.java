package com.example.mediastock.util;


import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.util.Log;

/**
 * Class to determine if a color is similar to another color.
 */
public class ColorHelper {
    // black, white, red, blue, green, yellow, orange, magenta, grey, cyan
    private final static String[] colorsID = {"#000000", "#ffffff", "#dc020e", "#0226dc", "#15a415", "#ffea00", "#ff8800", "#ff00ff", "888888", "00ffff"};
    private final boolean colorSimilarity;
    private int usersColor, darkVibrantSwatch, lightVibrantSwatch, vibrantSwatch, mutedSwatch, darkMutedSwatch, lightMutedSwatch;


    public ColorHelper(int colorType, Palette.Swatch vibrantSwatch, Palette.Swatch darkVibrantSwatch, Palette.Swatch lightVibrantSwatch,
                       Palette.Swatch mutedSwatch, Palette.Swatch darkMutedSwatch, Palette.Swatch lightMutedSwatch) {

        if (vibrantSwatch != null)
            this.vibrantSwatch = vibrantSwatch.getRgb();

        if (lightVibrantSwatch != null)
            this.lightVibrantSwatch = lightVibrantSwatch.getRgb();

        if (darkVibrantSwatch != null)
            this.darkVibrantSwatch = darkVibrantSwatch.getRgb();

        if (mutedSwatch != null)
            this.mutedSwatch = mutedSwatch.getRgb();

        if (darkMutedSwatch != null)
            this.darkMutedSwatch = darkMutedSwatch.getRgb();

        if (lightMutedSwatch != null)
            this.lightMutedSwatch = lightMutedSwatch.getRgb();

        // the color to search for
        this.usersColor = getUsersColor(colorType);

        // it determines if the swatches are similar to the users color
        this.colorSimilarity = analyseSwatches();
    }

    /**
     * It determines if the swatches are similar to the users color
     *
     * @return true if the swatches are similar, false otherwise
     */
    private boolean analyseSwatches() {
        return colorsSimilarity(vibrantSwatch) || colorsSimilarity(lightVibrantSwatch) ||
                colorsSimilarity(darkVibrantSwatch) || colorsSimilarity(mutedSwatch) ||
                colorsSimilarity(lightMutedSwatch) || colorsSimilarity(darkMutedSwatch);
    }

    private boolean isValid(int num) {

        return num > 0 && num < 100;
    }

    /**
     * It checks if the users color is similar to another color.
     *
     * @param colorOther the other color
     * @return true if the colors are similar, false otherwise
     */
    private boolean colorsSimilarity(int colorOther) {

        int r = Math.abs(Color.red(usersColor) - Color.red(colorOther));
        int g = Math.abs(Color.green(usersColor) - Color.green(colorOther));
        int b = Math.abs(Color.blue(usersColor) - Color.blue(colorOther));

        return isValid(r) && isValid(g) && isValid(b);

    }

    /**
     * It gets the users color
     */
    private int getUsersColor(int colorType) {

        switch (colorType) {

            // black
            case 1:
                return Color.parseColor("#000000");

            // white
            case 2:
                return Color.parseColor("#ffffff");

            // red
            case 3:
                return Color.parseColor("#dc020e");

            // blue
            case 4:
                return Color.parseColor("#0226dc");

            // green
            case 5:
                return Color.parseColor("#15a415");

            // yellow
            case 6:
                Log.i("color", " users color " + String.valueOf(Color.parseColor("#ffea00")));
                return Color.parseColor("#ffea00");

            default:
                return 0;
        }
    }

    public boolean getColorSimilarity() {
        return colorSimilarity;
    }
}
