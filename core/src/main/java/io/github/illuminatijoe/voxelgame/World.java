package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.Disposable;

public class World implements Disposable {
    public static final int WORLD_SIZE = 16;

    public Environment environment;

    private Chunk[][][] chunks;
    public ChunkManager chunkManager;
    public Player player;

    public static final int WORLD_SEED = 12345;

    public World() {
        environment = new Environment();
        environment.set(ColorAttribute.createAmbientLight(0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, 0.8f, 0.2f));

        player = new Player();
        chunks = new Chunk[WORLD_SIZE][WORLD_SIZE][WORLD_SIZE]; // initialize array
        chunkManager = new ChunkManager(this, player);

        generateWorld();
    }

    public void render(ModelBatch modelBatch) {
        for (Chunk chunk : chunkManager.chunkRenderList) {
            chunk.render(modelBatch, environment);
        }
    }

    public void update(float delta) {
        chunkManager.update(delta, player.camera);
        player.update(delta);
    }

    public void generateWorld() {
        chunks = new Chunk[WORLD_SIZE][WORLD_SIZE][WORLD_SIZE];
        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int y = 0; y < WORLD_SIZE; y++) {
                for (int z = 0; z < WORLD_SIZE; z++) {
                    chunks[x][y][z] = new Chunk(x, y, z); // create chunks
                }
            }
        }
    }

    @Override
    public void dispose() {
        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int y = 0; y < WORLD_SIZE; y++) {
                for (int z = 0; z < WORLD_SIZE; z++) {
                    if (chunks[x][y][z] != null) { // null safety
                        chunks[x][y][z].dispose();
                    }
                }
            }
        }
    }

    public Chunk getChunk(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 ||
            x >= WORLD_SIZE || y >= WORLD_SIZE || z >= WORLD_SIZE) {
            return null; // prevent out-of-bounds errors
        }
        return chunks[x][y][z];
    }
}
