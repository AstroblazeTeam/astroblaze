package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
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
    private static class DecalInfo {
        public Vector3 position;
        public Vector3 velocity;
        public float time;
        public Decal decal;
    }

    private final DecalBatch batch;
    private final Array<DecalInfo> activeDecals = new Array<>(64);
    private final Scene3D scene;
    private final Camera camera;
    private Animation<TextureRegion> explosionAnimation;
    private Array<TextureAtlas.AtlasRegion> texRegions;
    private final Vector3 billboardDirection = new Vector3();

    public DecalController(Scene3D scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
        this.batch = new DecalBatch(new CameraGroupStrategy(camera));
    }

    public void loadTextures() {
        this.texRegions = Assets.asset(Assets.atlas).findRegions("explosion1");
        this.explosionAnimation = new Animation<TextureRegion>(0.1f, texRegions);
    }

    public void addExplosion(Vector3 position, Vector3 velocity, float scale) {
        DecalInfo info = new DecalInfo();
        info.position = position.cpy();
        info.velocity = velocity.cpy();
        info.time = 0f;
        info.decal = Decal.newDecal(texRegions.first(), true);
        info.decal.setPosition(info.position);
        info.decal.setRotation(billboardDirection, Vector3.Y);
        info.decal.setScale(scale * 0.1f);
        info.decal.rotateZ(MathUtils.random(0f, 360f));
        activeDecals.add(info);
    }

    public void update(float delta) {
        billboardDirection.set(-camera.direction.x, -camera.direction.y, -camera.direction.z);
        for (int i = activeDecals.size - 1; i >= 0; i--) {
            DecalInfo info = activeDecals.get(i);
            info.time += delta;
            if (info.time >= explosionAnimation.getAnimationDuration()) {
                activeDecals.removeIndex(i);
            }
            info.position.mulAdd(info.velocity, delta);
            info.decal.setPosition(info.position);
        }
    }

    public void render() {
        for (DecalInfo info : activeDecals) {
            info.decal.setTextureRegion(explosionAnimation.getKeyFrame(info.time));
            batch.add(info.decal);
        }
        batch.flush();
    }
}
