package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.model.PlayerModel;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.CharacterShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SkeletonUbo;
import net.quepierts.endfieldpanorama.earlywindow.scene.Transform;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class PlayerRenderer implements Resource {

    @Getter
    private final Transform         transform   = new Transform();
    private final Matrix4f          matrix      = new Matrix4f();

    @Getter
    private @NotNull PlayerModel    model;
    private @NotNull ImageTexture   skin;

    private @NotNull SkeletonUbo    ubo;

    private boolean slim;

    public PlayerRenderer(@NotNull ImageTexture skin, boolean slim) {
        this.skin   = skin;
        this.model  = PlayerModel.create(slim);
        this.ubo    = this.model.getSkeleton().createUbo();

        this.slim = slim;
    }

    public void render(@NotNull CharacterShader shader) {

        this.transform.getMatrix(matrix);
        shader.uModelMatrix.setMatrix4f(
                new Matrix4f()
                        .scale(0.0625f)
                        .mul(matrix)
        );
        shader.bind(this.ubo);

        this.model.getSkeleton().apply(this.ubo);
        this.ubo.upload();
        this.ubo.bind();

        this.skin.bind(0);
        this.model.draw(shader);
        this.skin.unbind(0);

        this.ubo.unbind();

    }

    public void update(@NotNull ImageTexture skin, boolean slim) {

        if (this.skin != skin) {
            this.skin = skin;
        }

        if (this.slim != slim) {
            this.model.free();
            this.ubo.free();

            this.model = PlayerModel.create(slim);
            this.ubo = this.model.getSkeleton().createUbo();

            this.slim = slim;
        }

    }

    @Override
    public void free() {
        this.ubo.free();
        this.model.free();
    }
}
