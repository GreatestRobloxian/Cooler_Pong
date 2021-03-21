/*
   Li Zhang
   Mr. Benum
   1/6/2021
   Code for pong.
*/



import csta.ibm.pong.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.time.Instant;
import java.util.Random;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 * Class for the pong game.
 */
public class Pong extends Game {

    // =========
    // Variables
    // =========
    // keep this private, no point of keeping it public as it is never accessed outside of class.

    /**
     * The status of the current game.
     */
    private enum GameStatus {
        /**
         * The game is starting.
         */
        STARTING,
        /**
         * The game is running.
         */
        PLAYING,
        /**
         * The game is yielding.
         */
        YIELDING
    }

    private GameStatus gameStatus; // Enum value for the game status.

    private int maxPointage  = 11;

    // Constants for the pong game.
    private final int SPECIALEVENTMIN = 5, SPECIALEVENTMAX = 15, ABILITIESMAX = 8;
    private final int SLOWDOWNTIMEMIN = 2, SLOWDOWNTIMEMAX = 4;

    private Timer gameCountdown;
    private Random rand = new Random();


    private int ballSize;
    private int timerValue;
    private int eventTime = 0;

    private long lastEventTicked = Integer.MAX_VALUE; // Set this to max long value so it won't get ticked right away.

    private boolean hasGameEvent = false;

    private String currentGameLabelText = "";
    private int countDownValue;

    // ============
    // Preset Arrays
    // ============

    // Preset values for the game.
    private final int[] heightCourtPresets  = {300, 1000, 800};
    private final int[] widthCourtPresets   = {1400, 800, 800};
    private final int[] pointagePresets     = {11,   50,  75,  Integer.MAX_VALUE}; // For last index, Keep the max points as maximum as maximum integer value, they will be playing for a very long time so its basically freeplay.

    // ============
    // Game Objects
    // ============

    private Paddle firstPaddle, secondPaddle;
    private Ball playingBall;

    // ==========
    // GUI Objects
    // ==========

    private JLabel scoreLabelOne, scoreLabelTwo, gameMessageLabel;

    // =========
    // VARIABLES
    // =========

    private int firstScore = 0, secondScore = 0;

    // =======
    // METHODS
    // =======


    /**
     * Creates a popup on the user's interface.
     *
     * @param imageValue the string value of the image's name.
     * @param message the string that will be displayed on the interface.
     * @param title the string that will be displayed on the interface's title.
     * @param font the font that will be used.
     * @param fontSize an integer which sets the font size.
     * @param fontType an integer which sets the font type.
     * @param textColor a Color object which sets the text color.
     * @param backgroundColor a Color object which sets the background color.
     * @param messageOption a string that changes the popup type.
     * @param options A string array which stores all the clickable options.
     * @param dimension A dimension object which contains the width and height of the interface.
     * @param closeProgramOnX Whether or not the program should close when the user clicks on the X button.
     * @return An integer value based on the option selected.
     */
    private int newPopUp(String imageValue, String message, String title, String font, int fontSize, int fontType, Color textColor, Color backgroundColor, int messageOption, String[] options, Dimension dimension, boolean closeProgramOnX){
        ImageIcon popUpIcon = null;
        if (imageValue != null) { // If it is not null then there should be an image.
            popUpIcon = new ImageIcon(this.getClass().getResource(String.format("/resources/%s", imageValue)));
        }
        JPanel backPanel = new JPanel();
        backPanel.setBackground(backgroundColor);
        backPanel.setSize(dimension);
        backPanel.setLocation((getWidth() - backPanel.getWidth())/2, (getHeight() - backPanel.getHeight())/2); // Set the position to the middle of the screen.
        JLabel textLabel = new JLabel(message);
        textLabel.setSize(dimension);
        textLabel.setLocation(backPanel.getLocation());
        textLabel.setFont(new Font(font, fontType, fontSize));
        backPanel.add(textLabel);
        int returnValue = JOptionPane.showOptionDialog(this, backPanel, title, messageOption, JOptionPane.PLAIN_MESSAGE, popUpIcon, options, options[0]);
        if(returnValue == -1 && closeProgramOnX){ // If the user clicked on the x button and if it should exit program on x button.
            System.exit(0);// Exit the program
        }
        return returnValue;
    }

