package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.Random;

public class Enemy extends Renderable implements CollisionProvider {
    private final Vector3 moveVector = new Vector3();
    private final BoundingBox bb = new BoundingBox();
    private final float modelRadius;
    private final float maxHp = 100f;
    private float hp;

    public Enemy(Scene3D scene, Model model) {
        super(scene, model);
        model.calculateBoundingBox(bb);

        // just an approximation, don't need exact
        modelRadius = Math.max(bb.getWidth(), Math.max(bb.getHeight(), bb.getDepth())) * 0.5f;
    }

    public void reset(BoundingBox bb) {
        setPosition(new Vector3(MathUtils.random(bb.min.x, bb.max.x) * 0.9f, 0f, bb.max.z));
        setRotation(new Quaternion(Vector3.Y, 180f));
        setScale(0.25f);
        moveVector.set(0f, 0f, -30f);
        applyTRS();
        hp = maxHp;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        getPosition().mulAdd(moveVector, delta);
        addRotation(new Quaternion(Vector3.Z, delta * 90f));
        applyTRS();
    }

    @Override
    public boolean CheckCollision(Vector3 pos, float radius) {
        float dst2 = this.getPosition().dst2(pos);
        radius += modelRadius * getScale().x; // assume uniform scale
        return radius * radius > dst2;
    }

    @Override
    public void damageFromCollision(float damage) {
        this.hp -= damage;
        if (this.hp <= 0f) {
            scene.removeActors.add(this);
            Assets.asset(Assets.explosion).play(1f, 1f, MathUtils.random(-1f, 1f));
            scene.decals.addExplosion(this.getPosition(), moveVector, 0.1f);
        }
    }
}

