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
    private Scene3D scene;
    private ModelInstance modelInstance;
    private Vector3 hit = Vector3.Zero;

    public Renderable(Scene3D scene, Model model) {
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
        Ray ray = scene.getCamera().getPickRay(Gdx.input.getX(), Gdx.input.getX());
        Plane plane = new Plane(Vector3.Y, 0f);
        if (Intersector.intersectRayPlane(ray, plane, hit)) {
            DebugTextDrawer.setExtraReport(hit.toString());
        }
    }

    @Override
    public void render(ModelBatch batch, Environment environment) {
        batch.render(modelInstance, environment);
    }
}
