package net.quepierts.endfieldpanorama.animation.raw;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.Mth;
import net.quepierts.endfieldpanorama.animation.Consumer3f;
import net.quepierts.endfieldpanorama.animation.baked.BakedTimeline3f;
import net.quepierts.endfieldpanorama.animation.baked.Segment3f;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class RawTimeline3f {

    @Getter private final String channel;
    private final KeyFrame3f[] keyFrames;

    public static RawTimeline3f fromJson(
            @NotNull String channel,
            @NotNull JsonObject root,
            float multiplier
    ) {

        var entrySet        = root.entrySet();
        var size            = entrySet.size();

        var keyFrames       = new KeyFrame3f[size];
        var i               = 0;

        for (var entry : entrySet) {
            var strTime         = entry.getKey();
            var time            = Float.parseFloat(strTime);

            var content         = entry.getValue();
            var keyFrame        = KeyFrame3f.fromJson(time, multiplier, content);

            keyFrames[i++] = keyFrame;
        }

        return new RawTimeline3f(
                channel,
                keyFrames
        );
    }

    public BakedTimeline3f bake(Consumer3f consumer) {

        int n               = keyFrames.length;
        var segments        = new Segment3f[n - 1];

        var x               = new float[4];
        var y               = new float[4];
        var z               = new float[4];

        for (int i = 0; i < segments.length; i++) {
            var k0          = keyFrames[i];
            var k1          = keyFrames[i + 1];

            var smooth      = k0.getInterpolation() == KeyFrame3f.Interpolation.CATMULL_ROM
                                || k1.getInterpolation() == KeyFrame3f.Interpolation.CATMULL_ROM;

            if (smooth) {
                var i0      = Mth.clamp(i - 1, 0, n - 1);
                var i3      = Mth.clamp(i + 2, 0, n - 1);

                var km1     = keyFrames[i0];
                var kp2     = keyFrames[i3];

                x[0]        = km1.getX1();
                x[1]        = k0.getX1();
                x[2]        = k1.getX0();
                x[3]        = kp2.getX0();

                y[0]        = km1.getY1();
                y[1]        = k0.getY1();
                y[2]        = k1.getY0();
                y[3]        = kp2.getY0();

                z[0]        = km1.getZ1();
                z[1]        = k0.getZ1();
                z[2]        = k1.getZ0();
                z[3]        = kp2.getZ0();

                segments[i] = Segment3f.catmullRom(
                        k0.getTime(),
                        k1.getTime(),
                        x, y, z
                );



            } else {

                x[0]        = k0.getX1();
                y[0]        = k0.getY1();
                z[0]        = k0.getZ1();

                if (k0.isConstantSegment(k1)) {
                    segments[i] = Segment3f.constant(
                            k0.getTime(),
                            k1.getTime(),
                            x, y, z
                    );
                } else {

                    x[1]    = k1.getX0();
                    y[1]    = k1.getY0();
                    z[1]    = k1.getZ0();

                    segments[i] = Segment3f.linear(
                            k0.getTime(),
                            k1.getTime(),
                            x, y, z
                    );
                }

            }
        }


        return new BakedTimeline3f(consumer, segments);
    }

}
