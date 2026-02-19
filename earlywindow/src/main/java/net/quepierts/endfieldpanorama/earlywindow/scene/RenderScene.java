package net.quepierts.endfieldpanorama.earlywindow.scene;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.ResourceManager;
import net.quepierts.endfieldpanorama.earlywindow.MinecraftProfile;
import net.quepierts.endfieldpanorama.earlywindow.animation.definition.RawAnimationSet;
import net.quepierts.endfieldpanorama.earlywindow.render.Graphics;
import net.quepierts.endfieldpanorama.earlywindow.render.ImageTexture;
import net.quepierts.endfieldpanorama.earlywindow.render.model.PlayerModel;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.CharacterShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SceneUbo;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.TestShader;
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

    private final ResourceManager   resourceManager;
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

    private final ImageTexture      defaultPlayerTexture;
    private final ImageTexture      profilePlayerTexture;

    private final TestShader        testShader;
    private final CharacterShader   characterShader;

    private final PlayerModel       playerModel;
    private final SkeletonUbo       skeletonUbo;

    private final RawAnimationSet   animations;
    private final SceneAnimation    animation;

    // accept data from joml
    private final float[]       jomlArr;
    private float               time;

    private boolean             syncPlayerTexture;

    public RenderScene(
            @NotNull ResourceManager    resourceManager,
            @NotNull MinecraftProfile   profile
    ) {

        this.resourceManager = resourceManager;
        this.profile = profile;

        // init
        this.matProjection          = new Matrix4f();
        this.matProjectionInverse   = new Matrix4f();
        this.matView                = new Matrix4f();
        this.matViewInverse         = new Matrix4f();

        this.camera                 = new Camera();

        this.graphics               = new Graphics();

        this.sceneUbo               = new SceneUbo();
        this.shaders                = new ShaderManager();

        this.testShader             = new TestShader(this.shaders);
        this.characterShader        = new CharacterShader(this.shaders);
        this.defaultPlayerTexture   = ImageTexture.fromResource("slim.png", GL31.GL_NEAREST, GL31.GL_REPEAT);
        this.profilePlayerTexture   = this.createProfilePlayerTexture();

        this.playerModel            = PlayerModel.create(true);
        this.skeletonUbo            = this.playerModel.getSkeleton().createUbo();
        this.jomlArr                = new float[16];

        this.animations             = RawAnimationSet.fromSource("animations/slim.json");
        this.animation              = new SceneAnimation(
                this.animations,
                this.playerModel,
                this.camera
        );

        this.sceneUbo.uTime.set1f(this.time);
        this.testShader.bind(this.sceneUbo);
        this.testShader.uTexture.set1i(GL31.GL_TEXTURE0);

        this.characterShader.bind(this.sceneUbo);
        this.characterShader.bind(this.skeletonUbo);
        this.characterShader.uTexture.set1i(GL31.GL_TEXTURE0);

        resourceManager.register(this.testShader);
        resourceManager.register(this.characterShader);
        resourceManager.register(this.sceneUbo);
        resourceManager.register(this.defaultPlayerTexture);
        resourceManager.register(this.profilePlayerTexture);
        resourceManager.register(this.graphics);
        resourceManager.register(this.playerModel);
        resourceManager.register(this::free);

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

    public void render(float delta) {

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
            this.profilePlayerTexture.bind(0);
        }

        var texture = this.syncPlayerTexture ? this.profilePlayerTexture : this.defaultPlayerTexture;
        texture.bind(0);

        this.time += delta;

        GL31.glCullFace(GL31.GL_BACK);
        this.updateViewMatrix();
        this.sceneUbo.uProjectionMatrix.setMatrix4f(this.matProjection.get(this.jomlArr));
        this.sceneUbo.uInverseProjectionMatrix.setMatrix4f(this.matProjectionInverse.get(this.jomlArr));
        this.sceneUbo.uTime.set1f(this.time);
        this.sceneUbo.upload();
        this.sceneUbo.bind();

        this.graphics.blit(this.testShader);

        this.animation.update(delta);
        this.playerModel.getSkeleton().apply(this.skeletonUbo);
        this.skeletonUbo.upload();
        this.skeletonUbo.bind();

        var modelTransform = new Matrix4f();

        if (this.animation.isLooping()) {
            this.playerModel.getTransform().translate(0.0f, 0.0f, this.animation.getCurrentTime() * -16f);
        }

        this.playerModel.getTransform().getMatrix(modelTransform);
        this.characterShader.uModelMatrix.setMatrix4f(
                new Matrix4f()
                        .scale(0.0625f)
                        .mul(modelTransform)
        );

        // enable depth
        GL31.glEnable(GL31.GL_DEPTH_TEST);
        this.playerModel.draw(this.characterShader);
        GL31.glDisable(GL31.GL_DEPTH_TEST);
        texture.unbind(0);
    }

    public void trigger() {
        this.animation.trigger();
    }

    public void free() {
        this.shaders.free();
    }

    public void resize(int width, int height) {
        this.width  = width;
        this.height = height;

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

        if (this.animation.isLooping()) {
            this.camera.translate(0.0f, 0.0f, this.animation.getCurrentTime() * -16f);
        }
        this.camera.getViewMatrix(this.matView);
        this.camera.getInverseViewMatrix(this.matViewInverse);

        this.sceneUbo.uViewMatrix.setMatrix4f(this.matView.get(this.jomlArr));
        this.sceneUbo.uInverseViewMatrix.setMatrix4f(this.matViewInverse.get(this.jomlArr));
    }

    private static float calculateAspectRatio(int width, int height) {
        return width > height ? (float) width / height : (float) height / width;
    }

}
