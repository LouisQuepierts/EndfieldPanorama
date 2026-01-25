package net.quepierts.els.animation;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public final class AnimatablePlayerModel {

    private final PlayerModel<?> wide;
    private final PlayerModel<?> slim;

    @Getter
    private final Map<String, Consumer3f> targets;

    private final Vector3f position = new Vector3f();
    private final Vector3f rotation = new Vector3f();

    @Getter
    @Setter
    private boolean useSlim = false;

    public AnimatablePlayerModel(
            PlayerModel<?>  wide,
            PlayerModel<?>  slim
    ) {
        this.wide       = wide;
        this.slim       = slim;

        var builder     = ImmutableMap.<String, Consumer3f>builder();

        builder.put("root.position", this::setPosition);
        builder.put("root.rotation", this::setRotation);

        register("head", model -> model.head, builder);
        register("body", model -> model.body, builder);
        register("right_arm", model -> model.rightArm, builder);
        register("left_arm", model -> model.leftArm, builder);
        register("right_leg", model -> model.rightLeg, builder);
        register("left_leg", model -> model.leftLeg, builder);

        this.targets    = builder.build();
    }

    private void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    private void setRotation(float xRot, float yRot, float zRot) {
        this.rotation.set(xRot, yRot, zRot);
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
            wide.setPos(x, y, z);
            slim.setPos(x, y, z);
        }

        public void setRotation(float xRot, float yRot, float zRot) {
            wide.setRotation(xRot, yRot, zRot);
            slim.setRotation(xRot, yRot, zRot);
        }

        public void setScale(float xScale, float yScale, float zScale) {
            var target = parent.useSlim ? slim : wide;
            target.xScale = xScale;
            target.yScale = yScale;
            target.zScale = zScale;
        }
    }
}
