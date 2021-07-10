package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;

import java.text.DecimalFormat;

public class PlayerShip extends SpaceShip {
    public final float respawnNoControlTime = 1f;

    private final float gunInterval = 1f / 15f;
    private final float gunBulletSpeed = 200f;
    private final float missileInterval = 1f / 8f;
    private final float deathTimerMax = 5f;
    private final float destroyExplosionInterval = 0.1f;
    private final int defaultMissileSalvos = 8;

    private float missileClock;
    private float moveSpeed;
    private float currentBank;
    private float laserTime; // time remaining for laser shots
    private int missileSalvos; // amount of missile salvos player has.

    private float noControlTimer;
    private boolean isDying;
    private float deathTimer;
    private float destroyExplosionTimer = 0f;
    private float godModeTimer = 0f;
    private boolean hpBarEnabled = false; // flag to avoid event spam
    private PlayerShipVariant shipVariant;
    private boolean autoFireMissiles;
    private boolean autoFireLaser;

    public PlayerShip(Scene3D scene) {
        super(scene);
    }

    @Override
    public void show(Scene3D scene) {
        super.show(scene);
        DecalController decals = scene.getDecalController();
        float engineScale = Math.min(1.5f, getShipVariant().getUpgradeModifier(playerState, UpgradeEntryType.SpeedUpgrade));
        exhaustDecals.add(decals.addExhaust(this, -shipVariant.modelScale * 0.25f, 0.75f * engineScale));
        exhaustDecals.add(decals.addExhaust(this, 0f, 1.25f * engineScale));
        exhaustDecals.add(decals.addExhaust(this, +shipVariant.modelScale * 0.25f, 0.75f * engineScale));
        // normalize to 0f..1f range
        float colorScale = MathUtils.map(1f, 1.5f, 0f, 1f, engineScale);
        Color startColor = new Color(1f, 1f, 1f, 1f);
        Color endColor = new Color(0.95f, 0.32f, 0.25f, 1f);
        for (DecalController.DecalInfo d : exhaustDecals) {
            d.decal.setColor(startColor.lerp(endColor, colorScale));
        }
    }

    @Override
    public void hide(Scene3D scene) {
        super.hide(scene);
        for (DecalController.DecalInfo d : exhaustDecals) {
            d.life = 0f;
        }
        exhaustDecals.clear();
    }

    public float getRadius() {
        return shipVariant.modelScale * 0.4f; // slightly less than half scale
    }

    public float getMaxHitpoints() {
        return shipVariant.getMaxHp(playerState);
    }

    public int getMissileSalvos() {
        return missileSalvos;
    }

    public void modMissileSalvos(int mod) {
        missileSalvos += mod;
        reportExtras();
    }

    public void modHp(float hpModifier) {
        final float oldHp = hp;
        if (hpModifier < 0f && godModeTimer > 0f) {
            return;
        }
        hp = MathUtils.clamp(hp + hpModifier, 0f, getMaxHitpoints());
        game.reportStateChanged(this, hp, oldHp);
        if (hpModifier < 0f) {
            AstroblazeGame.getLevelStatTracker().addDamageTaken(hpModifier);
            scene.getCamera().shake();
        }
        Gdx.app.log("Ship", "Player hp modded from " + oldHp + " to " + hp);
        if (hp < 0f) {
            setNoControlTime(deathTimer);
        }
    }

    public Vector3 getMoveVector() {
        return this.moveVector.cpy();
    }

    public void setMoveVector(Vector3 newMoveVector, boolean force) {
        if (!force && noControlTimer > 0f)
            return;
        this.moveVector.set(newMoveVector);
    }

    public void stopMoving(boolean force) { // stops movement of ship
        setMoveVector(position, force);
    }

    public PlayerShipVariant getShipVariant() {
        return this.shipVariant;
    }

    public void resetShip() {
        resetShipType(getShipVariant());
    }

