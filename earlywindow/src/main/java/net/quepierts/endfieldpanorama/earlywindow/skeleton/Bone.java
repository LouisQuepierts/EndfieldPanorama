package net.quepierts.endfieldpanorama.earlywindow.skeleton;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.scene.Transform;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class Bone {

    private final int       id;
    private final int       parent;
    private final String    name;
    private final Transform defaultTransform;
    private final Transform transform;

    private final Box[]     boxes;

    public Bone(
            int         id,
            int         parent,
            String      name,
            Transform   defaultTransform,
            Box[]       boxes
    ) {
        this.id                 = id;
        this.parent             = parent;
        this.name               = name;
        this.defaultTransform   = defaultTransform.copy();
        this.transform          = defaultTransform.copy();
        this.boxes              = boxes;
    }

    public boolean hasParent() {
        return parent != -1;
    }

    public static Builder builder(int id, int parent, String name, float px, float py, float pz) {
        return new Builder(id, parent, name, px, py, pz);
    }

    public static final class Builder {

        @Getter
        private final Transform transform = new Transform();
        private final List<Box> boxes = new ArrayList<>();

        @Getter
        private final int id;
        private final int parent;
        private final String name;

        private Builder(int id, int parent, String name, float px, float py, float pz) {
            this.id = id;
            this.name = name;
            this.parent = parent;
            transform.setPivot(px, py, pz);
        }

        public Builder position(float x, float y, float z) {
            transform.setPosition(x, y, z);
            return this;
        }

        public Builder rotation(float rx, float ry, float rz) {
            transform.setRotation(rx, ry, rz);
            return this;
        }

        public Builder scale(float sx, float sy, float sz) {
            transform.setScale(sx, sy, sz);
            return this;
        }

        public Builder addBox(@NotNull Box box) {
            boxes.add(box);
            return this;
        }

        public Bone build() {
            return new Bone(
                    id,
                    parent,
                    name,
                    transform.copy(),
                    boxes.toArray(Box[]::new)
            );
        }

    }

}
