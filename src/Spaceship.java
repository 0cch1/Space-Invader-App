
import java.awt.event.KeyEvent;
import java.awt.Image;
import java.util.ArrayList;

public class Spaceship extends Sprite2D {
	private double xSpeed=0;
	private ArrayList<PlayerBullet> bullets;
	private Image bulletImage;
	
	public Spaceship(Image i, Image bulletImg) {
		super(i,i); // invoke constructor on superclass Sprite2D
		bullets = new ArrayList<>();
		this.bulletImage = bulletImg;
	}
	
	public void setXSpeed(double dx) {
		xSpeed=dx;
	}
	
	public void move() {
		// apply current movement
		x+=xSpeed;
		
		// stop movement at screen edge?
		if (x<=0) {
			x=0;
			xSpeed=0;
		}
		else if (x>=winWidth-myImage.getWidth(null)) {
			x=winWidth-myImage.getWidth(null);
			xSpeed=0;
		}
	}

	public void fire(InvadersApplication game){
		game.addBullet(new PlayerBullet(bulletImage, x + myImage.getWidth(null)/2.0 , y));
	}

	public ArrayList<PlayerBullet> getBullets() {
		return bullets;
	}

}
