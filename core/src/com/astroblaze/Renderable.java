package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Renderable extends SceneActor {
    protected final Scene3D scene;
    private Model model;
    private ModelInstance modelInstance;

    public Renderable(Scene3D scene, Model model) {
        this.scene = scene;
        this.setModel(model);
    }

    public Matrix4 getTransform() {
        return modelInstance.transform;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
        this.modelInstance = new ModelInstance(model);
    }

    @Override
    public void render(ModelBatch batch, Environment environment) {
        batch.render(modelInstance, environment);
    }
}
