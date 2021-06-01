package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;

public class Scene3D implements AstroblazeGame.ILoadingFinishedListener {
    private final ArrayList<SceneActor> actors = new ArrayList<>(1024);
    public final ArrayList<SceneActor> addActors = new ArrayList<>(64);
    public final ArrayList<SceneActor> removeActors = new ArrayList<>(64);
    private final Environment environment;
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private final AstroblazeGame game;
    private final Vector3 moveVector = new Vector3();
    private final Plane planeXZ = new Plane(Vector3.Y, 0f);
    private final ParticleSystem particles = new ParticleSystem();
    private final ParticlePool particlePool;
    private final MissilePool missilePool;
    private float timeScale = 1f;
    private float verticalSpan = 0f;

    public Ship ship;

    public Scene3D(AstroblazeGame game) {
        this.game = game;
        this.game.addOnLoadingFinishedListener(this);

        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));
        this.environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        this.particlePool = new ParticlePool(this.particles);
        this.missilePool = new MissilePool(this);

        PointSpriteParticleBatch batch = new PointSpriteParticleBatch(1024);
        batch.setCamera(this.getCamera());
        this.particles.add(batch);
    }

    public ParticlePool getParticlesPool() {
        return this.particlePool;
    }

    public MissilePool getMissilesPool() {
        return this.missilePool;
    }

    public ParticleSystem getParticlesSystem() {
        return this.particles;
    }

    @Override
    public void finishedLoadingAssets() {
        this.particlePool.setEffect(Assets.asset(Assets.flame2));
        this.missilePool.setAssets(particlePool, Assets.asset(Assets.missile));
    }

    public void act(float delta) {
        delta *= timeScale;

        if (delta <= 0f)
            return;

        if (Gdx.input.isTouched()) {
            Ray ray = getCamera().getPickRay(Gdx.input.getX(), Gdx.input.getY());
            Vector3 hit = Vector3.Zero;
            if (Intersector.intersectRayPlane(ray, planeXZ, hit)) {
                moveVector.set(hit);
                final float fingerOffset = 16f;
                moveVector.z += fingerOffset; // keeps ship off left edge and outside finger
                moveVector.x = MathUtils.clamp(moveVector.x, -verticalSpan, +verticalSpan);
            }
        }

        for (SceneActor actor : actors) {
            actor.act(delta);
        }

        if (ship != null) {
            ship.setMoveVector(moveVector);
        }

        this.particles.update(delta);
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        this.particles.begin();
        this.particles.draw();
        this.particles.end();

        game.batch.begin(camera);
        game.batch.render(particles);
        for (SceneActor actor : actors) {
            actor.render(game.batch, environment);
        }
        game.batch.end();

        if (Gdx.graphics.getFrameId() % 15 == 0) {
            // every ~2 seconds do cleanup of objects that go out of bounds
            final float maxBoundsSquared = 250f * 250f;
            for (SceneActor actor : actors) {
                if (actor instanceof Renderable) {
                    Vector3 pos = ((Renderable) actor).getPosition();
                    if (pos.len2() > maxBoundsSquared) {
                        removeActors.add(actor);
                    }
                }
            }
        }

        processActorMigrations();
    }

    private void processActorMigrations() {
        // complete actor actions
        for (SceneActor actor : addActors) {
            if (actor instanceof Renderable) {
                actor.show(game.getScene());
            }
            actors.add(actor);
        }
        addActors.clear();
        for (SceneActor actor : removeActors) {
            if (actor instanceof Missile) {
                missilePool.free((Missile) actor);
            } else if (actor instanceof Renderable) {
                actor.hide(game.getScene());
            } else {
                Gdx.app.error("Scene3D", "Removing unknown actor type " + actor.toString());
            }
            actors.remove(actor);
        }
        actors.removeAll(removeActors);
        removeActors.clear();
    }

    public void addActors() {
        ship = new Ship(this, Assets.asset(Assets.spaceShip2));
        actors.add(ship);
    }

    public void clearActors() {
        removeActors.addAll(actors);
        processActorMigrations();
        ship = null;
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.fieldOfView = 60f;
        // reset - for when android restores our activity
        camera.direction.set(0f, 0f, -1f);
        camera.up.set(0f, 1f, 0f);
        camera.update();

        camera.position.set(0f, 100f, 100f);
        camera.rotate(Vector3.X, -90f);
        camera.rotate(Vector3.Y, -90f);
        camera.near = 10f;
        camera.far = 500f;
        camera.update();

        Ray ray = camera.getPickRay(0, 0);
        Vector3 hit = Vector3.Zero;
        if (!Intersector.intersectRayPlane(ray, planeXZ, hit)) {
            throw new RuntimeException("Camera ray missed plane XZ, something is very wrong.");
        }
        verticalSpan = hit.x * 0.8f;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
    }
}
