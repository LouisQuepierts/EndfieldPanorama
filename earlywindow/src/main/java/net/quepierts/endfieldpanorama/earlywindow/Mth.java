package net.quepierts.endfieldpanorama.earlywindow;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Mth {

    public static final float PI = (float) Math.PI;
    public static final float HALF_PI = PI / 2;

    public static final float DEG_TO_RAD = (float) (Math.PI / 180);
    public static final float RAD_TO_DEG = (float) (180 / Math.PI);

    public static int clamp(int num, int min, int max) {
        return Math.min(Math.max(num, min), max);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static float catmullrom(float delta, float start, float left, float right, float end) {
        return lerp(delta, lerp(delta, start, left), lerp(delta, right, end));
    }

}
