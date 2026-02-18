package net.quepierts.endfieldpanorama.earlywindow.scene;

import net.quepierts.endfieldpanorama.earlywindow.animation.*;
import net.quepierts.endfieldpanorama.earlywindow.animation.definition.RawAnimationSet;
import net.quepierts.endfieldpanorama.earlywindow.render.model.PlayerModel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;

public final class SceneAnimation {

    public static final int LOCATION_BEGIN          = 0;
    public static final int LOCATION_TRANSITION     = 1;
    public static final int LOCATION_LOOP           = 2;

    private final AnimationProgram  program;
    private final AnimationState    state;

    private Phrase phrase = Phrase.BEGIN;

    private float currentTime = 0f;
    private float transitionDuration = 0.1f;
    private float transitionElapsed = 0f;

    private boolean signalReceived = false;

    public SceneAnimation(
            @NotNull RawAnimationSet    set,
            @NotNull PlayerModel        model,
            @NotNull Camera             camera
    ) {
        var begin       = set.getOrThrow("begin");
        var transition  = set.getOrThrow("transition");
        var loop        = set.getOrThrow("loop");

        var animations  = new Animation3f[] {
                begin.bake(),
                transition.bake(),
                loop.bake(),
        };

        var channels    = new ArrayList<String>();
        var outputs     = new ArrayList<Consumer3f>();
        var clear       = new FrameBuffer(2 + 2 + model.getSkeleton().size() * 3);

        // root
        channels.add("root.position");
        channels.add("root.rotation");

        var root = model.getTransform();
        outputs.add(root::setPosition);
        outputs.add(root::setRotation);

        // camera
        channels.add("camera.position");
        channels.add("camera.rotation");

        outputs.add(camera::setPosition);
        outputs.add(camera::setRotation);

        var cursor      = 6;

        for (var bone : model.getSkeleton()) {
            var name    = bone.getName();
            channels.add(name + ".position");
            channels.add(name + ".rotation");
            channels.add(name + ".scale");

            var transform = bone.getTransform();
            outputs.add(transform::setPosition);
            outputs.add(transform::setRotation);
            outputs.add(transform::setScale);

            clear.getConsumer(cursor).accept(1.0f, 1.0f, 1.0f); // set scale
            cursor += 3;
        }

        this.program    = new AnimationProgram(
                animations,
                channels.toArray(String[]::new),
                clear,
                0
        );

        this.state = new AnimationState(
                new int[channels.size()],
                outputs.toArray(Consumer3f[]::new)
        );
    }

    public void cloneState(SceneAnimation other) {
        this.currentTime        = other.currentTime;
        this.transitionDuration = other.transitionDuration;
        this.transitionElapsed  = other.transitionElapsed;
        this.phrase             = other.phrase;
    }

    public void trigger() {
        signalReceived = true;
    }

    public void update(float delta) {
        program.clear(AnimationProgram.OUTPUT_BUFFER);

        currentTime += delta;
        state.setTimer(currentTime);

        program.sample(
                state,
                LOCATION_BEGIN,
                AnimationProgram.OUTPUT_BUFFER,
                1.0f
        );

        /*switch (phrase) {
            case BEGIN:
                program.sample(
                        state,
                        LOCATION_BEGIN,
                        AnimationProgram.OUTPUT_BUFFER,
                        1f
                );

                if (signalReceived) {
                    phrase = Phrase.TRANSITION;
                    transitionElapsed = 0f;
                }
                break;

            case TRANSITION:
                transitionElapsed   += delta;
                float t             = Math.min(transitionElapsed / transitionDuration, 1f);

                program.sample(
                        state,
                        LOCATION_BEGIN,
                        AnimationProgram.OUTPUT_BUFFER,
                        1.0f
                );

                state.setTimer(transitionElapsed);
                program.sample(
                        state,
                        LOCATION_TRANSITION,
                        AnimationProgram.OUTPUT_BUFFER,
                        t
                );

                if (t >= 1.0f) {
                    phrase = Phrase.LOOP;
                    currentTime     = 0.0f;
                }
                break;

            case LOOP:
                program.sample(
                        state,
                        LOCATION_LOOP,
                        AnimationProgram.OUTPUT_BUFFER,
                        1.0f
                );
                break;
        }*/

        program.flush(state);
    }


    private enum Phrase {
        BEGIN,
        TRANSITION,
        LOOP
    }
}
