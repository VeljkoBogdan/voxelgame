package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.Attributes;

public class BlockShader extends DefaultShader {
    private final TextureArray textureArray;
    private int u_textureArray;

    public BlockShader(Renderable renderable, Config config, TextureArray textureArray) {
        super(renderable, config);

        this.textureArray = textureArray;
    }

    @Override
    public void init() {
        super.init();

        u_textureArray = program.fetchUniformLocation("u_textureArray", false);
        Gdx.app.log("GL", "Texture array location: " + u_textureArray);
    }

    @Override
    public void begin(com.badlogic.gdx.graphics.Camera camera, RenderContext context) {
        super.begin(camera, context);
        // Bind the TextureArray once per frame; record the sampler unit
        int unit = context.textureBinder.bind(textureArray);
        program.setUniformi(u_textureArray, unit);
    }

    @Override
    protected void bindMaterial(Attributes attributes) {
        super.bindMaterial(attributes);
    }
}
