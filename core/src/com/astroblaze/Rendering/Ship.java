package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

public class Ship extends Renderable {
    public final float respawnNoControlTime = 1f;

    private final Scene3D scene;
    private final PlayerState playerState;
    private final AstroblazeGame game;
    private final Vector3 moveVector = new Vector3();
    private final float gunInterval = 1f / 20f;
    private final float radius = 3f;
    private final float deathTimerMax = 5f;
    private final float destroyExplosionInterval = 0.1f;

    private float moveSpeed;
    private float currentBank;
    private int missileSalvos = 0; // amount of missile salvos player has.
    private float gunClock = 0f;
    private float gunDamage;
    private float noControlTimer;
    private float hp;
    private float modelRadius = 1f;
    private boolean isDying;
    private float deathTimer;
    private float destroyExplosionTimer = 0f;
    private float godModeTimer = 0f;
    private boolean hpBarEnabled = false; // flag to avoid event spam
    private PlayerShipVariant shipVariant;

    private final Array<DecalController.DecalInfo> exhaustDecals = new Array<>(8);

    public Ship(Scene3D scene) {
        this.scene = scene;
        this.game = AstroblazeGame.getInstance();
        this.playerState = AstroblazeGame.getPlayerState();
    }

    @Override
    public void show(Scene3D scene) {
        super.show(scene);
        float engineScale = getShipVariant().getUpgradeModifier(playerState, UpgradeEntryType.SpeedUpgrade);
        exhaustDecals.add(scene.getDecalController().addExhaust(position, -modelRadius * 0.25f, 0.75f * engineScale));
        exhaustDecals.add(scene.getDecalController().addExhaust(position, 0f, 1.25f * engineScale));
        exhaustDecals.add(scene.getDecalController().addExhaust(position, +modelRadius * 0.25f, 0.75f * engineScale));
        // normalize to 0f..1f range
        float colorScale = MathUtils.map(1f, 1.5f, 0f, 1f, engineScale);
        Color startColor = Color.WHITE;
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

    public Scene3D getScene() {
        return this.scene;
    }

    public float getRadius() {
        return this.radius;
    }

    public float getMaxHp() {
        return shipVariant.getMaxHp(playerState);
    }

    public int getMissileSalvos() {
        return missileSalvos;
    }

    public void modMissileSalvos(int mod) {
        missileSalvos += mod;
        game.reportExtrasChanged(this, String.valueOf(missileSalvos), "");
    }

    public void modHp(float hpModifier) {
        final float oldHp = hp;
        if (hpModifier < 0f && godModeTimer > 0f) {
            return;
        }
        hp = MathUtils.clamp(hp + hpModifier, 0f, getMaxHp());
        game.reportStateChanged(this, hp, oldHp);
        if (hpModifier < 0f) {
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
        modHp(getMaxHp());
        gunDamage = variant.getDamage(playerState);
        moveSpeed = variant.getSpeed(playerState);
        setModel(variant.modelDescriptor);
        setScale(variant.modelScale);
        noControlTimer = respawnNoControlTime;
        deathTimer = deathTimerMax;
        isDying = false;
        modMissileSalvos(-missileSalvos);
        // set to slightly closer than destroy bounds
        modelRadius = variant.modelScale;
        setMoveVector(new Vector3(), true);
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
        if (gunClock >= 0f) {
            return;
        }
        gunClock += gunInterval;

        final Vector3 pos = this.getPosition().cpy();
        final Vector3 vel = new Vector3(0f, 0f, 3f * moveSpeed);
        final int ports = shipVariant.gunPorts;
        final float offset = this.modelRadius / ports * 0.5f;
        for (float x = -ports * 0.5f + 0.5f; x < ports * 0.5f + 0.5f; x++) {
            scene.getDecalController().addBullet(pos.cpy().add(x * offset, 0f, 3f), vel, 0.1f, gunDamage)
                    .ignorePlayerCollision = true;
        }

        if (shipVariant.turretPorts > 0) {
            ITargetable t = scene.getClosestTarget(pos);
            if (t != null) {
                final float distance = (float) Math.sqrt(t.distanceSquaredTo(pos));
                if (distance > 1000f) { // don't shoot from off-screen
                    return;
                }
                final Vector3 dir = t.estimatePosition(distance / vel.len()).cpy().sub(pos).nor();
                final int turretPorts = shipVariant.turretPorts;
                final float turretOffset = this.modelRadius / turretPorts * 0.5f;
                float angle = MathUtils.atan2(dir.x, dir.z) * MathUtils.radiansToDegrees;
                vel.set(0f, 0f, 3f * moveSpeed).rotate(Vector3.Y, angle);
                for (float x = -turretPorts * 0.5f + 0.5f; x < turretPorts * 0.5f + 0.5f; x++) {
                    scene.getDecalController().addBullet(pos.cpy().add(x * turretOffset, 0f, 3f), vel, 0.1f, 0.75f * gunDamage)
                            .ignorePlayerCollision = true;
                }
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

        if (!isDying) {
            fireGuns(delta);

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
                DecalController.DecalInfo info = scene.getDecalController().addExplosion(v.scl(MathUtils.random(radius, 2f * radius)),
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

    public void fireMissiles() {
        if (!isControlled())
            return;
        if (missileSalvos <= 0)
            return;

        modMissileSalvos(-1);
        float ports = shipVariant.missilePorts;
        float speed = Missile.unpoweredSpeed / ports;
        Vector3 pos = this.getPosition().cpy();
        for (float x = -ports * 0.5f + 0.5f; x < ports * 0.5f + 0.5f; x++) {
            float offset = x * speed * Missile.maxUnpoweredTime;
            Missile missile = scene.getMissilesPool().obtain();
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
}
