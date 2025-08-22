package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import io.github.illuminatijoe.voxelgame.util.Vec3i;

import java.util.ArrayList;
import java.util.HashMap;

public class World implements Disposable {
    public static final int WORLD_SIZE = 16;

    public Environment environment;

    private final HashMap<Vec3i, Chunk> chunks;
    public ChunkManager chunkManager;
    public Player player;

    public static final int WORLD_SEED = 12345;

    public World() {
        environment = new Environment();
        environment.set(ColorAttribute.createAmbientLight(0.4f, 0.4f, 1f, 0.5f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 1f, -1f, 0.8f, 0.2f));

        player = new Player();
        chunks = new HashMap<>();
        chunkManager = new ChunkManager(this, player);
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

    @Override
    public void dispose() {
        for (Chunk chunk : chunks.values()) {
            chunk.dispose();
        }
    }

    public Chunk getChunk(int x, int y, int z) {
        Vec3i key = new Vec3i(x, y, z);
        Chunk chunk = chunks.get(key);

        if (chunk == null) {
            // lazy create the chunk
            chunk = new Chunk(x, y, z);
            chunks.put(key, chunk);
        }

        return chunk;
    }

    public void unloadChunk(Chunk chunk) {
        Vec3i key = new Vec3i(chunk.getX(), chunk.getY(), chunk.getZ());

        chunks.remove(key);
    }
}
