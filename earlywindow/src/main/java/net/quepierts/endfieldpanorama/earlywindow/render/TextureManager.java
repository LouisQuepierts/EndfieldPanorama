package net.quepierts.endfieldpanorama.earlywindow.render;

public final class TextureManager extends Manager<ImageTexture> {

    public ImageTexture create(String name, int glFilter, int glWrap) {
        var texture     = this.get(name);
        if (texture     != null) {
            return texture;
        }

        var instance    = ImageTexture.fromResource(name, glFilter, glWrap);
        this.resources  .put(name, instance);
        return instance;
    }

}
