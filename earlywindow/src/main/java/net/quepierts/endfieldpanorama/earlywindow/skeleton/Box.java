package net.quepierts.endfieldpanorama.earlywindow.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class Box {

    private float x, y, z;
    private float dx, dy, dz;
    private float rx, ry, rz;
    private float u, v;

    private float inflate;

    public Box position(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Box scale(float width, float height, float depth) {
        this.dx = width;
        this.dy = height;
        this.dz = depth;
        return this;
    }

    public Box uv(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public Box inflate(float inflate) {
        this.inflate = inflate;
        return this;
    }
    
}
