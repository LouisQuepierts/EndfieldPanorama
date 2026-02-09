package net.quepierts.endfieldpanorama.earlywindow.render.pipeline;

import lombok.Getter;
import org.lwjgl.opengl.GL31;

@Getter
public final class FrameBuffer {

    private final float[] clear = { 0.0f, 0.0f, 0.0f, 0.0f };
    private final int clearMask;
    private final boolean useDepth;

    private int framebuffer;
    private int texture;
    private int depth;

    private int width;
    private int height;


    public FrameBuffer(boolean useDepth) {
        this.useDepth       = useDepth;
        this.clearMask      = useDepth
                                ? GL31.GL_COLOR_BUFFER_BIT | GL31.GL_DEPTH_BUFFER_BIT
                                : GL31.GL_COLOR_BUFFER_BIT;

        this.framebuffer    = -1;
        this.texture        = -1;
        this.depth          = -1;
        this.width          = -1;
        this.height         = -1;
    }

    public void resize(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }

        this.free();

        this.framebuffer = GL31.glGenFramebuffers();
        GL31.glBindFramebuffer(GL31.GL_FRAMEBUFFER, this.framebuffer);

        this.texture = GL31.glGenTextures();
        GL31.glBindTexture(GL31.GL_TEXTURE_2D, this.texture);
        GL31.glTexImage2D(GL31.GL_TEXTURE_2D, 0, GL31.GL_RGBA8, width, height, 0, GL31.GL_RGBA, GL31.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_NEAREST);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_NEAREST);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_S, GL31.GL_CLAMP_TO_EDGE);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_T, GL31.GL_CLAMP_TO_EDGE);
        GL31.glFramebufferTexture2D(GL31.GL_FRAMEBUFFER, GL31.GL_COLOR_ATTACHMENT0, GL31.GL_TEXTURE_2D, this.texture, 0);

        if (this.useDepth) {
            this.depth = GL31.glGenRenderbuffers();
            GL31.glBindRenderbuffer(GL31.GL_RENDERBUFFER, this.depth);
            GL31.glRenderbufferStorage(GL31.GL_RENDERBUFFER, GL31.GL_DEPTH_COMPONENT, width, height);
            GL31.glFramebufferRenderbuffer(GL31.GL_FRAMEBUFFER, GL31.GL_DEPTH_ATTACHMENT, GL31.GL_RENDERBUFFER, this.depth);
        }

        GL31.glBindFramebuffer(GL31.GL_FRAMEBUFFER, 0);

        this.width = width;
        this.height = height;
    }

    public void bind() {
        GL31.glBindFramebuffer(GL31.GL_FRAMEBUFFER, this.framebuffer);
    }

    public void unbind() {
        GL31.glBindFramebuffer(GL31.GL_FRAMEBUFFER, 0);
    }

    public void clearColor(float r, float g, float b, float a) {
        this.clear[0] = r;
        this.clear[1] = g;
        this.clear[2] = b;
        this.clear[3] = a;
    }

    public void clear() {
        this.bind();

        GL31.glClearColor(this.clear[0], this.clear[1], this.clear[2], this.clear[3]);
        GL31.glClearDepth(1.0);
        GL31.glClear(this.clearMask);

        this.unbind();
    }

    public void free() {
        if (this.framebuffer == -1) {
            return;
        }

        GL31.glDeleteTextures(this.texture);
        if (this.useDepth) {
            GL31.glDeleteRenderbuffers(this.depth);
        }
        GL31.glDeleteFramebuffers(this.framebuffer);
    }

    public void draw(int width, int height) {
        GL31.glBindFramebuffer(GL31.GL_DRAW_FRAMEBUFFER, 0);
        GL31.glBindFramebuffer(GL31.GL_READ_FRAMEBUFFER, this.framebuffer);

//        GL31.glClearColor(this.clear[0], this.clear[1], this.clear[2], 1.0f);
//        GL31.glClear(GL31.GL_COLOR_BUFFER_BIT);

        GL31.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL31.GL_COLOR_BUFFER_BIT, GL31.GL_NEAREST);
        GL31.glBindFramebuffer(GL31.GL_FRAMEBUFFER, 0);
    }
}
