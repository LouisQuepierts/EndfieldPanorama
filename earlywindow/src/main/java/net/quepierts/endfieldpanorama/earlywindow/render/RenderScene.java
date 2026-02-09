package net.quepierts.endfieldpanorama.earlywindow.render;

import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.instance.SceneUbo;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.instance.TestShader;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class RenderScene {

    public static final float FOV   = 90f;
    public static final float NEAR  = 0.1f;
    public static final float FAR   = 1000f;

    private int width;
    private int height;

    private final Matrix4f      matProjection;
    private final Matrix4f      matProjectionInverse;
    private final Matrix4f      matView;
    private final Matrix4f      matViewInverse;

    private final Vector3f      cameraPosition;
    private final Quaternionf   cameraRotation;

    // GL
    private final SceneUbo      sceneUbo;
    private final ShaderManager shaders;

    private final TestShader testShader;

    // accept data from joml
    private final float[]       jomlArr;



    private float               time;

    public RenderScene() {
        // init
        this.matProjection          = new Matrix4f();
        this.matProjectionInverse   = new Matrix4f();
        this.matView                = new Matrix4f();
        this.matViewInverse         = new Matrix4f();

        this.cameraPosition         = new Vector3f(0f, 0f, 0f);
        this.cameraRotation         = new Quaternionf();

        this.sceneUbo               = new SceneUbo();
        this.shaders                = new ShaderManager();

        this.testShader             = new TestShader(this.shaders);

        this.jomlArr = new float[16];

        this.sceneUbo.buffer.bind();
        this.sceneUbo.uTime.set1f(this.time);
        this.testShader.bindUbo(this.sceneUbo.buffer);

        RenderHelper.init();
    }

    public void render(float delta) {

        this.time += delta;

        this.updateViewMatrix();
        this.sceneUbo.uTime.set1f(this.time);
        this.sceneUbo.buffer.upload();

        RenderHelper.blit(this.testShader);
    }

    public void free() {
        this.shaders.free();
        this.sceneUbo.buffer.free();

        RenderHelper.free();
    }

    public void resize(int width, int height) {
        this.width  = width;
        this.height = height;

        this.updateProjectionMatrix();
    }

    private void updateProjectionMatrix() {
        var aspect = calculateAspectRatio(width, height);

        this.matProjection.setPerspective(FOV, aspect, NEAR, FAR);
        this.matProjectionInverse.set(this.matProjection).invert();

        this.sceneUbo.uProjectionMatrix.setMatrix4f(this.matProjection.get(this.jomlArr));
        this.sceneUbo.uInverseProjectionMatrix.setMatrix4f(this.matProjectionInverse.get(this.jomlArr));
    }

    private void updateViewMatrix() {
        this.matView.identity()
                .translate(this.cameraPosition)
                .rotate(this.cameraRotation);

        this.matViewInverse.set(this.matView).invert();

        this.sceneUbo.uViewMatrix.setMatrix4f(this.matView.get(this.jomlArr));
        this.sceneUbo.uInverseViewMatrix.setMatrix4f(this.matViewInverse.get(this.jomlArr));
    }

    private static float calculateAspectRatio(int width, int height) {
        return (float) width / height;
    }

}
