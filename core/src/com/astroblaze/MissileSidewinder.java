package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

public class MissileSidewinder extends Missile {
    private float unpoweredTime = 0.5f;
    private final float unpoweredDir;
    private final static Random rng = new Random();

    public MissileSidewinder(Scene3D scene, Model model) {
        super(scene, model);
        final float unpoweredSpeed = 50f;
        unpoweredDir = (rng.nextFloat() - 0.5f) * unpoweredSpeed;
    }

    @Override
    public void act(float delta) {
        if (unpoweredTime >= 0f) {
            unpoweredTime -= delta;
            Vector3 currentPos = getPosition();

            setPosition(currentPos.add(unpoweredDir * delta, -3f * delta, 0f));
            addRotation(new Quaternion(Vector3.Z, delta * 720f));
            applyTRS();
        } else {
            missileMovement(delta);
        }

        effect.setTransform(getTransform().cpy());
        if (effect.isComplete()) {
            effect.reset();
            effect.start();
        }
    }
}
