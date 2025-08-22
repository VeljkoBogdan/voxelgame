package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import io.github.illuminatijoe.voxelgame.util.VoxelUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkManager {
    public static final int ASYNC_NUM_CHUNKS_PER_FRAME = 8;
    public static int RENDER_DISTANCE = 8;

    private final World world;
    private final Player player;
    private Chunk previousPlayerChunk;
    private Chunk playerChunk;

    public List<Chunk> chunkRenderList = new ArrayList<>();
    public List<Chunk> chunkLoadList = new ArrayList<>();
    public List<Chunk> chunkSetupList = new ArrayList<>();
    private List<Chunk> chunkRebuildList = new ArrayList<>();
    private List<Chunk> chunkUpdateFlagsList = new ArrayList<>();
    public List<Chunk> chunkUnloadList = new ArrayList<>();
    public List<Chunk> chunks = new ArrayList<>();

    private Vector3 previousCameraPosition;
    private boolean forceVisibilityUpdate;

    public ChunkManager(World world, Player player) {
        this.world = world;
        this.player = player;
        this.previousCameraPosition = player.position;

        playerChunk = getChunkFromWorldPosition(player.position);
        previousPlayerChunk = null;
        forceVisibilityUpdate = true;
    }

    public void update(float delta, PerspectiveCamera camera) {
        //updateAsyncChunker();
        updateLoadList();
        updateSetupList();
        updateRebuildList();
        //updateFlagsList();
        updateUnloadList();
        updateVisibilityList();
        //if (!previousCameraPosition.equals(camera.position)) {
            updateRenderList();
        //}

        previousCameraPosition.set(camera.position);
    }

    public void updateAsyncChunker() {

    }

    // Loads chunks in the LoadList if they are not loaded, forces a visibility update
    public void updateLoadList() {
        int numOfChunksLoaded = 0;

        for (Chunk chunk : chunkLoadList) {
            if (numOfChunksLoaded >= ASYNC_NUM_CHUNKS_PER_FRAME) {
                break;
            }

            if (!chunk.isLoaded()) {
                chunk.load();
                numOfChunksLoaded++;
                forceVisibilityUpdate = true;
            }
        }

        chunkLoadList.clear();
    }

    // Sets up chunks that are loaded and not set up, forces a visibility update
    public void updateSetupList() { // Setup any chunks that have not been set up yet
        for (Chunk chunk : chunkSetupList) {
            if (chunk.isLoaded() && !chunk.isSetup()) { // if the chunk is loaded but not set up
                chunk.setup();

                if (chunk.isSetup()) {// only force visibility if the chunk has actually been setup (some may wait for pre-setup stage)
                    forceVisibilityUpdate = true;
                }
            }
        }

        chunkSetupList.clear();
    }

    // Rebuilds chunks that need rebuilding, rebuilds chunks that are near the rebuilded chunks, forces a visibility update
    public void updateRebuildList() {
        int numRebuiltChunksThisFrame = 0;

        for (Chunk chunk : chunkRebuildList) {
            if (numRebuiltChunksThisFrame >= ASYNC_NUM_CHUNKS_PER_FRAME) {
                break;
            }

            if (chunk.isLoaded() && chunk.isSetup()) {
                chunk.buildChunk();
                chunkUpdateFlagsList.add(chunk);

                Chunk chunkXMinus = getChunk(chunk.getX() - 1, chunk.getY(), chunk.getZ());
                Chunk chunkXPlus = getChunk(chunk.getX() + 1, chunk.getY(), chunk.getZ());
                Chunk chunkYMinus = getChunk(chunk.getX(), chunk.getY() - 1, chunk.getZ());
                Chunk chunkYPlus = getChunk(chunk.getX(), chunk.getY() + 1, chunk.getZ());
                Chunk chunkZMinus = getChunk(chunk.getX(), chunk.getY(), chunk.getZ() - 1);
                Chunk chunkZPlus = getChunk(chunk.getX(), chunk.getY(), chunk.getZ() + 1);

                if (chunkXMinus != null) chunkRebuildList.add(chunkXMinus);
                if (chunkXPlus != null) chunkRebuildList.add(chunkXPlus);
                if (chunkYMinus != null) chunkRebuildList.add(chunkYMinus);
                if (chunkYPlus != null) chunkRebuildList.add(chunkYPlus);
                if (chunkZMinus != null) chunkRebuildList.add(chunkZMinus);
                if (chunkZPlus != null) chunkRebuildList.add(chunkZPlus);

                numRebuiltChunksThisFrame++;
                forceVisibilityUpdate = true;
            }
        }

        chunkRebuildList.clear();
    }

    // Unloads chunks that should be loaded, forces a visibility update
    public void updateUnloadList() {
        int numUnloadedChunks = 0;

        for (Chunk chunk : chunkUnloadList) {
            if (numUnloadedChunks <= ASYNC_NUM_CHUNKS_PER_FRAME * 8) {
                chunk.unload();
                world.unloadChunk(chunk);
                chunks.remove(chunk);
                forceVisibilityUpdate = true;
                numUnloadedChunks++;
            }
        }

        chunkUnloadList.clear();
    }

    // Checks if the chunks is in the cam frustum and adds them to the render list
    public void updateRenderList() {
        List<Chunk> visibleChunks = new ArrayList<>();
        for (Chunk chunk : chunks) {
            if (chunk.isLoaded() && chunk.isSetup() && chunk.shouldRender() && !chunk.isEmpty()) {
                // TODO: Check if the chunk is in the frustum and only render then
                visibleChunks.add(chunk);
            }
        }
        chunkRenderList = visibleChunks;
    }

    public void updateFlagsList() {

    }

    public void updateVisibilityList() {
        Chunk newPlayerChunk = getChunkFromWorldPosition(player.position);
        if (newPlayerChunk != null) playerChunk = newPlayerChunk;

        // Generate initial chunks if the previousPlayerChunk is null
        if (previousPlayerChunk == null) {
            for (int dx = -RENDER_DISTANCE; dx <= RENDER_DISTANCE; dx++) {
                for (int dy = -RENDER_DISTANCE; dy <= RENDER_DISTANCE; dy++) {
                    for (int dz = -RENDER_DISTANCE; dz <= RENDER_DISTANCE; dz++) {
                        int cx = playerChunk.getX() + dx;
                        int cy = playerChunk.getY() + dy;
                        int cz = playerChunk.getZ() + dz;

                        Chunk chunk = getChunk(cx, cy, cz);
                        if (!chunks.contains(chunk)) {
                            chunks.add(chunk); // add the chunk to the chunk list
                        }
                    }
                }
            }

            // Update the previous player chunk
            previousPlayerChunk = playerChunk;
            forceVisibilityUpdate = true;
        }

        // Look for new chunks only if the player has crossed the chunk border
        if (!previousPlayerChunk.equals(playerChunk)) {
            int movementX = playerChunk.getX() - previousPlayerChunk.getX();
            int movementY = playerChunk.getY() - previousPlayerChunk.getY();
            int movementZ = playerChunk.getZ() - previousPlayerChunk.getZ();

            // Only process axes where the player actually moved
            for (int dx = -RENDER_DISTANCE; dx <= RENDER_DISTANCE; dx++) {
                for (int dy = -RENDER_DISTANCE; dy <= RENDER_DISTANCE; dy++) {
                    for (int dz = -RENDER_DISTANCE; dz <= RENDER_DISTANCE; dz++) {

                        int cx = playerChunk.getX() + dx;
                        int cy = playerChunk.getY() + dy;
                        int cz = playerChunk.getZ() + dz;

                        // Only add new chunks along the "front" slice in the direction of movement
                        boolean addX = (movementX != 0) && (cx - playerChunk.getX() == movementX * RENDER_DISTANCE);
                        boolean addY = (movementY != 0) && (cy - playerChunk.getY() == movementY * RENDER_DISTANCE);
                        boolean addZ = (movementZ != 0) && (cz - playerChunk.getZ() == movementZ * RENDER_DISTANCE);

                        if (addX || addY || addZ) {
                            Chunk chunk = getChunk(cx, cy, cz);
                            if (!chunks.contains(chunk)) {
                                chunks.add(chunk);
                            }
                        }
                    }
                }
            }

            previousPlayerChunk = playerChunk;
            forceVisibilityUpdate = true;
        }


        for (Chunk chunk : chunks) {
            // check each chunk and see if they should be unloaded (by distance)
            if (VoxelUtils.squareDistance(playerChunk.getX(), playerChunk.getY(), playerChunk.getZ(),
                chunk.getX(), chunk.getY(), chunk.getZ()) > RENDER_DISTANCE) {
                chunkUnloadList.add(chunk);
                continue;
            }
            // check if chunks are not loaded and load them
            if (!chunk.isLoaded()) {
                chunkLoadList.add(chunk);
                continue;
            }
            // check if chunks are not setup and set them up
            if (chunk.isLoaded() && !chunk.isSetup()) {
                chunkSetupList.add(chunk);
                continue;
            }
            // add all the ready chunks to rendering
            if (chunk.isLoaded() && chunk.isSetup()) {
                chunk.setShouldRender(true);
            }
        }
    }

    public Chunk getChunk(int x, int y, int z) {
        return world.getChunk(x, y, z);
    }

    public Chunk getChunk(Vector3 pos) {
        return world.getChunk((int)pos.x, (int)pos.y, (int)pos.z);
    }

    public Chunk getChunkFromWorldPosition(Vector3 pos) {
        return world.getChunk((int)pos.x / Chunk.CHUNK_SIZE, (int)pos.y / Chunk.CHUNK_SIZE, (int)pos.z / Chunk.CHUNK_SIZE);
    }
}
