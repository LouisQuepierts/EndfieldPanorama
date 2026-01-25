package net.quepierts.endfieldpanorama.animation.baked;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class BakedAnimationSet {

    private final BakedAnimation3f[] animations;
    private final String[] names;

    public BakedAnimation3f get(int location) {
        return animations[location];
    }

    public int getLocation(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

}
