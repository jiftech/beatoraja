package bms.player.beatoraja.gauge;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import bms.model.BMSModel;
import bms.player.beatoraja.play.PlaySkin;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * イージーゲージ
 *
 * @author exch
 */
public class EasyGrooveGauge extends GrooveGauge {

    private boolean ispms = false;

    public EasyGrooveGauge(BMSModel model) {
        if (model.getUseKeys() == 9) {
            init(2, 120, 30, 85, CLEARTYPE_EASY, new float[]{(float) (model.getTotal() / model.getTotalNotes()),
                    (float) (model.getTotal() / model.getTotalNotes()), (float) (model.getTotal() / model.getTotalNotes()) / 2,
                    -1.0f, -3.0f, -3.0f});
            ispms = true;
        } else {
            init(2, 100, 20, 80, CLEARTYPE_EASY, new float[]{(float) (model.getTotal() / model.getTotalNotes()),
                    (float) (model.getTotal() / model.getTotalNotes()), (float) (model.getTotal() / model.getTotalNotes()) / 2,
                    -1.5f, -4.5f, -0.5f});
        }

    }

    @Override
    public void draw(SpriteBatch sprite, TextureRegion[] images, float x, float y, float w, float h) {
        sprite.begin();
        final int count = ispms ? 24 : 50;
        for (int i = 1; i <= count; i++) {
            final float border = i * getMaxValue() / count;
			sprite.draw(images[4 + (getValue() >= border ? 0 : 2) + (border < getBorder() ? 1 : 0)], x + w * (i - 1) / count,
					y, w / count, h);			
        }
        sprite.end();
    }
}
