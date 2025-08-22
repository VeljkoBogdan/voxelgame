package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class BlockShaderProvider implements ShaderProvider {
    private final BlockShader.Config config;
    private final TextureArray textureArray;
    private BlockShader shader = null; // cache a single instance

    public BlockShaderProvider(TextureArray textureArray) {
        ShaderProgram.prependVertexCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop)
            ? "#version 140\n #extension GL_EXT_texture_array : enable\n"
            : "#version 300 es\n";
        ShaderProgram.prependFragmentCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop)
            ? "#version 140\n #extension GL_EXT_texture_array : enable\n"
            : "#version 300 es\n";

        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("data/shaders/texturearray.vert").readString();
        config.fragmentShader = Gdx.files.internal("data/shaders/texturearray.frag").readString();
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
