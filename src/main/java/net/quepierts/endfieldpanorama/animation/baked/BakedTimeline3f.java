package net.quepierts.endfieldpanorama.animation.baked;

import net.minecraft.util.Mth;
import net.quepierts.endfieldpanorama.animation.Consumer3f;

public final class BakedTimeline3f {

    private final Consumer3f consumer;
    private final Segment3f[] segments;

    private final int max;

    public BakedTimeline3f(Consumer3f consumer, Segment3f[] segments) {
        this.consumer = consumer;
        this.segments = segments;

        this.max = segments.length - 1;
    }

    public void eval(float time, int[] cursor, int cid) {
        var cursorValue = cursor[cid];
        var segment = getSegment(cursorValue);

        if (segment.getEnd() <= time) {
            var newCursor = cursorValue + 1;
            segment = getSegment(newCursor);
            cursor[cid] = newCursor;
            if (segment.getEnd() <= time) {
                segment = getSegment(time, cursor, cid);
            }
        }

        segment.eval(time, consumer);
    }

    private Segment3f getSegment(float time, int[] cursor, int cid) {
        // search
        for (int i = 0; i < max; i++) {
            var segment = segments[i];
            if (segment.getStart() <= time && segment.getEnd() > time) {
                cursor[cid] = i;
                return segment;
            }
        }

        cursor[cid] = max;
        return segments[max];
    }


    private Segment3f getSegment(int cursor) {
        return segments[Mth.clamp(cursor, 0, max)];
    }
}
