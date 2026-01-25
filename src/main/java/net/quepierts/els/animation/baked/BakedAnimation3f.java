package net.quepierts.els.animation.baked;

public final class BakedAnimation3f {

    private final BakedTimeline3f[] timelines;

    public BakedAnimation3f(BakedTimeline3f[] timelines) {
        this.timelines = timelines;
    }

    public void eval(float time, int[] cursor) {
        for (int i = 0; i < timelines.length; i++) {
            timelines[i].eval(time, cursor, i);
        }
    }

    public int[] newCursors() {
        return new int[timelines.length];
    }
}
