package com.astroblaze.Rendering;

import com.astroblaze.Assets;
import com.astroblaze.AstroblazeGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * This class draws player's laser beam using a shader
 * and UV scrolling on the mesh (scrolling is done inside
 * the shader using 'time' uniform)
 */
public class LaserController {
    private static class LaserInfo {
        Vector3 origin;
        float width = 10f;
        float life;
    }

    private final float beamLength = 300f;
    private final Mesh mesh;
    private ShaderProgram shaderProgram;
    private final Array<LaserInfo> lasers = new Array<>(64);
    private final long timeStartup = TimeUtils.millis();

    public LaserController() {
        mesh = makeMesh(0f, 0f, 1f, beamLength);
    }

    public Array<LaserInfo> getLasers() {
        return lasers;
    }

    public void addLaser(Vector3 position) {
        if (lasers.size == 0) {
            LaserInfo laser = new LaserInfo();
            laser.origin = position;
            laser.life = 0.1f;
            lasers.add(laser);
        } else {
            lasers.get(0).life = 0.1f;
        }
    }

    public void update(float delta) {
        for (int i = lasers.size - 1; i >= 0; i--) {
            LaserInfo info = lasers.get(i);
            info.life -= delta;
            if (info.life < 0f) {
                lasers.removeIndex(i);
            }
        }
    }

    public void dispose() {
        mesh.dispose();
    }

    private Mesh makeMesh(float x, float z, float width, float length) {
        Mesh quadMesh = new Mesh(false, 4, 6,
                VertexAttribute.Position(), VertexAttribute.TexCoords(0));
        setVertices(quadMesh, x, z, width, length);
        quadMesh.setIndices(new short[]{0, 1, 2, 2, 3, 0});

        return quadMesh;
    }

    private void setVertices(Mesh mesh, float x, float z, float width, float length) {
        float uvmax = length / width / 3f;
        float uvmin = 0f;
        mesh.setVertices(new float[]{
                x - width, 0f, z, uvmax, 0f,
                x + width, 0f, z, uvmax, 1f,
                x + width, 0f, z + length, uvmin, 1f,
                x - width, 0f, z + length, uvmin, 0f,
        });
    }

    public void loadTextures() {
        this.shaderProgram = Assets.asset(Assets.laserShader);
        Assets.asset(Assets.laser).setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }

    public void render(ModelBatch batch, Camera camera) {
        if (batch == null)
            return;
        batch.begin(camera);
        Assets.asset(Assets.laser).bind();
        shaderProgram.bind();
        shaderProgram.setUniformMatrix("u_projTrans", camera.combined);
        shaderProgram.setUniformi("u_texture", 0);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        boolean anyLaserActive = false;
        for (LaserInfo laser : lasers) {
            anyLaserActive = true;
            setVertices(mesh, laser.origin.x, laser.origin.z, laser.width, beamLength);

            shaderProgram.setUniformf("time", TimeUtils.timeSinceMillis(timeStartup) / 373f);
            mesh.render(shaderProgram, GL20.GL_TRIANGLES);
            // draw another faster layer
            shaderProgram.setUniformf("time", TimeUtils.timeSinceMillis(timeStartup) / 200f);
            mesh.render(shaderProgram, GL20.GL_TRIANGLES);
        }
        batch.end();
        AstroblazeGame.getSoundController().setLaserActive(anyLaserActive);
    }
}
