package io.github.illuminatijoe.voxelgame;

public enum BlockType {
    Dirt(0),
    Grass(1),
    Stone(2);

    public final int textureIndex;

    BlockType(int textureIndex) {
        this.textureIndex = textureIndex;
    }
}
