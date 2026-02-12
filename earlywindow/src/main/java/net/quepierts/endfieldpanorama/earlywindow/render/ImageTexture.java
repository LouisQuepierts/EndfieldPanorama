package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.EarlyResourceLoader;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import org.lwjgl.opengl.GL31;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public final class ImageTexture extends BaseTexture implements Resource {

    private static int bind = 0;

    @Getter
    private final int textureId;

    @Getter
    private int width;

    @Getter
    private int height;

    public static ImageTexture fromResource(String path, int glFilter, int glWrap) {
        return fromByteArray(EarlyResourceLoader.loadByteArray("textures/" + path), glFilter, glWrap);
    }

    public static ImageTexture fromByteArray(byte[] bytes, int glFilter, int glWrap) {
        var texture     = new ImageTexture();

        texture.bind();
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, glFilter);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, glFilter);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_S, glWrap);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_T, glWrap);

        texture.upload(bytes);
        texture.unbind();
        return texture;
    }

    public ImageTexture() {
        this.textureId = GL31.glGenTextures();
    }

    public void setFilter(int glFilter) {
        GL31.glBindTexture(GL31.GL_TEXTURE_2D, textureId);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, glFilter);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, glFilter);
        GL31.glBindTexture(GL31.GL_TEXTURE_2D, 0);
    }

    public void setWrap(int glWrap) {
        GL31.glBindTexture(GL31.GL_TEXTURE_2D, textureId);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_S, glWrap);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_T, glWrap);
        GL31.glBindTexture(GL31.GL_TEXTURE_2D, 0);
    }

    public void upload(byte[] raw) {
        var tmp         = MemoryUtil.memAlloc(raw.length);
        tmp.put(raw);
        tmp.flip();

        var width       = new int[1];
        var height      = new int[1];
        var channels    = new int[1];

        var buffer      = STBImage.stbi_load_from_memory(tmp, width, height, channels, 4);

        if (buffer == null) {
            var reason = STBImage.stbi_failure_reason();
            throw new RuntimeException("Failed to load texture: " + reason);
        }

        this.upload(buffer, width[0], height[0]);
        MemoryUtil.memFree(tmp);
        STBImage.stbi_image_free(buffer);
    }

    public void upload(ByteBuffer buffer, int width, int height) {
        this.bind();
        GL31.glTexImage2D(
                GL31.GL_TEXTURE_2D,
                0,
                GL31.GL_RGBA,
                width, height,
                0,
                GL31.GL_RGBA,
                GL31.GL_UNSIGNED_BYTE,
                buffer
        );
        this.unbind();

        this.width = width;
        this.height = height;
    }

    @Override
    public void free() {
        if (this.textureId != 0) {
            this.unbind();
            GL31.glDeleteTextures(textureId);
        }
    }

    @Override
    public void bind() {
        if (bind != textureId) {
            bind = textureId;
            GL31.glBindTexture(GL31.GL_TEXTURE_2D, textureId);
        }
    }

    @Override
    public void unbind() {
        if (bind == textureId) {
            bind = 0;
            GL31.glBindTexture(GL31.GL_TEXTURE_2D, 0);
        }
    }
}
