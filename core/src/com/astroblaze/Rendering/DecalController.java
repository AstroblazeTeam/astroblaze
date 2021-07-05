package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class DecalController {
    public static class DecalInfo {
        public Vector3 position;
        public Vector3 velocity;
        public Vector3 origin; // not owned, don't change!!!
        public float time;
        public float life;
        public float angle;
        public float collisionDamage;
        public float radiusSquared;
        public boolean ignorePlayerCollision;
        public boolean ignoreEnemyCollision;
        public Decal decal;
        public IPlayerBonus bonus;
        public Animation<TextureRegion> animation;
    }

    private final DecalBatch batch;
    private final Array<DecalInfo> activeDecals = new Array<>(64);
    private final Scene3D scene;
    private final Camera camera;
    private Animation<TextureRegion> explosionAnimation;
    private Animation<TextureRegion> exhaustAnimation;
    private Array<TextureAtlas.AtlasRegion> explosionTexRegions;
    private Array<TextureAtlas.AtlasRegion> exhaustTexRegions;
    private final Vector3 billboardDirection = new Vector3();

    public DecalController(Scene3D scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
        this.batch = new DecalBatch(new CameraGroupStrategy(camera));
    }

    public void loadTextures() {
        this.explosionTexRegions = Assets.asset(Assets.atlas).findRegions("explosion1");
        this.exhaustTexRegions = Assets.asset(Assets.atlas).findRegions("exhaust");
        this.explosionAnimation = new Animation<TextureRegion>(0.1f, explosionTexRegions);
        this.exhaustAnimation = new Animation<TextureRegion>(0.025f, exhaustTexRegions, Animation.PlayMode.LOOP);
    }

    public Array<DecalInfo> getDecals() {
        return this.activeDecals;
    }

    public DecalBatch getDecalBatch() {
        return this.batch;
    }

    public DecalInfo addExhaust(Vector3 origin, float offset, float scale) {
        DecalInfo info = new DecalInfo();
        info.position = new Vector3(offset, -2f, -5f);
        info.velocity = new Vector3();
        info.life = 50000000f;
        info.ignorePlayerCollision = true;
        info.origin = origin;
        info.angle = -90f;
        info.decal = Decal.newDecal(exhaustTexRegions.first(), true);
        info.decal.setPosition(info.position);
        info.decal.setRotation(billboardDirection, Vector3.Y);
        info.decal.setScale(scale * 0.5f);
        info.decal.rotateZ(MathUtils.random(0f, 360f));
        info.animation = exhaustAnimation;
        activeDecals.add(info);
        return info;
    }

    public DecalInfo addBullet(Vector3 position, Vector3 velocity, float scale, float damage) {
        DecalInfo info = new DecalInfo();
        info.position = position.cpy();
        info.velocity = velocity.cpy();
        info.time = 0f;
        info.life = 5f;
        info.radiusSquared = 1f;
        info.collisionDamage = damage;
        int bulletIdx = MathUtils.clamp((int) (damage / 2f) - 1, 0, 10);
        info.decal = Decal.newDecal(Assets.bullets.get(bulletIdx), true);
        info.decal.setPosition(info.position);
        info.decal.setRotation(billboardDirection, Vector3.Y);
        info.decal.setScale(scale);
        info.angle = MathUtils.atan2(velocity.x, velocity.z) * MathUtils.radiansToDegrees;
        activeDecals.add(info);
        return info;
    }

    public DecalInfo addExplosion(Vector3 position, Vector3 velocity, float scale) {
        DecalInfo info = new DecalInfo();
        info.position = position.cpy();
        info.velocity = velocity.cpy();
        info.time = 0f;
        info.life = explosionAnimation.getAnimationDuration();
        info.decal = Decal.newDecal(explosionTexRegions.first(), true);
        info.decal.setPosition(info.position);
        info.decal.setRotation(billboardDirection, Vector3.Y);
        info.decal.setScale(scale);
        info.decal.rotateZ(MathUtils.random(0f, 360f));
        info.animation = explosionAnimation;
        activeDecals.add(info);
        return info;
    }

    public DecalInfo addBonus(Vector3 position, IPlayerBonus bonus) {
        DecalInfo info = new DecalInfo();
        info.position = position.cpy();
        info.velocity = new Vector3(0f, 0f, -15f);
        info.time = 0f;
        info.life = 60f;
        info.radiusSquared = 9f;
        info.ignorePlayerCollision = false;
        info.collisionDamage = 0f;
        info.bonus = bonus;
        info.decal = Decal.newDecal(bonus.getDecalTexture(), true);
        info.decal.setPosition(info.position);
        info.decal.setScale(bonus.getDecalScale());
        activeDecals.add(info);
        return info;
    }

    public void update(float delta) {
        billboardDirection.set(-camera.direction.x, -camera.direction.y, -camera.direction.z);
        for (int i = activeDecals.size - 1; i >= 0; i--) {
            DecalInfo info = activeDecals.get(i);
            info.time += delta;
            if (info.time > info.life) {
                activeDecals.removeIndex(i);
                continue;
            }
            info.position.mulAdd(info.velocity, delta);
            if (info.origin != null) {
                info.decal.setPosition(info.position.cpy().add(info.origin));
            } else {
                info.decal.setPosition(info.position.cpy());
            }
        }
    }

    public void render() {
        for (DecalInfo info : activeDecals) {
            if (info.time == 0f)
                continue; // skip fresh decals that update() hasn't processed yet

            if (info.animation != null) {
                info.decal.setTextureRegion(info.animation.getKeyFrame(info.time));
            }

            info.decal.lookAt(scene.getCamera().position, Vector3.X);
            info.decal.rotateZ(info.angle);
            batch.add(info.decal);
        }
        batch.flush();
    }
}