    /**
     * Starts a game countdown with a text label.
     *
     * @param newText the new string that will be displayed on the game event label.
     * @param countDownFrom the integer that the countdown will start from.
     * @param shouldYield if it should stop the game or not.
     */

    private void startGameCountDown(String newText, int countDownFrom, boolean shouldYield){
        gameMessageLabel.setVisible(true);
        currentGameLabelText = newText;
        countDownValue       = countDownFrom;
        hasGameEvent = true; // have game event.
        startCountDown();
        if (shouldYield){
            stopGame();
            gameStatus = GameStatus.YIELDING; // Set the game status to yielding.
        }
    }

    /**
     *
     * @param paddle the paddle object that the ability will have effect on.
     * @return the special ability phrase as a string.
     */
    private String activateSpeciality(Paddle paddle){
        int specialAbility = rand.nextInt(ABILITIESMAX) + 1; // Generate a special ability from 1 - ABILITIESMAX
        switch(specialAbility){
            case 1: // A Wall.
                paddle.setPaddleHeightScale(7);
                return "Is A Wall";
            case 2: // Make it long.
                paddle.setPaddleHeightScale(3);
                return "Is Now A Stick";
            case 3: // Make it wide.
                paddle.setPaddleWidthScale(4);
                return "Is Now Wider";
            case 4: // Make it faster.
                paddle.setMoveDistance(2);
                return "Is Now Faster";
            case 5: //Make it bigger on both sides
                paddle.setPaddleWidthScale(2);
                paddle.setPaddleHeightScale(4);
                return "Has Changed Its Size!";
            case 6: // Change the ball size
                playingBall.setBallSizeScale(playingBall.getBallSizeScale() * 2);
                return "Has Changed The Ball Size!";
            case 7: // Change the game interval.
                setDelay(rand.nextInt(SLOWDOWNTIMEMAX) + SLOWDOWNTIMEMIN);
                return "Has Slowed Down Time";
            case 8: // Change the ball up velocity.
                playingBall.setYVelocity(playingBall.getYVelocity() * -1);
                return "Has Changed The Ball Up Direction";
        }
        return "";
    }
    /**
     * Checks and starts a special event if it needs to be started.
     *
     */
    private void doSpecialEvent(){
        if (Instant.now().toEpochMilli() - lastEventTicked > (eventTime * 1000)) { // If the time passed in milliseconds is more than the event time (multiply it by 1000 to turn seconds into milliseconds)
            refreshObjectSize(true); // Refresh the objects and reset the sizes since it is a new event.
            lastEventTicked = Instant.now().toEpochMilli(); // Store the new event starting time.
            eventTime = rand.nextInt(SPECIALEVENTMAX) + SPECIALEVENTMIN;
            String firstAbility  = activateSpeciality(firstPaddle);
            String secondAbility = activateSpeciality(secondPaddle);
            startGameCountDown(String.format("P1 %s | P2 %s", firstAbility, secondAbility), eventTime, false); // Start new countdown with special ability time.
            refreshObjectSize(false); // Refresh the objects.
        }
    }
    /**
     * Starts the count down timer.
     *
     */
    private void startCountDown(){
        gameCountdown.start();
    } // Start the countdown

    /**
     * Stops the count down timer.
     *
     */
    private void stopCountDown(){
        gameCountdown.stop();
    } // Stop the countdown.

    /**
     * Method is invoked at every timer interval.
     *
     */
    private void onCountDownEvent(){
        if (countDownValue == 0 && hasGameEvent) { // if the countdown timer is at 0 and there is an event.
            stopCountDown(); // Stop the countdown.
            hasGameEvent = false;
            if (gameStatus == GameStatus.YIELDING){
                gameStatus = GameStatus.STARTING; // Change the yielding to starting.
                startGame(); // Start the game.
            }else{
                gameStatus = GameStatus.PLAYING; // Must be playing now.
            }
            gameMessageLabel.setVisible(false);
            gameMessageLabel.setText("");
        } else { // The timer continues.
            gameMessageLabel.setText(String.format("%s: (%d S)", currentGameLabelText, --countDownValue)); // Change the time label.
        }
        refreshGUIs(); // Refresh the GUIs since there is text change.
    }



