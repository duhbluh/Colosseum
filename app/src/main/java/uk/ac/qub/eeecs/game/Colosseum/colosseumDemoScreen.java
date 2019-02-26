package uk.ac.qub.eeecs.game.Colosseum;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.qub.eeecs.gage.Game;
import uk.ac.qub.eeecs.gage.engine.ElapsedTime;
import uk.ac.qub.eeecs.gage.engine.graphics.IGraphics2D;
import uk.ac.qub.eeecs.gage.engine.input.Input;
import uk.ac.qub.eeecs.gage.engine.input.TouchEvent;
import uk.ac.qub.eeecs.gage.ui.PushButton;
import uk.ac.qub.eeecs.gage.world.GameObject;
import uk.ac.qub.eeecs.gage.world.GameScreen;
import uk.ac.qub.eeecs.gage.world.LayerViewport;
import uk.ac.qub.eeecs.game.CoinTossScreen;
import uk.ac.qub.eeecs.game.Colosseum.Regions.ActiveRegion;
import uk.ac.qub.eeecs.game.Colosseum.Regions.HandRegion;
import uk.ac.qub.eeecs.game.EndGameScreen;
import uk.ac.qub.eeecs.game.PauseMenuScreen;
import uk.ac.qub.eeecs.game.FatigueScreen;


public class colosseumDemoScreen extends GameScreen {

    // /////////////////////////////////////////////////////////////////////////
    // Properties
    // /////////////////////////////////////////////////////////////////////////

    private LayerViewport mGameViewport;
    private Input mInput;
    private static final Random RANDOM = new Random();

    //Define cards
    private List<Card> mCards = new ArrayList<Card>();

    //Define the background board
    private GameObject mGameBackground;

    //Check if card is being held
    private Card mCardHeld = null;

    //Array List to hold the Push Buttons
    private List<PushButton> mButtons = new ArrayList<>();

    //Push button for ending player's turn
    private PushButton mEndTurnButton;
    private PushButton mEndTurnButtonOff;

    //Push Button for pausing the game
    private PushButton mPauseButton;

    //Push Button for drawing a card
    private PushButton mDrawButton;

    //Array list to hold a deck of cards - Story 7 Sprint 4
    private List<Card> dCards = new ArrayList<>();

    //Define a test player
    private Player p2;

    //Define a Test Deck
    private CardDeck playerDeck, enemyDeck;

    //Set up an int value for holding the coin flip result
    private int coinTossResult;

    boolean coinFlipDone = false;

    // Define a test Opponent
    private AIOpponent opponent;

    protected int edgeCounter = 0; //Used for edge case scenario of coin flip, User Story 18.1, Sprint 4 - Scott
    protected static boolean edgeCase = false;

    //Paint item that will be used to draw text
    private Paint mText;

    //Denarius
    //SINGLE COIN
    private GameObject pDenarius, eDenarius;

    //Test region
    ActiveRegion playerActiveRegion;
    ActiveRegion opponentActiveRegion;
    HandRegion playerHandRegion;
    HandRegion opponentHandRegion;

    // /////////////////////////////////////////////////////////////////////////
    // Constructors
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Create the Card game screen
     *
     * @param game Game to which this screen belongs
     */
    public colosseumDemoScreen(Game game) {
        super("CardScreen", game);

        coinFlipDone = false;

        setupViewports();
        setUpGameObjects();
        setUpDecks();
        coinFlipStart();
        coinFlipResult();

    }

