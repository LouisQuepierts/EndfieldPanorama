package net.quepierts.endfieldpanorama.earlywindow.scene;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@Getter
public final class Transform {

    private float x,    y,      z;  // position
    private float rx,   ry,     rz; // rotation
    private float sx,   sy,     sz; // scale
    private float px,   py,     pz; // pivot

    private boolean hasTranslated;
    private boolean hasRotated;
    private boolean hasScaled;
    private boolean hasPivoted;

    public Transform() {
        this.identity();
    }

    public Transform(
            float x,    float y,    float z,
            float rx,   float ry,   float rz,
            float sx,   float sy,   float sz,
            float px,   float py,   float pz
    ) {
        this.setPosition(x, y, z);
        this.setRotation(rx, ry, rz);
        this.setScale(sx, sy, sz);
        this.setPivot(px, py, pz);
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.hasTranslated = x != 0.0f || y != 0.0f || z != 0.0f;
    }

    public void setRotation(float rx, float ry, float rz) {
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;

        this.hasRotated = rx != 0.0f || ry != 0.0f || rz != 0.0f;
    }

    public void setScale(float sx, float sy, float sz) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;

        this.hasScaled = sx != 1.0f || sy != 1.0f || sz != 1.0f;
    }

    public void setPivot(float px, float py, float pz) {
        this.px = px;
        this.py = py;
        this.pz = pz;

        this.hasPivoted = px != 0.0f || py != 0.0f || pz != 0.0f;
    }

    public void set(@NotNull Transform other) {
        this.x  = other.x;
        this.y  = other.y;
        this.z  = other.z;
        this.rx = other.rx;
        this.ry = other.ry;
        this.rz = other.rz;
        this.sx = other.sx;
        this.sy = other.sy;
        this.sz = other.sz;
        this.px = other.px;
        this.py = other.py;
        this.pz = other.pz;

        this.hasTranslated = other.hasTranslated;
        this.hasRotated = other.hasRotated;
        this.hasScaled = other.hasScaled;
        this.hasPivoted = other.hasPivoted;
    }

    public void translate(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;

        this.hasTranslated = x != 0.0f || y != 0.0f || z != 0.0f;
    }

    public void rotate(float rx, float ry, float rz) {
        this.rx += rx;
        this.ry += ry;
        this.rz += rz;

        this.hasRotated = rx != 0.0f || ry != 0.0f || rz != 0.0f;
    }

    public @NotNull Transform copy() {
        var transform = new Transform();
        transform.set(this);
        return transform;
    }

    public void identity() {
        this.x  = 0.0f;
        this.y  = 0.0f;
        this.z  = 0.0f;
        this.rx = 0.0f;
        this.ry = 0.0f;
        this.rz = 0.0f;
        this.sx = 1.0f;
        this.sy = 1.0f;
        this.sz = 1.0f;
        this.px = 0.0f;
        this.py = 0.0f;
        this.pz = 0.0f;

        this.hasTranslated  = false;
        this.hasRotated     = false;
        this.hasScaled      = false;
        this.hasPivoted     = false;
    }

    public void getMatrix(Matrix4f matrix) {

        matrix.identity();

        if (this.hasTranslated) {
            matrix.translate(x, y, z);
        }

        if (this.hasPivoted) {
            matrix.translate(px, py, pz);
        }

        if (this.hasRotated) {
            matrix.rotateZYX(
                    rz * Mth.DEG_TO_RAD,
                    ry * Mth.DEG_TO_RAD,
                    rx * Mth.DEG_TO_RAD
            );
        }

        if (this.hasScaled) {
            matrix.scale(sx, sy, sz);
        }

        if (this.hasPivoted) {
            matrix.translate(-px, -py, -pz);
        }

    }
}