    //  Sets up the game.
    //  Parameters:
    //    (none)
    //  Return:
    //    None

    /**
     * Does all the initial setup for the game.
     *
     *
     */
    public void setup() {
        URL iconUrl = Pong.class.getResource("resources/pong.png");
        setIconImage(Toolkit.getDefaultToolkit().getImage(iconUrl));
        // Set it to invisible.
        setLocationRelativeTo(null);
        setVisible(false);

        // GAME OBJECTS
        firstPaddle  = new Paddle(Color.WHITE, 0, 0, 0, 0);
        secondPaddle = new Paddle(Color.WHITE, 0, 0, 0, 0);
        playingBall  = new Ball(Color.WHITE, 0, 0, 0, 0);


        add(firstPaddle);
        add(secondPaddle);
        add(playingBall);


        // GUI SETUP
        scoreLabelOne    = new JLabel();
        scoreLabelTwo    = new JLabel();
        gameMessageLabel = new JLabel();

        scoreLabelOne.setForeground(Color.LIGHT_GRAY);
        scoreLabelTwo.setForeground(Color.LIGHT_GRAY);
        gameMessageLabel.setForeground(Color.CYAN);

        // Paint the GUI
        getContentPane().add(scoreLabelOne);
        getContentPane().add(scoreLabelTwo);
        getContentPane().add(gameMessageLabel);

        gameCountdown = new Timer(1000, new ActionListener() { // A timer listener.
            public void actionPerformed(ActionEvent e) {
                onCountDownEvent();
            }
        });
        getContentPane().addComponentListener(new ComponentAdapter() { // Listen for resolution changes.
            public void componentResized(ComponentEvent event){
                resetObjects();
            }
        });
        newGame(); // Start the new game.
    }


    /**
     * Starts a new game.
     *
     */
    private void newGame(){
        scoreLabelOne.setText("0");
        scoreLabelTwo.setText("0");
        firstScore  = 0;
        secondScore = 0;
        newPopUp("question-mark.png",
                "<html>WELCOME TO PONG!<br/><br/>" +
                        "<h2>CONTROLS:</h2><br/>" +
                        "<pre>   Z/X <b>(UP | DOWN)</b> for Player 1</pre><br/>" +
                        "<pre>   N/M <b>(UP | DOWN)</b> for Player 2</pre><br/>" +
                        "<h2>INSTRUCTIONS:</h2><br/>" +
                        "<pre>   - To win a round of Pong you have to get the ball past the other player's paddle.</pre><br/>" +
                        "<pre>   - There will be random events that will take place in this version.</pre> <br/>" +
                    "    <pre>   - First one to the set score and is ahead by 2 points wins!</pre> <br/>",
                "PONG INFO",
                "Calibril",
                20,
                Font.PLAIN,
                new Color(37, 39, 39),
                new Color(210, 211, 211),
                JOptionPane.OK_OPTION,
                new String[]{"Start Game"},
                new Dimension(500, 500),
                false
        );
        int courtSelection = Math.max(newPopUp(null,
                "SELECT YOUR FIELD SIZE",
                "FIELD - SET UP",
                "Arial",
                20,
                Font.PLAIN,
                new Color(246, 171, 182),
                new Color(210, 211, 211),
                JOptionPane.OK_OPTION,
                new String[]{"WIDE FIELD (DIFFICULT) ", "LONG FIELD (MEDIUM)", "REGULAR-FIELD (REGULAR)", "FREESCALE"},
                new Dimension(200, 300),
                false
        ), 0);
        if (courtSelection < 3) {
            setSize(widthCourtPresets[courtSelection], heightCourtPresets[courtSelection]);
            setResizable(false); // Disable resizing.
        }else{ // Set it as default but let them resize.
            setSize(widthCourtPresets[2], heightCourtPresets[2]);
        }
        int pointageSelection = Math.max(newPopUp(null,
                "POINTAGE",
                "GAME - POINTAGE",
                "Arial",
                20,
                Font.PLAIN,
                new Color(246, 171, 182),
                new Color(210, 211, 211),
                JOptionPane.OK_OPTION,
                new String[]{"11 (Regular)", "50 (Medium) ", "75 (High)", "FREEPLAY"},
                new Dimension(200, 300),
                false
        ), 0);
        maxPointage = pointagePresets[pointageSelection]; // Set the max points based on the preset value.
        setLocationRelativeTo(null); // Put it to the center of the screen.
        setVisible(true); // Make it visible.
        initGameField();
    }


