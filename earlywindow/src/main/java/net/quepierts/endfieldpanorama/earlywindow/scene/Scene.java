package net.quepierts.endfieldpanorama.earlywindow.scene;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.ModelRenderer;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SceneUbo;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class Scene implements Resource {

    public static final float           NEAR  = 0.05f;
    public static final float           FAR   = 100.0f;

    private final Matrix4f              matProjection;
    private final Matrix4f              matProjectionInverse;
    private final Matrix4f              matView;
    private final Matrix4f              matViewInverse;

    @Getter
    private final SceneUbo              sceneUbo;

    @Getter
    private final Camera                camera;

    private final List<ModelRenderer>   models;

    private float fov                   = RenderScene.FOV;

    public Scene() {
        this.matProjection          = new Matrix4f();
        this.matProjectionInverse   = new Matrix4f();
        this.matView                = new Matrix4f();
        this.matViewInverse         = new Matrix4f();
        this.sceneUbo               = new SceneUbo();

        this.camera                 = new Camera();
        this.models                 = new ArrayList<>();
    }

    public void addModel(@NotNull ModelRenderer model) {
        models.add(model);
    }

    public void render(float time) {
        this.updateViewMatrix();
        this.sceneUbo.uProjectionMatrix.setMatrix4f(this.matProjection);
        this.sceneUbo.uInverseProjectionMatrix.setMatrix4f(this.matProjectionInverse);
        this.sceneUbo.uTime.set1f(time);
        this.sceneUbo.upload();
        this.sceneUbo.bind();

        for (ModelRenderer model : models) {
            model.render();
        }
    }

    @Override
    public void free() {
        this.sceneUbo.free();
    }

    public void updateProjectionMatrix(int width, int height) {
        var aspect = calculateAspectRatio(width, height);

        this.matProjection.setPerspective(this.fov, aspect, NEAR, FAR);
        this.matProjectionInverse.set(this.matProjection).invert();

        this.sceneUbo.uProjectionMatrix.setMatrix4f(this.matProjection);
        this.sceneUbo.uInverseProjectionMatrix.setMatrix4f(this.matProjectionInverse);
    }

    private void updateViewMatrix() {
        this.camera.getViewMatrix(this.matView);
        this.camera.getCameraWorldMatrix(this.matViewInverse);

        this.sceneUbo.uViewMatrix.setMatrix4f(this.matView);
        this.sceneUbo.uInverseViewMatrix.setMatrix4f(this.matViewInverse);
    }

    private static float calculateAspectRatio(int width, int height) {
        return width > height ? (float) width / height : (float) height / width;
    }
}
