package com.astroblaze.Rendering;

import com.astroblaze.Assets;
import com.astroblaze.AstroblazeGame;
import com.astroblaze.Interfaces.ILoadingFinishedListener;
import com.astroblaze.Utils.WeightedCollection;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class SkyJunkController implements ILoadingFinishedListener {
    private final int starCount = 512;
    private final Array<JunkInfo> activeDecals = new Array<>(1024);
    private final Array<TextureAtlas.AtlasRegion> stars = new Array<>(8);
    private final WeightedCollection<TextureAtlas.AtlasRegion> junkDistribution = new WeightedCollection<>();
    private final Scene3D scene;
    private final DecalController decalsController;

    public static class JunkInfo {
        public Vector3 position;
        public Vector3 velocity;
        public Decal decal;
        public Animation<TextureRegion> animation;
    }

    SkyJunkController(Scene3D scene, DecalController decalsController) {
        this.scene = scene;
        this.decalsController = decalsController;

        AstroblazeGame.getInstance().addOnLoadingFinishedListener(this);
    }

    @Override
    public void finishedLoadingAssets() {
        stars.addAll(Assets.atlas1.findRegions("stars"));
        BoundingBox gameBounds = scene.getGameBounds();
        for (int i = 0; i < starCount; i++) {
            JunkInfo star = new JunkInfo();
            star.decal = Decal.newDecal(stars.random(), true);
            Color c = new Color((MathUtils.random(0x888888, 0xCCCCCC) << 8) + MathUtils.random(128, 255));
            star.decal.setColor(c);
            star.decal.setScale(0.15f);
            final float xPos = (MathUtils.random() - 0.5f) * 2f * gameBounds.getWidth();
            star.position = new Vector3(xPos, -10f, MathUtils.random(gameBounds.min.z, gameBounds.max.z));
            star.velocity = new Vector3(0f, 0f, MathUtils.random(-5f, -1f));

            star.decal.rotateX(90f);
            star.decal.rotateZ(MathUtils.random(0f, 360f));

            activeDecals.add(star);
        }
    }

    public void update(float delta) {
        BoundingBox gameBounds = scene.getGameBounds();
        float sceneDepth = gameBounds.getDepth();
        for (int i = activeDecals.size - 1; i >= 0; i--) {
            JunkInfo info = activeDecals.get(i);
            info.position.mulAdd(info.velocity, delta);
            if (!gameBounds.contains(info.position)) {
                info.position.z += sceneDepth;
            }
        }
    }

    public void render() {
        DecalBatch batch = decalsController.getDecalBatch();
        for (JunkInfo info : activeDecals) {
            info.decal.setPosition(info.position);
            batch.add(info.decal);
        }
        Gdx.gl20.glDepthMask(false);
        batch.flush();
        Gdx.gl20.glDepthMask(true);
    }
}
