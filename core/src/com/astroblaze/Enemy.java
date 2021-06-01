package com.astroblaze;

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
    private final float modelRadius;

    public Enemy(Scene3D scene, Model model) {
        super(scene, model);
        BoundingBox bb = new BoundingBox();
        model.calculateBoundingBox(bb);
        modelRadius = bb.getWidth() / 2f;
    }

    public void reset(BoundingBox bb) {
        setPosition(new Vector3(MathUtils.random(bb.min.x, bb.max.x) * 0.9f, 0f, bb.max.z));
        this.setRotation(new Quaternion(Vector3.Y, 180f));
        this.setScale(0.25f);
        this.moveVector.set(0f, 0f, -30f);
        this.applyTRS();
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
        radius += modelRadius * getScale().x;
        return radius * radius > dst2;
    }
}
