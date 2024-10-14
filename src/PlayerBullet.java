import java.awt.Image;

public class PlayerBullet extends Sprite2D{
    private boolean isAlive;
    private double speed;

    public PlayerBullet(Image i, double x, double y){
        super(i,i);
        setPosition(x,y);
        speed = -5;
        isAlive = true;
    }

    public void move(){
        if(isAlive){
            y += speed;
            if(y < 0) isAlive = false; // Bullets goes off the screen
        }
    }

    public boolean isAlive(){
        return isAlive;
    }
}
