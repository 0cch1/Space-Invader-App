

import java.awt.Image;

public class Alien extends Sprite2D {
	private static double xSpeed=0;
	private boolean isAlive = true;
	
	public Alien(Image i, Image i2) {
		super(i, i2); // invoke constructor on superclass Sprite2D
	}
	
	// public interface
	public boolean move() {
		x+=xSpeed;
		
		// direction reversal needed?
		if (x <= 0){
			x = 0;
			return true;
		} else if (x + myImage.getWidth(null) >= winWidth){
			x = winWidth - myImage.getWidth(null);
			return true;
		}
		else
			return false;
	}
	
	public static void setFleetXSpeed(double dx) {
		xSpeed=dx;
	}
	
	public static void reverseDirection() {
		xSpeed=-xSpeed;
	}	
	
	public void jumpDownwards() {
		y+=20;
	}

	public static double getFleetXSpeed(){
		return xSpeed;
	}

	public boolean isAlive(){
		return isAlive;
	}

	public void setAlive(boolean alive){
		isAlive = alive;
	}
}

