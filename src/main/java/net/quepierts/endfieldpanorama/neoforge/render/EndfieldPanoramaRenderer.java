package net.quepierts.endfieldpanorama.neoforge.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.quepierts.endfieldpanorama.earlywindow.ResourceManager;
import net.quepierts.endfieldpanorama.earlywindow.scene.RenderScene;
import net.quepierts.endfieldpanorama.neoforge.animation.AnimatablePlayerModel;
import net.quepierts.endfieldpanorama.neoforge.animation.AnimationState;
import net.quepierts.endfieldpanorama.neoforge.reference.Animations;
import net.quepierts.endfieldpanorama.neoforge.reference.Shaders;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

public final class EndfieldPanoramaRenderer {

    private static EndfieldPanoramaRenderer instance;

    private final Minecraft minecraft;

    private final Supplier<PlayerSkin> skinSupplier;
    private final AnimatablePlayerModel model;

    private final PoseStack modelPose;

    private final Matrix4f modelMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f projectionMatrix;

    private final Matrix4f invViewMatrix;
    private final Matrix4f invProjectionMatrix;

    private final RenderTarget maskTarget;
    private final RenderTarget targetBackground;

    private final AnimationState state;

    private float timer;
    private float cachedPartialTick;

    private RenderScene scene;
    private ResourceManager manager;

    private boolean triggered;
    private float triggerTimer;

    private EndfieldPanoramaRenderer() {
        var minecraft           = Minecraft.getInstance();
        var profile             = minecraft.getGameProfile();

        var width               = minecraft.getWindow().getWidth();
        var height              = minecraft.getWindow().getHeight();
        var aspectRatio         = this.calculateAspectRatio(width, height);

        this.minecraft          = minecraft;

        this.skinSupplier       = minecraft.getSkinManager().lookupInsecure(profile);
        this.state              = new AnimationState();
        this.model              = AnimatablePlayerModel.create();
        this.model.bind(Animations.DEFAULT);

        this.modelPose          = new PoseStack();
        this.modelMatrix        = new Matrix4f()
                                    .translate(0f, 1.0f, -3.0f)
                                    .rotateY(Mth.PI)
                                    .scale(1, -1, 1);

        this.modelPose.mulPose(this.modelMatrix);

        this.viewMatrix         = new Matrix4f();
        this.projectionMatrix   = new Matrix4f()
                                    .setPerspective(70.0f, aspectRatio, 0.05f, 100.0f);

        this.invViewMatrix     = new Matrix4f(this.viewMatrix).invert();
        this.invProjectionMatrix = new Matrix4f(this.projectionMatrix).invert();

        this.maskTarget         = this.createTarget(width, height);
        this.targetBackground   = this.createTarget(width, height);
    }

    public void setup(RenderScene scene, ResourceManager manager) {
        this.scene = scene;
        this.manager = manager;
    }

    public void preparePanorama() {
        this.targetBackground.bindWrite(false);
    }

    public void update(float partialTick) {
        this.timer += partialTick * 0.05f;
        this.cachedPartialTick = partialTick;
    }

    public void renderScene() {
        /*var window          = this.minecraft.getWindow();
        var width           = window.getWidth();
        var height          = window.getHeight();

        var mainTarget      = this.minecraft.getMainRenderTarget();

        if (!this.state.isPlaying()) {
            this.model.play("walk", this.state);
        }

        this.prepareMask(this.cachedPartialTick);
        mainTarget.bindWrite(false);
        this.blitCharacter();*/

        var delta = this.cachedPartialTick * 0.05f;
        if (!triggered && this.triggerTimer > 0.0f) {
            this.triggerTimer -= delta;

            if (this.triggerTimer <= 0.0f) {
                this.triggered = true;
                this.scene.trigger();
            }
        }

        var mainTarget      = this.minecraft.getMainRenderTarget();

        mainTarget.bindWrite(false);
        this.scene.render(delta);

        VertexBuffer.unbind();
    }

