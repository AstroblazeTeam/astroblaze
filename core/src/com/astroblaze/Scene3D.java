package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.ArrayList;

public class Scene3D implements ILoadingFinishedListener {
    private final ArrayList<SceneActor> actors = new ArrayList<>(1024);
    public final ArrayList<SceneActor> addActors = new ArrayList<>(64);
    public final ArrayList<SceneActor> removeActors = new ArrayList<>(64);
    public final ArrayList<Missile> activeMissiles = new ArrayList<>(64);
    public final BoundingBox gameBounds = new BoundingBox();
    public final BoundingBox destroyBounds = new BoundingBox();
    public final EnemyPool enemyPool;
    private final Environment environment;
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private final AstroblazeGame game;
    private final Vector3 moveVector = new Vector3();
    private final Plane planeXZ = new Plane(Vector3.Y, 0f);
    private final ParticleSystem particles = new ParticleSystem();
    private final ParticlePool particlePool;
    private final MissilePool missilePool;
    private float timeScale = 1f;

    private final int maxLives = 3;
    private int lives = maxLives;

    // decals
    public final DecalController decals;

    private Ship player;

    public Scene3D(AstroblazeGame game) {
        this.game = game;
        this.game.addOnLoadingFinishedListener(this);

        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));
        this.environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        this.particlePool = new ParticlePool(this.particles);
        this.missilePool = new MissilePool(this);
        this.enemyPool = new EnemyPool(this);

        PointSpriteParticleBatch batch = new PointSpriteParticleBatch(1024);
        batch.setCamera(this.getCamera());
        this.particles.add(batch);
        this.decals = new DecalController(this, camera);
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
        this.enemyPool.setAssets(particlePool);
        this.decals.loadTextures();
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
                final float edgeOffset = 0.8f;
                moveVector.z += fingerOffset; // keeps ship off left edge and outside finger
                moveVector.x = MathUtils.clamp(moveVector.x,
                        gameBounds.min.x * edgeOffset,
                        gameBounds.max.x * edgeOffset);
            }
        }

        for (SceneActor actor : actors) {
            actor.act(delta);
        }

        if (player != null) {
            player.setMoveVector(moveVector, false);
        }

        this.particles.update(delta);
        this.decals.update(delta);

        for (SceneActor actor : actors) {
            if (!(actor instanceof ICollisionProvider))
                continue;

            ICollisionProvider provider = (ICollisionProvider) actor;

            if (player != null) {
                Vector3 playerPos = player.getPosition();
                // check if player clips enemy ship
                if (provider.checkCollision(playerPos, player.getRadius())) {
                    player.modHp(-player.getMaxHp() * 0.5f);
                    provider.damageFromCollision(100f);
                }

                final float bulletHitRadius = 1f;
                // check if player clips enemy bullet
                for (DecalController.DecalInfo d : decals.getDecals()) {
                    if (!d.fromPlayer && (playerPos.dst(d.position) < player.getRadius() + d.radiusSquared)) {
                        player.modHp(-d.collisionDamage);
                        d.life = 0f;
                    }
                }
            }

            for (Missile m : activeMissiles) {
                if (!provider.checkCollision(m.getPosition(), 3f)) {
                    continue;
                }

                provider.damageFromCollision(m.getDamage());
                decals.addExplosion(m.getPosition(), m.getVelocity().scl(0.5f), 0.05f);
                missilePool.free(m);
            }
            for (DecalController.DecalInfo d : decals.getDecals()) {
                if (!d.fromPlayer || d.collisionDamage <= 0f || !provider.checkCollision(d.position, 1f)) {
                    continue;
                }

                provider.damageFromCollision(d.collisionDamage);
                d.life = 0f;
            }
        }
    }

    public void render(ModelBatch batch) {
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin(camera);
        this.particles.begin();
        this.particles.draw();
        this.particles.end();
        batch.render(particles);
        for (SceneActor actor : actors) {
            actor.render(batch, environment);
        }
        batch.end();
        decals.render();

        if (Gdx.graphics.getFrameId() % 15 == 0) {
            // every ~2 seconds do cleanup of objects that go out of bounds
            for (SceneActor actor : actors) {
                if (actor instanceof Renderable) {
                    if (actor instanceof Ship) {
                        continue;
                    }
                    Vector3 pos = ((Renderable) actor).getPosition();
                    if (!destroyBounds.contains(pos)) {
                        if (actor instanceof Enemy) {
                            enemyPool.free(((Enemy) actor));
                        } else {
                            removeActors.add(actor);
                        }
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
            if (actor instanceof Missile) {
                activeMissiles.add((Missile) actor);
            }
            actors.add(actor);
        }
        addActors.clear();
        for (SceneActor actor : removeActors) {
            if (actor instanceof Missile) {
                activeMissiles.remove(actor);
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

    public void respawnPlayer() {
        if (player != null) {
            player.reset();
        } else {
            player = new Ship(this, Assets.asset(Assets.spaceShip2));
            actors.add(player);
        }
    }

    public Ship getPlayer() {
        return this.player;
    }

    public void reset() {
        removeActors.addAll(actors);
        player = null;
        processActorMigrations();
        particles.update(60f); // finish playing all particles
        decals.getDecals().clear();
        moveVector.setZero();
        lives = maxLives;
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

        Ray ray1 = camera.getPickRay(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Vector3 hit1 = new Vector3();
        if (!Intersector.intersectRayPlane(ray1, planeXZ, hit1)) {
            throw new RuntimeException("Camera ray missed plane XZ, something is very wrong.");
        }
        Ray ray2 = camera.getPickRay(0, 0);
        Vector3 hit2 = new Vector3();
        if (!Intersector.intersectRayPlane(ray2, planeXZ, hit2)) {
            throw new RuntimeException("Camera ray missed plane XZ, something is very wrong.");
        }

        // raycasting XZ plane the difference of Y components is 0, fix that
        hit1.y = -32f;
        hit2.y = +32f;
        gameBounds.set(hit1.cpy(), hit2.cpy());
        destroyBounds.set(hit1.scl(1.5f), hit2.scl(1.5f));
    }

    public Camera getCamera() {
        return this.camera;
    }

    public int getLives() {
        return this.lives;
    }

    public float getTimeScale() {
        return this.timeScale;
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
    }

    public void playerDied() {
        lives--;
        this.moveVector.setZero();
        if (lives > 0) {
            player.reset();
            player.setPosition(new Vector3(1000f, 0f, 0f));
            float duration = 3f;
            game.gameScreen.getStage().addAction(Actions.sequence(
                    Actions.delay(duration),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            player.reset();
                            player.setGodModeTimer(3f + player.respawnNoControlTime);
                        }
                    })));
        } else {
            Vector3 moveAway = new Vector3(1000f, 0f, 0f);
            player.reset();
            player.setPosition(moveAway);
            player.setMoveVector(moveAway, true);
            player.setGodModeTimer(1000000f);
            player.setNoControlTime(1000000f);
            game.renderText(0, "Game Over");
        }
    }
}
