
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.*; 
import javax.swing.*;
import java.awt.image.*;

enum GameState{
	IN_MENU,
	IN_PROGRESS,
	INTERMISSION;
}

public class InvadersApplication extends JFrame implements Runnable, KeyListener {
	
	// member data
	private static String workingDirectory;
	private static boolean isInitialised = false;
private static final Dimension WindowSize = new Dimension(800,600);
	private BufferStrategy strategy;
	private Graphics offscreenGraphics;
	private static final int NUMALIENS = 30;
	private Alien[] AliensArray = new Alien[NUMALIENS];
	private Spaceship PlayerShip;
	private ArrayList<PlayerBullet> bullets = new ArrayList<PlayerBullet>();
	int score, highScore= 0;
	private GameState gameState = GameState.IN_MENU;
	private JPanel menuP;
	private JButton startB;
	private long roundDisplayTime = 0;
	private final long ROUND_DURATION = 3000; // 3 seconds
	private int currentRound = 1;


	
	// constructor
	public InvadersApplication() {
        //Display the window, centred on the screen
        Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int x = screensize.width/2 - WindowSize.width/2;
        int y = screensize.height/2 - WindowSize.height/2;
        setBounds(x, y, WindowSize.width, WindowSize.height);
        setVisible(true);
    	this.setTitle("Space Invaders!");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // load image from disk
        ImageIcon icon = new ImageIcon(workingDirectory + "\\alien_ship_1.png");
		ImageIcon icon2 = new ImageIcon(workingDirectory + "\\alien_ship_2.png");
        Image alienImage = icon.getImage();
		Image alienImage2 = icon2.getImage();
        
        // create and initialise some aliens, passing them each the image we have loaded
        for (int i=0; i<NUMALIENS; i++) {
        	AliensArray[i] = new Alien(alienImage, alienImage2);
        	double xx = (i%5)*80 + 70;
        	double yy = (i/5)*40 + 50; // integer division!
        	AliensArray[i].setPosition(xx, yy);
        }
        Alien.setFleetXSpeed(2);
        
        // create and initialise the player's spaceship
		Image bulletI = new ImageIcon(workingDirectory + "\\bullet.png").getImage();
        icon = new ImageIcon(workingDirectory + "\\player_ship.png");
        Image shipImage = icon.getImage();
        PlayerShip = new Spaceship(shipImage, bulletI);
        PlayerShip.setPosition(300,530);
        
        // tell all sprites the window width
        Sprite2D.setWinWidth(WindowSize.width);
        
        // create and start our animation thread
        Thread t = new Thread(this);
        t.start();
        
        // send keyboard events arriving into this JFrame back to its own event handlers
        addKeyListener(this);
        
        // initialise double-buffering
        createBufferStrategy(2);
        strategy = getBufferStrategy();
        offscreenGraphics = strategy.getDrawGraphics();

        isInitialised = true;

	}
	
	// thread's entry point
	public void run() {
		while(true){

			if (gameState == GameState.INTERMISSION && System.currentTimeMillis() >= roundDisplayTime) {
				gameState = GameState.IN_PROGRESS; // Continue the game

			}
			if(gameState == GameState.IN_PROGRESS){

					// 1: sleep for 1/50 sec
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) { }

					// 2: animate game objects
					boolean alienDirectionReversalNeeded = false;
					for (int i=0;i<NUMALIENS; i++) {
						if (AliensArray[i].move())
							alienDirectionReversalNeeded=true;
					}
					if (alienDirectionReversalNeeded) {
						Alien.reverseDirection();
						for (int i=0;i<NUMALIENS; i++)
							AliensArray[i].jumpDownwards();
					}

					PlayerShip.move();

					// Update the bullet position
					for(Iterator<PlayerBullet> it = bullets.iterator(); it.hasNext();){
						PlayerBullet bullet = it.next();
						bullet.move();
						if(!bullet.isAlive()){
							it.remove();
							continue;
						}

						for(Alien alien : AliensArray){
							if(alien.isAlive() && isCollision(bullet,alien)){
								alien.setAlive(false); // Mark the alien as not alive
								it.remove(); // Remove the bullet
								score += 10; // Increment score for hitting the alien
								break;
							}
						}

					}

					boolean allAliensDead = true;
					for(Alien alien : AliensArray){
						checkCollision();
						checkHighScore();
						if(alien.isAlive()){
							allAliensDead = false;
							break;
						}
					}

					if(allAliensDead){
						roundDisplayTime = System.currentTimeMillis() + ROUND_DURATION;
						gameState = GameState.INTERMISSION;
						startNewWave();
					}

					// 3: force an application repaint
					this.repaint();


			} else if (gameState == GameState.IN_MENU){
				try{
					Thread.sleep(50);
				} catch (InterruptedException e ){
					e.printStackTrace();
				}

			}

			this.repaint();
		}
	}

