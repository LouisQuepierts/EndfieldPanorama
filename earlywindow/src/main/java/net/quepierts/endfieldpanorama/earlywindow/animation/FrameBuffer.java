package net.quepierts.endfieldpanorama.earlywindow.animation;

import lombok.Setter;
import net.quepierts.endfieldpanorama.earlywindow.Mth;
import org.jetbrains.annotations.NotNull;

public final class FrameBuffer implements WritableTarget, Consumer3f {

    private final   float[] buffer;
    private final   int     size;

    @Setter
    private         int     cursor;

    @Setter
    private         float   weight  = 1.0f;

    public FrameBuffer(int size) {
        this.buffer = new float[size * 3];
        this.size   = size;
    }

    public void blit(@NotNull WritableTarget consumers) {
        for (int i = 0, ptr = 0; i < size; i++, ptr += 3) {
            var consumer    = consumers.getConsumer(i);
            consumer.accept(buffer[ptr], buffer[ptr + 1], buffer[ptr + 2]);
        }
    }

    @Override
    public Consumer3f getConsumer(int cid) {
        cursor = cid * 3;
        return this;
    }

    @Override
    public void accept(float x, float y, float z) {

        if (weight >= 1f) {
            buffer[cursor] = x;
            buffer[cursor + 1] = y;
            buffer[cursor + 2] = z;
        } else if (weight > 0f) {
            buffer[cursor] = Mth.lerp(weight, buffer[cursor], x);
            buffer[cursor + 1] = Mth.lerp(weight, buffer[cursor + 1], y);
            buffer[cursor + 2] = Mth.lerp(weight, buffer[cursor + 2], z);
        }

    }
}
