package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Renderable extends SceneActor {
    protected final Vector3 position = new Vector3();
    protected final Quaternion rotation = new Quaternion();
    protected final Vector3 scale = new Vector3(1f, 1f, 1f);
    protected ModelInstance modelInstance;
    protected boolean visible = true;

    public Renderable() {
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
        this.scale.set(scale, scale, scale);
    }

    public void setScale(Vector3 scale) {
        this.scale.set(scale);
        applyTRS();
    }

    public void addRotation(Quaternion rotation) {
        this.rotation.mul(rotation);
    }

    public void applyTRS() {
        if (modelInstance != null) {
            modelInstance.transform.set(position, rotation, scale);
        }
    }

    public Matrix4 getTransform() {
        return modelInstance.transform;
    }

    @Override
    public void render(ModelBatch batch, Environment environment) {
        if (visible && modelInstance != null) {
            batch.render(modelInstance, environment);
        }
    }

    protected void setModel(AssetDescriptor<Model> model) {
        this.modelInstance = new ModelInstance(Assets.asset(model));
    }
}
