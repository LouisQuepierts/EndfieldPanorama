package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.model.PlayerModel;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.CharacterShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SkeletonUbo;
import net.quepierts.endfieldpanorama.earlywindow.scene.Transform;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class PlayerRenderer extends ModelRenderer implements Resource {

    private final Matrix4f          matrix      = new Matrix4f();

    @Getter
    private @NotNull ImageTexture   skin;

    private @NotNull SkeletonUbo    ubo;

    private boolean slim;

    public PlayerRenderer(
            @NotNull ImageTexture skin,
            @NotNull CharacterShader shader,
            boolean slim
    ) {
        super(PlayerModel.create(slim), shader);
        this.skin   = skin;
        this.ubo    = this.getModel().getSkeleton().createUbo();

        this.slim = slim;
    }

    @Override
    public void render() {

        this.getTransform().getMatrix(matrix);

        var shader  = (CharacterShader) this.getShader();
        var model   = this.getModel();
        shader      .uModelMatrix.setMatrix4f(
                    new Matrix4f()
                            .scale(0.0625f)
                            .mul(matrix)
        );
        shader      .bind(this.ubo);

        model.getSkeleton().apply(this.ubo);
        this.ubo.upload();
        this.ubo.bind();

        this.skin.bind(0);
        model.draw(shader);
        this.skin.unbind(0);

        this.ubo.unbind();

    }

    public void update(@NotNull ImageTexture skin, boolean slim) {

        if (this.skin != skin) {
            this.skin = skin;
        }

        if (this.slim != slim) {
            var model   = this.getModel();
            model.free();
            this.ubo.free();

            model = PlayerModel.create(slim);
            this.ubo = model.getSkeleton().createUbo();

            this.slim = slim;
        }

    }

    @Override
    public void free() {
        this.ubo.free();
    }
}
