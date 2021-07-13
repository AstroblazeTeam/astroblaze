package com.astroblaze.Utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * This class provides some simple math methods used throughout the project
 */
public class MathHelper {
    public static float repeat(float x, float max) {
        return MathUtils.clamp(x - MathUtils.floor(x / max) * max, 0f, max);
    }

    public static float deltaAngle(float current, float target) {
        final float delta = repeat(target - current, 360f);
        return delta > 180f ? delta - 360f : delta;
    }

    public static float moveTowards(float current, float target, float maxDelta) {
        if (Math.abs(target - current) <= maxDelta) return target;
        return current + Math.signum(target - current) * maxDelta;
    }

    public static void moveTowards(Vector3 val, Vector3 target, float maxDelta) {
        if (Vector3.dst2(target.x, target.y, target.z, val.x, val.y, val.z) <= maxDelta * maxDelta) {
            val.set(target);
            return;
        }
        val.add(target.cpy().sub(val).nor().scl(maxDelta));
    }

    public static float moveTowardsAngle(float current, float target, float maxDelta) {
        final float deltaAngle = deltaAngle(current, target);
        return Math.abs(deltaAngle) < maxDelta
                ? target
                : moveTowards(current, current + deltaAngle, maxDelta);
    }
}