    /**
     * Refreshes all the graphical user interfaces based on the game resolution.
     *
     */
    private void refreshGUIs(){
        scoreLabelOne.setBounds(getWidth()/4, getHeight()/20, getWidth()/8, getHeight()/8); // Scale it based on screen resolution.
        scoreLabelTwo.setBounds(getWidth() - getWidth()/3, getHeight()/20, getWidth()/8, getHeight()/8); // Scale it based on screen resolution.
        gameMessageLabel.setBounds(getWidth()/2 - gameMessageLabel.getWidth()/2, getHeight() - gameMessageLabel.getHeight()*2, getWidth()/3, getHeight()/5); // Scale it based on screen resolution.

        // Resize the font of the labels

        int scoreLabelOneStringWidth    = scoreLabelOne.getFontMetrics(scoreLabelOne.getFont()).stringWidth(scoreLabelOne.getText()); // Get the label text length.
        int scoreLabelOneWidth          = scoreLabelOne.getWidth();

        int scoreLabelTwoStringWidth    = scoreLabelTwo.getFontMetrics(scoreLabelTwo.getFont()).stringWidth(scoreLabelTwo.getText()); // Get the label text length.
        int scoreLabelTwoWidth          = scoreLabelTwo.getWidth();

        int gameMessageLabelStringWidth = gameMessageLabel.getFontMetrics(gameMessageLabel.getFont()).stringWidth(gameMessageLabel.getText()); // Get the label text length.
        int gameMessageLabelWidth       = gameMessageLabel.getWidth();

        // Get new text label font size ratios.
        double scoreLabelOneSizeRatio    = (double)scoreLabelOneWidth / (double)scoreLabelOneStringWidth;
        double scoreLabelTwoSizeRatio    = (double)scoreLabelTwoWidth / (double)scoreLabelTwoStringWidth;
        double gameMessageLabelSizeRatio = (double)gameMessageLabelWidth / (double)gameMessageLabelStringWidth;

        // Get the new font size but keep it at most the label's height.
        int scoreLabelOneFontSize     = Math.min((int)(scoreLabelOne.getFont().getSize() * scoreLabelOneSizeRatio), scoreLabelOne.getHeight());
        int scoreLabelTwoFontSize     = Math.min((int)(scoreLabelTwo.getFont().getSize() * scoreLabelTwoSizeRatio), scoreLabelTwo.getHeight());
        int gameMessageLabelFontSize  = Math.min((int)(gameMessageLabel.getFont().getSize() * gameMessageLabelSizeRatio), gameMessageLabel.getHeight());


        // Change up the label fonts.
        scoreLabelOne.setFont(   new Font("Arial",    Font.PLAIN, scoreLabelOneFontSize));
        scoreLabelTwo.setFont(   new Font("Arial",    Font.PLAIN, scoreLabelTwoFontSize));
        gameMessageLabel.setFont(new Font("Calibril", Font.BOLD,  gameMessageLabelFontSize));
        getContentPane().repaint(); // Repaint the GUI.
    }

