package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class UniformDefinition {
    private final String[] name;
    private final UniformType[] type;
    private final int[] offset;

    @Getter
    private final int uniformCount;

    @Getter
    private final int byteSize;

    public void accept(@NotNull UniformRegistrar registrar) {
        for (int i = 0; i < uniformCount; i++) {
            registrar.register(name[i], type[i], offset[i]);
        }
    }

    public boolean isEmpty() {
        return uniformCount == 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    @FunctionalInterface
    public interface UniformRegistrar {
        void register(
                @NotNull    String          name,
                @NotNull    UniformType     type,
                            int             byteOffset
        );
    }

    public static final class Builder {
        private final List<String>      name    = new ArrayList<>();
        private final List<UniformType> type    = new ArrayList<>();

        private int uniformAmount = 0;

        private Builder() {}

        public Builder add(String name, UniformType type) {
            if (this.name.contains(name)) {
                throw new IllegalArgumentException("Duplicate uniform name: " + name);
            }

            this.name.add(name);
            this.type.add(type);
            this.uniformAmount ++;
            return this;
        }

        public UniformDefinition build() {
            var amount      = this.uniformAmount;

            var names       = new String[amount];
            var types       = new UniformType[amount];
            var offsets     = new int[amount];

            var offset      = 0;

            for (int i = 0; i < amount; i++) {
                var type    = this.type.get(i);
                var align   = type.std140;

                offset      = alignUp(offset, align);

                names[i]    = this.name.get(i);
                types[i]    = type;
                offsets[i]  = offset;

                offset      += type.size;
            }

            int byteSize    = alignUp(offset, 16);
            return new UniformDefinition(names, types, offsets, amount, byteSize);
        }

        private int alignUp(int value, int alignment) {
            return (value + alignment - 1) & -alignment;
        }
    }

}
