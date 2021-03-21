/*
   Li Zhang
   Mr. Benum
   1/6/2021
   Code for a ball object for the game pong.
*/


import csta.ibm.pong.GameObject;
import java.awt.*;
import java.security.cert.CertificateNotYetValidException;
import java.time.*;

/**
 * Class for the ball game object.
 */
public class Ball extends GameObject {
    private int xVelocity = 1;
    private int yVelocity = 1; // Start this velocity off as -1 so the ball has initial movement.
    private long lastCollisionTick = 0;
    private int ballSizeScale = 1;
    private final int DEBOUNCERATE = 100;

    /**
     * Constructor for the ball object.
     * @param c a Color object which sets the ball object's color.
     * @param x an integer which sets the ball object's X position.
     * @param y an integer which sets the ball object's Y position.
     * @param width an integer which sets the ball object's width.
     * @param height an integer which sets the ball object's height.
     */
    public Ball(Color c, int x, int y, int width, int height){
        setColor(c);
        setSize(width, height);
        // POSITION
        setX(x);
        setY(y);
    }

    /**
     * Gets the ball object's X velocity.
     * @return an Integer which represents the ball object's X velocity.
     */
    public int getXVelocity(){
        return xVelocity;
    }
    /**
     * Gets the ball object's Y velocity.
     * @return an Integer which represents the ball object's Y velocity.
     */
    public int getYVelocity(){
        return yVelocity;
    }

    /**
     * Sets the ball object's X velocity.
     * @param xVelocity an integer which sets the ball object's X velocity.
     */
    public void setXVelocity(int xVelocity){
        this.xVelocity = xVelocity;
    }

    /**
     * Sets the ball object's Y velocity.
     * @param yVelocity an integer which sets the ball object's Y velocity.
     */
    public void setYVelocity(int yVelocity){
        this.yVelocity = yVelocity;
    }

    /**
     * Sets the ball object's size scale.
     * @param ballSizeScale an integer which sets the ball object's size scale.
     */
    public void setBallSizeScale(int ballSizeScale){
        this.ballSizeScale = ballSizeScale;
    }

    /**
     * Gets the ball object's size scale.
     * @return an integer which represents the ball object's size scale.
     */
    public int getBallSizeScale(){
        return ballSizeScale;
    }


    /**
     * Flips the Y velocity of the ball's object.
     */
    public void bounceOnWall(){
        yVelocity *= -1; // Flip the velocity
    }
    //  Flips the x velocity when it hits the left or right paddle.
    //  Parameters:
    //    (none)
    //  Return:
    //    None

    /**
     * Bounces the ball object on the paddle object.
     * @param paddle a paddle object that the ball will bounce on.
     */
    public void bounceOnPaddle(GameObject paddle){
        if (Instant.now().toEpochMilli() - lastCollisionTick > (paddle.getWidth()/5 * DEBOUNCERATE)) { // Scale the seconds waited with the size of the paddle.
            lastCollisionTick = Instant.now().toEpochMilli(); // Store last hit.
            xVelocity *= -1;
        }
    }


    /**
     * Ran every game interval.
     */
    public void act(){
        int xPosition = xVelocity + this.getX();
        int yPosition = yVelocity + this.getY();
        setX(xPosition);
        setY(yPosition);
    }
}
