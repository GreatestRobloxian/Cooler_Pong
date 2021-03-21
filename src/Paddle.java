/*
   Li Zhang
   Mr. Benum
   1/6/2021
   Code for a paddle object for the game pong.
*/



import csta.ibm.pong.GameObject;
import java.awt.*;
/**
 * Class for the paddle game object.
 */
public class Paddle extends GameObject  {
    // Fields:
    private int moveDistance = 1; // Keep this private to encapsulate it and tie it to the object so in the future paddles can have unique movement speeds.
    private int paddleHeightscale = 1, paddleWidthScale = 1;

    /**
     * Constructor for paddle object.
     * @param c a Color object which sets the game object's color.
     * @param x an integer representing the x position of the game object.
     * @param y an integer representing the y position of the game object.
     * @param width an integer representing the width of the object.
     * @param height an integer representing the height of the object.
     */
    public Paddle(Color c, int x, int y, int width, int height){
        // POSITION
        setX(x);
        setY(y);
        // COLOR
        setColor(c);
        // SIZE
        setSize(width, height);
    }

    public void act(){}

    /**
     * Moves the paddle object right moveDistance units.
     */
    public void moveRight(){
        setY(this.getY() + moveDistance);
    }


    /**
     * Moves the paddle to the left by moveDistance.
     */
    public void moveLeft(){
        setY(this.getY() - moveDistance);
    }


    /**
     * Hits the ball provided.
     * @param b the ball object.
     */
    public void hitBall(Ball b){
        b.bounceOnPaddle(this);
    }

    /**
     * Sets the paddle's height scale.
     * @param paddleHeightScale the integer that will be set as the paddle's height scale.
     */
    public void setPaddleHeightScale(int paddleHeightScale){
        this.paddleHeightscale = paddleHeightScale;
    }

    /**
     * Sets the move distance of the paddle
     * @param moveDistance the integer that will be set as the paddle's move distance.
     */
    public void setMoveDistance(int moveDistance){
        this.moveDistance = moveDistance;
    }

    /**
     * Sets the paddle's width scale.
     * @param paddleWidthScale the integer that will be set as the paddle's width scale.
     */
    public void setPaddleWidthScale(int paddleWidthScale){
        this.paddleWidthScale = paddleWidthScale;
    }

    /**
     * Gets the paddle's height scale.
     * @return an integer which represents the paddle's height scale.
     */
    public int getPaddleHeightScale(){
        return paddleHeightscale;
    }

    /**
     * Gets the paddle's width scale.
     * @return an integer which represents the paddle's width scale.
     */
    public int getPaddleWidthScale(){
        return paddleWidthScale;
    }

    /**
     * Gets the paddle's move distance.
     * @return an integer which represents the paddle's move distance.
     */
    public int getMoveDistance(){return moveDistance;}


}
