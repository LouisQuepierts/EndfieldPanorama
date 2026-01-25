package net.quepierts.els.animation.raw;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
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

}
