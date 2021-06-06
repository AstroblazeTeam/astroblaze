package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Renderable extends SceneActor {
    protected final Scene3D scene;
    private Model model;
    private ModelInstance modelInstance;

    private Vector3 position = new Vector3();
    private Quaternion rotation = new Quaternion(0f, 0f, 0f, 1f);
    private Vector3 scale = new Vector3(1f, 1f, 1f);
    protected boolean visible = true;

    public Renderable(Scene3D scene, Model model) {
        this.scene = scene;
        this.setModel(model);
        applyTRS();
    }

    public Vector3 getPosition() {
        return this.position;
    }

    public void setPosition(Vector3 position) {
        this.position.set(position);
        applyTRS();
    }

    public Quaternion getRotation() {
        return this.rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation.set(rotation);
        applyTRS();
    }

    public Vector3 getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        this.scale = new Vector3(scale, scale, scale);
    }

    public void setScale(Vector3 scale) {
        this.scale.set(scale);
        applyTRS();
    }

    public void addRotation(Quaternion rotation) {
        this.rotation.mul(rotation);
    }

    public void applyTRS() {
        modelInstance.transform.set(position, rotation, scale);
    }

    public Matrix4 getTransform() {
        return modelInstance.transform;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
        this.modelInstance = model == null ? null : new ModelInstance(model);
    }

    @Override
    public void render(ModelBatch batch, Environment environment) {
        if (visible) {
            batch.render(modelInstance, environment);
        }
    }
}