    private void setUpGameObjects() {
        // Load in the assets used by the steering demo
        mGame.getAssetManager().loadAssets("txt/assets/ColosseumAssets.JSON");
        mGame.getAssetManager().loadAssets("txt/assets/HeroAssets.JSON");
        //mGame.getAssetManager().loadAssets("txt/assets/CardAssets.JSON");

        if (edgeCase) { //Used for edge case scenario of coin flip, User Story 18.1, Sprint 4 - Scott
            edgeCaseTest();
        }

        // Spacing that will be used to position the objects:
        int spacingX = (int) mDefaultLayerViewport.getWidth() / 5;
        int spacingY = (int) mDefaultLayerViewport.getHeight() / 3;

        // Create the background
        Bitmap mBackgroundBitmap = getGame()
                .getAssetManager().getBitmap("ArenaFloor");

        mGameBackground = new GameObject(mDefaultLayerViewport.getWidth() / 2.0f,
                mDefaultLayerViewport.getHeight() / 2.0f, mDefaultLayerViewport.getWidth(),
                mDefaultLayerViewport.getHeight(), mBackgroundBitmap, this);


        //PAINT OBJECT:
        //Initialise Paint object I will use to draw text
        mText = new Paint();
        int screenHeight = mDefaultScreenViewport.height;
        float textHeight = screenHeight / 24.0f;
        mText.setTextSize(textHeight);
        mText.setColor(Color.rgb(255, 255, 255));
        mText.setTypeface(Typeface.create("Arial", Typeface.BOLD));


        //Create the Push Buttons:
        //This button is created twice, where one has no effect when clicked, ie it is greyed out.
        mEndTurnButton = new PushButton(
                spacingX * 4.5f, spacingY * 1.5f, spacingX * 0.5f, spacingY * 0.5f,
                "EndTurn", this);

        //'Greyed out' version of the endTurnButton:
        mEndTurnButtonOff = new PushButton(
                spacingX * 4.5f, spacingY * 1.5f, spacingX * 0.5f, spacingY * 0.5f,
                "EndTurn2", this);

        mPauseButton = new PushButton(
                spacingX * 4.7f, spacingY * 2.7f, spacingX * 0.4f, spacingY * 0.4f, "Cog", "CogSelected", this);
        mButtons.add(mPauseButton);

        //Button used to draw cards from deck:
        mDrawButton = new PushButton(
                spacingX * 0.5f, spacingY * 0.5f, spacingX * 0.8f, spacingY * 0.8f, "CardDeckImg", this);
        mButtons.add(mDrawButton);

        //Setting up demo player:
        p2 = new Player(this, "Meridia");
        opponent = new AIOpponent(this, "EmperorCommodus");

        //Create denarius objects
        Bitmap denarius = getGame()
                .getAssetManager().getBitmap("Denarius");
        //player
        p2.setCurrentMana(4);
        p2.setCurrentManaCap(4);
        //p2.setCurrentHealth(28); // For testing ability only, remove later

        opponent.setCurrentMana(4);
        opponent.setCurrentManaCap(4);

        //SINGLE COIN
        pDenarius = new GameObject(spacingX * 2.95f, spacingY * 0.38f, 30, 30, denarius, this);
        eDenarius = new GameObject(spacingX * 2.95f, spacingY * 2.78f, 30, 30, denarius, this);

        /* FOR MULTIPLE COINS
        createMultipleCoins(pDenarius, spacingX, spacingY, 0.38f, denarius);
        createMultipleCoins(eDenarius, spacingX, spacingY, 2.78f, denarius);
        */

        // Defining playable region width and height ( 50.0f/1.5f is the width of the cards)
        playerActiveRegion = new ActiveRegion(mDefaultLayerViewport.getLeft(), mDefaultLayerViewport.getRight(), mDefaultLayerViewport.getTop() / 2.0f, mDefaultLayerViewport.getBottom() + p2.position.y + (p2.getPortraitHeight() / 2));
        opponentActiveRegion = new ActiveRegion(mDefaultLayerViewport.getLeft(), mDefaultLayerViewport.getRight(), mDefaultLayerViewport.getTop() - (p2.position.y + (p2.getPortraitHeight() / 2)), mDefaultLayerViewport.getTop() / 2.0f);

        playerHandRegion = new HandRegion(mDefaultLayerViewport.getRight() / 2 - (4 * (50.0f / 1.5f)), mDefaultLayerViewport.getRight() / 2 + (4 * (50.0f / 1.5f)), p2.position.y - (p2.getPortraitHeight() / 2), mDefaultLayerViewport.getBottom());
        opponentHandRegion = new HandRegion(mDefaultLayerViewport.getRight() / 2 - (4 * (50.0f / 1.5f)), mDefaultLayerViewport.getRight() / 2 + (4 * (50.0f / 1.5f)), mDefaultLayerViewport.getTop(), opponent.position.y + (opponent.getPortraitHeight() / 2));

    }

    private void setupViewports() {
        // Setup the screen viewport to use the full screen.
        mDefaultScreenViewport.set(0, 0, mGame.getScreenWidth(), mGame.getScreenHeight());

        // Calculate the layer height that will preserved the screen aspect ratio
        // given an assume 480 layer width.
        float layerHeight = mGame.getScreenHeight() * (480.0f / mGame.getScreenWidth());

        mDefaultLayerViewport.set(240.0f, layerHeight / 2.0f, 240.0f, layerHeight / 2.0f);
        mGameViewport = new LayerViewport(240.0f, layerHeight / 2.0f, 240.0f, layerHeight / 2.0f);
    }

