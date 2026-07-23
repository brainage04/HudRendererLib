package io.github.brainage04.hudrendererlib.util;

public class MathUtils {
    // from https://stackoverflow.com/a/9354899
    public static byte getBit(int number, int position) {
        return (byte) ((number >> position) & 1);
    }

    public static boolean isBitOn(int number, int position) {
        return getBit(number, position) == 1;
    }
}