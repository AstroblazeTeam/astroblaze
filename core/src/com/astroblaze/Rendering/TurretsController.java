package com.astroblaze.Rendering;

import com.astroblaze.Assets;
import com.astroblaze.AstroblazeGame;
import com.astroblaze.Interfaces.ILoadingFinishedListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * This class controls the turret rendering for ships that request a turret to be rendered
 * on top of them
 */
public class TurretsController implements ILoadingFinishedListener {
    private final Array<TurretInfo> activeDecals = new Array<>(1024);
    private final Array<TextureAtlas.AtlasRegion> turrets = new Array<>(8);
    private final DecalController decalsController;

    public static class TurretInfo {
        public SpaceShip ship;
        public Decal decal;
    }

    TurretsController(DecalController decalsController) {
        this.decalsController = decalsController;

        AstroblazeGame.getInstance().addOnLoadingFinishedListener(this);
    }

    public void addTurret(SpaceShip ship) {
        TurretInfo turret = new TurretInfo();
        turret.ship = ship;
        turret.decal = Decal.newDecal(turrets.random(), true);
        turret.decal.setPosition(1000f, 0f, 0f);
        turret.decal.setScale(0.15f);
        turret.decal.rotateX(90f);
        activeDecals.add(turret);
    }

    public void removeTurrets(SpaceShip spaceShip) {
        for (int i = activeDecals.size - 1; i >= 0; i--) {
            TurretInfo turret = activeDecals.get(i);
            if (turret.ship == spaceShip) {
                activeDecals.removeIndex(i);
            }
        }
    }

    @Override
    public void finishedLoadingAssets() {
        turrets.addAll(Assets.atlas1.findRegions("turret"));
    }

    public static Quaternion getTurretRotation(SpaceShip ship) {
        final Quaternion q1 = new Quaternion(Vector3.Y, ship.turretAngle);
        final Quaternion q2 = new Quaternion(Vector3.X, 90f);
        return q1.mul(q2);
    }

    public void update(float delta) {
        for (int i = activeDecals.size - 1; i >= 0; i--) {
            final TurretInfo turret = activeDecals.get(i);
            turret.decal.setPosition(turret.ship.getPosition().cpy().add(0f, 0.15f * turret.ship.scale.x, 0f));
            turret.decal.setRotation(getTurretRotation(turret.ship));
        }
    }

    public void render() {
        final DecalBatch batch = decalsController.getDecalBatch();
        for (TurretInfo info : activeDecals) {
            info.decal.setColor(info.ship.getTintColor());
            batch.add(info.decal);
        }
        Gdx.gl20.glDepthMask(false);
        batch.flush();
        Gdx.gl20.glDepthMask(true);
    }
}
