package net.quepierts.endfieldpanorama.animation;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.endfieldpanorama.animation.baked.BakedAnimationSet;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnimatablePlayerModel {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final PlayerModel<?> wide;
    private final PlayerModel<?> slim;

    @Getter
    private final Map<String, Consumer3f> targets;

    @Getter
    private final Vector3f rootPosition = new Vector3f();
    @Getter
    private final Vector3f rootRotation = new Vector3f();

    @Getter
    private final Vector3f deltaPosition = new Vector3f();
    @Getter
    private final Vector3f deltaRotation = new Vector3f();

    @Getter
    private final Vector3f cameraPosition = new Vector3f();
    @Getter
    private final Vector3f cameraRotation = new Vector3f();

    @Getter
    @Setter
    private boolean useSlim = false;

    private BakedAnimationSet animations;

    public AnimatablePlayerModel(
            PlayerModel<?>  wide,
            PlayerModel<?>  slim
    ) {
        this.wide       = wide;
        this.slim       = slim;

        var builder     = ImmutableMap.<String, Consumer3f>builder();

        builder.put("root.position", this::setRootPosition);
        builder.put("root.rotation", this::setRootRotation);

        builder.put("camera.position", this.cameraPosition::set);
        builder.put("camera.rotation", this.cameraRotation::set);

        register("head", model -> model.head, builder);
        register("body", model -> model.body, builder);
        register("right_arm", model -> model.rightArm, builder);
        register("left_arm", model -> model.leftArm, builder);
        register("right_leg", model -> model.rightLeg, builder);
        register("left_leg", model -> model.leftLeg, builder);

        this.targets    = builder.build();
    }

    public static AnimatablePlayerModel bake(EntityModelSet set) {
        var wide = new PlayerModel<>(set.bakeLayer(ModelLayers.PLAYER), false);
        var slim = new PlayerModel<>(set.bakeLayer(ModelLayers.PLAYER_SLIM), true);

        wide.young = false;
        slim.young = false;

        return new AnimatablePlayerModel(wide, slim);
    }

    public void bind(ResourceLocation location) {
        var raw         = AnimationManager.INSTANCE.get(location);

        if (raw == null) {
            LOGGER.warn("Animation Set {} not found", location);
            return;
        }

        this.animations = raw.bake(this);
    }

    public boolean play(
            @NotNull String name,
            @NotNull AnimationState state
    ) {
        var location    = this.animations.getLocation(name);

        if (location == -1) {
            LOGGER.warn("Animation {} not found", name);
            return false;
        }

        var animation   = this.animations.get(location);

        state.cursors   = animation.newCursors();
        state.location  = location;
        state.timer     = 0;

        return true;
    }

    public PlayerModel<?> resolve(
            @NotNull    PlayerSkin         skin,
            @NotNull    AnimationState     state,
                        float               partialTick
    ) {
        this.useSlim    = skin.model() == PlayerSkin.Model.SLIM;
        var model       = this.useSlim ? this.slim : this.wide;

        if (state.isPlaying()) {
            var animation   = this.animations.get(state.location);
            animation.eval(state, partialTick);
        }

        model.hat.copyFrom(model.head);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);

        return model;
    }

    private void register(
            String                                      name,
            Function<PlayerModel<?>, ModelPart>         getter,
            ImmutableMap.Builder<String, Consumer3f>    builder
    ) {
        var target = new AnimatableTarget(this, getter);
        builder.put(name + ".position", target::setPosition);
        builder.put(name + ".rotation", target::setRotation);
        builder.put(name + ".scale", target::setScale);
    }

    private void setRootPosition(float x, float y, float z) {
        this.deltaPosition.set(this.rootPosition);
        this.rootPosition.set(x, y, z);
        this.deltaPosition.sub(this.rootPosition);
    }

    private void setRootRotation(float x, float y, float z) {
        this.deltaRotation.set(this.rootRotation);
        this.rootRotation.set(x, y, z);
        this.deltaRotation.sub(this.rootRotation);
    }

    private static final class AnimatableTarget {

        private final AnimatablePlayerModel parent;

        private final ModelPart wide;
        private final ModelPart slim;

        private AnimatableTarget(
                AnimatablePlayerModel                   model,
                Function<PlayerModel<?>, ModelPart>     getter
        ) {
            this.parent     = model;
            this.wide       = getter.apply(model.wide);
            this.slim       = getter.apply(model.slim);
        }

        public void setPosition(float x, float y, float z) {
            var target  = parent.useSlim ? slim : wide;
            var initial = target.getInitialPose();
            target.setPos(initial.x + x, initial.y - y, initial.z + z);
        }

        public void setRotation(float xRot, float yRot, float zRot) {
            var target  = parent.useSlim ? slim : wide;
            var initial = target.getInitialPose();
            target.setRotation(initial.xRot + xRot, initial.yRot + yRot, initial.zRot + zRot);
        }

        public void setScale(float xScale, float yScale, float zScale) {
            var target = parent.useSlim ? slim : wide;
            target.xScale = xScale;
            target.yScale = yScale;
            target.zScale = zScale;
        }
    }
}
