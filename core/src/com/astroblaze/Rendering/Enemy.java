package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Enemy extends Renderable implements ICollisionProvider, ITargetable {
    private final Scene3D scene;
    private final Vector3 moveVector = new Vector3();
    private float modelRadius;
    private float phaseClock = 0f;
    private final float phaseMagnitude = 50f;
    private final float phaseSpeed = 2f;
    private final float gunInterval = 1f / 1f;
    private final float gunDamage = 5f;
    private float gunClock = 0f;
    private int gunPellets = 8;
    private EnemyType typeId = EnemyType.Idle;
    private Sound explosionSound;
    private boolean enabled;
    private float hitpoints;

    public Enemy(Scene3D scene, EnemyType typeId) {
        this.scene = scene;
        setType(typeId);
    }

    public float getHitpoints() {
        return this.hitpoints;
    }

    public float getMaxHitpoints() {
        return this.typeId.hp;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setType(EnemyType enemyType) {
        this.typeId = enemyType;
        this.setModel(enemyType.modelDescriptor);

        // just an approximation of radius, don't need exact
        BoundingBox bb = new BoundingBox();
        Assets.asset(enemyType.modelDescriptor).calculateBoundingBox(bb);
        modelRadius = Math.max(bb.getWidth(), Math.max(bb.getHeight(), bb.getDepth())) * 0.5f;
        reset(scene.getGameBounds());
    }

    public void reset(BoundingBox bb) {
        setPosition(new Vector3(MathUtils.random(bb.min.x, bb.max.x) * 0.9f, 0f, bb.max.z * 1.1f));
        setRotation(new Quaternion(Vector3.Y, 180f));
        addRotation(new Quaternion(Vector3.Z, MathUtils.random(0, 360f)));
        setScale(typeId.modelScale);
        moveVector.set(0f, 0f, -typeId.speed);
        applyTRS();
        hitpoints = typeId.hp;
        gunClock = MathUtils.random(0f, gunInterval);

        // specialization per enemy type
        gunPellets = typeId.gunPellets;

        setEnabled(true);
    }

    public void fireGuns(float delta) {
        gunClock -= delta;
        if (gunClock >= 0f) {
            return;
        }

        gunClock = gunInterval;
        final float count = gunPellets;
        final Vector3 pos = this.getPosition().cpy();
        final Vector3 vel = new Vector3(0, 0, -3f * moveVector.len());
        final float offset = this.modelRadius / count * 0.5f;
        for (float x = -count * 0.5f + 0.5f; x < count * 0.5f + 0.5f; x++) {
            scene.getDecalController().addBullet(pos.cpy().add(x * offset, 0f, -3f), vel, 0.1f, gunDamage)
                    .ignorePlayerCollision = false;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!enabled)
            return;

        fireGuns(delta);

        phaseClock += phaseSpeed * delta;
        switch (this.typeId) {
            default:
            case Idle:
            case Simple:
            case MoneyDrop:
                getPosition().mulAdd(moveVector, delta);
                break;
            case SineWave: // zigzag enemy, shooting diagonals
                if (phaseClock > 2 * MathUtils.PI)
                    phaseClock -= 2 * MathUtils.PI;
                float phase = MathUtils.sin(phaseClock);
                getPosition().mulAdd(moveVector, delta).add(phase * phaseMagnitude * delta, 0f, 0f);
                break;
        }

        addRotation(new Quaternion(Vector3.Z, delta * 90f));
        applyTRS();
    }

    @Override
    public void render(ModelBatch batch, Environment environment) {
        super.render(batch, environment);
    }

    @Override
    public boolean checkCollision(Vector3 pos, float radius) {
        if (!enabled || this.hitpoints <= 0f)
            return false;
        float dst2 = this.getPosition().dst2(pos);
        radius += modelRadius * getScale().x; // assume uniform scale
        return radius * radius > dst2;
    }

    @Override
    public void damageFromCollision(float damage, boolean isPlayer) {
        if (!enabled)
            return;
        this.hitpoints -= damage;
        if (this.hitpoints <= 0f) {
            scene.removeActor(this);
            AstroblazeGame.getSoundController().playExplosionSound();
            scene.getDecalController().addExplosion(this.getPosition(), moveVector, 0.1f);

            if (isPlayer) {
                AstroblazeGame.getPlayerState().modPlayerScore(typeId.value);
            }
            dropBonus();
        }
    }

    private void dropBonus() {
        dropBonus(scene.rollRandomBonus());
    }

    private void dropBonus(IPlayerBonus bonus) {
        if (bonus == null) // random chance for no bonus drop
            return;
        scene.getDecalController().addBonus(this.getPosition(), bonus);
    }

    @Override
    public Vector3 estimatePosition(float time) {
        return position.cpy().mulAdd(moveVector, time);
    }

    @Override
    public float distanceSquaredTo(Vector3 pos) {
        return position.dst2(pos);
    }

    @Override
    public boolean isTargetable() {
        return typeId != EnemyType.Rammer;
    }
}

