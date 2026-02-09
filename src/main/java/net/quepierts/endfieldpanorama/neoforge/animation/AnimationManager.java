package net.quepierts.endfieldpanorama.neoforge.animation;

import com.google.common.collect.MapMaker;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.quepierts.endfieldpanorama.neoforge.EndfieldPanorama;
import net.quepierts.endfieldpanorama.neoforge.animation.raw.RawAnimationSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;

@EventBusSubscriber(modid = EndfieldPanorama.MODID, value = Dist.CLIENT)
public final class AnimationManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final AnimationManager INSTANCE = new AnimationManager();

    private final Map<ResourceLocation, Holder> animations = new MapMaker().weakValues().concurrencyLevel(1).makeMap();

    private AnimationManager() {
        super(new Gson(), "animation_templates");
    }

    @Nullable
    public RawAnimationSet get(ResourceLocation location) {
        var holder = getHolder(location);
        return holder == null ? null : holder.get();
    }

    private Holder getHolder(ResourceLocation location) {
        return animations.computeIfAbsent(location, Holder::new);
    }

    @Override
    protected void apply(
            @NotNull Map<ResourceLocation, JsonElement> object,
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profiler
    ) {
        animations.values().forEach(Holder::unbind);

        for (var entry : object.entrySet()) {
            var location    = entry.getKey();
            var json        = entry.getValue();
            var holder      = getHolder(location);

            try {
                var animation = RawAnimationSet.fromJson(json.getAsJsonObject());

                holder.bind(animation);

            } catch (Exception e) {
                LOGGER.error("Failed to load animation {}", entry.getKey(), e);
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Holder {

        private final ResourceLocation location;
        private @Nullable RawAnimationSet animation;

        public @Nullable RawAnimationSet get() {
            return animation;
        }

        void bind(RawAnimationSet set) {
            this.animation = set;
        }

        void unbind() {
            this.animation = null;
        }

    }

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(INSTANCE);
    }
}
