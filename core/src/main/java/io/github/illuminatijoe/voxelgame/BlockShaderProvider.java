package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

public class BlockShaderProvider implements ShaderProvider {
    private final BlockShader.Config config;
    private final TextureArray textureArray;
    private BlockShader shader = null; // cache a single instance

    public BlockShaderProvider(DefaultShader.Config config, TextureArray textureArray) {
        this.config = config;
        this.textureArray = textureArray;
    }

    @Override
    public Shader getShader(Renderable renderable) {
        if (shader == null) {
            shader = new BlockShader(renderable, config, textureArray);
            shader.init();
        }
        return shader;
    }

    @Override
    public void dispose() {
        if (shader != null) {
            shader.dispose();
            shader = null;
        }
    }
}
