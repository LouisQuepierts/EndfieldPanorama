package net.quepierts.endfieldpanorama.animation.raw;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.quepierts.endfieldpanorama.animation.AnimatablePlayerModel;
import net.quepierts.endfieldpanorama.animation.baked.BakedAnimation3f;
import net.quepierts.endfieldpanorama.animation.baked.BakedAnimationSet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@RequiredArgsConstructor
public final class RawAnimationSet {

    private final Map<String, RawAnimation3f> animations;
    private final String version;

    public static RawAnimationSet fromJson(@NotNull JsonObject root) {
        var version     = root.get("format_version").getAsString();

        var animations  = root.getAsJsonObject("animations");
        var map         = ImmutableMap.<String, RawAnimation3f>builder();

        for (var entry : animations.entrySet()) {
            var name        = entry.getKey();
            var content     = entry.getValue();

            try {
                var animation = RawAnimation3f.fromJson(content.getAsJsonObject());
                map.put(name, animation);
            } catch (Exception e) {
                // cannot load this animation
                System.out.println("Cannot load animation " + name);
                continue;
            }

        }

        return new RawAnimationSet(map.build(), version);
    }

    public BakedAnimationSet bake(AnimatablePlayerModel model) {
        int size            = animations.size();
        int index           = 0;

        var animations      = new BakedAnimation3f[size];
        var names           = new String[size];

        for (var entry : this.animations.entrySet()) {
            var name        = entry.getKey();
            var animation   = entry.getValue();

            var baked       = animation.bake(model);

            animations[index] = baked;
            names[index] = name;
            index++;
        }
        return new BakedAnimationSet(animations, names);
    }

}
