package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.utils.ScreenUtils;

public class VoxelGame extends ApplicationAdapter {
    private ModelBatch modelBatch;
    private BitmapFont bitmapFont;
    private SpriteBatch spriteBatch;

    private BlockShaderProvider blockShaderProvider;

    private World world;

    @Override
    public void create() {
        bitmapFont = new BitmapFont();
        spriteBatch = new SpriteBatch();

        world = new World();

        // Textures
        String[] texPaths = new String[] {
            "textures/dirt.png",
            "textures/grass.png",
            "textures/stone.png"
        };
        FileHandle[] texFiles = new FileHandle[texPaths.length];
        for (int i = 0; i < texFiles.length; i++) {
            texFiles[i] = Gdx.files.internal(texPaths[i]);
        }
        TextureArray textureArray = new TextureArray(true, texFiles);
        textureArray.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        textureArray.setFilter(Texture.TextureFilter.MipMapNearestNearest, Texture.TextureFilter.Nearest);

        BlockShader.Config cfg = new DefaultShader.Config();
        cfg.vertexShader = Gdx.files.internal("data/shaders/texturearray.vert").readString();
        cfg.fragmentShader = Gdx.files.internal("data/shaders/texturearray.frag").readString();
        blockShaderProvider = new BlockShaderProvider(cfg, textureArray);
        modelBatch = new ModelBatch(blockShaderProvider);

        Gdx.app.log("GL", "Texture array supported: " + Gdx.graphics.supportsExtension("GL_EXT_texture_array"));
        Gdx.app.log("Assets", "Texture array size: " + textureArray.getDepth());
    }

    @Override
    public void render() {
        world.update(Gdx.graphics.getDeltaTime());

        // Start render
        ScreenUtils.clear(Color.BLACK, true);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        modelBatch.begin(world.player.camera);
            world.render(modelBatch);
        modelBatch.end();

        spriteBatch.begin();
            bitmapFont.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
            bitmapFont.draw(spriteBatch, "Memory: " + Gdx.app.getJavaHeap() / 1000 / 1000 + " MB", 10, 40);
            bitmapFont.draw(spriteBatch, "Chunks Rendered: " + world.chunkManager.chunkRenderList.size(), 10, 60);
            bitmapFont.draw(spriteBatch, "Chunks Loaded: " + world.chunkManager.chunks.size(), 10, 80);
        spriteBatch.end();
        // End render
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        spriteBatch.dispose();
        bitmapFont.dispose();
        world.dispose();
        blockShaderProvider.dispose();
    }
}
