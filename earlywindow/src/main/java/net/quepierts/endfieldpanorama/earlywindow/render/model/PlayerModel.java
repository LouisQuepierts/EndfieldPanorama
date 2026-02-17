package net.quepierts.endfieldpanorama.earlywindow.render.model;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.skeleton.Skeleton;
import org.jetbrains.annotations.NotNull;

public final class PlayerModel extends AbstractModel {

    @Getter
    private final boolean slim;

    public static PlayerModel create(boolean slim) {
        var skeleton    = skeleton(slim);
        return new PlayerModel(slim, skeleton);
    }

    private PlayerModel(
                        boolean     slim,
            @NotNull    Skeleton    skeleton
    ) {
        super(skeleton);
        this.slim = slim;
    }

    private static Skeleton skeleton(boolean slim) {

        int armWidth = slim ? 3 : 4;
        int armPivot = slim ? 5 : 6;

        var builder     = Skeleton.builder()
                .begin("head", 0, 24, 0)
                    .position(-4, 24, -4)
                    .box(0.0f)
                        .scale(8, 8, 8)
                        .uv(0, 0)
                    .end()
                    .box(0.5f)
                        .scale(8, 8, 8)
                        .uv(32, 0)
                    .end()
                .end()

                .begin("body", 0, 12, 0)
                    .position(-4, 12, -2)
                    .box(0.0f)
                        .scale(8, 12, 4)
                        .uv(16, 16)
                    .end()
                    .box(0.25f)
                        .scale(8, 12, 4)
                        .uv(16, 32)
                    .end()
                .end()

                .begin("right_arm", armPivot, 22, 0)
                    .position(4, 12, -2)
                    .box(0.0f)
                        .scale(armWidth, 12, 4)
                        .uv(40, 16)
                    .end()
                    .box(0.25f)
                        .scale(armWidth, 12, 4)
                        .uv(40, 32)
                    .end()
                .end()

                .begin("left_arm", -armPivot, 12, 0)
                    .position(-7, 12, -2)
                    .box(0.0f)
                        .scale(armWidth, 12, 4)
                        .uv(32, 48)
                    .end()
                    .box(0.25f)
                        .scale(armWidth, 12, 4)
                        .uv(48, 48)
                    .end()
                .end()

                .begin("right_leg", 2, 12, 0)
                    .position(0, 0, -2)
                    .box(0.0f)
                        .scale(4, 12, 4)
                        .uv(0, 16)
                    .end()
                    .box(0.0f)
                        .scale(4, 12, 4)
                        .uv(0, 32)
                    .end()
                .end()

                .begin("left_leg", -2, 12, 0)
                    .position(-4, 0, -2)
                    .box(0.0f)
                        .scale(4, 12, 4)
                        .uv(16, 48)
                    .end()
                    .box(0.0f)
                        .scale(4, 12, 4)
                        .uv(0, 48)
                    .end()
                .end();

        return builder.build();
    }
}
