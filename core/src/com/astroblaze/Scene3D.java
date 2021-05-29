package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.ScreenUtils;

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
        //Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        game.batch.begin(camera);
        for (SceneActor actor : actors) {
            actor.render(game.batch, environment);
        }
        game.batch.end();
    }

    public void addActors() {
        SpaceShip ship = new SpaceShip(this, Assets.asset(Assets.spaceShip2));
        ship.getTransform().setTranslation(50f, 0f, 0f);
        actors.add(ship);
        ship = new SpaceShip(this, Assets.asset(Assets.spaceShip1));
        ship.getTransform().setTranslation(0f, 0f, 0f);
        actors.add(ship);
        ship = new SpaceShip(this, Assets.asset(Assets.spaceShip3));
        ship.getTransform().setTranslation(-50f, 0f, 0f);
        actors.add(ship);
    }

    public void clearActors() {
        actors.clear();
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.fieldOfView = 60;
        camera.position.set(40f, 40f, 40f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();
    }
}
