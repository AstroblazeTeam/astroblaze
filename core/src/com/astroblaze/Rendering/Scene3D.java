package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.astroblaze.Bonuses.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
    private final ArrayList<SceneActor> addActors = new ArrayList<>(64);
    private final ArrayList<SceneActor> removeActors = new ArrayList<>(64);
    private final ArrayList<Missile> activeMissiles = new ArrayList<>(64);
    private final WeightedCollection<IPlayerBonus> bonusDistribution = new WeightedCollection<>();

    private final Vector3 moveVector = new Vector3();
    private final Plane planeXZ = new Plane(Vector3.Y, 0f);
    private final BoundingBox gameBounds = new BoundingBox();
    private final BoundingBox destroyBounds = new BoundingBox();
    private final CameraController camera = new CameraController();
    private final ParticleSystem particles = new ParticleSystem();
    private final Environment environment = new Environment();
    private final AstroblazeGame game;
    private final EnemyPool enemyPool;
    private final DecalController decalController;
    private final ParticlePool particlePool;
    private final MissilePool missilePool;
    private final int defaultMaxLives = 3;

    private Ship player;
    private int lives = defaultMaxLives;
    private float timeScale = 1f;

    public Scene3D(AstroblazeGame game) {
        this.game = game;
        this.game.addOnLoadingFinishedListener(this);

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        particlePool = new ParticlePool(particles);
        missilePool = new MissilePool(this);
        enemyPool = new EnemyPool(this);

        PointSpriteParticleBatch batch = new PointSpriteParticleBatch(1024);
        batch.setCamera(camera);
        particles.add(batch);
        decalController = new DecalController(this, camera);
        setupBonusDistribution();
    }

    private void setupBonusDistribution() {
        bonusDistribution.add(50, null);
        bonusDistribution.add(40, new PlayerBonusMissiles());
        bonusDistribution.add(20, new PlayerBonusShieldRestore());
        bonusDistribution.add(10, new PlayerBonusLife());
    }

    public Ship getPlayer() {
        return player;
    }

    public EnemyPool getEnemyPool() {
        return enemyPool;
    }

    public MissilePool getMissilesPool() {
        return missilePool;
    }

    public ParticleSystem getParticlesSystem() {
        return particles;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public DecalController getDecalController() {
        return decalController;
    }

    public Vector3 getRespawnPosition() {
        return new Vector3(0f, 0f, destroyBounds.min.z + 5f);
    }

    public BoundingBox getGameBounds() {
        return new BoundingBox(gameBounds);
    }

    public IPlayerBonus rollRandomBonus() {
        return bonusDistribution.getRandom();
    }

    public CameraController getCamera() {
        return camera;
    }

    public int getLives() {
        return lives;
    }

    public void modLives(int mod) {
        lives += mod;
    }

    public float getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(float newTimeScale) {
        timeScale = newTimeScale;
    }

    @Override
    public void finishedLoadingAssets() {
        particlePool.setEffect(Assets.asset(Assets.flame));
        missilePool.setAssets(particlePool, Assets.missile);
        decalController.loadTextures();
    }

    public void act(float delta) {
        delta *= timeScale;

        if (delta <= 0f)
            return;

        if (player != null && Gdx.input.isTouched()) {
            Ray ray = getCamera().getPickRay(Gdx.input.getX(), Gdx.input.getY());
            Vector3 hit = new Vector3();
            if (Intersector.intersectRayPlane(ray, planeXZ, hit)) {
                final float fingerOffset = 16f;
                final float edgeOffset = 0.8f;
                moveVector.set(hit);
                moveVector.z += fingerOffset; // keeps ship off left edge and outside finger
                moveVector.x = MathUtils.clamp(moveVector.x,
                        gameBounds.min.x * edgeOffset,
                        gameBounds.max.x * edgeOffset);

                player.setMoveVector(moveVector, false);
            }
        }

        for (SceneActor actor : actors) {
            actor.act(delta);
        }

        particles.update(delta);
        decalController.update(delta);

        for (SceneActor actor : actors) {
            if (!(actor instanceof ICollisionProvider))
                continue;

            ICollisionProvider provider = (ICollisionProvider) actor;

            if (player != null) {
                Vector3 playerPos = player.getPosition();
                // check if player clips enemy ship
                if (provider.checkCollision(playerPos, player.getRadius())) {
                    player.modHp(-player.getMaxHp() * 0.5f);
                    provider.damageFromCollision(100f, true);
                }

                // check if player clips enemy bullet or bonus
                for (DecalController.DecalInfo d : decalController.getDecals()) {
                    if (!d.ignorePlayerCollision && (playerPos.dst(d.position) < player.getRadius() + d.radiusSquared)) {
                        if (d.life > 0f) {
                            d.life = 0f;
                            player.modHp(-d.collisionDamage);
                            if (d.bonus != null) {
                                d.bonus.applyBonus(player);
                            }
                        }
                    }
                }
            }

            for (Missile m : activeMissiles) {
                if (!provider.checkCollision(m.getPosition(), 3f)) {
                    continue;
                }

                provider.damageFromCollision(m.getDamage(), true);
                decalController.addExplosion(m.getPosition(), m.getVelocity().scl(0.5f), 0.05f);
                missilePool.free(m);
            }
            for (DecalController.DecalInfo d : decalController.getDecals()) {
                if (!d.ignorePlayerCollision || d.collisionDamage <= 0f || !provider.checkCollision(d.position, 1f)) {
                    continue;
                }

                provider.damageFromCollision(d.collisionDamage, d.ignorePlayerCollision);
                d.life = 0f;
            }
        }
    }

    public void render(ModelBatch batch) {
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        camera.update();
        batch.begin(camera);
        particles.begin();
        particles.draw();
        particles.end();
        batch.render(particles);
        for (SceneActor actor : actors) {
            actor.render(batch, environment);
        }
        batch.end();
        decalController.render();

        if (Gdx.graphics.getFrameId() % 15 == 0) {
            // every few frames do cleanup of objects that go out of bounds
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

    public void respawnPlayer(PlayerShipVariant variant) {
        if (player == null) {
            player = new Ship(this);
            player.resetShipType(variant);

            actors.add(player);
        }

        player.resetShip();
    }

    public void reset() {
        removeActors.addAll(actors);
        player = null;
        processActorMigrations();
        particles.update(60f); // finish playing all particles
        decalController.getDecals().clear();
        moveVector.setZero();
        lives = defaultMaxLives;
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
        Ray ray2 = camera.getPickRay(0f, 0f);
        Vector3 hit2 = new Vector3();
        if (!Intersector.intersectRayPlane(ray2, planeXZ, hit2)) {
            throw new RuntimeException("Camera ray missed plane XZ, something is very wrong.");
        }

        // raycasting XZ plane the difference of Y components is 0
        // this messes up some calculations, this fixes it
        hit1.y = -32f;
        hit2.y = +32f;
        gameBounds.set(hit1.cpy(), hit2.cpy());
        destroyBounds.set(hit1.scl(1.5f), hit2.scl(1.5f));
    }

    public void playerDied() {
        lives--;
        moveVector.setZero();
        if (lives > 0) {
            player.resetShip();
            player.setPosition(new Vector3(1000f, 0f, 0f));
            float duration = 3f;
            game.gameScreen.getStage().addAction(Actions.sequence(
                    Actions.delay(duration),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            player.resetShip();
                            player.setGodModeTimer(3f + player.respawnNoControlTime);
                        }
                    })));
        } else {
            Vector3 moveAway = new Vector3(1000f, 0f, 0f);
            player.resetShip();
            player.setPosition(moveAway);
            player.setMoveVector(moveAway, true);
            player.setGodModeTimer(1000000f);
            player.setNoControlTime(1000000f);
            game.renderText(0, "Game Over");
        }
    }

    public boolean getXZIntersection(float screenX, float screenY, Vector3 worldPosition) {
        Ray ray = camera.getPickRay(screenX, screenY);
        Gdx.app.log("FragmentLevelSelect", "unproject (" + ray + ")");
        return Intersector.intersectRayPlane(ray, planeXZ, worldPosition);
    }

    public ITargetable getClosestTarget(Vector3 pos) {
        float distanceSq = 100000000f;
        ITargetable result = null;
        for (SceneActor actor : actors) {
            if (!(actor instanceof ITargetable)) {
                continue;
            }
            ITargetable t = (ITargetable) actor;
            float dstToActorSq = t.distanceSquaredTo(pos);
            if (t.isTargetable() && dstToActorSq < distanceSq) {
                distanceSq = dstToActorSq;
                result = t;
            }
        }
        return result;
    }

    public void addActor(SceneActor actor) {
        if (!addActors.contains(actor))
            addActors.add(actor);
    }

    public void removeActor(SceneActor actor) {
        if (!removeActors.contains(actor))
            removeActors.add(actor);
    }
}
