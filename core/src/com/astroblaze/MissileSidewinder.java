package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

public class MissileSidewinder extends Missile {
    private float unpoweredTime = 1f;
    private final float unpoweredDir;
    private final static Random rng = new Random();

    public MissileSidewinder(Scene3D scene, Model model) {
        super(scene, model);
        final float unpoweredSpeed = 40f;
        unpoweredDir = (rng.nextFloat() - 0.5f) * unpoweredSpeed;
    }

    @Override
    public void act(float delta) {
        if (unpoweredTime >= 0f) {
            unpoweredTime -= delta;
            Vector3 currentPos = getTransform().getTranslation(new Vector3());

            getTransform().set(currentPos.add(unpoweredDir * delta, -3f * delta, 0f),
                    new Quaternion(Vector3.Y, 180f),
                    new Vector3(0.5f, 0.5f, 0.5f));
        } else {
            super.act(delta);
        }
    }
}
