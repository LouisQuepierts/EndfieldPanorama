package net.quepierts.els.animation.raw;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.quepierts.els.animation.AnimatablePlayerModel;
import net.quepierts.els.animation.baked.BakedAnimation3f;
import net.quepierts.els.animation.baked.BakedTimeline3f;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class RawAnimation3f {

    private final RawTimeline3f[] timelines;
    private final boolean loop;
    private final float length;

    public static RawAnimation3f fromJson(@NotNull JsonObject root) {

        var loop        = root.has("loop") && root.get("loop").getAsBoolean();
        var length      = root.get("animation_length").getAsFloat();

        var bones       = root.getAsJsonObject("bones");
        var out         = new ArrayList<RawTimeline3f>();

        for (var entry : bones.entrySet()) {
            var name        = entry.getKey();
            var content     = entry.getValue();

            _bone(name, content.getAsJsonObject(), out);
        }

        var array       = out.toArray(RawTimeline3f[]::new);

        return new RawAnimation3f(
                array,
                loop,
                length
        );
    }

    private static void _bone(
            @NotNull String boneName,
            @NotNull JsonObject bone,
            @NotNull List<RawTimeline3f> out
    ) {

        for (var entry : bone.entrySet()) {
            var channel     = entry.getKey();
            var content     = entry.getValue();

            var path        = boneName + "." + channel;
            var timeline    = RawTimeline3f.fromJson(path, content.getAsJsonObject());

            out.add(timeline);
        }
    }

    public @NotNull BakedAnimation3f bake(@NotNull AnimatablePlayerModel model) {

        var targets         = model.getTargets();
        var out             = new ArrayList<BakedTimeline3f>();

        for (var timeline : this.timelines) {
            var channel     = timeline.getChannel();

            if (!targets.containsKey(channel)) {
                continue;
            }

            var consumer    = targets.get(channel);
            var baked       = timeline.bake(consumer);
            out.add(baked);
        }

        var array           = out.toArray(BakedTimeline3f[]::new);
        return new BakedAnimation3f(array);
    }

}