    // /////////////////////////////////////////////////////////////////////////
    // Methods
    // /////////////////////////////////////////////////////////////////////////

    public void endPlayerTurn() {
        //When player ends turn, a 'YourTurn' variable on the player should turn to false
        //This will prevent player from attempting to draw from their deck, or playing a card
        //It will also trigger the AI to make a move
        p2.setYourTurn(false);
    }

    public void setUpDecks() {
        //This method sets up the player and enemy decks, called when screen is loaded
        playerDeck = new CardDeck(1, "Basic Player Deck", this, false);
        enemyDeck = new CardDeck(2, "Basic Enemy Deck", this, true);

        for (int i = 0; i < enemyDeck.getmCardHand().size(); i++) {
            enemyDeck.getmCardHand().get(i).flipCard(this.mGame);
        }
    }

    private Card generateRandomDeck() { //Scott Barham, Story 7 Sprint 4
        Card dCard;
        dCard = new MinionCard(RANDOM.nextInt((int) mDefaultLayerViewport.getWidth() - 75) + 35, //-75, +35 to prevent cards spawning outside the side regions of the screen.
                RANDOM.nextInt((int) mDefaultLayerViewport.getHeight() - 75) + 35, //-75, +35 to prevent cards spawning outside the top/bottom regions of the screen.
                this, RANDOM.nextInt(10), RANDOM.nextInt(10), RANDOM.nextInt(10));//, "CardFront");
        //dCard.setAttack(RANDOM.nextInt(10)); //limit of values 0-9
        //dCard.setDefence(RANDOM.nextInt(10)); //limit of values 0-9
        //dCard.setMana(RANDOM.nextInt(10)); //limit of values 0-9

        return dCard;
    }

    private int coinFlipStart() { //Scott, User Story 16, Sprint 4
        int flip = RANDOM.nextInt(6001);
        if (flip == 6000) { //side of coin (1/6000 chance to auto-win)
            return 2;
        } else if (flip >= 3000 && flip < 6000) { //heads (ai starts)
            return 1;
        } else if (flip >= 0 && flip < 3000) { //tails (user starts)
            return 0;
        }
        return -1; //for error testing only
    }

    // Method for building hand (4 if heads, 3 if tails) - Dearbhaile

    private void coinFlipResult() {
        coinTossResult = coinFlipStart();
        switch (coinTossResult) {
            case 0: // ie, player starts

                p2.setYourTurn(true);
                for (int i = 0; i < 3; i++) {
                    playerDeck.drawTopCard();
                }

                for (int i = 0; i < 4; i++) {
                    enemyDeck.drawTopCard();
                }
                break;

            case 1: // ie, ai starts
                p2.setYourTurn(false);
                for (int i = 0; i < 4; i++) {
                    playerDeck.drawTopCard();
                }

                for (int i = 0; i < 3; i++) {
                    enemyDeck.drawTopCard();
                }
                break;
            case 2: //edge of coin - set opponent health to 0, auto win game.

                break;
            default: //output an error as this should not be reached

                break;
        }
    }

    private void edgeCaseTest() { //Testing for the edge case scenario of the coin flip, User Story 18.1, Sprint 4 - Scott
        boolean i = true;
        while (i) {
            edgeCounter++;
            switch (coinFlipStart()) {
                case 0://tails - player starts
                    break;
                case 1: //heads - ai starts
                    break;
                case 2: //edge of coin - set opponent health to 0, auto win game.
                    i = false;
                    break;
                default: //output an error
                    break;
            }
        }
    }

    public boolean getEdgeCase() {
        return edgeCase;
    }

    public static void setEdgeCase(boolean edgeCaseInput) {
        edgeCase = edgeCaseInput;
    }

    public void createMultipleCoins(List<GameObject> denarius, float spacingX, float spacingY, float scaleVert, Bitmap b) {
        float scaleHor = 2.95f;

        for (int i = 0; i < p2.getCurrentMana(); i++) {
            denarius.add(new GameObject(spacingX * scaleHor, spacingY * scaleVert, 30, 30, b, this));
            scaleHor += 0.04f;
        }
    }

