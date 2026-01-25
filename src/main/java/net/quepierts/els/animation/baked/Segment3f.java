package net.quepierts.els.animation.baked;

import lombok.Getter;
import net.minecraft.util.Mth;
import net.quepierts.els.animation.Consumer3f;

public abstract class Segment3f {

    @Getter private final float start;
    @Getter private final float end;

    private final float length;
    private final float invLength;

    public static Segment3f linear(
            float start, float end,
            float[] x,
            float[] y,
            float[] z
    ) {
        return new Linear(start, end, x, y, z);
    }

    public static Segment3f constant(
            float start, float end,
            float[] x,
            float[] y,
            float[] z
    ) {
        return new Constant(start, end, x, y, z);
    }

    public static Segment3f catmullRom(
            float start, float end,
            float[] x,
            float[] y,
            float[] z
    ) {
        return new CatmullRom(start, end, x, y, z);
    }

    protected Segment3f(float start, float end) {
        this.start = start;
        this.end = end;
        this.length = end - start;
        this.invLength = 1.0f / length;
    }

    public final void eval(float time, Consumer3f consumer) {
        this._eval(time * this.invLength, consumer);
    }

    protected abstract void _eval(float localTime, Consumer3f consumer);

    public static class Constant extends Segment3f {

        private final float x, y, z;

        public Constant(
                float start, float end,
                float[] x,
                float[] y,
                float[] z
        ) {
            super(start, end);
            this.x = x[0];
            this.y = y[0];
            this.z = z[0];
        }

        @Override
        protected void _eval(float localTime, Consumer3f consumer) {
            consumer.accept(x, y, z);
        }
    }

    public static class Linear extends Segment3f {

        private final float x0, y0, z0;
        private final float x1, y1, z1;

        public Linear(
                float start, float end,
                float[] x,
                float[] y,
                float[] z
        ) {
            super(start, end);
            this.x0 = x[0];
            this.y0 = y[0];
            this.z0 = z[0];
            this.x1 = x[1];
            this.y1 = y[1];
            this.z1 = z[1];
        }

        @Override
        public void _eval(float localTime, Consumer3f consumer) {
            consumer.accept(
                    Mth.lerp(localTime, x0, x1),
                    Mth.lerp(localTime, y0, y1),
                    Mth.lerp(localTime, z0, z1)
            );
        }
    }

    public static class CatmullRom extends Segment3f {

        private final float x0, y0, z0;
        private final float x1, y1, z1;
        private final float x2, y2, z2;
        private final float x3, y3, z3;

        public CatmullRom(
                float start, float end,
                float[] x,
                float[] y,
                float[] z
        ) {
            super(start, end);
            this.x0 = x[0];
            this.y0 = y[0];
            this.z0 = z[0];
            this.x1 = x[1];
            this.y1 = y[1];
            this.z1 = z[1];
            this.x2 = x[2];
            this.y2 = y[2];
            this.z2 = z[2];
            this.x3 = x[3];
            this.y3 = y[3];
            this.z3 = z[3];
        }

        @Override
        protected void _eval(float localTime, Consumer3f consumer) {
            consumer.accept(
                    Mth.catmullrom(localTime, x0, x1, x2, x3),
                    Mth.catmullrom(localTime, y0, y1, y2, y3),
                    Mth.catmullrom(localTime, z0, z1, z2, z3)
            );
        }
    }
}