    @SuppressWarnings("all")
    private void prepareMask(float partialTick) {

        var skin                = this.skinSupplier.get();
        var model               = this.model.resolve(skin, this.state, partialTick);
        var texture             = this.minecraft.getTextureManager().getTexture(skin.texture());

        var format              = DefaultVertexFormat.POSITION_TEX;
        var buffer              = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, format);

        this.modelPose.pushPose();
        model.renderToBuffer(this.modelPose, buffer, 15728880, OverlayTexture.NO_OVERLAY);
        this.modelPose.popPose();

        var mesh                = buffer.buildOrThrow();
        var vbo                 = format.getImmediateDrawVertexBuffer();
        var shader              = GameRenderer.getPositionTexShader();

        var viewMatrix          = new Matrix4f()
                .translate(0.0f, 0.0f, 0.0f)
                .rotateX(-0.2f);
        var projectionMatrix    = this.projectionMatrix;

        this.maskTarget.clear(false);
        this.maskTarget.bindWrite(false);

        shader.MODEL_VIEW_MATRIX.set(viewMatrix);
        shader.PROJECTION_MATRIX.set(projectionMatrix);
        shader.setSampler("Sampler0", texture.getId());

        GL11.glCullFace(GL11.GL_FRONT);
        RenderSystem.enableDepthTest();

        shader.apply();
        vbo.bind();
        vbo.upload(mesh);
        vbo.draw();

        VertexBuffer.unbind();

        shader.clear();

        RenderSystem.disableDepthTest();
        GL11.glCullFace(GL11.GL_BACK);
    }

    private void blitCharacter() {
        var shader      = Shaders.FANCY_BACKGROUND.getInstance();
        var invViewMatrix = new Matrix4f()
                .translate(0.0f, 0.0f, -this.timer)
                .rotateX(-0.2f);

        shader.setSampler("uMaskSampler", this.maskTarget);
        shader.setSampler("uBackgroundSampler", this.targetBackground);

        shader.uTime.set(this.timer);
        shader.uInverseViewMatrix.set(invViewMatrix);
        shader.uInverseProjectionMatrix.set(this.invProjectionMatrix);

        shader.apply();
        RenderHelper.blit();
        shader.clear();
    }

    public void resize(int width, int height) {
        this.maskTarget.resize(width, height, true);
        this.targetBackground.resize(width, height, true);

        var aspectRatio = this.calculateAspectRatio(width, height);
        this.projectionMatrix.setPerspective(70.0f, aspectRatio, 0.05f, 100.0f);
        this.invProjectionMatrix.set(this.projectionMatrix).invert();

        if (this.scene != null) {
            this.scene.resize(width, height);
        }
    }

    public void destroy() {
        this.maskTarget.destroyBuffers();
        this.targetBackground.destroyBuffers();

        this.manager.free();
//        this.window.close();
    }

    public static void setup() {
        if (instance != null) {
            return;
        }

        instance = new EndfieldPanoramaRenderer();
    }

    @NotNull
    public static EndfieldPanoramaRenderer getInstance() {
        return instance;
    }

    public static boolean setuped() {
        return instance != null;
    }

    private RenderTarget createTarget(int width, int height) {
        var target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        return target;
    }

    private float calculateAspectRatio(int width, int height) {
        return width > height ? (float) width / height : (float) height / width;
    }

    public void trigger() {
        this.triggerTimer = 1.0f;
    }

    private record Model(
            PlayerModel<?> wide,
            PlayerModel<?> slim
    ) {
        private static Model bake(EntityModelSet set) {
            var wide = new PlayerModel<>(set.bakeLayer(ModelLayers.PLAYER), false);
            var slim = new PlayerModel<>(set.bakeLayer(ModelLayers.PLAYER_SLIM), true);

            wide.young = false;
            slim.young = false;

            return new Model(wide, slim);
        }

        public PlayerModel<?> unpack(PlayerSkin skin) {
            return skin.model() == PlayerSkin.Model.SLIM ? this.slim : this.wide;
        }
    }

}