    @Override
    public void update(ElapsedTime elapsedTime) {
        // Process any touch events occurring since the update
        mInput = mGame.getInput();

        while (!coinFlipDone) {
            mGame.getScreenManager().addScreen(new CoinTossScreen(mGame, getCoinTossResult()));
            coinFlipDone = true;
        }

        p2.update(elapsedTime);
        opponent.update(elapsedTime);

        if (EndGameScreen.getCoinFlipResult()) { //If the coin flip was on the edge, win the game go to next end game screen
            try {
                Thread.sleep(1000); //Allows player to see when they have won rather than immediately jumping
            } catch (InterruptedException e) {
            }
            EndGameScreen.setMostRecentResult("win");
            mGame.getScreenManager().changeScreenButton(new EndGameScreen(mGame));
        } else if (p2.getCurrentHealth() <= 0 || opponent.getCurrentHealth() <= 0) { //if either of the health is below 0 enter the if statement
            try {
                Thread.sleep(1000); //Allows player to see when they have won rather than immediately jumping
            } catch (InterruptedException e) {
            }

            if (p2.getCurrentHealth() <= 0 && opponent.getCurrentHealth() <= 0) { //if both sides health is 0 or less, the game ends in a draw
                EndGameScreen.setMostRecentResult("draw");
            } else if (p2.getCurrentHealth() <= 0) { //if the player reaches 0 or less health, they lose
                EndGameScreen.setMostRecentResult("loss");
            } else if (opponent.getCurrentHealth() <= 0) { //if the opponent reaches 0 or less health, the player wins
                EndGameScreen.setMostRecentResult("win");
            }
            mGame.getScreenManager().changeScreenButton(new EndGameScreen(mGame)); //swap to the end game screen regardless of whatever outcome occurs
        } else {

            List<TouchEvent> touchEvents = mInput.getTouchEvents();
            if (touchEvents.size() > 0) {

                for (int i = 0; i < playerDeck.getmCardHand().size(); i++) {
                    playerDeck.getmCardHand().get(i).cardDrag(playerDeck.getmCardHand(), mDefaultScreenViewport, mGameViewport, mGame);

                    // Updates both regions for all cards
                    playerActiveRegion.update(playerDeck.getmCardHand().get(i));
                    opponentActiveRegion.update(playerDeck.getmCardHand().get(i));

                    playerHandRegion.update(playerDeck.getmCardHand().get(i));
                    opponentHandRegion.update(playerDeck.getmCardHand().get(i));

                }

                for (int i = 0; i < enemyDeck.getmCardHand().size(); i++) {
                    enemyDeck.getmCardHand().get(i).cardDrag(enemyDeck.getmCardHand(), mDefaultScreenViewport, mGameViewport, mGame);

                    // Updates both regions for all cards
                    playerActiveRegion.update(enemyDeck.getmCardHand().get(i));
                    opponentActiveRegion.update(enemyDeck.getmCardHand().get(i));

                    playerHandRegion.update(enemyDeck.getmCardHand().get(i));
                    opponentHandRegion.update(enemyDeck.getmCardHand().get(i));

                }

                for (PushButton button : mButtons)
                    button.update(elapsedTime);

                if (mPauseButton.isPushTriggered()) {
                    mGame.getScreenManager().changeScreenButton(new PauseMenuScreen(mGame));
                }

                mEndTurnButton.update(elapsedTime);
                mEndTurnButtonOff.update(elapsedTime);

                if (mEndTurnButton.isPushTriggered()) {
                    endPlayerTurn();
                }


                //Still working on this - Dearbhaile
                //TODO: Finish Fatigue Screen
                //If player draws a card once their deck is at 0, they will be navigated to 'FatigueScreen'
                //On this screen, it will be displayed how much health they will lose (cumulative value)
                //Screen then disappears after 3 seconds.

                if (mDrawButton.isPushTriggered()) {
                    if (!playerDeck.getDeck().isEmpty()) {
                        playerDeck.drawTopCard();
                        playerDeck.destroyCardOverLimit();
                    } else {
                        mGame.getScreenManager().addScreen(new FatigueScreen(mGame));
                    }
                }


                for (Card deckOfCards : dCards) //Allows each card held within the "dCards" variable to be dragged, Story 29 Sprint 5 Scott
                    deckOfCards.cardDrag(dCards, mDefaultScreenViewport, mGameViewport, mGame);


                //Two sets of Hands (player and enemy) are able to be dragged:
                for (Card cards : playerDeck.getmCardHand()) {
                    cards.cardDrag(playerDeck.getmCardHand(), mDefaultScreenViewport, mDefaultLayerViewport, mGame);
                }

                for (Card cards : enemyDeck.getmCardHand()) {
                    cards.cardDrag(enemyDeck.getmCardHand(), mDefaultScreenViewport, mDefaultLayerViewport, mGame);
                }
            }
        }
    }

