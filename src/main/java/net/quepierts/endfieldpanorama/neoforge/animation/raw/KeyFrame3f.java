package net.quepierts.endfieldpanorama.neoforge.animation.raw;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public final class KeyFrame3f {

    private final float time;
    private final float x0, y0, z0;
    private final float x1, y1, z1;
    private final Interpolation interpolation;

    public KeyFrame3f(
            float time,
            float x0, float y0, float z0,
            Interpolation  interpolation
    ) {
        this.time           = time;
        this.x0             = x0;
        this.y0             = y0;
        this.z0             = z0;
        this.x1             = x0;
        this.y1             = y0;
        this.z1             = z0;
        this.interpolation  = interpolation;
    }

    public boolean isConstantSegment(KeyFrame3f next) {
        return this.x1 == next.x0 && this.y1 == next.y0 && this.z1 == next.z0;
    }

    public static KeyFrame3f fromJson(
                        float       time,
                        float       multiplier,
            @NotNull    JsonElement element
    ) {
        if (element.isJsonArray()) {

            var array      = parseFloatArray(element);
            return new KeyFrame3f(
                    time,
                    array[0] * multiplier,
                    array[1] * multiplier,
                    array[2] * multiplier,
                    Interpolation.LINEAR
            );

        } else {

            var jObject     = element.getAsJsonObject();

            if (!jObject.has("post")) {
                throw new IllegalArgumentException("KeyFrame3f must have post");
            }

            var post        = parseFloatArray(jObject.get("post"));
            var pre         = post;

            if (jObject.has("pre")) {
                pre         = parseFloatArray(jObject.get("pre"));
            }

            var interpolation = Interpolation.LINEAR;
            if (jObject.has("interpolation")) {
                interpolation = Interpolation.fromString(jObject.get("lerp_mode").getAsString());
            }

            return new KeyFrame3f(
                    time,
                    pre[0] * multiplier,
                    pre[1] * multiplier,
                    pre[2] * multiplier,
                    post[0] * multiplier,
                    post[1] * multiplier,
                    post[2] * multiplier,
                    interpolation
            );
        }
    }

    private static float[] parseFloatArray(JsonElement element) {
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("KeyFrame3f array must be JsonArray");
        }

        var jArray      = element.getAsJsonArray();

        if (jArray.size() != 3) {
            throw new IllegalArgumentException("KeyFrame3f array size must be 3");
        }

        return new float[] {
                jArray.get(0).getAsFloat(),
                jArray.get(1).getAsFloat(),
                jArray.get(2).getAsFloat()
        };
    }

    public enum Interpolation {
        LINEAR,
        CATMULL_ROM;

        public static Interpolation fromString(String name) {
            if (name.equals("catmullrom")) {
                return CATMULL_ROM;
            }
            return LINEAR;
        }
    }
}
