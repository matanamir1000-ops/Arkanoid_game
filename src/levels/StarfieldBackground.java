package levels;

import biuoop.DrawSurface;
import game.Background;
import game.Sprite;

import java.awt.Color;
import java.util.Random;

/**
 * A field of stars that twinkle in place.
 * <p>
 * The star positions are drawn from a seeded generator, so a level looks the
 * same every time it is played. An unseeded field would rearrange itself each
 * time the level was replayed after losing a ball, which reads as a glitch
 * rather than as scenery.
 * </p>
 * <p>
 * Only the brightness is animated, and it is derived from the frame count
 * rather than stored per star, so the whole field costs one integer of state.
 * </p>
 */
public class StarfieldBackground implements Sprite {
    private static final int STAR_COUNT = 90;
    private static final int MIN_BRIGHTNESS = 60;
    private static final int BRIGHTNESS_RANGE = 195;
    private static final double TWINKLE_RATE = 0.05;

    private final Background sky;
    private final int[] starX;
    private final int[] starY;
    private final double[] starPhase;
    private int frame;

    /**
     * Constructor.
     *
     * @param skyColor the colour behind the stars.
     * @param width    the playfield width.
     * @param height   the playfield height.
     * @param seed     fixes the layout, so the same level always looks the same.
     */
    public StarfieldBackground(Color skyColor, int width, int height, long seed) {
        this.sky = new Background(skyColor, 0, 0, width, height);
        this.starX = new int[STAR_COUNT];
        this.starY = new int[STAR_COUNT];
        this.starPhase = new double[STAR_COUNT];
        this.frame = 0;

        Random random = new Random(seed);
        for (int i = 0; i < STAR_COUNT; i++) {
            this.starX[i] = random.nextInt(width);
            this.starY[i] = random.nextInt(height);
            this.starPhase[i] = random.nextDouble() * Math.PI * 2;
        }
    }

    @Override
    public void drawOn(DrawSurface surface) {
        this.sky.drawOn(surface);

        for (int i = 0; i < STAR_COUNT; i++) {
            double wave = (Math.sin(this.frame * TWINKLE_RATE + this.starPhase[i]) + 1.0) / 2.0;
            int level = MIN_BRIGHTNESS + (int) (wave * BRIGHTNESS_RANGE);
            if (level > 255) {
                level = 255;
            }
            surface.setColor(new Color(level, level, level));
            surface.fillCircle(this.starX[i], this.starY[i], 1);
        }
    }

    @Override
    public void timePassed() {
        this.frame++;
    }
}