    /**
     * Draw the card demo screen
     *
     * @param elapsedTime Elapsed time information
     * @param graphics2D  Graphics instance
     */

    private Paint textPaint = new Paint();

    @Override
    public void draw(ElapsedTime elapsedTime, IGraphics2D graphics2D) {

        // Clear the screen
        graphics2D.clear(Color.WHITE);
        graphics2D.clipRect(mDefaultScreenViewport.toRect());

        // Draw the background first of all
        mGameBackground.draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);

        // Draw the player portrait (Just for testing, still working on it)
        p2.draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);
        opponent.draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);

        //Draw initial 'End Turn' button onscreen, which toggles between pressable and not pressable image:
        if (p2.getYourTurn())
            mEndTurnButton.draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);
        else
            mEndTurnButtonOff.draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);

        //Draw the remainder of the buttons:
        for (PushButton buttons : mButtons) {
            buttons.draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);
        }

        if (edgeCase) { //To test for the edge case of the coin flip, User Story 18.1, Sprint 4 - Scott
            int screenHeight = graphics2D.getSurfaceHeight();
            float textHeight = screenHeight / 30.0f;
            textPaint.setTextSize(textHeight); //create a appropriate sizing of text
            graphics2D.drawText("Iterations to reach Edge Case:", 100.0f, 50.0f, textPaint); //draw the text "Iterations to reach Edge Case:"
            graphics2D.drawText(String.valueOf(edgeCounter), 100.0f, 100.0f, textPaint);
        }

        //Draw the two player's hands, user and enemy:
        for (int i = 0; i < playerDeck.getmCardHand().size(); i++) {
            playerDeck.getmCardHand().get(i).draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);
        }

        for (int i = 0; i < enemyDeck.getmCardHand().size(); i++) {
            enemyDeck.getmCardHand().get(i).draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);
        }

        //Spacing that will be used to position everything:
        int spacingX = (int) mDefaultLayerViewport.getWidth() / 5;
        int spacingY = (int) mDefaultLayerViewport.getHeight() / 3;

        //PLAYER STATS BEING DRAWN:
        //Draw player mana
        //SINGLE COIN
        pDenarius.draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);

        graphics2D.drawText(p2.getCurrentMana() + "/" + p2.getCurrentManaCap(), spacingX * 12.9f, spacingY * 12f, mText);

        //Draw player card stats
        int pCardsLeft = playerDeck.getDeck().size();
        int pCardsHand = playerDeck.getmCardHand().size();
        int pCardsDead = playerDeck.getmDiscardPile().size(); // All stats accurate - Dearbhaile
        graphics2D.drawText("Deck: " + pCardsLeft, spacingX * 7.0f, spacingY * 11.6f, mText);
        graphics2D.drawText("Hand: " + pCardsHand, spacingX * 7.0f, spacingY * 12.2f, mText);
        graphics2D.drawText("Graveyard: " + pCardsDead, spacingX * 7.0f, spacingY * 12.8f, mText);

        //Draw opponent mana
        //SINGLE COIN
        eDenarius.draw(elapsedTime, graphics2D, mGameViewport, mDefaultScreenViewport);

        graphics2D.drawText(opponent.getCurrentMana() + "/" + opponent.getCurrentManaCap(), spacingX * 12.9f, spacingY * 1.2f, mText);

        //Draw opponent card stats
        int eCardsLeft = enemyDeck.getDeck().size();
        int eCardsHand = enemyDeck.getmCardHand().size();
        int eCardsDead = enemyDeck.getmDiscardPile().size(); // All stats accurate - Dearbhaile
        graphics2D.drawText("Deck: " + eCardsLeft, spacingX * 7.0f, spacingY * 0.6f, mText);
        graphics2D.drawText("Hand: " + eCardsHand, spacingX * 7.0f, spacingY * 1.2f, mText);
        graphics2D.drawText("Graveyard: " + eCardsDead, spacingX * 7.0f, spacingY * 1.8f, mText);
    }

    public int getCoinTossResult() {
        return this.coinTossResult;
    }
}
