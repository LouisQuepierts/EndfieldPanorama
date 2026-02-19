package net.quepierts.endfieldpanorama.earlywindow.skeleton;

import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SkeletonUbo;
import net.quepierts.endfieldpanorama.earlywindow.scene.Transform;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class Skeleton implements Iterable<Bone> {

    private final Bone[]    bones;
    private final float[]   cache;
    private final Matrix4f[] c;

    private Skeleton(List<Bone> bones) {
        this.bones = bones.toArray(Bone[]::new);
        this.cache = new float[16 * bones.size()];
        this.c = new Matrix4f[bones.size()];
        for (int i = 0; i < this.c.length; i++) {
            this.c[i] = new Matrix4f();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public @NotNull SkeletonUbo createUbo() {
        return new SkeletonUbo(bones.length);
    }

    public void apply(@NotNull SkeletonUbo ubo) {

        if (ubo.getLength() < bones.length) {
            throw new IllegalArgumentException("SkeletonUbo length is too small");
        }

        for (int i = 0; i < this.bones.length; i++) {
            this.bones[i].getTransform().getMatrix(c[i]);
        }

        ubo.put(this.c);

        /*final Matrix4f  matrix4f    = new Matrix4f();

        for (
                int i = 0, offset = 0;
                i < bones.length;
                i++, offset += 16
        ) {
            var bone = bones[i];
            bone.getTransform().getMatrix(matrix4f);
            matrix4f.get(this.cache, offset);
        }

        ubo.put(this.cache);*/

    }

    @Override
    public @NotNull Iterator<Bone> iterator() {
        return Arrays.stream(bones).iterator();
    }

    public int size() {
        return bones.length;
    }

    public static final class Builder {

        private final   List<Box>   boxes           = new ArrayList<>();
        private final   List<Bone>  bones           = new ArrayList<>();
        private final Transform transform       = new Transform();

        private         String      current;
        private         Box         box;

        public Builder begin(String name, float pivotX, float pivotY, float pivotZ) {
            this.transform.identity();
            transform.setPivot(pivotX, pivotY, pivotZ);
            this.current = name;

            box = null;
            boxes.clear();

            return this;
        }

        public Builder position(float x, float y, float z) {
            if (current == null) {
                throw new IllegalStateException("Cannot set position without a bone");
            }

            if (box != null) {
                box.position(x, y, z);
            } else {
                transform.setPosition(x, y, z);
            }
            return this;
        }

        public Builder rotation(float x, float y, float z) {
            if (current == null) {
                throw new IllegalStateException("Cannot set position without a bone");
            }

            transform.setRotation(x, y, z);

            return this;
        }

        public Builder scale(float x, float y, float z) {
            if (current == null) {
                throw new IllegalStateException("Cannot set position without a bone");
            }

            if (box != null) {
                box.scale(x, y, z);
            } else {
                transform.setScale(x, y, z);
            }

            return this;
        }

        public Builder box(float inflate) {
            box = new Box()
                    .position(
                            transform.getX(),
                            transform.getY(),
                            transform.getZ()
                    )
                    .inflate(inflate);
            boxes.add(box);
            return this;
        }

        public Builder uv(int u, int v) {
            if (box == null) {
                throw new IllegalStateException("Cannot set uv without a box");
            }

            box.uv(u, v);
            return this;
        }

        public Builder end() {
            if (box != null) {
                box = null;
            } else {
                var bone = new Bone(
                        bones.size(),
                        current,
                        transform,
                        this.boxes.toArray(Box[]::new)
                );
                current = null;
                bones.add(bone);
            }
            return this;
        }

        public Skeleton build() {
            return new Skeleton(bones);
        }
    }

}
