package com.astroblaze.GdxActors;

import com.astroblaze.*;
import com.astroblaze.Bonuses.PlayerBonusCash;
import com.astroblaze.Bonuses.PlayerBonusLaserCharge;
import com.astroblaze.Bonuses.PlayerBonusLife;
import com.astroblaze.Bonuses.PlayerBonusMissiles;
import com.astroblaze.Bonuses.PlayerBonusShieldRestore;
import com.astroblaze.Interfaces.IPlayerBonus;
import com.astroblaze.Interfaces.TranslatedStringId;
import com.astroblaze.Rendering.*;
import com.astroblaze.PlayerState;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

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

    @Override
    public void act(float delta) {
        super.act(delta);
        if (this.scene.getLives() == 0) {
            this.clearActions();
        }
    }

    public void runTutorial() {
        final PlayerShip player = scene.getPlayer();
        this.addAction(Actions.sequence(
                new RunnableAction() {
                    @Override
                    public void run() {
                        player.modLaserTime(-player.getLaserTime());
                        player.modMissileSalvos(-player.getMissileSalvos());
                    }
                },
                delay(0.5f),
                showText(TranslatedStringId.TutorialTouchScreenToMove),
                waitForPlayerMovement(),
                showText(TranslatedStringId.TutorialPrimaryWeapons),
                spawnEnemyAndWaitDeath(EnemyType.TrainingDummy),
                showText(TranslatedStringId.TutorialDodgeBulletsAndEnemies),
                spawnWallOfEnemiesAndWaitDeath(EnemyType.Rammer, 9),
                showText(TranslatedStringId.TutorialUseButtonToFireMissiles),
                new RunnableAction() {
                    @Override
                    public void run() {
                        player.modMissileSalvos(8);
                    }
                },
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        return player.getMissileSalvos() <= 0;
                    }
                },
                showText(TranslatedStringId.TutorialUseButtonToFireLasers),
                new RunnableAction() {
                    @Override
                    public void run() {
                        player.modLaserTime(-player.getLaserTime());
                        player.modLaserTime(3f);
                    }
                },
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        return player.getLaserTime() <= 0f;
                    }
                },
                showText(TranslatedStringId.TutorialBonusCash),
                spawnBonusAndWaitPickup(new PlayerBonusCash()),
                showText(TranslatedStringId.TutorialBonusShield),
                new RunnableAction() {
                    @Override
                    public void run() {
                        player.modHp(-0.5f * player.getHitpoints());
                    }
                },
                spawnBonusAndWaitPickup(new PlayerBonusShieldRestore()),
                showText(TranslatedStringId.TutorialBonusLaser),
                spawnBonusAndWaitPickup(new PlayerBonusLaserCharge()),
                showText(TranslatedStringId.TutorialBonusMissiles),
                spawnBonusAndWaitPickup(new PlayerBonusMissiles()),
                showText(TranslatedStringId.TutorialBonusLife),
                spawnBonusAndWaitPickup(new PlayerBonusLife()),
                showText(""),
                delay(1.5f),
                showText(TranslatedStringId.TutorialComplete),
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
                delay(2f),
                new RunnableAction() {
                    @Override
                    public void run() {
                        scene.getPlayer().setMoveVector(new Vector3(0f, 0f, 2f * scene.getGameBounds().max.z), true);
                    }
                },
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        PlayerShip player = scene.getPlayer();
                        player.setPosition(player.getPosition().cpy().add(0f, 0f, 300f * delta));
                        return !scene.getGameBounds().contains(player.getPosition());
                    }
                },
                new RunnableAction() {
                    @Override
                    public void run() {
                        PlayerState state = AstroblazeGame.getPlayerState();
                        state.setMaxLevel(Math.max(level + 1, state.getMaxLevel()));
                        AstroblazeGame.getInstance().getGuiRenderer().navigateToLevelComplete();
                    }
                });
    }

    public float getCurrentLevelReward() {
        return getLevelReward(getLevel());
    }

    public static float getLevelReward(int level) {
        Random rng = new Random(AstroblazeGame.getPlayerState().getSeed()); // 123 is just random seed
        float sum = 500f;
        if (level == 0) return sum;
        for (int i = 0; i < level; i++) {
            sum += (level + 2) * 250f * rng.nextFloat();
        }
        return sum; // somewhat pseudo-random reward sum
    }

    private RunnableAction showText(final TranslatedStringId id) {
        String text = AstroblazeGame.getInstance().getGuiRenderer().getTranslatedString(id);
        return showText(text);
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

    private Action spawnWallOfEnemiesAndWaitDeath(final EnemyType type, int count) {
        final Array<EnemyShip> enemies = new Array<>();
        enemies.setSize(count);
        Action r = spawnWallOfEnemies(type, enemies);
        return Actions.sequence(r, new Action() {
            @Override
            public boolean act(float delta) {
                for (EnemyShip value : enemies) {
                    if (value != null && value.getHitpoints() > 0f && scene.getGameBounds().contains(value.getPosition()))
                        return false;
                }
                return true;
            }
        });
    }

    private Action spawnBoss(final EnemyType bossType) {
        RunnableAction r = new RunnableAction();
        final EnemyShip[] enemyShip = new EnemyShip[1]; // array wrapper for closure
        r.setRunnable(new Runnable() {
            @Override
            public void run() {
                Vector3 spawnPos = scene.getPlayer().getPosition().cpy();
                // spawn away by mirrored x axis just in case
                spawnPos.add(
                        Math.copySign(scene.getGameBounds().max.x * 0.75f, -spawnPos.x),
                        0f,
                        scene.getGameBounds().max.z * 0.75f);

                enemyShip[0] = scene.getEnemyPool().obtain();
                enemyShip[0].setType(bossType);
                enemyShip[0].setPosition(spawnPos);

                AstroblazeGame.getInstance().gameScreen.getBossTracker().setTrackedEnemy(enemyShip[0]);
                Gdx.app.log("LevelControllerActor", "Spawned " + bossType);
            }
        });

        return Actions.sequence(
                delay(5f),
                showText(TranslatedStringId.BossIncoming),
                playSound(Assets.soundWarning),
                delay(2f),
                showText(""),
                r,
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        return enemyShip[0].getHitpoints() <= 0f;
                    }
                });
    }

    private Action spawnMiniBoss(final EnemyType bossType) {
        RunnableAction r = new RunnableAction();
        final EnemyShip[] enemyShip = new EnemyShip[1]; // array wrapper for closure
        r.setRunnable(new Runnable() {
            @Override
            public void run() {
                Vector3 spawnPos = scene.getPlayer().getPosition().cpy();
                // spawn away by mirrored x axis just in case
                spawnPos.add(
                        Math.copySign(scene.getGameBounds().max.x * 0.75f, -spawnPos.x),
                        0f,
                        scene.getGameBounds().max.z * 0.75f);

                enemyShip[0] = scene.getEnemyPool().obtain();
                enemyShip[0].setType(bossType);
                enemyShip[0].setPosition(spawnPos);

                AstroblazeGame.getInstance().gameScreen.getBossTracker().setTrackedEnemy(enemyShip[0]);
                Gdx.app.log("LevelControllerActor", "Spawned " + bossType);
            }
        });

        return Actions.sequence(
                showText(TranslatedStringId.MiniBossIncoming),
                playSound(Assets.soundWarning),
                delay(3f),
                showText(""),
                r);
    }

    private Action spawnEnemyAndWaitDeath(final EnemyType type) {
        RunnableAction r = new RunnableAction();
        final EnemyShip[] enemyShip = new EnemyShip[1]; // array wrapper for closure
        r.setRunnable(new Runnable() {
            @Override
            public void run() {
                Vector3 spawnPos = scene.getPlayer().getPosition().cpy();
                // spawn away by mirrored x axis just in case
                spawnPos.add(
                        Math.copySign(scene.getGameBounds().max.x * 0.75f, -spawnPos.x),
                        0f,
                        scene.getGameBounds().max.z * 0.75f);

                enemyShip[0] = scene.getEnemyPool().obtain();
                enemyShip[0].setType(type);
                enemyShip[0].setPosition(spawnPos);
            }
        });
        return Actions.sequence(r, new Action() {
            @Override
            public boolean act(float delta) {
                return enemyShip[0].getHitpoints() <= 0f;
            }
        });
    }

    private Action spawnBonusAndWaitPickup(final IPlayerBonus type) {
        RunnableAction r = new RunnableAction();
        final DecalController.DecalInfo[] bonus = new DecalController.DecalInfo[1]; // array wrapper for closure
        r.setRunnable(new Runnable() {
            @Override
            public void run() {
                Vector3 spawnPos = scene.getPlayer().getPosition().cpy();
                // spawn away by mirrored x axis just in case
                spawnPos.x += Math.copySign(scene.getGameBounds().max.x * 0.75f, -spawnPos.x);
                spawnPos.z = scene.getGameBounds().max.z;

                bonus[0] = scene.getDecalController().addBonus(spawnPos, type);
            }
        });
        return Actions.sequence(r, new Action() {
            @Override
            public boolean act(float delta) {
                return bonus[0].life <= 0f; // waits until bonus pickup
            }
        });
    }

    private Action spawnWallOfEnemies(final EnemyType type, final Array<EnemyShip> result) {
        return new RunnableAction() {
            @Override
            public void run() {
                float spawnZoneX = scene.getGameBounds().getWidth();
                float noSpawnRadius = scene.getPlayer().getRadius() * 3f;
                float noSpawnX = MathUtils.random(scene.getGameBounds().min.x, scene.getGameBounds().max.x) * 0.75f;
                for (int i = 0; i < result.size; i++) {
                    // spawn away by mirrored x axis just in case
                    Vector3 spawnPos = new Vector3(
                            spawnZoneX * (((i + 0.5f) / result.size) - 0.5f),
                            0f,
                            scene.getGameBounds().max.z);
                    if (MathUtils.isEqual(spawnPos.x, noSpawnX, noSpawnRadius)) {
                        continue;
                    }
                    EnemyShip enemyShip = scene.getEnemyPool().obtain();
                    enemyShip.setType(type);
                    enemyShip.setPosition(spawnPos);
                    result.set(i, enemyShip);
                }

                Gdx.app.log("LevelControllerActor", "Spawned wall of " + result.size + " x " + type.name());
            }
        };
    }

    private Action spawnWallOfEnemies(final EnemyType type, final int count) {
        Array<EnemyShip> enemies = new Array<>();
        enemies.setSize(count);
        return spawnWallOfEnemies(type, enemies);
    }

    private Action spawnSequenceOfEnemies(final EnemyType type, final int enemyCount, final float interval) {
        return new Action() {
            private float time = interval;
            private final float spawnX = MathUtils.random(-0.45f, 0.45f) * scene.getGameBounds().getWidth();
            private int count = enemyCount;

            @Override
            public boolean act(float delta) {
                time -= delta * AstroblazeGame.getInstance().getScene().getTimeScale();
                if (time < interval) {
                    time += interval;
                    count--;

                    Vector3 spawnPos = new Vector3(spawnX, 0f, scene.getGameBounds().max.z);
                    EnemyShip enemyShip = scene.getEnemyPool().obtain();
                    enemyShip.setType(type);
                    enemyShip.setPosition(spawnPos);
                }
                if (count <= 0) {
                    Gdx.app.log("LevelControllerActor", "Finished spawning sequence of " + enemyCount + " x " + type.name());
                    return true;
                }
                return false;
            }
        };
    }

    private Action spawnDiagonalSequenceOfEnemies(final EnemyType type, final int enemyCount, final float interval) {
        return new Action() {
            private final BoundingBox bounds = scene.getGameBounds();
            private final float spawnX = Math.signum(MathUtils.random() - 0.5f) * 0.55f * bounds.getWidth();
            private final float spawnZ = MathUtils.random((bounds.max.z - bounds.min.z) / 2f, bounds.max.z);
            private final Vector3 spawnPos = new Vector3(spawnX, 0f, spawnZ);
            private final Vector3 moveVector = new Vector3(-Math.signum(spawnX) * type.speed, 0f, -type.speed);

            private int count = enemyCount;
            private float time = interval;

            @Override
            public boolean act(float delta) {
                time -= delta * AstroblazeGame.getInstance().getScene().getTimeScale();
                if (time < interval) {
                    time += interval;
                    count--;

                    EnemyShip enemyShip = scene.getEnemyPool().obtain();
                    enemyShip.setType(type);
                    enemyShip.setPosition(spawnPos);
                    enemyShip.setMoveVector(moveVector);
                }
                if (count <= 0) {
                    Gdx.app.log("LevelControllerActor", "Finished spawning diagonal sequence of " + enemyCount + " x " + type.name());
                    return true;
                }
                return false;
            }
        };
    }

    private Action delay(float duration) {
        return new SceneDelayAction(duration);
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
        Gdx.app.log("LevelController", "Set level to " + level);

        if (level == 0) {
            AstroblazeGame.getLevelStatTracker().reset(0);
            runTutorial();
        } else {
            float levelLog = level > 4 ? (float) Math.log(level) : 1f;
            float textDelay = 1f;
            float waveDelay = 6f - levelLog;
            SequenceAction seq = Actions.sequence(
                    delay(textDelay),
                    showText(TranslatedStringId.LevelStartReady),
                    delay(textDelay),
                    showText(TranslatedStringId.LevelStartSet),
                    delay(textDelay),
                    showText(TranslatedStringId.LevelStartGo),
                    delay(textDelay),
                    showText(""));

            final int waveCount = 6 + (int) (MathUtils.random(0.8f, 1.2f) * 4f * levelLog);
            final int minibossSpawn1 = MathUtils.random(waveCount / 3, (waveCount * 2 / 3) - 1);
            final int minibossSpawn2 = MathUtils.random() > 0.5f // 50% chance for additional miniboss
                    ? -1 : MathUtils.random((waveCount / 3) + 1, waveCount * 2 / 3);

            Gdx.app.log("LevelControllerActor", "Starting level " + level + " level logarithm is " + levelLog);
            Gdx.app.log("LevelControllerActor", "Generating " + waveCount + " waves.");
            Gdx.app.log("LevelControllerActor", "Wave delay = " + waveDelay);
            Gdx.app.log("LevelControllerActor", "Miniboss spawns at " + minibossSpawn1 + " and " + minibossSpawn2);
            for (int i = 0; i < waveCount; i++) {
                EnemyType waveType = waveTypeWeights.getRandom();
                int count = (int) (MathUtils.random(0.8f, 1.2f) * 8f * levelLog);
                if (i == minibossSpawn1 || i == minibossSpawn2) {
                    seq.addAction(spawnMiniBoss(EnemyType.MiniBoss1));
                } else {
                    switch (MathUtils.random(0, 2)) {
                        case 0:
                        default:
                            seq.addAction(spawnWallOfEnemies(waveType, count));
                            break;
                        case 1:
                            seq.addAction(spawnSequenceOfEnemies(waveType, count, 2f * waveDelay / count));
                            break;
                        case 2:
                            seq.addAction(spawnDiagonalSequenceOfEnemies(waveType, count, 2f * waveDelay / count));
                            break;
                    }
                }
                seq.addAction(delay(waveDelay));
            }

            seq.addAction(spawnBoss(EnemyType.Boss));

            seq.addAction(showText(TranslatedStringId.LevelComplete));
            seq.addAction(finishLevel());

            AstroblazeGame.getLevelStatTracker().reset(getLevel());
            this.addAction(seq);
        }
    }

    public Action playSound(final AssetDescriptor<Sound> sound) {
        return new RunnableAction() {
            @Override
            public void run() {
                AstroblazeGame.getSoundController().playSoundAsset(sound);
            }
        };
    }

    private Action waitForPlayerMovement() {
        return new Action() {
            private Vector3 origVector;

            @Override
            public boolean act(float delta) {
                if (origVector == null) { // first iteration - copy the original vector to compare
                    origVector = scene.getPlayer().getMoveVector().cpy();
                }
                return !scene.getPlayer().getMoveVector().epsilonEquals(origVector, 0.1f);
            }
        };
    }
}
