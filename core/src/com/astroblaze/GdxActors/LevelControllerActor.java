package com.astroblaze.GdxActors;

import com.astroblaze.*;
import com.astroblaze.Rendering.*;
import com.astroblaze.PlayerState;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class LevelControllerActor extends Actor {
    private final Scene3D scene;
    private final WeightedCollection<EnemyType> waveTypeWeights = new WeightedCollection<>();
    private int level;

    public LevelControllerActor(Scene3D scene) {
        this.scene = scene;
        waveTypeWeights.add(50, EnemyType.Simple);
        waveTypeWeights.add(20, EnemyType.SineWave);
        waveTypeWeights.add(20, EnemyType.Rammer);
        waveTypeWeights.add(10, EnemyType.MoneyDrop);
    }

    public void runTutorial() {
        float defaultDelay = 3f;
        this.addAction(Actions.sequence(
                Actions.delay(defaultDelay),
                showText("Touch the screen to move!"),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        return !scene.getPlayer().getMoveVector().isZero();
                    }
                },
                showText("Primary weapon is always active, just aim and kill!"),
                spawnEnemyAndWaitDeath(EnemyType.TrainingDummy),
                showText("Dodge the bullets and enemies!"),
                spawnWallOfEnemiesAndWaitDeath(EnemyType.Rammer, 9, 2),
                showText("Use buttons below to fire missiles"),
                new RunnableAction() {
                    @Override
                    public void run() {
                        scene.getPlayer().modMissileSalvos(1);
                    }
                },
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        return scene.getPlayer().getMissileSalvos() < 1;
                    }
                },
                showText("Tutorial complete"),
                finishLevel()
        ));
    }

    private Action finishLevel() {
        return Actions.sequence(
                new RunnableAction() {
                    @Override
                    public void run() {
                        scene.getPlayer().setNoControlTime(100f);
                        scene.getPlayer().setGodModeTimer(100f);
                        scene.getPlayer().setMoveVector(new Vector3(0f, 0f, 0f), true);
                    }
                },
                Actions.delay(2f),
                new RunnableAction() {
                    @Override
                    public void run() {
                        scene.getPlayer().setMoveVector(new Vector3(0f, 0f, 300f), true);
                    }
                },
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        Ship player = scene.getPlayer();
                        player.setPosition(player.getPosition().cpy().add(0f, 0f, 500f * delta));
                        return !scene.gameBounds.contains(player.getPosition());
                    }
                },
                new RunnableAction() {
                    @Override
                    public void run() {
                        PlayerState state = AstroblazeGame.getPlayerState();
                        state.setMaxLevel(Math.max(level + 1, state.getMaxLevel()));
                        AstroblazeGame.getInstance().getGuiRenderer().backToLevelSelect();
                    }
                });
    }

    private RunnableAction showText(final String txt) {
        RunnableAction r = new RunnableAction();
        r.setRunnable(new Runnable() {
            @Override
            public void run() {
                AstroblazeGame.getInstance().renderText(0, txt,
                        Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 4f);
            }
        });
        return r;
    }

    private Action spawnWallOfEnemiesAndWaitDeath(final EnemyType type, final int count, final int removeMiddle) {
        final Enemy[] enemy = new Enemy[count]; // array wrapper for closure
        RunnableAction r = new RunnableAction() {
            @Override
            public void run() {
                float spawnZoneX = scene.gameBounds.max.x - scene.gameBounds.min.x;
                for (int i = 0; i < count; i++) {
                    if (i > count / 2 - removeMiddle && i < count / 2 + removeMiddle) {
                        enemy[i] = null;
                        continue;
                    }
                    // spawn away by mirrored x axis just in case
                    Vector3 spawnPos = new Vector3(
                            spawnZoneX * (((i + 0.5f) / count) - 0.5f),
                            0f,
                            scene.gameBounds.max.z);

                    enemy[i] = scene.enemyPool.obtain();
                    enemy[i].setType(type);
                    enemy[i].setPosition(spawnPos);
                }
            }
        };
        return Actions.sequence(r, new Action() {
            @Override
            public boolean act(float delta) {
                int alive = 0;
                for (Enemy value : enemy) {
                    if (value != null && value.getHitpoints() > 0f
                            && scene.gameBounds.contains(value.getPosition()))
                        alive++;
                }
                return alive <= 0;
            }
        });
    }

    private Action spawnEnemyAndWaitDeath(final EnemyType type) {
        RunnableAction r = new RunnableAction();
        final Enemy[] enemy = new Enemy[1]; // array wrapper for closure
        r.setRunnable(new Runnable() {
            @Override
            public void run() {
                Vector3 spawnPos = scene.getPlayer().getPosition().cpy();
                // spawn away by mirrored x axis just in case
                spawnPos.add(
                        Math.copySign(scene.gameBounds.max.x * 0.75f, -spawnPos.x),
                        0f,
                        scene.gameBounds.max.z * 0.75f);

                enemy[0] = scene.enemyPool.obtain();
                enemy[0].setType(type);
                enemy[0].setPosition(spawnPos);
            }
        });
        return Actions.sequence(r, new Action() {
            @Override
            public boolean act(float delta) {
                return enemy[0].getHitpoints() <= 0f;
            }
        });
    }

    private Action spawnWallOfEnemies(final EnemyType type, final int count, final int removeMiddle) {
        return new RunnableAction() {
            @Override
            public void run() {
                float spawnZoneX = scene.gameBounds.max.x - scene.gameBounds.min.x;
                for (int i = 0; i < count; i++) {
                    if (i > count / 2 - removeMiddle && i < count / 2 + removeMiddle) {
                        continue;
                    }
                    // spawn away by mirrored x axis just in case
                    Vector3 spawnPos = new Vector3(
                            spawnZoneX * (((i + 0.5f) / count) - 0.5f),
                            0f,
                            scene.gameBounds.max.z);
                    Enemy enemy = scene.enemyPool.obtain();
                    enemy.setType(type);
                    enemy.setPosition(spawnPos);
                }
            }
        };
    }

    public void setLevel(int level) {
        this.level = level;
        Gdx.app.log("LevelController", "Set level to " + level);

        if (level == 0) {
            runTutorial();
        } else {
            float textDelay = 1f;
            float waveDelay = 3.5f;
            SequenceAction seq = Actions.sequence(
                    Actions.delay(textDelay),
                    showText("Ready!"),
                    Actions.delay(textDelay),
                    showText("Set!"),
                    Actions.delay(textDelay),
                    showText("Go!"),
                    Actions.delay(textDelay),
                    showText(""));

            for (int i = 0; i < 10 + level; i++) {
                seq.addAction(spawnWallOfEnemies(waveTypeWeights.getRandom(),
                        MathUtils.random(3 + level, 7 + level), MathUtils.random(1, 2)));
                seq.addAction(Actions.delay(waveDelay));
            }

            seq.addAction(spawnEnemyAndWaitDeath(EnemyType.Boss));

            seq.addAction(showText("Level complete"));
            seq.addAction(finishLevel());

            this.addAction(seq);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (this.scene.getLives() == 0) {
            this.clearActions();
        }
    }
}
