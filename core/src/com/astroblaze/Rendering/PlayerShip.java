package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.TimeUtils;

import java.text.DecimalFormat;

public class PlayerShip extends SpaceShip {
    public final float respawnNoControlTime = 1f;

    private final float gunBulletSpeed = 200f;
    private final float missileInterval = 1f / 8f;
    private final float deathTimerMax = 5f;
    private final float destroyExplosionInterval = 0.1f;
    private final int defaultMissileSalvos = 8;

    private float missileClock;
    private float moveSpeed;
    private float currentBank;
    private float maxLaserTime;
    private float laserTime; // time remaining for laser shots
    private int maxMissileSalvos; // maximum amount of missile salvos
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

        scene.getTurretsController().addTurret(this);
    }

    public float getRadius() {
        return shipVariant.modelScale * 0.4f;
    }

    public float getMaxHitpoints() {
        return shipVariant.getMaxHp(playerState);
    }

    public int getMissileSalvos() {
        return missileSalvos;
    }

    public void modMissileSalvos(int mod) {
        missileSalvos = MathUtils.clamp(missileSalvos + mod, 0, maxMissileSalvos);
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
            lastShieldHit = TimeUtils.millis();
            AstroblazeGame.getLevelStatTracker().addDamageTaken(hpModifier);
            scene.getCamera().shake();
            AstroblazeGame.getSoundController().playShieldSound();
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
        turretAngularSpeed = variant.getTurretSpeed(playerState);
        turretDefaultAngle = 0f;
        turretAngle = turretDefaultAngle;
        maxLaserTime = variant.getLaserCapacity(playerState);
        laserTime = maxLaserTime;
        maxMissileSalvos = variant.getMaxMissiles(playerState);
        modMissileSalvos(maxMissileSalvos);
        gunInterval = 1f / 15f;
        setMoveVector(new Vector3(0f, 0f, scene.getGameBounds().min.z + getRadius() * 3f), true);
        setPosition(scene.getRespawnPosition());
        setRotation(new Quaternion());
        applyTRS();
    }

    public void setGodModeTimer(float time) {
        this.godModeTimer = time;
    }

    @Override
    public int getGunAmount() {
        return shipVariant.gunPorts;
    }

    @Override
    public float getGunDamage() {
        return shipVariant.getGunDamage(playerState);
    }

    @Override
    public float getGunBulletSpeed() {
        return gunBulletSpeed;
    }

    @Override
    public float getGunFireInterval() {
        return gunInterval;
    }

    @Override
    public float getModelScale() {
        return shipVariant.modelScale;
    }

    @Override
    public int getTurretAmount() {
        return shipVariant.turretPorts;
    }

    @Override
    public float getTurretBulletSpeed() {
        return gunBulletSpeed;
    }

    @Override
    public float getTurretFireInterval() {
        return gunInterval;
    }

    @Override
    public float getTurretDamage() {
        return shipVariant.getTurretDamage(playerState);
    }

    @Override
    protected void addBullet(Vector3 pos, Vector3 vel, float damage) {
        scene.getDecalController().addBullet(pos, vel, 0.1f, damage)
                .ignorePlayerCollision = true;
    }

    @Override
    public ITargetable getClosestTargetable() {
        return isControlled()
                ? scene.getClosestTarget(this, position)
                : null;
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
        laserTime = MathHelper.moveTowards(laserTime, MathUtils.clamp(laserTime + delta, 0f, maxLaserTime), Math.abs(delta));
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