    /**
     * Resizes the game objects based on resolution.
     * @param shouldResetObjectSize Whether it should reset the object scales back to the default, 1.
     */
    private void refreshObjectSize(boolean shouldResetObjectSize){
        if (shouldResetObjectSize){
            // Reset all the scales back to 1.
            setDelay(1);
            firstPaddle.setPaddleHeightScale(1);
            firstPaddle.setPaddleWidthScale(1);
            secondPaddle.setPaddleWidthScale(1);
            secondPaddle.setPaddleHeightScale(1);
            playingBall.setBallSizeScale(1);
            firstPaddle.setMoveDistance(1);
            secondPaddle.setMoveDistance(1);
        }
        int playerOnePaddleWidth  = getWidth()/150  *  firstPaddle.getPaddleWidthScale();   // Scale the paddle width.
        int playerOnePaddleHeight = getHeight()/10  *  firstPaddle.getPaddleHeightScale();  // Scale the paddle height.
        int playerTwoPaddleWidth  = getWidth()/150  *  secondPaddle.getPaddleWidthScale();  // Scale the paddle width.
        int playerTwoPaddleHeight = getHeight()/10  *  secondPaddle.getPaddleHeightScale(); // Scale the paddle height.
        ballSize                  = getWidth()/75   *  playingBall.getBallSizeScale();      // Scale the ball size.

        firstPaddle.setX(getFieldWidth()/14); // Set the x position based on resolution
        secondPaddle.setX(getFieldWidth() - getFieldWidth()/14); // Set the y position based on resolution


        firstPaddle.setSize(playerOnePaddleWidth, playerOnePaddleHeight); // Resize the first paddle.
        secondPaddle.setSize(playerTwoPaddleWidth, playerTwoPaddleHeight); // Resize the second paddle.

        playingBall.setSize(ballSize, ballSize); // Resize the paddle.

    }

    /**
     * Resets objects based on the game status.
     *
     */
    private void resetObjects(){
        refreshObjectSize(false);
        refreshGUIs();
        if(gameStatus != GameStatus.PLAYING) { // If we are not playing.
            refreshObjectSize(true); // refresh the objects.
            secondPaddle.setY(getFieldHeight()/ 2 - secondPaddle.getHeight() / 2); // Scale this paddle to the center.
            firstPaddle.setY(getFieldHeight()/2 - firstPaddle.getHeight()/2); // Scale this paddle to the center.
            playingBall.setX(getFieldWidth()/2); // Set ball to center of screen
            playingBall.setY(getFieldHeight()/2);
            if (gameStatus == GameStatus.STARTING) { // If the game is starting.
                refreshObjectSize(true); // reset size and position of the objects.
                refreshGUIs(); // Refresh the GUIs.
                lastEventTicked = Long.MAX_VALUE; // Set the last event ticked to a very big number so it never gets ticked.
                playingBall.setYVelocity(0); // Freeze the ball.
                playingBall.setXVelocity(0);
                startGameCountDown("GAME STARTS IN", 5, true); // Start the countdown.
            }
        }
        getContentPane().repaint(); // Repaint the GUI updates.
    }

    /**
     * Resets the game field.
     *
     */
    private void initGameField(){

        try{
            Robot robot = new Robot(); // Keep this as a local variable since we will have to change a lot of this method since this assigning might throw an exception.
            robot.keyPress(KeyEvent.VK_Z);   // Press Z Key.
            robot.keyRelease(KeyEvent.VK_Z); // Release Z key.
            robot.keyPress(KeyEvent.VK_X);   // Press X Key.
            robot.keyRelease(KeyEvent.VK_X); // Release X Key.
            robot.keyPress(KeyEvent.VK_N);   // Press N Key.
            robot.keyRelease(KeyEvent.VK_N); // Release N Key.
            robot.keyPress(KeyEvent.VK_M);   // Press M Key.
            robot.keyRelease(KeyEvent.VK_M); // Release M Key.
        } catch(AWTException e){ // Throws an exception
            System.err.println("AWTException"); // Output the exception.
            System.exit(0); // Exit out of the program.
        }

        gameStatus = GameStatus.STARTING; // The game is now starting.
        resetObjects(); // Reset the objects.
        startGame();// Start the game
    }

    /**
     * Displays the winner GUI and ends the game.
     *
     * @param winner the integer value of the winner 1 for first player, 2 for second player.
     */
    private void endGame(int winner){
        stopCountDown();
        stopGame();
        setVisible(false); // Make the game invisible.
        String finalWinner = "";
        if(winner == 1){
            finalWinner = "Player One";
        }else{
            finalWinner = "Player Two";
        }
        int endGameSelection = newPopUp(
                "trophy.png",
                String.format("%s wins!", finalWinner),
                "GAME - WINNER SCREEN",
                "Arial",
                20,
                Font.PLAIN,
                new Color(246, 171, 182),
                new Color(210, 211, 211),
                JOptionPane.OK_OPTION,
                new String[]{"New Game", "Quit"},
                new Dimension(200, 300),
                true
        );
        if (endGameSelection == 0){
            newGame();
        }else{ // Exit the game.
            System.exit(0);
        }
    }


