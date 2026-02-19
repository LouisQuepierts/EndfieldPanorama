package net.quepierts.endfieldpanorama.earlywindow.render.model;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.DefaultVertexFormats;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.Mesh;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexBuffer;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderProgram;
import net.quepierts.endfieldpanorama.earlywindow.skeleton.Skeleton;
import net.quepierts.endfieldpanorama.earlywindow.scene.Transform;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractModel implements Resource {

    public static final int MASK_FRONT  = 1;
    public static final int MASK_BACK   = 2;
    public static final int MASK_LEFT   = 4;
    public static final int MASK_RIGHT  = 8;
    public static final int MASK_TOP    = 16;
    public static final int MASK_BOTTOM = 32;

    public static final int MASK_ALL    = 63;
    public static final int MASK_UPPER  = 31;
    public static final int MASK_LOWER  = 47;

    @Getter
    private final Transform transform   = new Transform();

    @Getter
    private final Skeleton skeleton;

    private final Mesh mesh;
    private final VertexBuffer buffer;

    protected AbstractModel(
            @NotNull Skeleton skeleton
    ) {
        this.skeleton = skeleton;
        this.mesh = mesh(skeleton);
        this.buffer = new VertexBuffer();
        this.buffer.upload(this.mesh);
    }

    public void draw() {
        this.buffer.draw();
    }

    public void draw(@NotNull ShaderProgram program) {
        program.bind();
        program.upload();

        this.draw();

        program.unbind();
    }

    @Override
    public void free() {
        this.buffer.free();
        this.mesh.free();
    }

    @Override
    public void bind() {
        this.buffer.bind();
    }

    @Override
    public void unbind() {
        this.buffer.unbind();
    }

    protected static Mesh mesh(@NotNull Skeleton skeleton) {
        var builder = Mesh.builder(DefaultVertexFormats.CHARACTER, 1024);

        for (var bone : skeleton) {
            var id          = bone.getId();

            for (var box : bone.getBoxes()) {
                AbstractModel.bone(
                        builder,
                        box.getX(),
                        box.getY(),
                        box.getZ(),
                        box.getDx(),
                        box.getDy(),
                        box.getDz(),
                        box.getInflate(),
                        box.getU(),
                        box.getV(),
                        64,
                        id
                );
            }
        }

        return builder.build();
    }

    protected static void bone(
            Mesh.Builder builder,
            float px,           float py,           float pz,
            float dx,           float dy,           float dz,
            float inflate,
            float uOffset,      float vOffset,
            int   textureSize,  int   group
    ) {

        float x0 = px - inflate;
        float y0 = py - inflate;
        float z0 = pz - inflate;
        float x1 = px + dx + inflate;
        float y1 = py + dy + inflate;
        float z1 = pz + dz + inflate;

        // front
        quad(
                builder,
                x1, y0, z1,
                x1, y1, z1,
                x0, y1, z1,
                x0, y0, z1,
                uOffset + dz * 2 + dx * 2, vOffset + dz,
                -dx, dy,
                textureSize, group
        );

        // back
        quad(
                builder,
                x0, y0, z0,
                x0, y1, z0,
                x1, y1, z0,
                x1, y0, z0,
                uOffset + dz + dx, vOffset + dz,
                -dx, dy,
                textureSize, group
        );

        // left
        quad(
                builder,
                x1, y0, z0,
                x1, y1, z0,
                x1, y1, z1,
                x1, y0, z1,
                uOffset + dz, vOffset + dz,
                -dz, dy,
                textureSize, group
        );

        // right
        quad(
                builder,
                x0, y0, z1,
                x0, y1, z1,
                x0, y1, z0,
                x0, y0, z0,
                uOffset + dz * 2 + dx, vOffset + dz,
                -dz, dy,
                textureSize, group
        );

        // top
        quad(
                builder,
                x0, y1, z0,
                x0, y1, z1,
                x1, y1, z1,
                x1, y1, z0,
                uOffset + dz, vOffset,
                dx, dz,
                textureSize, group
        );

        // bottom
        quad(
                builder,
                x0, y0, z0,
                x1, y0, z0,
                x1, y0, z1,
                x0, y0, z1,
                uOffset + dz + dx, vOffset,
                dx, dz,
                textureSize, group
        );

    }

    protected static void quad(
            Mesh.Builder builder,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float u,  float v,
            float w,  float h,
            int textureSize,
            int group
    ) {

        float u0 = uv(u, textureSize);
        float v0 = uv(v, textureSize);
        float u1 = uv(u + w, textureSize);
        float v1 = uv(v + h, textureSize);

        int idx = builder.getVertexCount();

        builder.floats(x0, y0, z0, u0, v1);
        builder.intValue(group);
        builder.countVertex();

        builder.floats(x1, y1, z1, u0, v0);
        builder.intValue(group);
        builder.countVertex();

        builder.floats(x2, y2, z2, u1, v0);
        builder.intValue(group);
        builder.countVertex();

        builder.floats(x3, y3, z3, u1, v1);
        builder.intValue(group);
        builder.countVertex();

        builder.index(idx);
        builder.index(idx + 1);
        builder.index(idx + 2);
        builder.index(idx + 2);
        builder.index(idx + 3);
        builder.index(idx);
    }

    protected static float uv(float px, int textureSize) {
        return px / (float) textureSize;
    }
}
