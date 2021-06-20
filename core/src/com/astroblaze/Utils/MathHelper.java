package com.astroblaze.Utils;

import com.badlogic.gdx.math.Vector3;

public class MathHelper {
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
}
