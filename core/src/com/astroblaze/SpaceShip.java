package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;

public class SpaceShip extends SceneActor {
    private Scene3D scene;
    private ModelInstance modelInstance;

    public SpaceShip(Scene3D scene, Model model) {
        this.scene = scene;
        this.setModel(model);
    }

    public Matrix4 getTransform() {
        return modelInstance.transform;
    }

    public void setModel(Model model) {
        modelInstance = new ModelInstance(model);
    }

    @Override
    public void act(float delta) {
        modelInstance.transform.rotate(0f, 1f, 0f, 10f * delta);
    }

    @Override
    public void render(ModelBatch batch, Environment environment) {
        batch.render(modelInstance, environment);
    }
}
