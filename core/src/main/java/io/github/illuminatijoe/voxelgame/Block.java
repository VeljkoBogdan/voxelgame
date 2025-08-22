package io.github.illuminatijoe.voxelgame;

public class Block {
    private boolean isActive = true;
    private BlockType blockType = BlockType.Dirt;

    public Block() {
        isActive = false;
        blockType = BlockType.Dirt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }

    public BlockType getBlockType() {
        return blockType;
    }
}
