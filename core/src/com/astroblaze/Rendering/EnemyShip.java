package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;

public class EnemyShip extends SpaceShip implements ICollisionProvider {
    private float modelRadius;
    private float moveClock = 0f;
    private final float moveMagnitude = 50f;
    private final float moveClockSpeed = 2f;
    private float aiDecisionClock = 0f;
    private Vector3 aiDecisionMove = new Vector3();
    private EnemyType typeId = EnemyType.Idle;
    private boolean enabled;

    private final float exhaustScaleModifier = 1f / 10f;

    public EnemyShip(Scene3D scene) {
        super(scene);
        setType(EnemyType.Idle);
    }

    @Override
    public void show(Scene3D scene) {
        super.show(scene);
        exhaustDecals.add(scene.getDecalController().addExhaust(position, 0f,
                exhaustScaleModifier * typeId.modelScale));
        for (DecalController.DecalInfo d : exhaustDecals) {
            // flip exhaust position (enemy forward is reverse from player)
            d.angle = -d.angle;
            d.position.z = -d.position.z;
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

    public float getMaxHitpoints() {
        return this.typeId.hp;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    @Override
    public boolean isTargetable() {
        return typeId != EnemyType.Rammer;
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
        setRotation(new Quaternion(Vector3.Y, 180f)); // rotate to face left
        setScale(typeId.modelScale);
        moveVector.set(0f, 0f, -typeId.speed);
        applyTRS();
        hp = typeId.hp;
        gunClock = MathUtils.random(0f, typeId.gunFireInterval);
        turretClock = MathUtils.random(0f, typeId.turretFireInterval);

        for (DecalController.DecalInfo d : exhaustDecals) {
            d.decal.setScale(exhaustScaleModifier * typeId.modelScale);
        }

        setEnabled(true);
    }

    private void fireGuns(float delta) {
        gunClock -= delta;
        if (gunClock >= 0f) {
            return;
        }

        gunClock += typeId.gunFireInterval;
        final float count = typeId.guns;
        final Vector3 pos = this.getPosition().cpy();
        final Vector3 vel = new Vector3(0, 0, -typeId.turretBulletSpeed);
        final float offset = this.modelRadius * getScale().x / count * 0.5f;
        for (float x = -count * 0.5f + 0.5f; x < count * 0.5f + 0.5f; x++) {
            scene.getDecalController().addBullet(pos.cpy().add(x * offset, 0f, -3f), vel, 0.1f, typeId.gunDamage)
                    .ignoreEnemyCollision = true;
        }
    }

    private void fireTurrets(float delta) {
        turretClock -= delta;
        if (turretClock >= 0f) {
            return;
        }
        turretClock += typeId.turretFireInterval;

        final Vector3 pos = this.getPosition().cpy();
        final Vector3 vel = new Vector3();
        final ITargetable t = scene.getPlayer();
        if (t != null && t.isTargetable()) {
            final float distance = (float) Math.sqrt(t.distanceSquaredTo(pos.cpy()));
            final Vector3 targetPos = t.estimatePosition(distance / typeId.turretBulletSpeed);
            if (!scene.getGameBounds().contains(t.getPosition())) {
                return;
            }
            final Vector3 dir = targetPos.cpy().sub(pos).nor();
            final int turretPorts = typeId.turrets;
            final float turretOffset = 0.5f * typeId.modelScale / turretPorts;
            final float angle = MathUtils.atan2(dir.x, dir.z) * MathUtils.radiansToDegrees;
            if (Float.isNaN(angle) || Float.isInfinite(angle)) {
                return;
            }
            vel.set(0f, 0f, typeId.turretBulletSpeed).rotate(Vector3.Y, angle);
            for (float x = -turretPorts * 0.5f + 0.5f; x < turretPorts * 0.5f + 0.5f; x++) {
                scene.getDecalController().addBullet(pos.cpy().add(x * turretOffset, 0f, 3f), vel, 0.1f, typeId.turretDamage)
                        .ignoreEnemyCollision = true;
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!enabled)
            return;

        fireGuns(delta);
        fireTurrets(delta);

        aiDecisionClock -= delta;
        moveClock += moveClockSpeed * delta;
        switch (this.typeId) {
            default:
            case Idle:
            case Simple:
            case MoneyDrop:
                getPosition().mulAdd(moveVector, delta);
                break;
            case SineWave: // zigzag enemy, shooting diagonals
                if (moveClock > 2 * MathUtils.PI)
                    moveClock -= 2 * MathUtils.PI;
                float phase = MathUtils.sin(moveClock);
                getPosition().mulAdd(moveVector, delta).add(phase * moveMagnitude * delta, 0f, 0f);
                break;
            case Boss:
                if (typeId.aiMoveDecisionTime != 0f) {
                    if (aiDecisionClock < 0f) {
                        aiDecisionClock += typeId.aiMoveDecisionTime;
                        BoundingBox bb = scene.getGameBounds();
                        aiDecisionMove.set(
                                MathUtils.random(bb.min.x, bb.max.x),
                                0f,
                                MathUtils.random(bb.min.z, bb.max.z));
                    }

                    MathHelper.moveTowards(this.position, aiDecisionMove, typeId.speed * delta);
                    moveVector.set(aiDecisionMove.cpy().sub(this.position).nor());
                } else {
                    aiDecisionClock = 0f;
                }

                break;
        }

        applyTRS();
    }

    @Override
    public void render(ModelBatch batch, Environment environment) {
        super.render(batch, environment);
    }

    @Override
    public boolean checkCollision(Vector3 pos, float radius) {
        if (!enabled || this.hp <= 0f)
            return false;
        float dst2 = this.getPosition().dst2(pos);
        radius += modelRadius * getScale().x; // assume uniform scale
        return radius * radius > dst2;
    }

    @Override
    public void damageFromCollision(float damage, boolean isPlayer) {
        if (!enabled)
            return;
        this.hp -= damage;
        if (this.hp <= 0f) {
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
}