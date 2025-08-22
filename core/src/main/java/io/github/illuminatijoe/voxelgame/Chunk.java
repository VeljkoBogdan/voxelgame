package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import io.github.illuminatijoe.voxelgame.util.SimplexNoise;

public class Chunk implements Disposable {
    public static final int CHUNK_SIZE = 16;

    private Block[][][] blocks;
    private Model chunkModel;
    private ModelInstance chunkModelInstance;

    private boolean isLoaded = false;
    private boolean isSetup = false;
    private boolean shouldRender = false;
    private boolean needsRebuild = false;

    private final int X, Y, Z;

    public void update(float delta) {

    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (isLoaded && isSetup && shouldRender) {
            modelBatch.render(chunkModelInstance, environment);
        }
    }

    public Chunk(int X, int Y, int Z) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;

        // removed because this will happen on chunk load
        blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    blocks[x][y][z] = new Block();
                }
            }
        }
    }

    public void buildChunk() {
        // Needed for rebuilding
        // we don't want the reference of the old model to be lost when generating a new model
        // otherwise memory leaks
        if (chunkModel != null) {
            chunkModel.dispose();
            chunkModel = null;
        }

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Material mat = new Material(new TextureLayerAttribute(BlockType.Stone.textureIndex));
        VertexAttributes usage = new VertexAttributes(
            VertexAttribute.Position(),
            VertexAttribute.Normal(),
            VertexAttribute.TexCoords(0), // uv
            VertexAttribute.TexCoords(1) // texture layer index
        );
        MeshPartBuilder builder = modelBuilder.part("chunk", GL30.GL_TRIANGLES, usage, mat);

        float blockSize = 1f;
        float half = blockSize / 2f;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (!blocks[x][y][z].isActive()) continue; // Ignore inactive blocks

                    float bx = x * blockSize;
                    float by = y * blockSize;
                    float bz = z * blockSize;

                    float u1 = 0f, v1 = 0f;
                    float u2 = 1f, v2 = 1f;
                    float layer = (float) blocks[x][y][z].getBlockType().textureIndex;

                    // Front (Z+)
                    if (z == CHUNK_SIZE - 1 || !blocks[x][y][z + 1].isActive()) {
                        short i0 = builder.vertex(bx - half, by - half, bz + half, // pos
                            0, 0, 1, // normal
                            u1, v1, // texcoords
                            layer, 0f); // index
                        short i1 = builder.vertex(bx + half, by - half, bz + half,
                            0, 0, 1,
                            u2, v1,
                            layer, 0f);
                        short i2 = builder.vertex(bx + half, by + half, bz + half,
                            0, 0, 1,
                            u2, v2,
                            layer, 0f);
                        short i3 = builder.vertex(bx - half, by + half, bz + half,
                            0, 0, 1,
                            u1, v2,
                            layer, 0f);
                        builder.triangle(i0, i1, i2);
                        builder.triangle(i0, i2, i3);
                    }

                    // Back (Z-)
                    if (z == 0 || !blocks[x][y][z - 1].isActive()) {
                        short i0 = builder.vertex(bx + half, by - half, bz - half,
                            0, 0, -1,
                            u1, v1,
                            layer, 0f);
                        short i1 = builder.vertex(bx - half, by - half, bz - half,
                            0, 0, -1,
                            u2, v1,
                            layer, 0f);
                        short i2 = builder.vertex(bx - half, by + half, bz - half,
                            0, 0, -1,
                            u2, v2,
                            layer, 0f);
                        short i3 = builder.vertex(bx + half, by + half, bz - half,
                            0, 0, -1,
                            u1, v2,
                            layer, 0f);
                        builder.triangle(i0, i1, i2);
                        builder.triangle(i0, i2, i3);
                    }

                    // Left (X-)
                    if (x == 0 || !blocks[x - 1][y][z].isActive()) {
                        short i0 = builder.vertex(bx - half, by - half, bz - half,
                            -1, 0, 0, u1, v1, layer, 0f);
                        short i1 = builder.vertex(bx - half, by - half, bz + half,
                            -1, 0, 0, u2, v1, layer, 0f);
                        short i2 = builder.vertex(bx - half, by + half, bz + half,
                            -1, 0, 0, u2, v2, layer, 0f);
                        short i3 = builder.vertex(bx - half, by + half, bz - half,
                            -1, 0, 0, u1, v2, layer, 0f);
                        builder.triangle(i0, i1, i2);
                        builder.triangle(i0, i2, i3);
                    }

                    // Right (X+)
                    if (x == CHUNK_SIZE - 1 || !blocks[x + 1][y][z].isActive()) {
                        short i0 = builder.vertex(bx + half, by - half, bz + half,
                            1, 0, 0, u1, v1, layer, 0f);
                        short i1 = builder.vertex(bx + half, by - half, bz - half,
                            1, 0, 0, u2, v1, layer, 0f);
                        short i2 = builder.vertex(bx + half, by + half, bz - half,
                            1, 0, 0, u2, v2, layer, 0f);
                        short i3 = builder.vertex(bx + half, by + half, bz + half,
                            1, 0, 0, u1, v2, layer, 0f);
                        builder.triangle(i0, i1, i2);
                        builder.triangle(i0, i2, i3);
                    }

                    // Top (Y+)
                    if (y == CHUNK_SIZE - 1 || !blocks[x][y + 1][z].isActive()) {
                        short i0 = builder.vertex(bx - half, by + half, bz + half,
                            0, 1, 0, u1, v1, layer, 0f);
                        short i1 = builder.vertex(bx + half, by + half, bz + half,
                            0, 1, 0, u2, v1, layer, 0f);
                        short i2 = builder.vertex(bx + half, by + half, bz - half,
                            0, 1, 0, u2, v2, layer, 0f);
                        short i3 = builder.vertex(bx - half, by + half, bz - half,
                            0, 1, 0, u1, v2, layer, 0f);
                        builder.triangle(i0, i1, i2);
                        builder.triangle(i0, i2, i3);
                    }

                    // Bottom (Y-)
                    if (y == 0 || !blocks[x][y - 1][z].isActive()) {
                        short i0 = builder.vertex(bx - half, by - half, bz - half,
                            0, -1, 0, u1, v1, layer, 0f);
                        short i1 = builder.vertex(bx + half, by - half, bz - half,
                            0, -1, 0, u2, v1, layer, 0f);
                        short i2 = builder.vertex(bx + half, by - half, bz + half,
                            0, -1, 0, u2, v2, layer, 0f);
                        short i3 = builder.vertex(bx - half, by - half, bz + half,
                            0, -1, 0, u1, v2, layer, 0f);
                        builder.triangle(i0, i1, i2);
                        builder.triangle(i0, i2, i3);
                    }

                }
            }
        }

        this.chunkModel = modelBuilder.end();
        this.chunkModelInstance = new ModelInstance(chunkModel);
        this.chunkModelInstance.transform.setToTranslation(
            X * CHUNK_SIZE * blockSize,
            Y * CHUNK_SIZE * blockSize,
            Z * CHUNK_SIZE * blockSize
        );
    }

    @Override
    public void dispose() {
        if (chunkModel != null) {
            chunkModel.dispose();
            chunkModel = null;
            chunkModelInstance = null;
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    // Chunk is loaded into memory
    public void load() {
        if (blocks == null) {
            blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
            for (int x = 0; x < CHUNK_SIZE; x++) {
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    for (int z = 0; z < CHUNK_SIZE; z++) {
                        blocks[x][y][z] = new Block();
                    }
                }
            }
        }

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                // world coordinates
                int worldX = X * CHUNK_SIZE + x;
                int worldZ = Z * CHUNK_SIZE + z;

                double nx = (worldX + World.WORLD_SEED) * 0.01;
                double nz = (worldZ + World.WORLD_SEED) * 0.01;
                double e = SimplexNoise.noise(nx, nz)
                    + 0.5 * SimplexNoise.noise(nx * 2, nz * 2)
                    + 0.25 * SimplexNoise.noise(nx * 4, nz * 4);

                // scale to max world height (e.g., CHUNK_SIZE * WORLD_HEIGHT)
                int maxWorldHeight = CHUNK_SIZE / 8 * World.WORLD_SIZE; // adjust if needed
                int terrainHeight = (int) (((e + 1) / 2) * maxWorldHeight);

                for (int y = 0; y < CHUNK_SIZE; y++) {
                    int worldY = Y * CHUNK_SIZE + y;
                    if (worldY <= terrainHeight) {
                        blocks[x][y][z].setActive(true);

                        // block type layering
                        if (worldY < terrainHeight - 1) {
                            blocks[x][y][z].setBlockType(BlockType.Stone);
                        } else {
                            blocks[x][y][z].setBlockType(BlockType.Grass);
                        }
                    }
                }
            }
        }

        isLoaded = true;
    }

    public boolean isSetup() {
        return isSetup;
    }

    // Chunk becomes renderable
    public void setup() {
        buildChunk();

        isSetup = true;
    }

    public void unload() {
        if (chunkModel != null) {
            chunkModel.dispose();
            chunkModel = null;
            chunkModelInstance = null;
        }

        this.isLoaded = false;
        this.isSetup = false;
        blocks = null;
    }

    public int getZ() {
        return Z;
    }

    public int getY() {
        return Y;
    }

    public int getX() {
        return X;
    }

    public Vector3 getPosition() {
        return new Vector3(X, Y, Z);
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public boolean isEmpty() {
        return chunkModel.meshes.isEmpty();
    }
}
