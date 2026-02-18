package net.quepierts.endfieldpanorama.earlywindow.animation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public final class AnimationState implements AnimationStateView {
    final int[]         cursors;
    final Consumer3f[]  consumers;

    @Setter
    @Nullable WritableTarget target;

    @Getter
    int location = -1;

    @Getter
    @Setter
    float timer;

    public boolean isPlaying() {
        return location != -1;
    }

    @Override
    public int getCursor(int cid) {
        return cursors[cid];
    }

    @Override
    public void setCursor(int cid, int cursor) {
        cursors[cid] = cursor;
    }

    @Override
    public Consumer3f getConsumer(int cid) {
        return target == null ? consumers[cid] : target.getConsumer(cid);
    }
}
