package net.quepierts.endfieldpanorama.earlywindow.render.procedure;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.quepierts.endfieldpanorama.earlywindow.ResourceManager;
import net.quepierts.endfieldpanorama.earlywindow.render.BaseTexture;
import net.quepierts.endfieldpanorama.earlywindow.render.Graphics;
import net.quepierts.endfieldpanorama.earlywindow.render.ImageTexture;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.FrameBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenderProcedure implements RenderContext {

    private final Map<String, Object>       signals         = new HashMap<>();

    private final Map<String, FrameBuffer>  name2buffer;
    private final Map<String, BaseTexture>  name2texture;
    private final FrameBuffer[]             buffers;
    private final BaseTexture[]             textures;
    private final RenderPass[]              passes;

    private final Graphics                  graphics;

    private @NotNull Runnable               bindMainTarget  = () -> {};

    public void render(
            @NotNull Runnable bindMainTarget,
            float delta,
            float time
    ) {

        this.bindMainTarget = bindMainTarget;

        for (var pass : this.passes) {
            pass.render(this, delta, time);
        }
    }

    public void resize(int width, int height) {
        for (var buffer : this.buffers) {
            buffer.resize(width, height);
        }

        for (var pass : this.passes) {
            pass.resize(width, height);
        }
    }

    @Override
    public void signal(String signal, Object value, boolean enable) {
        if (enable) {
            this.signals.put(signal, value);
        } else {
            this.signals.remove(signal);
        }
    }

    @Override
    public void bindFrameBuffer(String name) {
        if ("main".equals(name)) {
            this.bindMainTarget.run();
        } else {
            var buffer = this.name2buffer.get(name);
            buffer.bind();
        }
    }

    @Override
    public void clearFrameBuffer(String name) {
        var buffer = this.name2buffer.get(name);
        buffer.clear();
    }

    @Override
    public void bindTexture(String name, int glTextureSlot) {
        var texture = this.name2texture.get(name);
        texture.bind(glTextureSlot);
    }

    @Override
    public void unbindTexture(String name, int glTextureSlot) {
        var texture = this.name2texture.get(name);
        texture.unbind(glTextureSlot);
    }

    @Override
    public boolean hasSignal(String name) {
        return this.signals.containsKey(name);
    }

    @Override
    public @Nullable Object getSignal(String name) {
        return this.signals.get(name);
    }

    public void duplicate(RenderProcedure other) {
        for (int i = 0; i < passes.length; i++) {
            this.passes[i].duplicate(other.passes[i]);
        }
    }

    public static final class Builder {

        private final List<Supplier<RenderPass>>    passes          = new ArrayList<>();

        private final Set<BufferDefinition>         buffers         = new HashSet<>();
        private final Set<TextureDefinition>        textures        = new HashSet<>();

        private final Set<BufferLink>               bufferLinks     = new HashSet<>();
        private final Set<TextureLink>              textureLinks    = new HashSet<>();

        public Builder registerPass(Supplier<RenderPass> pass) {
            this.passes.add(pass);
            return this;
        }

        public Builder registerBuffer(String name, boolean useDepth) {
            var definition  = new BufferDefinition(name, useDepth, 0.0f, 0.0f, 0.0f, 0.0f);
            this.buffers.add(definition);
            return this;
        }

        public Builder registerBuffer(
                String name, boolean useDepth,
                float clearR, float clearG, float clearB, float clearA
        ) {
            var definition  = new BufferDefinition(name, useDepth, clearR, clearG, clearB, clearA);
            this.buffers.add(definition);
            return this;
        }

        public Builder registerTexture(String name, int glFilter, int glWrap) {
            var definition  = new TextureDefinition(name, glFilter, glWrap);
            this.textures.add(definition);
            return this;
        }

        public Builder linkBuffer(String name, FrameBuffer buffer) {
            var link        = new BufferLink(name, buffer);
            this.bufferLinks.add(link);
            return this;
        }

        public Builder linkTexture(String name, BaseTexture texture) {
            var link        = new TextureLink(name, texture);
            this.textureLinks.add(link);
            return this;
        }

        public RenderProcedure build(
                @NotNull ResourceManager    manager,
                @NotNull Graphics           graphics
        ) {
            var name2buffer = new HashMap<String, FrameBuffer>();
            var name2image  = new HashMap<String, BaseTexture>();
            var buffers     = new FrameBuffer[this.buffers.size()];
            var textures    = new BaseTexture[this.textures.size()];
            var passes      = new RenderPass[this.passes.size()];

            var i           = 0;

            for (i = 0; i < this.passes.size(); i++) {
                var pass        = this.passes.get(i).get();
                passes[i]       = pass;
                manager         .register(pass);
            }

            i               = 0;
            for (var definition     : this.buffers) {
                var name        = definition.name;
                var depth       = definition.useDepth;

                var buffer      = new FrameBuffer(depth);
                buffer.clearColor(definition.r, definition.g, definition.b, definition.a);
                buffers[i]      = buffer;

                name2buffer     .put(name, buffer);
                name2image      .put("buffer." + name, buffer);
                manager         .register(buffer);

                i               ++;
            }

            for (var link           : this.bufferLinks) {
                var name        = link.name;
                var buffer      = link.buffer;
                name2buffer     .put(name, buffer);
                name2image      .put("buffer." + name, buffer);
            }

            i               = 0;
            for (var definition     : this.textures) {
                var path        = definition.name;
                var glFilter    = definition.glFilter;
                var glWrap      = definition.glWrap;

                var texture     = ImageTexture.fromResource(path, glFilter, glWrap);
                textures[i]     = texture;
                name2image      .put(path, texture);
                manager         .register(texture);

                i               ++;
            }

            for (var link           : this.textureLinks) {
                var name        = link.name;
                var texture     = link.texture;
                name2image      .put(name, texture);
            }

            return new RenderProcedure(
                    name2buffer,
                    name2image,
                    buffers,
                    textures,
                    passes,
                    graphics
            );
        }

        @RequiredArgsConstructor
        static class NamedObject {
            final String    name;

            @Override
            public int hashCode() {
                return name.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof NamedObject && name.equals(((NamedObject) obj).name);
            }
        }

        static final class BufferDefinition extends NamedObject {
            final boolean   useDepth;
            final float r, g, b, a;

            public BufferDefinition(String name, boolean useDepth, float r, float g, float b, float a) {
                super(name);
                this.useDepth = useDepth;
                this.r = r;
                this.g = g;
                this.b = b;
                this.a = a;
            }

        }

        static final class TextureDefinition extends NamedObject {
            private final int glFilter;
            private final int glWrap;

            TextureDefinition(
                    String name,
                    int glFilter,
                    int glWrap
            ) {
                super(name);
                this.glFilter = glFilter;
                this.glWrap = glWrap;
            }
        }

        static final class BufferLink extends NamedObject {
            private final FrameBuffer buffer;

            BufferLink(String name, FrameBuffer buffer) {
                super(name);
                this.buffer = buffer;
            }
        }

        static final class TextureLink extends NamedObject {
            private final BaseTexture texture;

            TextureLink(String name, BaseTexture texture) {
                super(name);
                this.texture = texture;
            }
        }

    }
}
