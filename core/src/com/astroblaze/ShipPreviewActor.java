package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

class ShipPreviewActor extends Actor {
    private final ModelBatch modelBatch;
    private final Scene3D scene;
    private ModelInstance modelInstance;
    public Vector3 position = new Vector3();
    public Quaternion rotation = new Quaternion();
    public Vector3 scale = new Vector3(1f, 1f, 1f);

    public ShipPreviewActor(Scene3D scene, ModelInstance modelInstance) {
        this.scene = scene;
        this.modelBatch = AstroblazeGame.getInstance().getBatch();
        setModelInstance(modelInstance);
    }

    public void prevShip() {
        Gdx.app.log("ShipPreviewActor", "Selected prev ship");
    }

    public void nextShip() {
        Gdx.app.log("ShipPreviewActor", "Selected next ship");
    }

    public void setModelInstance(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
    }

    public void applyTRS() {
        modelInstance.transform.set(position, rotation, scale);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        /// rotate
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (!isVisible() || modelInstance == null)
            return;

        modelBatch.render(modelInstance, scene.getEnvironment());
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }
}
