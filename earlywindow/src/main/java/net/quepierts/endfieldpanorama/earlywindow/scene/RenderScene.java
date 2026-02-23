package net.quepierts.endfieldpanorama.earlywindow.scene;

import net.quepierts.endfieldpanorama.earlywindow.ResourceManager;
import net.quepierts.endfieldpanorama.earlywindow.MinecraftProfile;
import net.quepierts.endfieldpanorama.earlywindow.animation.definition.RawAnimationSet;
import net.quepierts.endfieldpanorama.earlywindow.render.Graphics;
import net.quepierts.endfieldpanorama.earlywindow.render.ImageTexture;
import net.quepierts.endfieldpanorama.earlywindow.render.ModelRenderer;
import net.quepierts.endfieldpanorama.earlywindow.render.PanoramaRenderer;
import net.quepierts.endfieldpanorama.earlywindow.render.model.PlayerModel;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.CharacterShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SceneUbo;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SkeletonUbo;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL31;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RenderScene {

    public static final float FOV   = 70.0f;
    public static final float NEAR  = 0.05f;
    public static final float FAR   = 100.0f;

    private static final Logger LOGGER = LoggerFactory.getLogger(RenderScene.class);

    private int width;
    private int height;

    private final MinecraftProfile  profile;

    private final Matrix4f          matProjection;
    private final Matrix4f          matProjectionInverse;
    private final Matrix4f          matView;
    private final Matrix4f          matViewInverse;

    private final Camera            camera;

    private final Graphics          graphics;

    // GL
    private final SceneUbo          sceneUbo;
    private final ShaderManager     shaders;

//    private final FrameBuffer       maskFrameBuffer;
//    private final FrameBuffer       backgroundFrameBuffer;

    private final ImageTexture      defaultPlayerTexture;
    private final ImageTexture      profilePlayerTexture;

    private final CharacterShader   characterShader;
//    private final EndfieldShader    backgroundShader;

    private final PlayerModel       playerModel;
    private final SkeletonUbo       skeletonUbo;

    private final SceneProcedure    procedure;
    private final ModelRenderer     player;
    private final PanoramaRenderer  panorama;

    private final RawAnimationSet   animations;
    private final SceneAnimation    animation;

    // accept data from joml
    private final float[]       jomlArr;
    private float               time;

    private boolean             syncPlayerTexture;

    public RenderScene(
            @NotNull ResourceManager    resources,
            @NotNull MinecraftProfile   profile
    ) {

        this.profile                = profile;

        // init
        this.matProjection          = new Matrix4f();
        this.matProjectionInverse   = new Matrix4f();
        this.matView                = new Matrix4f();
        this.matViewInverse         = new Matrix4f();

        this.camera                 = new Camera();

        this.graphics               = new Graphics();

        this.sceneUbo               = new SceneUbo();
        this.shaders                = new ShaderManager();

//        this.maskFrameBuffer        = new FrameBuffer(true);
//        this.backgroundFrameBuffer  = new FrameBuffer(false);
//
        this.characterShader        = new CharacterShader(this.shaders);
//        this.backgroundShader       = new EndfieldShader(this.shaders);

        this.defaultPlayerTexture   = ImageTexture.fromResource("slim.png", GL31.GL_NEAREST, GL31.GL_REPEAT);
        this.profilePlayerTexture   = this.createProfilePlayerTexture();

        this.playerModel            = PlayerModel.create(true);
        this.skeletonUbo            = this.playerModel.getSkeleton().createUbo();
        this.jomlArr                = new float[16];

        this.procedure              = new SceneProcedure(
                                        this.shaders,
                                        this.graphics,
                                        this.sceneUbo
        );

        this.player                 = new ModelRenderer(
                                        this.playerModel,
                                        this.characterShader
        );

        this.panorama               = new PanoramaRenderer(this.shaders);

        this.animations             = RawAnimationSet.fromSource("animations/character.json");
        this.animation              = new SceneAnimation(
                                        this.animations,
                                        this.player,
                                        this.camera
        );

        this.sceneUbo.uTime.set1f(this.time);

        this.characterShader.bind(this.sceneUbo);
        this.characterShader.bind(this.skeletonUbo);
        this.characterShader.uTexture.set1i(GL31.GL_TEXTURE0);

//        resources.register(this.maskFrameBuffer);
//        resources.register(this.backgroundFrameBuffer);
        resources.register(this.characterShader);
//        resources.register(this.backgroundShader);
        resources.register(this.defaultPlayerTexture);
        resources.register(this.profilePlayerTexture);
        resources.register(this.procedure);
        resources.register(this.graphics);
        resources.register(this.playerModel);
        resources.register(this.player);
        resources.register(this.panorama);
        resources.register(this.sceneUbo);
        resources.register(this.skeletonUbo);
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
        scene.animation.cloneState(this.animation);
        return scene;
    }

    public void render(
            float delta,
            Runnable bindMainBuffer
    ) {

        this.time += delta;
        this.animation.update(delta);

//        delta *= 0.1f;

        if (this.profile.isDone() && !this.syncPlayerTexture) {

            long currentContext = GLFW.glfwGetCurrentContext();
            Thread thread = Thread.currentThread();
            LOGGER.info("Current context: {}", currentContext);
            LOGGER.info("Current thread: {}", thread);

            this.syncPlayerTexture = true;
            var bytes = this.profile.getSkin();
            this.profilePlayerTexture.upload(bytes);
            this.profilePlayerTexture.setFilter(GL31.GL_NEAREST);
        }

        if (this.animation.isLooping()) {
            this.procedure.setRenderPattern(true);
        }

        var texture = this.syncPlayerTexture ? this.profilePlayerTexture : this.defaultPlayerTexture;
        texture.bind(0);

        GL31.glDisable(GL31.GL_CULL_FACE);
        this.updateViewMatrix();
        this.sceneUbo.uProjectionMatrix.setMatrix4f(this.matProjection.get(this.jomlArr));
        this.sceneUbo.uInverseProjectionMatrix.setMatrix4f(this.matProjectionInverse.get(this.jomlArr));
        this.sceneUbo.uTime.set1f(this.time);
        this.sceneUbo.upload();
        this.sceneUbo.bind();

//        this.maskFrameBuffer.clear();

        // prepare background
//        this.backgroundFrameBuffer.bind();
        this.procedure.bindBackgroundTarget();
        this.panorama.render(
                this.matProjection,
                this.time
        );

        // prepare mask
        this.playerModel.getSkeleton().apply(this.skeletonUbo);
        this.skeletonUbo.upload();
        this.skeletonUbo.bind();

        var modelTransform = new Matrix4f();

        this.player.getTransform().getMatrix(modelTransform);
        this.characterShader.uModelMatrix.setMatrix4f(
                new Matrix4f()
                        .scale(0.0625f)
                        .mul(modelTransform)
        );

//        this.maskFrameBuffer.bind();
        this.procedure.bindMaskTarget();
        GL31.glEnable(GL31.GL_DEPTH_TEST);
        this.player.render();
        GL31.glDisable(GL31.GL_DEPTH_TEST);
        texture.unbind(0);

        // post
        this.procedure.render(bindMainBuffer);
//        bindMainBuffer.run();
//        this.maskFrameBuffer.bind(0);
//        this.backgroundFrameBuffer.bind(1);
//
//        this.graphics.blit(this.backgroundShader);
//
//        this.maskFrameBuffer.unbind(0);
//        this.backgroundFrameBuffer.unbind(1);

        GL31.glEnable(GL31.GL_CULL_FACE);
    }

    public void trigger() {
        this.animation.trigger();
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

        this.procedure.resize(width, height);
        this.updateProjectionMatrix();
    }

    private ImageTexture createProfilePlayerTexture() {
        if (this.profile.isDone() && !this.syncPlayerTexture) {
            this.syncPlayerTexture = true;
            return ImageTexture.fromByteArray(this.profile.getSkin(), GL31.GL_NEAREST, GL31.GL_REPEAT);
        } else {
            return new ImageTexture();
        }
    }

    private void updateProjectionMatrix() {
        var aspect = calculateAspectRatio(width, height);

        this.matProjection.setPerspective(FOV, aspect, NEAR, FAR);
        this.matProjectionInverse.set(this.matProjection).invert();

        this.sceneUbo.uProjectionMatrix.setMatrix4f(this.matProjection.get(this.jomlArr));
        this.sceneUbo.uInverseProjectionMatrix.setMatrix4f(this.matProjectionInverse.get(this.jomlArr));
    }

    private void updateViewMatrix() {
/*        this.matView.identity()
                .translate(0.0f, 0.0f, time)
                .rotateX(0.2f);

        this.matViewInverse.identity()
                .translate(0.0f, 0.0f, -time)
                .rotateX(-0.2f);*/

        this.camera.getViewMatrix(this.matView);
        this.camera.getCameraWorldMatrix(this.matViewInverse);

        this.sceneUbo.uViewMatrix.setMatrix4f(this.matView.get(this.jomlArr));
        this.sceneUbo.uInverseViewMatrix.setMatrix4f(this.matViewInverse.get(this.jomlArr));
    }

    private static float calculateAspectRatio(int width, int height) {
        return width > height ? (float) width / height : (float) height / width;
    }

}
