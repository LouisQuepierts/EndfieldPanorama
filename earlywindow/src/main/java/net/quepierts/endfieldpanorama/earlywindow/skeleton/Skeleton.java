package net.quepierts.endfieldpanorama.earlywindow.skeleton;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.quepierts.endfieldpanorama.earlywindow.EarlyResourceLoader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SkeletonUbo;
import net.quepierts.endfieldpanorama.earlywindow.scene.Transform;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public final class Skeleton implements Iterable<Bone> {

    private static final Gson GSON  = new Gson();

    private final Bone[]        bones;
    private final Bone[]        updates;
    private final Matrix4f[]    matrices;

    private Skeleton(Bone[] bones) {
        this.bones      = bones;
        this.updates    = createUpdateArray(bones);
        this.matrices   = new Matrix4f[bones.length];

        for (int i = 0; i < this.matrices.length; i++) {
            this.matrices[i] = new Matrix4f();
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

        var tmp         = new Matrix4f();

        for (var bone : updates) {
            var id      = bone.getId();

            if (!bone.hasParent()) {
                bone.getTransform().getMatrix(matrices[id]);
                continue;
            }

            var parent  = bone.getParent();
            bone.getTransform().getMatrix(tmp);

            matrices[id]
                    .set(matrices[parent])
                    .mul(tmp);
        }

        ubo.put(matrices);

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

    @SuppressWarnings("unchecked")
    private static Bone[] createUpdateArray(final Bone[] bones) {
        List<Bone>[] children   = new List[bones.length + 1];

        for (int i = 0; i < children.length; i++) {
            children[i]         = new ArrayList<>();
        }

        for (var bone : bones) {
            children[bone.getParent() + 1].add(bone);
        }

        var ptr                 = 0;
        var result              = new Bone[bones.length];

        for (var list : children) {
            for (var child : list) {
                result[ptr++] = child;
            }
        }

        return result;
    }

    public static Skeleton fromResource(String name) {
        var path = "models/" + name + ".json";

        try (var source = EarlyResourceLoader.loadFileOrCreate(path)) {
            var reader = new JsonReader(new InputStreamReader(source));
            return fromJson(GSON.fromJson(reader, JsonObject.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Skeleton fromJson(JsonObject json) {
        var entries     = json.entrySet();
        var bones       = new Bone[entries.size()];
        var parents     = new HashMap<String, Integer>();

        var ptr   = 0;

        for (var entry : json.entrySet()) {
            var name        = entry.getKey();
            var content     = entry.getValue();

            if (!content.isJsonObject()) {
                throw new IllegalArgumentException("Invalid bone content");
            }

            var object      = content.getAsJsonObject();

            if (object.has("children")) {
                var children = object.getAsJsonArray("children");

                for (var child : children) {
                    parents.put(child.getAsString(), ptr);
                }
            }

            var parent      = parents.getOrDefault(name, -1);
            var pivot       = object.getAsJsonArray("pivot");

            var px          = pivot.get(0).getAsFloat();
            var py          = pivot.get(1).getAsFloat();
            var pz          = pivot.get(2).getAsFloat();

            var builder     = Bone.builder(
                                ptr, parent,
                                name,
                                px, py, pz
            );

            tryApply(object, "position", builder::position);
            tryApply(object, "rotation", builder::rotation);
            tryApply(object, "scale", builder::scale);

            var boxes       = object.getAsJsonArray("boxes");
            addBoxes(builder, boxes);

            bones[ptr]      = builder.build();
            ptr += 1;
        }

        return new Skeleton(bones);
    }

    private static void addBoxes(
            @NotNull Bone.Builder builder,
            @NotNull JsonArray boxes
    ) {
        var transform   = builder.getTransform();

        for (var json : boxes) {
            var box     = new Box();
            box.position(transform.getX(), transform.getY(), transform.getZ());

            var object  = json.getAsJsonObject();

            if (object.has("inflate")) {
                var inflate = object.get("inflate");

                if (inflate.isJsonPrimitive()) {
                    box.inflate(inflate.getAsFloat());
                } else {
                    var array   = inflate.getAsJsonArray();

                    if (array.size() != 6) {
                        throw new IllegalArgumentException("Box inflate array size must be 6");
                    }

                    box.inflate(
                            array.get(0).getAsFloat(),
                            array.get(1).getAsFloat(),
                            array.get(2).getAsFloat(),
                            array.get(3).getAsFloat(),
                            array.get(4).getAsFloat(),
                            array.get(5).getAsFloat()
                    );
                }
            }

            var uv     = object.getAsJsonArray("uv");
            box.uv(
                    uv.get(0).getAsFloat(),
                    uv.get(1).getAsFloat()
            );

            tryApply(object, "position", box::position);
            tryApply(object, "scale", box::scale);

            if (object.has("mask")) {
                var maskObject  = object.getAsJsonObject("mask");
                for (var maskEntry : maskObject.entrySet()) {
                    var maskName  = maskEntry.getKey();
                    var maskValue = maskEntry.getValue().getAsBoolean();

                    if (maskValue) {
                        switch (maskName) {
                            case "front"  -> box.applyMask(Box.MASK_FRONT);
                            case "back"   -> box.applyMask(Box.MASK_BACK);
                            case "left"   -> box.applyMask(Box.MASK_LEFT);
                            case "right"  -> box.applyMask(Box.MASK_RIGHT);
                            case "top"    -> box.applyMask(Box.MASK_TOP);
                            case "bottom" -> box.applyMask(Box.MASK_BOTTOM);
                        }
                    } else {
                        switch (maskName) {
                            case "front"  -> box.removeMask(Box.MASK_FRONT);
                            case "back"   -> box.removeMask(Box.MASK_BACK);
                            case "left"   -> box.removeMask(Box.MASK_LEFT);
                            case "right"  -> box.removeMask(Box.MASK_RIGHT);
                            case "top"    -> box.removeMask(Box.MASK_TOP);
                            case "bottom" -> box.removeMask(Box.MASK_BOTTOM);
                        }
                    }
                }
            }

            if (object.has("flag")) {
                box.flag(object.get("flag").getAsInt());
            }

            builder.addBox(box);
        }
    }

    private static void tryApply(
            @NotNull JsonObject root,
            @NotNull String     name,
            @NotNull Consumer3f consumer
    ) {
        if (!root.has(name)) {
            return;
        }

        var array   = root.getAsJsonArray(name);
        var v0      = array.get(0).getAsFloat();
        var v1      = array.get(1).getAsFloat();
        var v2      = array.get(2).getAsFloat();

        consumer.accept(v0, v1, v2);
    }

    @FunctionalInterface
    private interface Consumer3f {
        void accept(float x, float y, float z);
    }

    public static final class Builder {

        private final   List<Bone.Builder>  builders        = new ArrayList<>();
        private final   List<Bone>          bones           = new ArrayList<>();

        private         Bone.Builder        current;
        private         Box                 box;

        public Builder begin(String name, float pivotX, float pivotY, float pivotZ) {
            var id          = bones.size();
            var parent      = current == null ? -1 : current.getId();
            var builder     = Bone.builder(id, parent, name, pivotX, pivotY, pivotZ);

            this.current    = builder;
            builders.add(builder);

            box = null;

            return this;
        }

        public Builder position(float x, float y, float z) {
            if (current == null) {
                throw new IllegalStateException("Cannot set position without a bone");
            }

            if (box != null) {
                box.position(x, y, z);
            } else {
                current.position(x, y, z);
            }
            return this;
        }

        public Builder rotation(float x, float y, float z) {
            if (current == null) {
                throw new IllegalStateException("Cannot set position without a bone");
            }

            current.rotation(x, y, z);

            return this;
        }

        public Builder scale(float x, float y, float z) {
            if (current == null) {
                throw new IllegalStateException("Cannot set position without a bone");
            }

            if (box != null) {
                box.scale(x, y, z);
            } else {
                current.scale(x, y, z);
            }

            return this;
        }

        public Builder box(float inflate) {
            var transform = current.getTransform();
            box = new Box()
                    .position(
                            transform.getX(),
                            transform.getY(),
                            transform.getZ()
                    )
                    .inflate(inflate);

            current.addBox(box);
            return this;
        }

        public Builder box(float ixp, float iyp, float izp, float ixn, float iyn, float izn) {
            var transform = current.getTransform();
            box = new Box()
                    .position(
                            transform.getX(),
                            transform.getY(),
                            transform.getZ()
                    )
                    .inflate(ixp, iyp, izp, ixn, iyn, izn);

            current.addBox(box);
            return this;
        }

        public Builder mask(int mask, boolean apply) {
            if (box == null) {
                throw new IllegalStateException("Cannot set mask without a box");
            }

            if (apply) {
                box.applyMask(mask);
            } else {
                box.removeMask(mask);
            }
            return this;
        }

        public Builder flag(int flag) {
            if (box == null) {
                throw new IllegalStateException("Cannot set flag without a box");
            }

            box.flag(flag);
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
                var bone = current.build();
                current = builders.isEmpty() ? null : builders.removeLast();
                bones.add(bone);
            }
            return this;
        }

        public Skeleton build() {
            if (!builders.isEmpty()) {
                throw new IllegalStateException("Not all bones are ended");
            }

            return new Skeleton(bones.toArray(Bone[]::new));
        }
    }

}
