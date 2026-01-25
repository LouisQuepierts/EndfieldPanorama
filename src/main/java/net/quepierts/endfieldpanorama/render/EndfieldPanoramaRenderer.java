package net.quepierts.endfieldpanorama.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.quepierts.endfieldpanorama.EndfieldPanorama;
import net.quepierts.endfieldpanorama.animation.AnimatablePlayerModel;
import net.quepierts.endfieldpanorama.animation.AnimationState;
import net.quepierts.endfieldpanorama.reference.Shaders;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

public final class EndfieldPanoramaRenderer {

    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath(
            EndfieldPanorama.MODID,
            "slim"
    );

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

    private EndfieldPanoramaRenderer() {
        var minecraft           = Minecraft.getInstance();
        var profile             = minecraft.getGameProfile();
        var models              = minecraft.getEntityModels();

        var width               = minecraft.getWindow().getWidth();
        var height              = minecraft.getWindow().getHeight();
        var aspectRatio         = this.calculateAspectRatio(width, height);

        this.minecraft          = minecraft;

        this.skinSupplier       = minecraft.getSkinManager().lookupInsecure(profile);
        this.state              = new AnimationState();
        this.model              = AnimatablePlayerModel.bake(models);
        this.model.bind(ANIMATION_LOCATION);

        this.modelPose          = new PoseStack();
        this.modelMatrix        = new Matrix4f()
                                    .translate(0f, 0.5f, -3.2f)
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

    public void preparePanorama() {
        this.targetBackground.bindWrite(false);
    }

    public void renderScene(GuiGraphics guiGraphics, float partialTick) {
        var window          = this.minecraft.getWindow();
        var width           = window.getWidth();
        var height          = window.getHeight();

        var mainTarget      = this.minecraft.getMainRenderTarget();

        if (!this.state.isPlaying()) {
            this.model.play("walk", this.state);
        }

        this.timer += partialTick * 0.05f;

        this.prepareMask(partialTick);
        mainTarget.bindWrite(false);
        this.blitCharacter();
    }

    @SuppressWarnings("all")
    private void prepareMask(float partialTick) {

        var skin                = this.skinSupplier.get();
        var model               = this.model.resolve(skin, this.state, partialTick);
        var texture             = this.minecraft.getTextureManager().getTexture(skin.texture());

        var format              = DefaultVertexFormat.POSITION_TEX;
        var buffer              = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, format);

//        this.modelPose.pushPose();
        model.renderToBuffer(this.modelPose, buffer, 15728880, OverlayTexture.NO_OVERLAY);
//        this.modelPose.popPose();

        var mesh                = buffer.buildOrThrow();
        var vbo                 = format.getImmediateDrawVertexBuffer();
        var shader              = GameRenderer.getPositionTexShader();

        var viewMatrix          = this.viewMatrix;
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
        var shader      = Shaders.TITLE_COMBINE.getInstance();
        var invViewMatrix = new Matrix4f().translate(0.0f, 0.0f, -state.getTimer());

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
    }

    public void destroy() {
        this.maskTarget.destroyBuffers();
        this.targetBackground.destroyBuffers();
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
