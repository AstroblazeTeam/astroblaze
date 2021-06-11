package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;

class ShipPreviewActor extends Actor {
    public static int VARIANT_COUNT = 3;

    class ShipModel {
        public AssetDescriptor<Model> assetDescriptor;
        public ModelInstance modelInstance;
        public Quaternion rotation = new Quaternion();
        public Vector3 scale = new Vector3(1f, 1f, 1f);
        public int index;

        public void applyTRS() {
            modelInstance.transform.set(
                    selectedPosition.cpy().add(
                            0f,
                            0f,
                            (index - page) * spaceBetween - slidePosition),
                    rotation,
                    scale);
        }
    }

    private final ModelBatch modelBatch;
    private final Scene3D scene;
    private final ArrayList<ShipModel> variants = new ArrayList<>(4);
    private final Vector3 selectedPosition = new Vector3();
    private float slidePosition;
    private final float spaceBetween = 80f;
    private int page;

    public ShipPreviewActor(Scene3D scene) {
        this.scene = scene;
        this.modelBatch = AstroblazeGame.getInstance().getBatch();

        addVariant(Assets.spaceShip2);
        addVariant(Assets.spaceShip1);
        addVariant(Assets.spaceShip3);
    }

    public int getVariantCount() {
        return this.variants.size();
    }

    public void setSelectedPosition(Vector3 position) {
        this.selectedPosition.set(position);
    }

    public void addVariant(AssetDescriptor<Model> modelAsset) {
        ShipModel model = new ShipModel();
        model.assetDescriptor = modelAsset;
        model.index = variants.size();
        model.modelInstance = new ModelInstance(Assets.asset(modelAsset));
        model.scale.set(0.5f, 0.5f, 0.5f);
        variants.add(model);
    }

    public AssetDescriptor<Model> getVariant(int variant) {
        return this.variants.get(variant).assetDescriptor;
    }


    public void prevShip() {
        Gdx.app.log("ShipPreviewActor", "Selected prev ship");
    }

    public void nextShip() {
        Gdx.app.log("ShipPreviewActor", "Selected next ship");
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        /// rotate
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (!isVisible())
            return;

        for (ShipModel model : variants) {
            modelBatch.render(model.modelInstance, scene.getEnvironment());
        }
    }

    public void setSlide(int page, float positionOffset) {
        this.page = page;
        slidePosition += positionOffset * spaceBetween;
        for (ShipModel model : variants) {
            model.applyTRS();
        }
    }
}
