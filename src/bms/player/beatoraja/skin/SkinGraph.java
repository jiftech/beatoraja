package bms.player.beatoraja.skin;

import bms.player.beatoraja.MainState;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * グラフイメージ
 */
public class SkinGraph extends SkinObject {

	/**
	 * イメージ
	 */
	private TextureRegion[] image;
	/**
	 * グラフの参照値
	 */
	private int id = -1;
	/**
	 * グラフの伸びる向き(1:下, それ以外:右)
	 */
	private int direction = 1;

	public SkinGraph(int imageid) {
		setImageID(imageid);
	}

	public SkinGraph(TextureRegion[] image, int cycle) {
		setImage(image, cycle);
	}

	public TextureRegion[] getImage() {
		return image;
	}

	public TextureRegion getImage(long time, MainState state) {
		if(getImageID() != -1) {
			return state.getImage(getImageID());
		}
		return image[getImageIndex(image.length, time, state)];
	}

	public void setImage(TextureRegion[] image, int cycle) {
		this.image = image;
		setCycle(cycle);
	}

	public void draw(SpriteBatch sprite, long time, MainState state) {
		if (getImageID() != -1) {
			Rectangle r = this.getDestination(time, state);
			TextureRegion image = state.getImage(getImageID());
			if (r != null && image != null) {
				float value = 0;
				if (id != -1) {
					value = state.getSliderValue(id);
				}
				// sprite.draw(image, r.x, r.y, r.width, r.height);
				if (direction == 1) {
					draw(sprite,
							new TextureRegion(image, 0, image.getRegionY() + image.getRegionHeight()
									- (int) (image.getRegionHeight() * value), image.getRegionWidth(),
									(int) ((int) image.getRegionHeight() * value)), r.x, r.y, r.width,
							r.height * value, getColor(time, state), getAngle(time, state));
				} else {
					draw(sprite, new TextureRegion(image, 0, image.getRegionY(),
							(int) (image.getRegionWidth() * value), image.getRegionHeight()), r.x, r.y,
							r.width * value, r.height, getColor(time, state), getAngle(time, state));
				}
			}
		} else {
			if (image == null) {
				return;
			}
			Rectangle r = this.getDestination(time, state);
			if (r != null) {
				float value = 0;
				if (id != -1) {
					value = state.getSliderValue(id);
					// System.out.println("bargraph id : " + id + " value : " +
					// value);
				}
				TextureRegion image = getImage(time, state);
				// sprite.draw(image, r.x, r.y, r.width, r.height);
				if (direction == 1) {
					draw(sprite,
							new TextureRegion(image, 0, image.getRegionHeight()
									- (int) (image.getRegionHeight() * value), image.getRegionWidth(),
									(int) ((int) image.getRegionHeight() * value)), r.x, r.y, r.width,
							r.height * value, getColor(time, state), getAngle(time, state));
				} else {
					draw(sprite,
							new TextureRegion(image, 0, 0, (int) (image.getRegionWidth() * value), image
									.getRegionHeight()), r.x, r.y, r.width * value, r.height, getColor(time, state),
							getAngle(time, state));
				}
			}
		}
	}

	public void dispose() {
		if (image != null) {
			for (TextureRegion tr : image) {
				tr.getTexture().dispose();
			}
			image = null;
		}
	}

	public void setReferenceID(int id) {
		this.id = id;
	}

	public int getReferenceID() {
		return id;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
}
