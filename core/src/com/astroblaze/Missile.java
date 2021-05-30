package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Missile extends Renderable {
    private final float moveSpeed = 80f;
    private final Vector3 moveVector = new Vector3();

    public void setTargetVector(Vector3 moveVector) {
        this.moveVector.set(moveVector);
        setScale(0.75f);
    }

    public Missile(Scene3D scene, Model model) {
        super(scene, model);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Vector3 currentPos = getPosition();
        Vector3 diff = moveVector.cpy().sub(currentPos);
        float travelDist = moveSpeed * delta;

        if (diff.len() > travelDist) {
            currentPos.mulAdd(diff.nor(), travelDist);
        } else {
            currentPos = moveVector.cpy();
        }

        setPosition(currentPos);
        addRotation(new Quaternion(Vector3.Z, delta * 360f));
        applyTRS();
    }
}
