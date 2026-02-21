package net.quepierts.endfieldpanorama.earlywindow.animation;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.quepierts.endfieldpanorama.earlywindow.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class Animation3f {

    private final Timeline3f[]  timelines;
    private final String[]      channels;
    private final int[]         channelIds;

    @Getter
    private final boolean       loop;
    @Getter
    private final float         duration;

    public Animation3f(
            @NotNull    Timeline3f[]    timelines,
            @NotNull    String[]        channels,
                        boolean         loop,
                        float           duration
    ) {
        this.timelines = timelines;
        this.channels = channels;
        this.channelIds = new int[timelines.length];
        this.loop = loop;
        this.duration = duration;
    }

    public void link(@NotNull AnimationProgram program) {
        for (int i = 0; i < channels.length; i++) {
            channelIds[i] = program.getChannelId(channels[i]);
        }
    }

    public void eval(
            @NotNull    AnimationStateView  state,
                        float               time
    ) {
        var localTime   = loop ?
                            time % duration :
                            time;

        for (int i = 0; i < timelines.length; i++) {
            if (channelIds[i] == -1) {
                continue;
            }
            timelines[i].eval(state, localTime, channelIds[i]);
        }
    }

    @RequiredArgsConstructor
    public static final class Definition {

        private final Timeline3f.Definition[] timelines;
        private final boolean loop;
        private final float length;

        public static Definition fromJson(@NotNull JsonObject root) {

            var loop        = root.has("loop") && root.get("loop").getAsBoolean();
            var length      = root.get("animation_length").getAsFloat();

            var bones       = root.getAsJsonObject("bones");
            var out         = new ArrayList<Timeline3f.Definition>();

            for (var entry : bones.entrySet()) {
                var name        = entry.getKey();
                var content     = entry.getValue();

                _bone(name, content.getAsJsonObject(), out);
            }

            var array       = out.toArray(Timeline3f.Definition[]::new);

            return new Definition(
                    array,
                    loop,
                    length
            );
        }

        private static void _bone(
                @NotNull String boneName,
                @NotNull JsonObject bone,
                @NotNull List<Timeline3f.Definition> out
        ) {

            for (var entry : bone.entrySet()) {
                var channel     = entry.getKey();
                var content     = entry.getValue();

                var path        = boneName + "." + channel;
                var multiplier  = 1.0f;
                var timeline    = Timeline3f.Definition.fromJson(path, content.getAsJsonObject(), multiplier);

                out.add(timeline);
            }
        }

        public @NotNull Animation3f bake() {

            var size        = this.timelines.length;
            var timelines   = new Timeline3f[size];
            var channels    = new String[size];

            var definitions = this.timelines;
            for (int i = 0; i < definitions.length; i++) {
                var timeline    = definitions[i];

                timelines[i]    = timeline.bake();
                channels[i]     = timeline.getChannel();
            }

            return new Animation3f(timelines, channels, loop, length);
        }

    }
}
