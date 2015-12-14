package com.example.mediastock.util;

import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;


/**
 * Class to determine if a color is similar to another color.
 */
public class ColorHelper {
    // black, white, red, blue, green, yellow, orange, magenta, cyan
    private final static String[] colorsID = {"#000000", "#ffffff", "#dc020e", "#0226dc", "#15a415", "#ffea00", "#ff8800", "#ff00ff", "#00ffff"};
    private int usersColor = 0;

    public ColorHelper() {
    }

    /**
     * It determines if the swatches are similar to the users color
     *
     * @return true if the swatches are similar, false otherwise
     */
    private boolean analyseSwatches(int vibrantSwatch, int darkVibrantSwatch, int lightVibrantSwatch,
                                    int mutedSwatch, int darkMutedSwatch, int lightMutedSwatch) {

        boolean vibrant, darkVibrant, lightVibrant, muted, darkMuted, lightMuted;
        vibrant = darkVibrant = lightVibrant = muted = darkMuted = lightMuted = false;

        if (vibrantSwatch != 0)
            vibrant = colorsSimilarity(vibrantSwatch);

        if (darkVibrantSwatch != 0)
            darkVibrant = colorsSimilarity(darkVibrantSwatch);

        if (lightVibrantSwatch != 0)
            lightVibrant = colorsSimilarity(lightVibrantSwatch);

        if (mutedSwatch != 0)
            muted = colorsSimilarity(mutedSwatch);

        if (lightMutedSwatch != 0)
            lightMuted = colorsSimilarity(lightMutedSwatch);

        if (darkMutedSwatch != 0)
            darkMuted = colorsSimilarity(darkMutedSwatch);

        Log.i("img", "values: " + String.valueOf(vibrant) + "  " + String.valueOf(lightVibrant) + " " + String.valueOf(darkVibrant) +
                " " + String.valueOf(muted) + " " + String.valueOf(darkMuted) + " " + String.valueOf(lightMuted));

        return vibrant || lightVibrant || darkVibrant || muted || lightMuted || darkMuted;
    }


    /**
     * It checks if the users color is similar to another color, following the Euclidean space algorithm.
     *
     * @param colorOther the other color
     * @return true if the colors are similar, false otherwise
     */
    private boolean colorsSimilarity(int colorOther) {
        int r = Math.abs(Color.red(colorOther) - Color.red(usersColor));
        int g = Math.abs(Color.green(colorOther) - Color.green(usersColor));
        int b = Math.abs(Color.blue(colorOther) - Color.blue(usersColor));

        int distance = (r * r) + (g * g) + (b * b);

        return distance < 14000;

    }

    /**
     * It checks if the users color is similar to another color, following the Euclidean space algorithm.
     *
     * @param colorOther  the other color
     * @param targetColor the target color
     * @return true if the colors are similar, false otherwise
     */
    private boolean colorsSimilarityBis(int targetColor, int colorOther) {
        int r = Math.abs(Color.red(colorOther) - Color.red(targetColor));
        int g = Math.abs(Color.green(colorOther) - Color.green(targetColor));
        int b = Math.abs(Color.blue(colorOther) - Color.blue(targetColor));

        int distance = (r * r) + (g * g) + (b * b);

        return distance < 14000;

    }

    /**
     * It gets the users color from the array of colors
     */
    private int getUsersColor(int selectedColorPosition) {

        return Color.parseColor(colorsID[selectedColorPosition]);
    }

    public void setTargetColor(int color) {
        final SparseArray<Boolean> list = new SparseArray<>();
        int finalColor = 0;

        for (int i = 0; i < colorsID.length; i++)
            list.put(i, colorsSimilarityBis(color, Color.parseColor(colorsID[i])));

        for (int i = 0; i < list.size(); i++)
            if (list.get(i))
                finalColor = Color.parseColor(colorsID[i]);

        this.usersColor = finalColor;
    }

    public void setTargetColorFromArray(int position) {
        this.usersColor = getUsersColor(position);
    }

    /**
     * It determines if the swatches are similar to the users color
     *
     * @return true if the swatches are similar to the users color, false otherwise
     */
    public boolean getColorSimilarity(int vibrantSwatch, int darkVibrantSwatch, int lightVibrantSwatch,
                                      int mutedSwatch, int darkMutedSwatch, int lightMutedSwatch) {

        return analyseSwatches(vibrantSwatch, darkVibrantSwatch, lightVibrantSwatch, mutedSwatch, darkMutedSwatch, lightMutedSwatch);
    }

}
