package net.quepierts.endfieldpanorama.neoforge.animation;

import com.mojang.logging.LogUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import net.quepierts.endfieldpanorama.neoforge.animation.baked.BakedAnimationSet;
import net.quepierts.endfieldpanorama.neoforge.animation.raw.RawAnimationSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnimationHolder {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String PATH = "animationsets/";

    @Getter
    private final @NotNull ResourceLocation location;

    @Getter
    private @Nullable RawAnimationSet animation;

    public static AnimationHolder of(@NotNull ResourceLocation location) {
        var loc = ResourceLocation.fromNamespaceAndPath(
                location.getNamespace(),
                PATH + location.getPath() + ".json"
        );
        return new AnimationHolder(loc);
    }

    public void load(@NotNull ResourceProvider provider) {
        this.animation = null;

        try (var reader = provider.openAsReader(this.location)) {

            var jsonobject = GsonHelper.parse(reader);
            this.animation = RawAnimationSet.fromJson(jsonobject);

        } catch (IOException e) {
            LOGGER.error("Failed to load animation template: {}", this.location);
        }
    }

    public BakedAnimationSet bake(final AnimatablePlayerModel model) {
        if (animation == null) {
            throw new NullPointerException("AnimationSet is null");
        }
        return animation.bake(model);
    }

}
