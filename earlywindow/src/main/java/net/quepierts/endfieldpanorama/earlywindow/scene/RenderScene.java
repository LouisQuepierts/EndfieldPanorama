package net.quepierts.endfieldpanorama.earlywindow.scene;

import net.quepierts.endfieldpanorama.earlywindow.ResourceManager;
import net.quepierts.endfieldpanorama.earlywindow.MinecraftProfile;
import net.quepierts.endfieldpanorama.earlywindow.render.*;
import net.quepierts.endfieldpanorama.earlywindow.render.procedure.RenderProcedure;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.scene.passes.BackgroundPass;
import net.quepierts.endfieldpanorama.earlywindow.scene.passes.CombinePass;
import net.quepierts.endfieldpanorama.earlywindow.scene.passes.MaskPass;
import net.quepierts.endfieldpanorama.earlywindow.ui.EndfieldLoadingControl;
import net.quepierts.endfieldpanorama.earlywindow.ui.OverlayPass;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL31;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RenderScene {

    public static final float       FOV   = 70.0f;
    public static final float       NEAR  = 0.05f;
    public static final float       FAR   = 100.0f;

    private static final Logger     LOGGER = LoggerFactory.getLogger(RenderScene.class);

    private final MinecraftProfile  profile;
    private final ShaderManager     shaders;
    private final RenderProcedure   procedure;

    private final MaskPass          scenePass;

    private final Graphics          graphics;

    private float                   time;
    private int                     width;
    private int                     height;

    public RenderScene(
            @NotNull ResourceManager    resources,
            @NotNull MinecraftProfile   profile
    ) {

        this.profile                = profile;
        this.shaders                = new ShaderManager();
        this.scenePass              = new MaskPass(profile, shaders);

        var font                    = Font.minecraft();
        var graphics                = new Graphics(font, shaders, 1920, 1080);
        var backgroundPass          = new BackgroundPass(shaders);

        var scene                   = this.scenePass.getScene();
        var combinePass             = new CombinePass(
                                        shaders,
                                        scene.getSceneUbo()
        );

        var overlayPass             = createOverlayPass(graphics);

        var procedureBuilder        = createProcedureBuilder()
                                    .registerPass(() -> backgroundPass)
                                    .registerPass(() -> this.scenePass)
                                    .registerPass(() -> combinePass)
                                    .registerPass(() -> overlayPass);

        this.graphics               = graphics;
        this.procedure              = procedureBuilder.build(resources, graphics);

        resources.register(graphics);
        resources.register(this::free);

        long currentContext = GLFW.glfwGetCurrentContext();
        Thread thread = Thread.currentThread();
        LOGGER.info("Current context: {}", currentContext);
        LOGGER.info("Current thread: {}", thread);

    }

    public RenderScene duplicate(ResourceManager manager) {
        var profile = this.profile;

        var scene   = new RenderScene(manager, profile);
        scene.time  = this.time;
        scene.resize(this.width, this.height);
        scene.procedure.duplicate(this.procedure);
//        scene.animation.cloneState(this.animation);
        return scene;
    }

    public void render(
            float delta,
            Runnable bindMainBuffer
    ) {

        this.time += delta;

        GL31.glDisable(GL31.GL_CULL_FACE);
        this.procedure.render(bindMainBuffer, delta, this.time);
        GL31.glEnable(GL31.GL_CULL_FACE);

    }

    public void trigger() {
        this.scenePass.trigger();
    }

    public void free() {
        this.shaders.free();
    }

    public void resize(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }

        this.width  = width;
        this.height = height;

//        this.backgroundFrameBuffer.resize(width, height);
//        this.maskFrameBuffer.resize(width, height);

        this.graphics.resize(width, height);
        this.procedure.resize(width, height);
    }

    private static OverlayPass createOverlayPass(@NotNull Graphics graphics) {
        var font    = graphics.getFont();
        var pass    = new OverlayPass();
        pass    .add(EndfieldLoadingControl::new)
                /*.add(() -> new TextElement(font, "// Compiling Shaders...")
                        .vertical(UiElement.Layout.MAX)
                        .horizontal(UiElement.Layout.CENTER)
                        .position(0, -24))*/;

        return pass;
    }

    private static RenderProcedure.Builder createProcedureBuilder() {

        return new RenderProcedure.Builder()
                .registerBuffer("background", false)
                .registerBuffer("mask", false)
                .registerBuffer("overlay", false)
                .registerBuffer("scene", true, 0.943f, 0.943f, 0.943f, 1.0f);
    }

}
