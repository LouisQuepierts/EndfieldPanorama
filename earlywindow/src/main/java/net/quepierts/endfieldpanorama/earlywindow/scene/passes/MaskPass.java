package net.quepierts.endfieldpanorama.earlywindow.scene.passes;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.MinecraftProfile;
import net.quepierts.endfieldpanorama.earlywindow.animation.definition.RawAnimationSet;
import net.quepierts.endfieldpanorama.earlywindow.render.ImageTexture;
import net.quepierts.endfieldpanorama.earlywindow.render.PlayerRenderer;
import net.quepierts.endfieldpanorama.earlywindow.render.procedure.RenderContext;
import net.quepierts.endfieldpanorama.earlywindow.render.procedure.RenderPass;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.CharacterShader;
import net.quepierts.endfieldpanorama.earlywindow.scene.Scene;
import net.quepierts.endfieldpanorama.earlywindow.scene.SceneAnimation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL31;

public final class MaskPass implements RenderPass {

    @Getter
    private final Scene             scene;

    private final MinecraftProfile  profile;
    private final ImageTexture      defaultPlayerTexture;
    private final ImageTexture      profilePlayerTexture;

    private final SceneAnimation    animation;
    private final PlayerRenderer    player;
    private final CharacterShader   characterShader;

    private boolean                 syncPlayerTexture;

    public MaskPass(
            @NotNull MinecraftProfile   profile,
            @NotNull ShaderManager      shaders
    ) {
        this.profile                = profile;

        this.scene                  = new Scene();
        this.characterShader        = new CharacterShader(shaders);
        this.characterShader        .bind(this.scene.getSceneUbo());
        this.characterShader.uTexture.set1i(GL31.GL_TEXTURE0);

        this.defaultPlayerTexture   = ImageTexture.fromResource("character_regular.png", GL31.GL_NEAREST, GL31.GL_REPEAT);
        this.profilePlayerTexture   = this.createProfilePlayerTexture();

        var playerTexture           = this.syncPlayerTexture ? this.profilePlayerTexture : defaultPlayerTexture;
        this.player                 = new PlayerRenderer(playerTexture, this.characterShader, false);
        this.scene                  .addModel(this.player);

        var animations              = RawAnimationSet.fromSource("animations/character.json");

        this.animation              = new SceneAnimation(
                                    animations,
                                    this.player,
                                    this.scene.getCamera()
        );
    }

    @Override
    public void render(RenderContext context, float delta, float time) {

        if (this.profile.isDone() && !this.syncPlayerTexture) {

            this.syncPlayerTexture = true;
            var bytes = this.profile.getSkin();
            this.profilePlayerTexture.upload(bytes);
            this.profilePlayerTexture.setFilter(GL31.GL_NEAREST);

            this.player.update(this.profilePlayerTexture, this.profile.isSlim());
        }

        this.animation.update(delta);

        var modelTransform = new Matrix4f();

        this.player.getTransform().getMatrix(modelTransform);
        this.characterShader.uModelMatrix.setMatrix4f(
                new Matrix4f()
                        .scale(0.0625f)
                        .mul(modelTransform)
        );

        context.bindFrameBuffer("mask");

        GL31.glEnable(GL31.GL_DEPTH_TEST);
        this.scene.render(time);
        GL31.glDisable(GL31.GL_DEPTH_TEST);

        if (animation.isShowPattern() && !context.hasSignal(CombinePass.SIGNAL_CHLADNI)) {
            context.signal(CombinePass.SIGNAL_CHLADNI, true, true);
        }

    }

    @Override
    public void free() {
        this.player                 .free();
        this.scene                  .free();
        this.characterShader        .free();

        this.defaultPlayerTexture   .free();
        this.profilePlayerTexture   .free();
    }

    @Override
    public void resize(int width, int height) {
        this.scene.updateProjectionMatrix(width, height);
    }

    @Override
    public void duplicate(RenderPass other) {
        if (other instanceof MaskPass pass) {
            this.animation.cloneState(pass.animation);
        }
    }

    private ImageTexture createProfilePlayerTexture() {
        if (this.profile.isDone() && !this.syncPlayerTexture) {
            this.syncPlayerTexture = true;
            var raw = this.profile.getSkin();

            if (raw != null && raw.length != 0) {
                return ImageTexture.fromByteArray(this.profile.getSkin(), GL31.GL_NEAREST, GL31.GL_REPEAT);
            }
        }

        var slim = this.profile.isSlim();
        return ImageTexture.fromResource(
                slim ? "character_slim.png" : "character_regular.png",
                GL31.GL_NEAREST, GL31.GL_REPEAT
        );
    }

    public void trigger() {
        this.animation.trigger();
    }
}
