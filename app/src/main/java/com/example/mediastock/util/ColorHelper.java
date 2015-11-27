package com.example.mediastock.util;


import android.graphics.Color;

/**
 * Class to determine if a color is similar to another color.
 */
public class ColorHelper {
    // black, white, red, blue, green, yellow, orange, magenta, grey, cyan
    private final static String[] colorsID = {"#000000", "#ffffff", "#dc020e", "#0226dc", "#15a415", "#ffea00", "#ff8800", "#ff00ff", "#888888", "#00ffff"};
    private final boolean colorSimilarity;
    private final int usersColor, darkVibrantSwatch, lightVibrantSwatch, vibrantSwatch, mutedSwatch, darkMutedSwatch, lightMutedSwatch;


    public ColorHelper(int selectedColorPosition, int vibrantSwatch, int darkVibrantSwatch, int lightVibrantSwatch,
                       int mutedSwatch, int darkMutedSwatch, int lightMutedSwatch) {

        this.vibrantSwatch = vibrantSwatch;
        this.lightVibrantSwatch = lightVibrantSwatch;
        this.darkVibrantSwatch = darkVibrantSwatch;
        this.mutedSwatch = mutedSwatch;
        this.darkMutedSwatch = darkMutedSwatch;
        this.lightMutedSwatch = lightMutedSwatch;

        // the color to search for
        this.usersColor = getUsersColor(selectedColorPosition);

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
    private int getUsersColor(int selectedColorPosition) {

        return Color.parseColor(colorsID[selectedColorPosition]);
    }

    public boolean getColorSimilarity() {
        return colorSimilarity;
    }
}