//	Checks if two sprites are colliding.
	public boolean isCollision(Sprite2D sprite1, Sprite2D sprite2){
		// each sprite's bounding rectangle is created based on its x and y position, as well as the dimension of image
		Rectangle rec1 = new Rectangle((int)sprite1.x,(int)sprite1.y,sprite1.myImage.getWidth(null),sprite1.myImage.getHeight(null));
		Rectangle rec2 = new Rectangle((int)sprite2.x,(int)sprite2.y,sprite2.myImage.getWidth(null),sprite2.myImage.getHeight(null));
		// the method returns true if these rectangles intersect, indicates a collision occurred.
		return rec1.intersects(rec2);
	}

//	Checks for collision between aliens and player's ship
	public void checkCollision(){
		// this method goes through each alien in the AliensArray, check if its alive
		for(Alien alien : AliensArray){
			// For each alive alien, it then checks if its colliding with player's ship using isCollision method
			if(alien.isAlive() && isCollision(alien,PlayerShip)) {
				// if a collision is detected, it then changes the game state to IN_MENU and reset the game
				gameState = GameState.IN_MENU;
				checkHighScore();
				resetGame();
				// if a collision is detected with any alien, the loop breaks, no need to check the rest of the aliens
				break;

			}
		}
	}

//	Reset the game to initial state
	public void resetGame(){
		score = 0; // score is reset to 0, clearing any points in previous game.
		currentRound = 1; // Reset to the first round
		PlayerShip.setPosition(300, 530); // repositioned the player's location
		bullets.clear(); // clear the bullets list
		resetAliens(); // reset aliens by calling the resetAliens method
		Alien.setFleetXSpeed(2); // the horizontal speed is reset
	}

//	Reinitializes the aliens
	public void resetAliens(){
		ImageIcon icon1 = new ImageIcon(workingDirectory + "\\alien_ship_1.png");
		ImageIcon icon2 = new ImageIcon(workingDirectory + "\\alien_ship_2.png");
		Image alienImage1 = icon1.getImage();
		Image alienImage2 = icon2.getImage();

		for (int i = 0; i < NUMALIENS; i++) {
			AliensArray[i] = new Alien(alienImage1, alienImage2);
			double xx = (i % 5) * 80 + 70;
			double yy = (i / 5) * 40 + 50;
			AliensArray[i].setPosition(xx, yy);
			AliensArray[i].setAlive(true);
		}

	}


	public void checkHighScore(){
		if(score > highScore){
			highScore = score; // update the high score if the current score is higher.
		}
	}