    /**
     * Checks if the game should be yielding.
     *
     *
     *
     * @return true of the game is yielding, false otherwise.
     */
    private boolean checkForYield(){
        if (gameStatus == GameStatus.YIELDING){ // If it is yielding
            return true;
        }else if(gameStatus == GameStatus.STARTING){ // If the game is starting.
            int lowestScore = Math.min(firstScore, secondScore);
            if(lowestScore == firstScore){ // Throw the ball based on the side that is losing.
                playingBall.setXVelocity(-1);
            }else{
                playingBall.setXVelocity(1);
            }
        }
        return false; // it is not yielding.
    }

    /**
     * Called when the ball first gets hit.
     *
     *
     *
     */
    private void doFirstHit(){
        playingBall.setYVelocity(-1); //Change the Y velocity.
        gameStatus = GameStatus.PLAYING; // the game is now in playing status.
        lastEventTicked = 0; // Let the game start with event.
        resetObjects(); // Rescale the objects.
    }

    /**
     * Ran at every game interval.
     */
    public void act() {
        if (checkForYield()) return;


        // HANDLE GAME

        if(firstPaddle.collides(playingBall)){ // Check if player one's paddle hits the ball.
            if (gameStatus == GameStatus.STARTING){
                doFirstHit();
            }
            firstPaddle.hitBall(playingBall); // Hits the ball.
        }else if(secondPaddle.collides(playingBall)){ // Check if player two's paddle hits the ball.
            if (gameStatus == GameStatus.STARTING){
                doFirstHit();
            }
            secondPaddle.hitBall(playingBall); // Hits the ball.
        }

        // Border Collisions

        // Top Bottom Check:
        if(playingBall.getY() < ballSize){ // Check if the ball is hitting the top of the field.
            playingBall.bounceOnWall(); // Make it bounce on the wall.
        }else if(playingBall.getY() > getFieldHeight() - ballSize){ // Check if the ball is hitting the bottom of the field.
            playingBall.bounceOnWall(); // Make it bounce on the wall.
        }



        // Left Right Check:
        if(playingBall.getX() < ballSize/2 || playingBall.getX() > (getFieldWidth() - playingBall.getWidth())){ // If it goes past left/right threshold someone has lost.
            if (playingBall.getX() < ballSize) { // If it hits the right side.
                secondScore++; // Player two wins. Add that to their total score.
                scoreLabelTwo.setText(String.format("%d", secondScore)); // Update the label.
                resetObjects();
            }else { // Player one wins.
                firstScore++; // Player one wins. add that to their total score.
                scoreLabelOne.setText(String.format("%d", firstScore)); // Update the label.
                resetObjects();
            }
            getContentPane().repaint(); // Repaint the GUI updates.
            if(firstScore >= maxPointage && Math.abs(firstScore - secondScore) > 1){ // Checks if first player hits point limit and checks if they are two ahead.
                endGame(1);
            }else if(secondScore >= maxPointage && Math.abs(firstScore - secondScore) > 1){ // Checks if second player hits point limit and checks if they are two ahead.
               endGame(2);
            }else{
                initGameField(); // Resets the field since no one won.
            }
        }
        // HANDLE KEYPRESS
        if(ZKeyPressed() && firstPaddle.getY() - firstPaddle.getMoveDistance() >= 0){ //Clamp the left movement
            firstPaddle.moveLeft();
        }
        if(XKeyPressed() && firstPaddle.getY() + firstPaddle.getHeight() + firstPaddle.getMoveDistance() < getFieldHeight()){ //Clamp the right movement.
            firstPaddle.moveRight();
        }
        if(NKeyPressed() && secondPaddle.getY() - secondPaddle.getMoveDistance() >= 0){ //Clamp the left movement
            secondPaddle.moveLeft();
        }
        if(MKeyPressed() && secondPaddle.getY() + secondPaddle.getHeight() + secondPaddle.getMoveDistance() < getFieldHeight()){  //Clamp the right movement.
            secondPaddle.moveRight();
        }

        doSpecialEvent(); // Check if it can do special event.
    }

}
