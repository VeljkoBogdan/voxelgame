package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.graphics.g3d.Attribute;

public class TextureLayerAttribute extends Attribute {
    public static final String Alias = "textureLayer";
    public static final long ID = register(Alias);

    public int layer;

    public TextureLayerAttribute(int layer) {
        super(ID);
        this.layer = layer;
    }

    @Override
    public Attribute copy() {
        return new TextureLayerAttribute(layer);
    }

    @Override
    public int compareTo(Attribute o) {
        if (o == this) return 0;
        if (o == null) return 1;
        if (type != o.type) return type < o.type ? -1 : 1;
        TextureLayerAttribute other = (TextureLayerAttribute) o;
        return Integer.compare(this.layer, other.layer);
    }
}
