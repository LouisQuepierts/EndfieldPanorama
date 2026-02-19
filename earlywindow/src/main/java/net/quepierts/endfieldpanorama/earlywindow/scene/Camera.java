package net.quepierts.endfieldpanorama.earlywindow.scene;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class Camera {

    private static final float SCALE = 1f / 16f;

    private float x, y, z;
    private float roll, pitch, yaw;

    public void setPosition(float x, float y, float z) {
        this.x = x * SCALE;
        this.y = y * SCALE;
        this.z = z * SCALE;
    }

    public void setRotation(float pitch, float yaw, float roll) {
        this.roll   = roll  *  Mth.DEG_TO_RAD;
        this.pitch  = pitch *  Mth.DEG_TO_RAD;
        this.yaw    = yaw   *  Mth.DEG_TO_RAD;
    }

    public void translate(float x, float y, float z) {
        this.x += x * SCALE;
        this.y += y * SCALE;
        this.z += z * SCALE;
    }

    public void rotate(float pitch, float yaw, float roll) {
        this.roll   += roll  *  Mth.DEG_TO_RAD;
        this.pitch  += pitch *  Mth.DEG_TO_RAD;
        this.yaw    += yaw   *  Mth.DEG_TO_RAD;
    }

    public void getViewMatrix(@NotNull Matrix4f output) {
        output.identity()
                .rotateY(-yaw)
                .rotateX(pitch)
                .rotateZ(-roll)
                .translate(-x, -y, -z);
    }

    public void getInverseViewMatrix(@NotNull Matrix4f output) {
        output.identity()
                .translate(-x, y, -z)
                .rotateZ(-roll)
                .rotateX(pitch)
                .rotateY(yaw);
    }
}
