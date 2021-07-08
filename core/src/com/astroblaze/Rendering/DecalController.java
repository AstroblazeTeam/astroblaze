package com.astroblaze.Rendering;

import com.astroblaze.Assets;
import com.astroblaze.Interfaces.IPlayerBonus;
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
    private final DecalBatch batch;
    private final Array<DecalInfo> activeDecals = new Array<>(256);
    private Animation<TextureRegion> explosionAnimation;
    private Animation<TextureRegion> exhaustAnimation;
    private Array<TextureAtlas.AtlasRegion> explosionTexRegions;
    private Array<TextureAtlas.AtlasRegion> exhaustTexRegions;

    public DecalController(Camera camera) {
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

    private DecalInfo createDecal(TextureRegion textureRegion, Vector3 position, float scale) {
        final DecalInfo d = new DecalInfo(textureRegion, position, scale);
        activeDecals.add(d);
        return d;
    }

    public DecalInfo addExhaust(SpaceShip ship, float offset, float scale) {
        DecalInfo info = createDecal(exhaustTexRegions.first(), new Vector3(offset, -2f, -5f), scale * 0.5f);
        info.life = 50000000f;
        info.ignorePlayerCollision = true;
        info.origin = ship.position;
        info.angle = -90f;
        info.animation = exhaustAnimation;
        return info;
    }

    public DecalInfo addBullet(Vector3 position, Vector3 velocity, float scale, float damage) {
        int bulletIdx = MathUtils.clamp((int) (damage / 2f) - 1, 0, 9);
        DecalInfo info = createDecal(Assets.bullets.get(bulletIdx), position, scale);
        info.velocity.set(velocity);
        info.life = 5f;
        info.radiusSquared = 1f;
        info.collisionDamage = damage;
        info.angle = MathUtils.atan2(velocity.x, velocity.z) * MathUtils.radiansToDegrees;
        return info;
    }

    public DecalInfo addExplosion(Vector3 position, Vector3 velocity, float scale) {
        DecalInfo info = createDecal(explosionTexRegions.first(), position, scale);
        info.velocity.set(velocity.cpy());
        info.life = explosionAnimation.getAnimationDuration();
        info.angle = MathUtils.random(0f, 360f);
        info.animation = explosionAnimation;
        return info;
    }

    public DecalInfo addBonus(Vector3 position, IPlayerBonus bonus) {
        DecalInfo info = createDecal(bonus.getDecalTexture(), position, bonus.getDecalScale());
        info.velocity.set(0f, 0f, -15f);
        info.life = 60f;
        info.radiusSquared = 9f;
        info.ignorePlayerCollision = false;
        info.bonus = bonus;
        return info;
    }

    public void update(float delta) {
        for (int i = activeDecals.size - 1; i >= 0; i--) { // loop backwards so removal is possible
            DecalInfo info = activeDecals.get(i);
            info.time += delta;
            if (info.time > info.life) {
                activeDecals.removeIndex(i);
                continue;
            }
            info.position.mulAdd(info.velocity, delta);
            if (info.animation != null) {
                info.decal.setTextureRegion(info.animation.getKeyFrame(info.time));
            }
            if (info.origin != null) {
                info.decal.setPosition(info.position.cpy().add(info.origin));
            } else {
                info.decal.setPosition(info.position.cpy());
            }
        }
    }

    public void render() {
        for (DecalInfo d : activeDecals) {
            if (d.time == 0f)
                continue; // skip fresh decals that update() hasn't processed yet

            // rotate to face "up" into camera
            d.decal.setRotation(Vector3.Y, Vector3.X);
            // then rotate by required d.angle
            d.decal.rotateZ(d.angle);

            // queue to render batch
            batch.add(d.decal);
        }
        batch.flush();
    }

    public static class DecalInfo {
        // libgdx does weird stuff with it's Decal class fields
        // so it's safer to just wrap over it rather than inherit
        public Decal decal;

        public Vector3 position = new Vector3();
        public Vector3 velocity = new Vector3();
        public Vector3 origin; // owned by other objects, read only, don't modify!
        public Animation<TextureRegion> animation;
        public IPlayerBonus bonus;
        public float time;
        public float life;
        public float angle;

        // collision variables
        public float collisionDamage;
        public float radiusSquared;
        public boolean ignorePlayerCollision;
        public boolean ignoreEnemyCollision;

        public DecalInfo(TextureRegion textureRegion, Vector3 position, float scale) {
            this.position.set(position);

            decal = Decal.newDecal(textureRegion, true);
            decal.setPosition(this.position);
            decal.setScale(scale);
        }
    }
}
