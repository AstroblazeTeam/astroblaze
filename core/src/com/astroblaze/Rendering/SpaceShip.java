package com.astroblaze.Rendering;

import com.astroblaze.AstroblazeGame;
import com.astroblaze.Interfaces.ITargetable;
import com.astroblaze.PlayerState;
import com.astroblaze.Utils.MathHelper;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public abstract class SpaceShip extends Renderable implements ITargetable {
    protected final Vector3 moveVector = new Vector3();
    protected final Scene3D scene;
    protected final PlayerState playerState;
    protected final AstroblazeGame game;
    protected final Array<DecalController.DecalInfo> exhaustDecals = new Array<>(8);

    protected float hp;
    protected float gunClock = 0f;
    protected float turretClock = 0f;
    protected float turretAngle = 0f;
    protected float turretAngularSpeed = 180f;

    protected SpaceShip(Scene3D scene) {
        this.scene = scene;
        this.game = AstroblazeGame.getInstance();
        this.playerState = AstroblazeGame.getPlayerState();
    }

    public float getTurretAngle() {
        return turretAngle;
    }

    public Scene3D getScene() {
        return scene;
    }

    @Override
    public void hide(Scene3D scene) {
        super.hide(scene);
        scene.getTurretsController().removeTurrets(this);
    }

    public float getHitpoints() {
        return this.hp;
    }

    public abstract float getMaxHitpoints();

    public abstract float getModelScale();

    public abstract int getTurretAmount();

    public abstract float getTurretBulletSpeed();

    public abstract float getTurretFireInterval();

    public abstract float getTurretDamage();

    public abstract ITargetable getClosestTargetable();

    protected abstract void addBullet(Vector3 pos, Vector3 vel, float damage);

    private void moveTurretAngle(float targetAngle, float delta) {
        turretAngle = MathHelper.moveTowardsAngle(turretAngle, targetAngle, delta * turretAngularSpeed);
    }

    protected void fireTurrets(float delta) {
        ITargetable t = getClosestTargetable();
        if (t == null) {
            moveTurretAngle(0f, delta);
            return;
        }

        final float distance = (float) Math.sqrt(t.distanceSquaredTo(position));
        if (distance > 1000f) { // don't shoot from off-screen
            moveTurretAngle(0f, delta);
            return;
        }

        final Vector3 targetPos = t.estimatePosition(distance / getTurretBulletSpeed());
        final Vector3 dir = new Vector3(targetPos.cpy().sub(position).nor());
        //noinspection SuspiciousNameCombination
        final float angle = MathUtils.atan2(dir.x, dir.z) * MathUtils.radiansToDegrees;
        if (Float.isNaN(angle) || Float.isInfinite(angle)) {
            moveTurretAngle(0f, delta);
            return;
        }

        // prevent firing until turret clock clears
        moveTurretAngle(angle, delta);
        turretClock -= delta;
        if (turretClock >= 0f) {
            return;
        }
        turretClock += getTurretFireInterval();

        final int turretCount = getTurretAmount();
        final Vector3 vel = new Vector3(0f, 0f, getTurretBulletSpeed())
                .rotate(Vector3.Y, turretAngle);
        final float offset = 0.5f * getModelScale() / turretCount;
        final float damage = getTurretDamage();
        final Quaternion rot = TurretsController.getTurretRotation(this);
        for (float x = -turretCount * 0.5f; x < turretCount * 0.5f; x++) {
            Vector3 curOffset = new Vector3((x + 0.5f) * offset, 0f, 1f).mul(rot);

            addBullet(position.cpy().add(curOffset), vel, damage);
        }
    }

    @Override
    public float distanceSquaredTo(Vector3 pos) {
        return position.dst2(pos);
    }
}
