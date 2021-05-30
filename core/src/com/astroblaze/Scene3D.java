package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;

public class Scene3D {
    private final ArrayList<SceneActor> actors = new ArrayList<>(1024);
    public final ArrayList<SceneActor> addActors = new ArrayList<>(64);
    public final ArrayList<SceneActor> removeActors = new ArrayList<>(64);
    private final Environment environment;
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private final AstroblazeGame game;
    private final Vector3 moveVector = new Vector3();
    private final Plane planeXZ = new Plane(Vector3.Y, 0f);

    public Ship ship;

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

        DebugTextDrawer.setExtraReport("actors: " + actors.size());

        if (Gdx.input.isTouched()) {
            Ray ray = getCamera().getPickRay(Gdx.input.getX(), Gdx.input.getY());
            Vector3 hit = Vector3.Zero;
            if (Intersector.intersectRayPlane(ray, planeXZ, hit)) {
                moveVector.set(hit);
            }
        }

        if (ship != null) {
            ship.setMoveVector(moveVector);
        }
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        game.batch.begin(camera);
        for (SceneActor actor : actors) {
            actor.render(game.batch, environment);
        }
        game.batch.end();

        if (Gdx.graphics.getFrameId() % 120 == 0) {
            // every ~2 seconds do cleanup of objects that go out of bounds
            final float maxBoundsSquared = 500f * 500f;
            for (SceneActor actor : actors) {
                if (actor instanceof Renderable) {
                    Vector3 pos = ((Renderable) actor).getPosition();
                    if (pos.len2() > maxBoundsSquared) {
                        removeActors.add(actor);
                    }
                }
            }
        }

        // complete actor actions
        actors.addAll(addActors);
        addActors.clear();
        actors.removeAll(removeActors);
        removeActors.clear();
    }

    public void addActors() {
        ship = new Ship(this, Assets.asset(Assets.spaceShip2));
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
