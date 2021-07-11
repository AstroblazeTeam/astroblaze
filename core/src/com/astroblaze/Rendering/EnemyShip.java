package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.TimeUtils;

public class EnemyShip extends SpaceShip implements ICollisionProvider {
    private final LevelStatTracker statTracker;
    private float modelRadius;
    private float moveClock = 0f;
    private final float moveMagnitude = 50f;
    private final float moveClockSpeed = 2f;
    private float aiDecisionClock = 0f;
    private final Vector3 aiDecisionMove = new Vector3();
    private EnemyType typeId = EnemyType.Simple;
    private boolean enabled;

    private final float exhaustScaleModifier = 1f / 10f;

    public EnemyShip(Scene3D scene) {
        super(scene);
        setType(typeId);
        statTracker = AstroblazeGame.getLevelStatTracker();
    }

    @Override
    public void show(Scene3D scene) {
        super.show(scene);
        exhaustDecals.add(scene.getDecalController().addExhaust(this, 0f,
                exhaustScaleModifier * typeId.modelScale));
        for (DecalController.DecalInfo d : exhaustDecals) {
            // flip exhaust position (enemy forward is reverse from player)
            d.angle = -d.angle;
            d.position.z = -d.position.z;
        }
    }

    @Override
    public float getMaxHitpoints() {
        int level = AstroblazeGame.getLevelController() != null
                ? AstroblazeGame.getLevelController().getLevel()
                : AstroblazeGame.getPlayerState().getMaxLevel();
        return typeId.baseHp + typeId.hpPerLevel * level;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public void setMoveVector(Vector3 newVector) {
        moveVector.set(newVector);
    }

    public void reset(BoundingBox bb) {
        setPosition(new Vector3(MathUtils.random(bb.min.x, bb.max.x) * 0.9f, 0f, bb.max.z * 1.1f));
        setRotation(new Quaternion(Vector3.Y, 180f)); // rotate to face left
        setScale(typeId.modelScale);
        moveVector.set(0f, 0f, -typeId.speed);
        applyTRS();
        hp = getMaxHitpoints();
        gunClock = MathUtils.random(0f, typeId.gunFireInterval);
        gunInterval = typeId.gunFireInterval;
        turretClock = MathUtils.random(0f, typeId.turretFireInterval);
        turretAngle = 180f;
        scene.getTurretsController().removeTurrets(this);
        if (typeId.turrets >= 1) {
            scene.getTurretsController().addTurret(this);
        }
        for (DecalController.DecalInfo d : exhaustDecals) {
            d.decal.setScale(exhaustScaleModifier * typeId.modelScale);
        }

        setEnabled(true);
    }

    @Override
    public int getGunAmount() {
        return typeId.guns;
    }

    @Override
    public float getGunDamage() {
        return typeId.gunDamage;
    }

    @Override
    public float getGunBulletSpeed() {
        return -typeId.gunBulletSpeed;
    }

    @Override
    public float getGunFireInterval() {
        return typeId.gunFireInterval;
    }

    @Override
    public float getModelScale() {
        return typeId.modelScale;
    }

    @Override
    public int getTurretAmount() {
        return typeId.turrets;
    }

    @Override
    public float getTurretBulletSpeed() {
        return typeId.turretBulletSpeed;
    }

    @Override
    public float getTurretFireInterval() {
        return typeId.turretFireInterval;
    }

    @Override
    public float getTurretDamage() {
        return typeId.turretDamage;
    }

    @Override
    protected void addBullet(Vector3 pos, Vector3 vel, float damage) {
        scene.getDecalController().addBullet(pos, vel, 0.1f, damage)
                .ignoreEnemyCollision = true;
    }

    @Override
    public ITargetable getClosestTargetable() {
        final ITargetable t = scene.getPlayer();
        return t != null && t.isTargetable() ? t : null;
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
            case MiniBoss1:
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
    public boolean checkCollision(Vector3 pos, float radius) {
        if (!enabled || this.hp <= 0f)
            return false;
        float dst2 = this.getPosition().dst2(pos);
        radius += modelRadius * getScale().x; // assume uniform scale
        return radius * radius > dst2;
    }

    @Override
    public void applyDamage(float damage, boolean isPlayer) {
        if (!enabled)
            return;
        this.hp -= damage;
        statTracker.addDamageDone(damage);
        lastShieldHit = TimeUtils.millis();
        if (this.hp <= 0f) { // die, give kills/score/reward and drop bonuses
            scene.removeActor(this);
            AstroblazeGame.getSoundController().playExplosionSound();
            scene.getDecalController().addExplosion(this.getPosition(), moveVector, 0.1f);

            if (isPlayer) {
                statTracker.addKill(typeId);
                AstroblazeGame.getPlayerState().modPlayerScore(typeId.value);
                if (typeId == EnemyType.Boss) {
                    float reward = AstroblazeGame.getLevelController().getCurrentLevelReward();
                    AstroblazeGame.getPlayerState().modPlayerMoney(reward);
                }
            }
            if (typeId != EnemyType.Boss) { // boss doesn't drop bonuses
                dropBonus();
            }
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