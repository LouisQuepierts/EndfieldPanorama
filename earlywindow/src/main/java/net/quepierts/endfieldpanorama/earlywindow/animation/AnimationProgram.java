package net.quepierts.endfieldpanorama.earlywindow.animation;

import org.jetbrains.annotations.NotNull;

public final class AnimationProgram {

    public static final int     OUTPUT_BUFFER = 0;

    private final FrameBuffer   clear;
    private final FrameBuffer[] buffers;

    private final Animation3f[] animations;
    private final String[]      channels;

    public AnimationProgram(
            Animation3f[]   animations,
            String[]        channels,
            FrameBuffer     clear,
            int             extraBufferAmount
    ) {
        this.clear = clear;
        this.buffers = new FrameBuffer[extraBufferAmount + 1];
        this.animations = animations;
        this.channels = channels;

        for (int i = 0; i < this.buffers.length; i++) {
            this.buffers[i] = new FrameBuffer(channels.length);
        }

        for (Animation3f animation : animations) {
            animation.link(this);
        }
    }

    public void sample(@NotNull AnimationState state, int sampler, int dest, float weight) {
        var buffer      = buffers[dest];
        var animation   = animations[sampler];

        state.setTarget(buffer);
        buffer.setWeight(weight);
        animation.eval(state, state.getTimer());
    }

    public void assign(int src, int dest) {
        buffers[dest].setWeight(1.0f);
        buffers[src].blit(buffers[dest]);
    }

    public void flush(@NotNull AnimationState state) {
        state.setTarget(null);
        buffers[OUTPUT_BUFFER].blit(state);
    }

    public void clear(int location) {
        var buffer = buffers[location];
        buffer.setWeight(1.0f);
        clear.blit(buffer);
    }

    public int getChannelId(String name) {

        for (int i = 0; i < channels.length; i++) {
            if (channels[i].equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public Animation3f getAnimation(int location) {
        return animations[location];
    }
}
