package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.model.AbstractModel;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderProgram;
import net.quepierts.endfieldpanorama.earlywindow.scene.Transform;
import org.jetbrains.annotations.NotNull;

@Getter
public class ModelRenderer implements Resource {

    @Getter
    private final Transform transform   = new Transform();

    private final AbstractModel model;
    private final ShaderProgram shader;

    public ModelRenderer(@NotNull AbstractModel model, @NotNull ShaderProgram shader) {
        this.model      = model;
        this.shader     = shader;
    }

    public void render() {
        this.model      .draw(this.shader);
    }

    @Override
    public void free() {
        this.model      .free();
        this.shader     .free();
    }
}
