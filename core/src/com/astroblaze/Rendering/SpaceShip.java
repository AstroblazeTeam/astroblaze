package com.astroblaze.Rendering;

import com.astroblaze.AstroblazeGame;
import com.astroblaze.Interfaces.ITargetable;
import com.astroblaze.PlayerState;
import com.astroblaze.Utils.MathHelper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public abstract class SpaceShip extends Renderable implements ITargetable {
    protected final Vector3 moveVector = new Vector3();
    protected final Scene3D scene;
    protected final PlayerState playerState;
    protected final AstroblazeGame game;
    protected final Array<DecalController.DecalInfo> exhaustDecals = new Array<>(8);

    protected float hp;
    protected long lastShieldHit;

    protected float gunClock = 0f;
    protected float gunInterval;

    protected float turretDefaultAngle = 0f;
    protected float turretClock = 0f;
    protected float turretAngle = 0f;
    protected float turretAngularSpeed;

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
        for (DecalController.DecalInfo d : exhaustDecals) {
            d.life = 0f;
        }
        exhaustDecals.clear();
    }

    public float getHitpoints() {
        return this.hp;
    }

    public abstract float getMaxHitpoints();

    public abstract float getModelScale();

    public abstract int getGunAmount();

    public abstract float getGunDamage();

    public abstract float getGunBulletSpeed();

    public abstract float getGunFireInterval();

    public abstract int getTurretAmount();

    public abstract float getTurretBulletSpeed();

    public abstract float getTurretFireInterval();

    public abstract float getTurretDamage();

    public abstract ITargetable getClosestTargetable();

    protected abstract void addBullet(Vector3 pos, Vector3 vel, float damage);

    protected void fireGuns(float delta) {
        gunClock -= delta;
        if (gunClock >= 0f) {
            return;
        }
        gunClock += getGunFireInterval();

        final float count = getGunAmount();
        final float damage = getGunDamage();
        final Vector3 vel = new Vector3(0, 0, getGunBulletSpeed());
        final float offset = getModelScale() / count * 0.5f;
        for (float x = -count * 0.5f + 0.5f; x < count * 0.5f + 0.5f; x++) {
            addBullet(position.cpy().add(x * offset, 0f, -3f), vel, damage);
        }
    }

    private void moveTurretAngle(float targetAngle, float delta) {
        turretAngle = MathHelper.moveTowardsAngle(turretAngle, targetAngle, delta * turretAngularSpeed);
    }

    protected void fireTurrets(float delta) {
        ITargetable t = getClosestTargetable();
        if (t == null) {
            moveTurretAngle(turretDefaultAngle, delta);
            return;
        }

        final float distance = (float) Math.sqrt(t.distanceSquaredTo(position));
        if (distance > 1000f) { // don't shoot from off-screen
            moveTurretAngle(turretDefaultAngle, delta);
            return;
        }

        final Vector3 targetPos = t.estimatePosition(distance / getTurretBulletSpeed());
        final Vector3 dir = new Vector3(targetPos.cpy().sub(position).nor());
        //noinspection SuspiciousNameCombination
        final float angle = MathUtils.atan2(dir.x, dir.z) * MathUtils.radiansToDegrees;
        if (Float.isNaN(angle) || Float.isInfinite(angle)) {
            moveTurretAngle(turretDefaultAngle, delta);
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

    public Color getTintColor() {
        final float timeDiff = TimeUtils.timeSinceMillis(lastShieldHit) / 1000f;
        return new Color(1f, 0f, 0f, 1f)
                .lerp(new Color(1f, 1f, 1f, 1f), timeDiff);
    }

    @Override
    public void render(ModelBatch batch, Environment environment) {
        if (visible && modelInstance != null) {
            modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(getTintColor()));
        }

        super.render(batch, environment);
    }
}
