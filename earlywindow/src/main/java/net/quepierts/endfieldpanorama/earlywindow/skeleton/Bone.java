package net.quepierts.endfieldpanorama.earlywindow.skeleton;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.scene.Transform;

@Getter
public final class Bone {

    private final int       id;
    private final String    name;
    private final Transform defaultTransform;
    private final Transform transform;

    private final Box[]     boxes;

    public Bone(
            int         id,
            String      name,
            Transform   defaultTransform,
            Box[]       boxes
    ) {
        this.id                 = id;
        this.name               = name;
        this.defaultTransform   = defaultTransform.copy();
        this.transform          = defaultTransform.copy();
        this.boxes              = boxes;
    }

}
