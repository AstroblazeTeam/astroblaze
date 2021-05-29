package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class Scene3D {
    public final ArrayList<SceneActor> actors = new ArrayList<>(1024);
    private final Environment environment;
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private final AstroblazeGame game;

    public Scene3D(AstroblazeGame game) {
        this.game = game;
        this.environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    public void act(float delta) {
        for (SceneActor actor : actors) {
            actor.act(delta);
        }
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        game.batch.begin(camera);
        for (SceneActor actor : actors) {
            actor.render(game.batch, environment);
        }
        game.batch.end();
    }

    public void addActors() {
        Renderable ship = new Renderable(this, Assets.asset(Assets.spaceShip2));
        float scale = 0.5f;
        ship.getTransform().setToTranslationAndScaling(0f, 0f, 0f, scale, scale, scale);
        actors.add(ship);
    }

    public void clearActors() {
        actors.clear();
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.fieldOfView = 60f;
        camera.position.set(0f, 100f, 100f);
        camera.rotate(Vector3.X, -90f);
        camera.rotate(Vector3.Y, -90f);
        camera.near = 10f;
        camera.far = 500f;
        camera.update();
    }

    public Camera getCamera() {
        return this.camera;
    }
}
