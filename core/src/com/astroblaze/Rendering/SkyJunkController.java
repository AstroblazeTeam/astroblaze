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

    private Color randomStarColor() {
        return new Color((MathUtils.random(0x888888, 0xFFFFFF) << 8) + MathUtils.random(128, 255));
    }

    @Override
    public void finishedLoadingAssets() {
        stars.addAll(Assets.atlas1.findRegions("stars"));

        final BoundingBox gameBounds = scene.getGameBounds();

        // generate all stars, they will be recycled, no need for pooling
        for (int i = 0; i < starCount; i++) {
            final JunkInfo star = new JunkInfo();
            star.decal = Decal.newDecal(stars.random(), true);

            star.decal.setColor(randomStarColor());
            star.decal.setScale(0.15f);

            star.position = new Vector3(
                    MathUtils.random(-1f, 1f) * gameBounds.getWidth(),
                    5f, // put slightly above so they're offscreen with perspective projection
                    MathUtils.random(gameBounds.min.z, gameBounds.max.z));

            star.velocity = new Vector3(0f, 0f, MathUtils.random(-5f, -1f));

            star.decal.rotateX(90f);
            star.decal.rotateZ(MathUtils.random(0f, 360f));

            activeDecals.add(star);
        }
    }

    public void update(float delta) {
        final BoundingBox gameBounds = scene.getGameBounds();
        final float sceneDepth = gameBounds.getDepth();

        for (int i = activeDecals.size - 1; i >= 0; i--) {
            final JunkInfo info = activeDecals.get(i);

            info.position.mulAdd(info.velocity, delta); // apply velocity

            if (!gameBounds.contains(info.position)) {
                info.position.z += sceneDepth;
            }
        }
    }

    public void render() {
        final DecalBatch batch = decalsController.getDecalBatch();
        for (JunkInfo info : activeDecals) {
            info.decal.setPosition(info.position);
            batch.add(info.decal);
        }
        Gdx.gl20.glDepthMask(false);
        batch.flush();
        Gdx.gl20.glDepthMask(true);
    }
}
