package com.astroblaze;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Enemy extends Renderable implements CollisionProvider {
    private final Vector3 moveVector = new Vector3();
    private final float maxHp = 100f;
    private float modelRadius;
    private float hp;
    private float modelScale = 1f;
    private float phaseClock = 0f;
    private float phaseMagnitude = 50f;
    private float phaseSpeed = 2f;
    private float gunInterval = 1f / 1f;
    private float gunClock = 0f;
    private float gunDamage = 5f;
    private int gunPellets = 8;
    private int modelId = 0;
    private Sound explosionSound;

    public Enemy(Scene3D scene, int typeId) {
        super(scene);

        setType(typeId);
    }

    public void setType(int enemyTypeId) {
        Model model = Assets.enemyModels.get(enemyTypeId);
        this.modelId = enemyTypeId;
        this.setModel(new ModelInstance(model));

        // just an approximation of radius, don't need exact
        BoundingBox bb = new BoundingBox();
        model.calculateBoundingBox(bb);
        modelRadius = Math.max(bb.getWidth(), Math.max(bb.getHeight(), bb.getDepth())) * 0.5f;
        reset(scene.gameBounds);
    }

    public void reset(BoundingBox bb) {
        switch (this.modelId) {
            default:
            case 0: // simple forward shooting enemy
                modelScale = 1f;
                gunPellets = 2;
                break;
            case 1: // fast enemy, shooting diagonals
                modelScale = 0.5f;
                gunPellets = 3;
                break;
            case 2: // money tanker
                modelScale = 0.35f;
                gunPellets = 1;
                break;
        }

        setPosition(new Vector3(MathUtils.random(bb.min.x, bb.max.x) * 0.9f, 0f, bb.max.z * 1.1f));
        setRotation(new Quaternion(Vector3.Y, 180f));
        addRotation(new Quaternion(Vector3.Z, MathUtils.random(0, 360f)));
        setScale(modelScale);
        moveVector.set(0f, 0f, -30f);
        applyTRS();
        hp = maxHp;
        gunClock = MathUtils.random(0f, gunInterval);
        explosionSound = Assets.asset(Assets.explosion);
    }

    public void fireGuns() {
        final float count = gunPellets;
        final Vector3 pos = this.getPosition().cpy();
        final Vector3 vel = new Vector3(0, 0, -3f * moveVector.len());
        final float span = this.modelRadius / count;
        for (float x = -count * 0.5f + 0.5f; x < count * 0.5f + 0.5f; x++) {
            scene.decals.addBullet(pos.cpy().add(x * span, 0f, -3f), vel, 0.1f, gunDamage)
                    .fromPlayer = false;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        gunClock -= delta;
        if (gunClock < 0f) {
            gunClock = gunInterval;

            fireGuns();
        }

        phaseClock += phaseSpeed * delta;
        switch (this.modelId) {
            default:
            case 0: // simple forward shooting enemy
            case 2: // money tanker
                getPosition().mulAdd(moveVector, delta);
                break;
            case 1: // zigzag enemy, shooting diagonals
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
        float dst2 = this.getPosition().dst2(pos);
        radius += modelRadius * getScale().x; // assume uniform scale
        return radius * radius > dst2;
    }

    @Override
    public void damageFromCollision(float damage) {
        this.hp -= damage;
        if (this.hp <= 0f) {
            scene.removeActors.add(this);
            explosionSound.play(1f, 1f, MathUtils.random(-1f, 1f));
            scene.decals.addExplosion(this.getPosition(), moveVector, 0.1f);
        }
    }
}

