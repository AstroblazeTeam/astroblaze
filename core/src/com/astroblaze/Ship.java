package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Ship extends Renderable {
    private final float bankSpeed = 90f;
    public final float respawnNoControlTime = 1f;
    private float moveSpeed = 80f;
    private float currentBank;
    private final Vector3 moveVector = new Vector3();
    private float fireInterval = 1f / 2f;
    private float fireIntervalGun = 1f / 20f;
    private float fireClock = 0f;
    private float fireClockGun = 0f;
    private float gunDamage = 3f;
    private float noControlTimer;
    private float maxHp = 100f;
    private float hp;
    private float radius = 3f;
    private boolean isDying;
    private float deathTimer;
    private float deathTimerMax = 5f;
    private float destroyExplosionTimer = 0f;
    private float destroyExplosionInterval = 0.1f;
    private float godModeTimer = 0f;
    private float godModeTimerOnDeath = 3f;

    public Ship(Scene3D scene, Model model) {
        super(scene, model);
        reset();
    }

    public float getRadius() {
        return this.radius;
    }

    public float getHp() {
        return this.hp;
    }

    public float getMaxHp() {
        return this.maxHp;
    }

    public void modHp(float hpModifier) {
        final float oldHp = hp;
        if (hpModifier < 0f && godModeTimer > 0f) {
            return;
        }
        hp = MathUtils.clamp(hp + hpModifier, 0f, maxHp);
        AstroblazeGame.getInstance().reportHpChanged(this, hp, oldHp);
        Gdx.app.log("Ship", "Player hp modded from " + oldHp + " to " + hp);
        if (hp < 0f) {
            setNoControlTime(deathTimer);
        }
    }

    public void modGunDamage(float mod) {
        this.gunDamage = MathUtils.clamp(this.gunDamage + mod, 10f, 100f);
    }

    public void setMoveVector(Vector3 moveVector, boolean force) {
        if (!force && noControlTimer > 0f)
            return;
        this.moveVector.set(moveVector);
    }

    public void reset() {
        modHp(maxHp);
        noControlTimer = respawnNoControlTime;
        deathTimer = deathTimerMax;
        moveVector.setZero();
        isDying = false;
        // set to slightly closer than destroy bounds
        setPosition(new Vector3(0f, 0f, scene.destroyBounds.min.z + 5f));
        setRotation(new Quaternion());
        setScale(0.5f);
        applyTRS();
    }

    public void setGodModeTimer(float time) {
        this.godModeTimer = time;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        this.setNoControlTime(noControlTimer - delta);

        Vector3 currentPos = getPosition().cpy();
        Vector3 diff = moveVector.cpy().sub(currentPos);

        if (MathUtils.isEqual(currentBank, 0f, 0.1f) && MathUtils.isEqual(diff.x, 0f, 0.1f)) {
            currentBank = 0f;
        } else {
            currentBank = MathHelper.moveTowards(currentBank, -diff.x, bankSpeed * delta);
            currentBank = MathUtils.clamp(currentBank, -15f, +15f);
        }

        MathHelper.moveTowards(currentPos, moveVector, moveSpeed * delta);
        setPosition(currentPos);
        setRotation(new Quaternion(Vector3.Z, currentBank));
        applyTRS();

        if (!isDying) {
            fireClock -= delta;
            if (isControlled() && fireClock < 0f) {
                fireClock = fireInterval;

                Missile missile = scene.getMissilesPool().obtain();
                Vector3 pos = this.getPosition().cpy();
                missile.setPosition(pos);
                missile.setTargetVector(pos.cpy().add(0, 0, 1000f));
                missile.applyTRS();
            }

            fireClockGun -= delta;
            if (isControlled() && fireClockGun < 0f) {
                fireClockGun = fireIntervalGun;

                final Vector3 vel = new Vector3(0, 0, 3f * moveSpeed);
                scene.decals.addBullet(this.getPosition().cpy().add(+3f, 0f, 3f), vel, 0.1f, gunDamage);
                scene.decals.addBullet(this.getPosition().cpy().add(-3f, 0f, 3f), vel, 0.1f, gunDamage);
            }
            if (hp <= 0f) {
                isDying = true;
                setNoControlTime(deathTimer);
            }
        }

        // handle temporary godmode after respawn
        this.godModeTimer -= delta;
        this.visible = this.godModeTimer <= 0f || (((int) (this.godModeTimer * 8f)) % 2 == 0);

        // handle destroy explosions animation
        if (isDying) {
            moveSpeed = MathHelper.moveTowards(moveSpeed, 0f, 1f / deathTimerMax * delta);
            destroyExplosionTimer -= delta;
            deathTimer -= delta;

            if (destroyExplosionTimer < 0f) {
                destroyExplosionTimer = destroyExplosionInterval;
                Vector3 v = new Vector3();
                v.setToRandomDirection();
                DecalController.DecalInfo info = scene.decals.addExplosion(v.scl(MathUtils.random(radius, 2f * radius)),
                        new Vector3(), 0.05f);
                info.origin = this.getPosition();
            }

            if (deathTimer < 0f) {
                scene.playerDied();
            }
        }
    }

    public Vector3 getVelocity() {
        return moveVector.cpy().sub(getPosition()).nor().scl(moveSpeed);
    }

    public boolean isControlled() {
        return noControlTimer <= 0f;
    }

    public void setNoControlTime(float time) {
        this.noControlTimer = time;
        AstroblazeGame.getInstance().reportHpEnabled(this, time <= 0f);
    }
}
