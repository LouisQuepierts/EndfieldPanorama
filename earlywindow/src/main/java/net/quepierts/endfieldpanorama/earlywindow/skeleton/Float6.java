package net.quepierts.endfieldpanorama.earlywindow.skeleton;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Float6 {

    private float x0, y0, z0;
    private float x1, y1, z1;

    public void v0(float x, float y, float z) {
        this.x0 = x;
        this.y0 = y;
        this.z0 = z;
    }

    public void v1(float x, float y, float z) {
        this.x1 = x;
        this.y1 = y;
        this.z1 = z;
    }

}