//	Prepare the game for a new round
	public void startNewWave(){

		gameState = GameState.INTERMISSION; // change the state to INTERMISSION, a pause between waves.
		currentRound++;// Increment the round number
		roundDisplayTime = System.currentTimeMillis() + ROUND_DURATION; // set how long the intermission will last.
		Alien.setFleetXSpeed(Alien.getFleetXSpeed() * 2 ); // double the speed of aliens from previous value
		resetAliens(); // reactivate all aliens instances
		bullets.clear();
		PlayerShip.setPosition(300,530);
	}

	public void addBullet(PlayerBullet bullet){
		bullets.add(bullet);
	} // adds new bullet to list of bullets
	
	// Three Keyboard Event-Handler functions
    public void keyPressed(KeyEvent e) {

		// press any key changes the state from menu to in progress
		if(gameState == GameState.IN_MENU){

			gameState = GameState.IN_PROGRESS;

			return;
		}

		if(gameState == GameState.IN_PROGRESS){
			Image bulletI = new ImageIcon(workingDirectory + "\\bullet.png").getImage();
			if (e.getKeyCode()==KeyEvent.VK_LEFT)
				PlayerShip.setXSpeed(-4);
			else if (e.getKeyCode()==KeyEvent.VK_RIGHT)
				PlayerShip.setXSpeed(4);

			if(e.getKeyCode() == KeyEvent.VK_SPACE){
				PlayerShip.fire(this);
			}
		}
    }
    
    public void keyReleased(KeyEvent e) {	
    	if (e.getKeyCode()==KeyEvent.VK_LEFT || e.getKeyCode()==KeyEvent.VK_RIGHT) 
    		PlayerShip.setXSpeed(0);

    }
    
    public void keyTyped(KeyEvent e) {
    }
    //

	// application's paint method
	public void paint(Graphics g) {		
		if (!isInitialised)
			return;
		
		g = offscreenGraphics;
		
		// clear the canvas with a big black rectangle
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WindowSize.width, WindowSize.height);

		if(gameState == GameState.IN_MENU){
			// Draw the menu
			g.setColor(Color.WHITE);
			g.setFont(new Font("Helvetica", Font.BOLD, 36));
			FontMetrics fm = g.getFontMetrics();
			int StringX = (WindowSize.width - fm.stringWidth("Game Over")) / 2;
			int StringY = (WindowSize.height / 2) - 50;
			g.drawString("Game Over",StringX,StringY);

			// Draw the highest score
			g.setFont(new Font("Helvetica", Font.PLAIN, 20));
			g.drawString("Highest Score: " + highScore, 10, 80);

			// Draw prompt text
			g.setFont(new Font("Helvetica", Font.PLAIN, 20));
			fm = g.getFontMetrics();
			int promptX = (WindowSize.width - fm.stringWidth("Press any KEY to play")) / 2;
			int promptY = StringY + 50;
			g.drawString("Press any KEY to play",promptX,promptY);

			//Draw instructions
			fm = g.getFontMetrics();
			int insX = (WindowSize.width - fm.stringWidth("[Arrows key to move, space to fire]")) / 2;
			int insY = promptY + 50;
			g.drawString("[Arrows key to move, space to fire]",insX,insY);
		} else if (gameState == GameState.IN_PROGRESS){

			g.setColor(Color.WHITE);
			g.setFont(new Font("Helvetica", Font.BOLD, 14));
			g.drawString("Score: " + score, 10, 50);

			// only draw aliens that alive
			for(Alien alien : AliensArray){
				if(alien.isAlive()){
					alien.paint(g);
				}
			}
			//Draw bullets
			for(PlayerBullet bullet : bullets){
				if(bullet.isAlive()){
					bullet.paint(g);
				}
			}

			PlayerShip.paint(g);

		} else if (gameState == GameState.INTERMISSION){
			long currentTime = System.currentTimeMillis();

			// Draw the score and round number
			if(currentTime - roundDisplayTime < ROUND_DURATION){
				g.setColor(Color.WHITE);
				g.setFont(new Font("Helvetica", Font.BOLD, 36));
				String roundStr = "Round " + currentRound;
				// Center the round string
				int stringWidth = g.getFontMetrics().stringWidth(roundStr);
				int stringX = (WindowSize.width - stringWidth) / 2;
				int stringY = WindowSize.height / 2;
				g.drawString(roundStr, stringX, stringY);
			}
		}
		// flip the buffers offscreen<-->onscreen
		strategy.show();
	}
	
	// application entry point
	public static void main(String[] args) {
		workingDirectory = ".\\image";
		System.out.println("current working directory " + workingDirectory);
		InvadersApplication w = new InvadersApplication();
	}

}

