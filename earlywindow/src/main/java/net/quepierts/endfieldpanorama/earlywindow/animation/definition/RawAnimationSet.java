package net.quepierts.endfieldpanorama.earlywindow.animation.definition;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import lombok.RequiredArgsConstructor;
import net.quepierts.endfieldpanorama.earlywindow.EarlyResourceLoader;
import net.quepierts.endfieldpanorama.earlywindow.animation.Animation3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.util.Map;

@RequiredArgsConstructor
public final class RawAnimationSet {

    private static final Gson GSON = new Gson();

    private final Map<String, Animation3f.Definition> animations;
    private final String version;

    public static RawAnimationSet fromSource(@NotNull String url) {
        try (var source = EarlyResourceLoader.loadResource(url)) {
            var reader = new JsonReader(new InputStreamReader(source));
            return fromJson(GSON.fromJson(reader, JsonObject.class));
        } catch (Exception e) {
            // cannot load, throw
            throw new RuntimeException(e);
        }
    }

    public static RawAnimationSet fromJson(@NotNull JsonObject root) {
        var version     = root.get("format_version").getAsString();

        var animations  = root.getAsJsonObject("animations");
        var map         = ImmutableMap.<String, Animation3f.Definition>builder();

        for (var entry : animations.entrySet()) {
            var name        = entry.getKey();
            var content     = entry.getValue();

            try {
                var animation = Animation3f.Definition.fromJson(content.getAsJsonObject());
                map.put(name, animation);
            } catch (Exception e) {
                // cannot load this animation
                System.out.println("Cannot load animation " + name);
                continue;
            }

        }

        return new RawAnimationSet(map.build(), version);
    }

    public @Nullable Animation3f.Definition getAnimation(String name) {
        return animations.get(name);
    }

    public @NotNull Animation3f.Definition getOrThrow(String name) {
        var animation = getAnimation(name);
        if (animation == null) {
            throw new NullPointerException("Animation " + name + " not found");
        }
        return animation;
    }


}
