package net.quepierts.endfieldpanorama.neoforge.animation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public final class AnimationState {
    int[] cursors;

    int location = -1;
    float timer;

    public boolean isPlaying() {
        return location != -1;
    }
}
