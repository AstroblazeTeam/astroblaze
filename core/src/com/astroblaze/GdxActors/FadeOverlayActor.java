package com.astroblaze.GdxActors;

import com.astroblaze.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Actor to provide interpolated fade overlay when going in/out of the game for smooth transition
 */
public class FadeOverlayActor {
    private final ShaderProgram shader;
    private final Mesh mesh;

    public FadeOverlayActor() {
        this.shader = Assets.asset(Assets.fadeShader);
        this.mesh = makeMesh();
    }

    private Mesh makeMesh() {
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
        final float x = 1f;
        meshBuilder.rect(
                -x, -x, 0f,
                +x, -x, 0f,
                +x, +x, 0f,
                -x, +x, 0f,
                0, 0, 1);

        return meshBuilder.end();
    }

    public void draw(Batch batch, float parentAlpha) {
        final boolean blend = batch.isBlendingEnabled();
        final float alpha = 1f - parentAlpha;
        int srcFunc = batch.getBlendSrcFunc();
        int dstFunc = batch.getBlendDstFunc();
        batch.enableBlending();
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        batch.begin();
        batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_DST_ALPHA);
        shader.bind();
        shader.setUniformf("alpha", alpha);
        mesh.render(shader, GL20.GL_TRIANGLES);
        batch.end();
        batch.setBlendFunction(srcFunc, dstFunc);
        batch.setShader(null);
        if (!blend)
            batch.disableBlending();
    }
}
