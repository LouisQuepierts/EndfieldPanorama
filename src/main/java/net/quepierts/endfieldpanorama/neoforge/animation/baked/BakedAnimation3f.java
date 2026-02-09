package net.quepierts.endfieldpanorama.neoforge.animation.baked;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.quepierts.endfieldpanorama.neoforge.animation.AnimationState;

import java.util.Arrays;

@RequiredArgsConstructor
public final class BakedAnimation3f {

    private final BakedTimeline3f[] timelines;
    @Getter private final boolean loop;
    @Getter private final float duration;

    public void eval(AnimationState state, float partialTick) {

        var timer       = state.getTimer() + partialTick * 0.05f;

        if (this.loop && timer >= this.duration) {
            var extra    = timer % duration;
            Arrays.fill(state.getCursors(), 0);
            timer = extra;
        }

        state.setTimer(timer);

        for (int i = 0; i < timelines.length; i++) {
            timelines[i].eval(timer, state.getCursors(), i);
        }
    }

    public int[] newCursors() {
        return new int[timelines.length];
    }
}
