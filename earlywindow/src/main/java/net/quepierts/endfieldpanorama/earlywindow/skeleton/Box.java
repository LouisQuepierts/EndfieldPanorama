package net.quepierts.endfieldpanorama.earlywindow.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class Box {

    public static final int MASK_FRONT  = 1;
    public static final int MASK_BACK   = 2;
    public static final int MASK_LEFT   = 4;
    public static final int MASK_RIGHT  = 8;
    public static final int MASK_TOP    = 16;
    public static final int MASK_BOTTOM = 32;

    public static final int MASK_ALL    = 63;

    private final Float6 cube           = new Float6();
    private final Float6 inflate        = new Float6();

    private int mask                    = MASK_ALL;
    private int flag;

    private float rx, ry, rz;
    private float u, v;

    public Box position(float x, float y, float z) {
        this.cube.v0(x, y, z);
        return this;
    }

    public Box scale(float width, float height, float depth) {
        this.cube.v1(width, height, depth);
        return this;
    }

    public Box uv(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public Box inflate(float inflate) {
        this.inflate.v0(inflate, inflate, inflate);
        this.inflate.v1(inflate, inflate, inflate);
        return this;
    }

    public Box inflate(float xp, float yp, float zp, float xn, float yn, float zn) {
        this.inflate.v0(xp, yp, zp);
        this.inflate.v1(xn, yn, zn);
        return this;
    }

    public Box flag(int data) {
        this.flag = data;
        return this;
    }

    public Box applyMask(int mask) {
        this.mask |= mask;
        return this;
    }

    public Box removeMask(int mask) {
        this.mask &= ~mask;
        return this;
    }

    public boolean hasMask(int mask) {
        return (this.mask & mask) != 0;
    }
    
}
