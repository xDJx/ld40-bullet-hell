package net.bmagic.ld40;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Zombie {

    // Tweakable constants
    public static final int SPEED = 80;
    public static final float DEATH_VOLUME = 0.25f;
    public static final float SPRITE_SCALE = 1.5f;    
    public static final int HITBOX_OFFSET_X = 9;
    public static final int HITBOX_OFFSET_Y = 0;
    public static final int HITBOX_WIDTH = 12;
    public static final int HITBOX_HEIGHT = 32;
    // End tweakable constants

    // Cached instances
    private static Game game;
    private static Texture texture;
    private static Animation<TextureRegion> anim;
    private static Sound deathSound;
    
    // Private properties
    private Rectangle rect;
    private Vector2 path;
    private float stateTime;

    public Zombie(int x, int y) {
        rect = new Rectangle(
            x - anim.getKeyFrames()[0].getRegionWidth()/2 + HITBOX_OFFSET_X,
            y - anim.getKeyFrames()[0].getRegionHeight()/2 + HITBOX_OFFSET_Y,
            HITBOX_WIDTH * SPRITE_SCALE,
            HITBOX_HEIGHT * SPRITE_SCALE);
        path = new Vector2();
        stateTime = 0;
    }

    public static void init() {
        game = Game.getInstance();
        texture = new Texture(Gdx.files.internal("zombie.png"));
        TextureRegion[][] frames = TextureRegion.split(texture, 32, 32);
        anim = new Animation<TextureRegion>(0.25f, frames[0]);
        deathSound = Gdx.audio.newSound(Gdx.files.internal("zombie_death.wav"));
    }

    public void update() {
        Rectangle playerRect = game.getPlayer().getRectangle();
        path.set(playerRect.x - rect.x, playerRect.y - rect.y);
        path = path.limit(SPEED * Gdx.graphics.getDeltaTime());

        rect.x += path.x;
        rect.y += path.y;

        // Detect collision with bullet
        List<Bullet> bullets = game.getBullets();
        for (int i = 0; i < bullets.size(); i++)
            if (rect.contains(bullets.get(i).getX(), bullets.get(i).getY())) {
                game.getBullets().remove(i);
                die();
            }

        stateTime += Gdx.graphics.getDeltaTime();            
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = anim.getKeyFrame(stateTime, true);
        batch.draw(
            frame,
            rect.x - HITBOX_OFFSET_X, rect.y - HITBOX_OFFSET_Y,
            frame.getRegionWidth()/2, frame.getRegionHeight()/2,
            frame.getRegionWidth(), frame.getRegionHeight(),
            SPRITE_SCALE * (path.x > 0 ? 1 : -1), SPRITE_SCALE,
            0);
    }

    public static void dispose() {
        texture.dispose();
    }

    public Rectangle getRectangle() {
        return rect;
    }

    public void die() {
        deathSound.play(DEATH_VOLUME);
        game.getAmmo().add(new Ammo(
            (int) (rect.x + rect.width/2), (int) (rect.y + rect.height/2)));
        game.getHUDController().incrScore();
        game.getZombies().remove(this);
    }

}