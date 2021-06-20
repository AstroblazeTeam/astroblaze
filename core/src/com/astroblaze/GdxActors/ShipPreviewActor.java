package com.astroblaze.GdxActors;

import com.astroblaze.*;
import com.astroblaze.Rendering.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;

public class ShipPreviewActor extends Actor {
    public static int VARIANT_COUNT = 3;

    class PlayerShipVariantInstance {
        public PlayerShipVariant variant;
        public AssetDescriptor<Model> assetDescriptor;
        public ModelInstance modelInstance;
        public Quaternion baseRotation = new Quaternion();
        public Quaternion rotation = new Quaternion();
        public float scale;
        public int index;
        private final Vector3 scaleVec = new Vector3(1f, 1f, 1f);

        public void applyTRS() {
            final float final_scale = variant.modelScale * scale;
            scaleVec.set(final_scale, final_scale, final_scale);
            modelInstance.transform.set(
                    selectedPosition.cpy().add(
                            0f,
                            0f,
                            (index - page) * spaceBetween - slidePosition),
                    baseRotation.cpy().mul(rotation),
                    scaleVec);
        }
    }

    private final ModelBatch modelBatch;
    private final Scene3D scene;
    private final ArrayList<PlayerShipVariantInstance> variants = new ArrayList<>(4);
    private final Vector3 selectedPosition = new Vector3();
    private final float spaceBetween = 80f;
    private final float scaleSpeed = 3f;
    private float slidePosition;
    private float scaleTarget;
    private int page;

    public ShipPreviewActor(Scene3D scene) {
        this.scene = scene;
        this.modelBatch = AstroblazeGame.getInstance().getBatch();

        addVariant(PlayerShipVariant.Scout);
        addVariant(PlayerShipVariant.Cruiser);
        addVariant(PlayerShipVariant.Destroyer);
    }

    public int getVariantCount() {
        return this.variants.size();
    }

    public void setSelectedPosition(Vector3 position) {
        this.selectedPosition.set(position);
    }

    public void addVariant(PlayerShipVariant variant) {
        PlayerShipVariantInstance model = new PlayerShipVariantInstance();
        model.index = variants.size();
        model.variant = variant;
        model.assetDescriptor = variant.modelDescriptor;
        model.modelInstance = new ModelInstance(Assets.asset(model.assetDescriptor));
        model.baseRotation = new Quaternion(Vector3.Z, -30f).mul(new Quaternion(Vector3.X, 30f));
        model.rotation = new Quaternion(Vector3.Y, MathUtils.random(0f, 360f));
        model.scale = 0f;
        variants.add(model);
    }

    public PlayerShipVariant getVariant(int variant) {
        return this.variants.get(variant).variant;
    }

    @Override
    public void setVisible(boolean visible) {
        // super.setVisible(visible);
        // don't run base class - we'll handle "visibility" via scaling
        scaleTarget = visible ? 1f : 0f;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        Quaternion rot = new Quaternion(Vector3.Y, 30f * delta);
        Vector3 translation = new Vector3();
        // animation for zooming selected ship - by scaling
        for (int i = 0, variantsSize = variants.size(); i < variantsSize; i++) {
            PlayerShipVariantInstance model = variants.get(i);
            model.rotation.mul(rot);
            model.applyTRS(); // apply to recalculate translation
            model.modelInstance.transform.getTranslation(translation);
            float mod = 1f - MathUtils.clamp((translation.dst(selectedPosition) / spaceBetween), 0f, 1f);
            model.scale = MathHelper.moveTowards(model.scale, mod * scaleTarget, delta * scaleSpeed);
            model.applyTRS();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        for (PlayerShipVariantInstance model : variants) {
            if (MathUtils.isEqual(model.scale, 0f, 0.05f))
                continue; // assume all scales are the same

            modelBatch.render(model.modelInstance, scene.getEnvironment());
        }
    }

    public void setSlide(int page, float positionOffset) {
        this.page = page;
        slidePosition += positionOffset * spaceBetween;
    }
}
