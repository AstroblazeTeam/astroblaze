package com.astroblaze.Rendering;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

/**
 * Represents a SceneActor with base empty methods the classes inheriting it may override
 */
public class SceneActor {
    public void show(Scene3D scene) {
    }

    public void hide(Scene3D scene) {
    }

    public void act(float delta) {
    }

    public void render(ModelBatch batch, Environment environment) {
    }
}