    public void resetShipType(PlayerShipVariant variant) {
        // ship variant
        shipVariant = variant;
        modHp(getMaxHitpoints());
        moveSpeed = variant.getSpeed(playerState);
        setModel(variant.getVariantAssetModel());
        setScale(variant.modelScale);
        noControlTimer = respawnNoControlTime;
        deathTimer = deathTimerMax;
        isDying = false;
        modMissileSalvos(-missileSalvos);
        modMissileSalvos(defaultMissileSalvos);
        laserTime = 3f;
        setMoveVector(new Vector3(0f, 0f, scene.getGameBounds().min.z + getRadius() * 3f), true);
        setPosition(scene.getRespawnPosition());
        setRotation(new Quaternion());
        applyTRS();
    }

    public void setGodModeTimer(float time) {
        this.godModeTimer = time;
    }

    public void fireGuns(float delta) {
        if (!isControlled())
            return;

        gunClock -= delta;
        if (gunClock > 0f) {
            return;
        }
        gunClock += gunInterval;

        final Vector3 pos = this.getPosition().cpy();
        final Vector3 vel = new Vector3(0f, 0f, gunBulletSpeed);
        final int ports = shipVariant.gunPorts;
        final float offset = shipVariant.modelScale / ports * 0.5f;
        final float bulletDamage = shipVariant.getGunDamage(playerState);
        for (float x = -ports * 0.5f + 0.5f; x < ports * 0.5f + 0.5f; x++) {
            scene.getDecalController().addBullet(pos.cpy().add(x * offset, 0f, 3f), vel, 0.1f, bulletDamage)
                    .ignorePlayerCollision = true;
        }
    }

    public void fireTurrets(float delta) {
        if (!isControlled() || shipVariant.turretPorts == 0)
            return;

        turretClock -= delta;
        if (turretClock >= 0f) {
            return;
        }
        turretClock += gunInterval;

        final Vector3 pos = this.getPosition().cpy();
        final Vector3 vel = new Vector3(0f, 0f, gunBulletSpeed);

        ITargetable t = scene.getClosestTarget(this, pos);
        if (t != null) {
            final float distance = (float) Math.sqrt(t.distanceSquaredTo(pos));
            if (distance > 1000f) { // don't shoot from off-screen
                return;
            }
            final Vector3 dir = t.estimatePosition(distance / vel.len()).cpy().sub(pos).nor();
            final int turretPorts = shipVariant.turretPorts;
            final float turretOffset = shipVariant.modelScale / turretPorts * 0.5f;
            float angle = MathUtils.atan2(dir.x, dir.z) * MathUtils.radiansToDegrees;
            if (Float.isNaN(angle) || Float.isInfinite(angle)) {
                return;
            }
            vel.set(0f, 0f, gunBulletSpeed).rotate(Vector3.Y, angle);
            final float turretDamage = shipVariant.getTurretDamage(playerState);
            for (float x = -turretPorts * 0.5f + 0.5f; x < turretPorts * 0.5f + 0.5f; x++) {
                scene.getDecalController().addBullet(pos.cpy().add(x * turretOffset, 0f, 3f), vel, 0.1f, turretDamage)
                        .ignorePlayerCollision = true;
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        setNoControlTime(noControlTimer - delta);

        Vector3 currentPos = getPosition().cpy();
        Vector3 diff = moveVector.cpy().sub(currentPos);

        if (MathUtils.isEqual(currentBank, 0f, 0.1f) && MathUtils.isEqual(diff.x, 0f, 0.1f)) {
            currentBank = 0f;
        } else {
            final float bankSpeed = 90f;
            currentBank = MathHelper.moveTowards(currentBank, -diff.x, bankSpeed * delta);
            currentBank = MathUtils.clamp(currentBank, -15f, +15f);
        }

        MathHelper.moveTowards(currentPos, moveVector, moveSpeed * delta);
        setPosition(currentPos);
        setRotation(new Quaternion(Vector3.Z, currentBank));
        applyTRS();

        if (!isDying && scene.getGameBounds().contains(this.position)) {
            fireGuns(delta);
            fireTurrets(delta);
            fireMissiles(delta);
            fireLaser(delta);

            if (hp <= 0f) {
                isDying = true;
                setNoControlTime(deathTimer);
            }
        }

        // handle temporary godmode after respawn
        godModeTimer -= delta;
        visible = godModeTimer <= 0f || (((int) (godModeTimer * 8f)) % 2 == 0);

        // handle destroy explosions animation
        if (isDying) {
            moveSpeed = MathHelper.moveTowards(moveSpeed, 0f, 1f / deathTimerMax * delta);
            destroyExplosionTimer -= delta;
            deathTimer -= delta;

            if (destroyExplosionTimer < 0f) {
                destroyExplosionTimer = destroyExplosionInterval;
                Vector3 v = new Vector3();
                v.setToRandomDirection();
                DecalController.DecalInfo info = scene.getDecalController().addExplosion(
                        v.scl(MathUtils.random(0f, 1f) * shipVariant.modelScale),
                        new Vector3(), 0.05f);
                info.origin = this.getPosition();
            }

            if (deathTimer < 0f) {
                scene.playerDied();
            }
        }
    }

    public boolean isControlled() {
        return noControlTimer <= 0f;
    }

    public float getLaserTime() {
        return this.laserTime;
    }

    public void modLaserTime(float delta) {
        laserTime = MathHelper.moveTowards(laserTime, MathUtils.clamp(laserTime + delta, 0f, 15f), Math.abs(delta));
        reportExtras();
    }

    public void reportExtras() {
        game.reportExtrasChanged(this, String.valueOf(missileSalvos), new DecimalFormat("#.#").format(laserTime));
    }

    private void fireLaser(float delta) {
        if (!autoFireLaser || !isControlled() || laserTime <= 0f)
            return;
        modLaserTime(-delta);
        scene.getLaserController().addLaser(scene.getPlayer().getPosition());
        float damage = shipVariant.getLaserDamage(playerState) * delta;
        for (EnemyShip enemy : scene.beamCast(this)) {
            enemy.applyDamage(damage, true);
        }
    }

    private void fireMissiles(float delta) {
        if (!autoFireMissiles || !isControlled() || missileSalvos <= 0)
            return;

        missileClock -= delta;
        if (missileClock > 0f)
            return;
        missileClock += missileInterval;
        modMissileSalvos(-1);
        float ports = shipVariant.missilePorts;
        float speed = Missile.unpoweredSpeed / ports;
        float damage = shipVariant.getMissileDamage(playerState);
        Vector3 pos = this.getPosition().cpy();
        for (float x = -ports * 0.5f + 0.5f; x < ports * 0.5f + 0.5f; x++) {
            float offset = x * speed * Missile.maxUnpoweredTime;
            Missile missile = scene.getMissilesPool().obtain();
            missile.setDamage(damage);
            missile.setUnpoweredDir(x * speed, 0f, 0f);
            missile.setPosition(pos);
            missile.setTargetVector(pos.cpy().add(offset, 0f, 1000f));
            missile.applyTRS();
        }
    }

    public void setNoControlTime(float time) {
        this.noControlTimer = time;
        final boolean newHpBarEnabled = time <= 0f
                && godModeTimer <= 0f
                && scene.getGameBounds().contains(getPosition());
        if (hpBarEnabled != newHpBarEnabled) {
            hpBarEnabled = newHpBarEnabled;
            game.reportHpEnabled(this, hpBarEnabled);
        }
    }

    @Override
    public boolean isTargetable() {
        return true;
    }

    @Override
    public Vector3 estimatePosition(float time) {
        return getPosition().cpy().mulAdd(moveVector.cpy().sub(position).nor(), time);
    }

    public void setAutoFireMissiles(boolean autoFire) {
        this.autoFireMissiles = autoFire;
    }

    public void setAutoFireLaser(boolean autoFire) {
        this.autoFireLaser = autoFire;
    }
}
