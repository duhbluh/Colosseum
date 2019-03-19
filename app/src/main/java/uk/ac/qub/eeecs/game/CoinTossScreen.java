package uk.ac.qub.eeecs.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;

import java.util.List;
import java.util.Random;

import uk.ac.qub.eeecs.gage.Game;
import uk.ac.qub.eeecs.gage.engine.ElapsedTime;
import uk.ac.qub.eeecs.gage.engine.ScreenManager;
import uk.ac.qub.eeecs.gage.engine.graphics.IGraphics2D;
import uk.ac.qub.eeecs.gage.engine.input.Input;
import uk.ac.qub.eeecs.gage.engine.input.TouchEvent;
import uk.ac.qub.eeecs.gage.ui.FPSCounter;
import uk.ac.qub.eeecs.gage.ui.PushButton;
import uk.ac.qub.eeecs.gage.ui.TitleImage;
import uk.ac.qub.eeecs.gage.world.GameObject;
import uk.ac.qub.eeecs.gage.world.GameScreen;
import uk.ac.qub.eeecs.gage.world.LayerViewport;
import uk.ac.qub.eeecs.game.Colosseum.AIOpponent;
import uk.ac.qub.eeecs.game.Colosseum.CardDeck;
import uk.ac.qub.eeecs.game.Colosseum.Coin;
import uk.ac.qub.eeecs.game.Colosseum.Player;
import uk.ac.qub.eeecs.game.Colosseum.Regions.HandRegion;
import uk.ac.qub.eeecs.game.Colosseum.Turn;
import uk.ac.qub.eeecs.game.Colosseum.UserWhoStarted;

//CoinTossScreen, coded by Dearbhaile Walsh
public class CoinTossScreen extends GameScreen {

    // Properties
    private ScreenManager mScreenManager = new ScreenManager(mGame);

    //Different objects required for this screen to function
    private GameObject mCTSBackground;
    private LayerViewport mGameViewport;
    private TitleImage mCoinTossTitle;
    private FPSCounter fpsCounter;

    //Variables required for the time delay on this screen:
    private long mCoinToss_Timeout = 10000;
    private long mTimeOnCreate, mCurrentTime;
    private long mTimeRemaining;

    //Create PushButton necessary to skip animation
    PushButton mSkipButton;

    //Turn object that stores all data about the current turn:
    private Turn mCurrentTurn = new Turn();

    //Define the Player
    private Player mPlayer;

    // Define the Opponent
    private AIOpponent mOpponent;

    //Define the two Decks
    private CardDeck mPlayerDeck, mEnemyDeck;

    //UserWhoStarted variable to hold data about who started in this match:
    private UserWhoStarted mUserWhoStarted;

    //Variable to store time of enemy turn beginning (if applicable):
    private long mEnemyTurnBegins = 0;

    //Variables required for the message (lines 1 and 2) to display properly
    private int mCoinTossResult = 0;
    private String mCoinTossMsg1 = "";
    private String mCoinTossMsg2 = "";

    //Paint items that will be used to draw text
    private Paint mMessageText;
    private Paint mTimerText;

    //Information needed to set Music/SFX/FPS Preferences:
    private Context mContext = mGame.getActivity();
    private SharedPreferences mGetPreference = PreferenceManager.getDefaultSharedPreferences(mContext);

    //Create instance of Coin object:
    private Coin mCoin;

    // Constructor
    //Create the 'CoinTossScreen' screen
    public CoinTossScreen(Game game) {
        super("CoinTossScreen", game);
        mTimeOnCreate = System.currentTimeMillis();
        setupViewports();
        setUpCTSObjects();
        setUpGameObjects();
        coinFlipStart();
        mCoinTossResult = coinFlipStart();
        coinFlipResult(mCoinTossResult);
        chooseTextToDisplay();
    }

    public void setUpGameObjects() {
        //This class acts as a loader class for the colosseumDemoScreen:
        mGame.getAssetManager().loadAssets("txt/assets/ColosseumAssets.JSON");
        mGame.getAssetManager().loadAssets("txt/assets/HeroAssets.JSON");
        mGame.getAssetManager().loadAssets("txt/assets/CardAssets.JSON");

        //Setting up demo player:
        mPlayer = new Player(this, "Meridia");
        mOpponent = new AIOpponent(this, "EmperorCommodus");

        mPlayer.setCurrentMana(4);
        mPlayer.setCurrentManaCap(4);

        mOpponent.setCurrentMana(4);
        mOpponent.setCurrentManaCap(4);

        //This method sets up the player and enemy decks, called when screen is loaded. - Dearbhaile
        HandRegion playerHandRegion = new HandRegion(mDefaultLayerViewport.getRight() / 2 - (4 * (50.0f / 1.5f)), mDefaultLayerViewport.getRight() / 2 + (4 * (50.0f / 1.5f)), mPlayer.position.y - (mPlayer.getPortraitHeight() / 2), mDefaultLayerViewport.getBottom());
        HandRegion opponentHandRegion = new HandRegion(mDefaultLayerViewport.getRight() / 2 - (4 * (50.0f / 1.5f)), mDefaultLayerViewport.getRight() / 2 + (4 * (50.0f / 1.5f)), mDefaultLayerViewport.getTop(), mOpponent.position.y + (mOpponent.getPortraitHeight() / 2));

        mPlayerDeck = new CardDeck(1, "Basic Player Deck", this, false, playerHandRegion);
        mEnemyDeck = new CardDeck(2, "Basic Enemy Deck", this, true, opponentHandRegion);

        for (int i = 0; i < mEnemyDeck.getmCardHand().size(); i++) {
            mEnemyDeck.getmCardHand().get(i).flipCard();
        }
    }

    public void setUpCTSObjects() {
        mGame.getAssetManager().loadAssets("txt/assets/CoinTossAssets.JSON");

        mCTSBackground = new GameObject(mGameViewport.getWidth()/ 2.0f,
                mGameViewport.getHeight()/ 2.0f, mGameViewport.getWidth(),
                mGameViewport.getHeight(), getGame()
                .getAssetManager().getBitmap("CTSBackground"), this);

        //Set up the FPS counter:
        fpsCounter = new FPSCounter( mGameViewport.getWidth() * 0.50f, mGameViewport.getHeight() * 0.20f , this) {};

        //Set up Coin object for display in animation:
        mCoin = new Coin( mDefaultLayerViewport.getRight() / 2.f, mDefaultLayerViewport.getTop() / 2.f,100.0f,100.0f, this, getmCoinTossResult());

        // Spacing that will be used to position the Coin Toss Screen Objects:
        int spacingX = (int) mDefaultLayerViewport.getWidth() / 5;
        int spacingY = (int) mDefaultLayerViewport.getHeight() / 3;

        // Create the title image
        mCoinTossTitle = new TitleImage(mDefaultLayerViewport.getWidth() / 2.0f, spacingY * 2.5f, spacingX*1.5f, spacingY/2.2f, "CTSTitle",this);

        //Create the Skip button
        mSkipButton = new PushButton(spacingX * 3.9f, spacingY * 2.5f, spacingX*0.8f, spacingY*0.8f,
                "SkipArrow", this);

        //PAINT OBJECTS:
        //Initialise Paint Objects I will use to draw text
        mMessageText = new Paint();
        int screenHeight = mDefaultScreenViewport.height;
        float textHeight = screenHeight / 20.0f;
        mMessageText.setTextSize(textHeight);
        mMessageText.setColor(Color.BLACK);
        mMessageText.setTypeface(Typeface.create("Arial", Typeface.BOLD));

        mTimerText = new Paint();
        float smallTextHeight = screenHeight / 24.0f;
        mTimerText.setTextSize(smallTextHeight);
        mTimerText.setColor(Color.BLACK);
        mTimerText.setTypeface(Typeface.create("Arial", Typeface.BOLD));
    }

    public void setupViewports() {
        // Setup the screen viewport to use the full screen.
        mDefaultScreenViewport.set(0, 0, mGame.getScreenWidth(), mGame.getScreenHeight());

        // Calculate the layer height that will preserved the screen aspect ratio
        // given an assume 480 layer width.
        float layerHeight = mGame.getScreenHeight() * (480.0f / mGame.getScreenWidth());

        mDefaultLayerViewport.set(240.0f, layerHeight / 2.0f, 240.0f, layerHeight / 2.0f);
        mGameViewport = new LayerViewport(240.0f, layerHeight / 2.0f, 240.0f, layerHeight / 2.0f);
    }

    private int coinFlipStart() {
        Random RANDOM = new Random();
        int flip = RANDOM.nextInt(6001);
        if (flip == 6000) { //side of coin (1/6000 chance to auto-win)
            return 2;
        } else if (flip >= 3000 && flip < 6000) { //heads (ai starts)
            return 1;
        } else if (flip >= 0 && flip < 3000) { //tails (user starts)
            return 0;
        }
        return -1;
    }

    // Method for setting up stats based on Coin Toss:
    private void coinFlipResult(int result) {
        switch (result) {
            case 0: // ie, player starts
                mCurrentTurn.setUpStats_PlayerStarts(mPlayer, mPlayerDeck, mOpponent, mEnemyDeck);
                mUserWhoStarted = UserWhoStarted.PLAYERSTARTED;
                break;
            case 1: // ie, ai starts
                mCurrentTurn.setUpStats_EnemyStarts(mPlayer, mPlayerDeck, mOpponent, mEnemyDeck);
                mUserWhoStarted = UserWhoStarted.ENEMYSTARTED;
                break;
            case 2: //edge of coin - set opponent health to 0, auto win game.
                EndGameScreen.setCoinFlipResult(true);
                break;
        }
    }

    public void chooseTextToDisplay() {
        if (mCoinTossResult == 0) {
            mCoinTossMsg1 = "The coin landed on heads! You get to play first.";
            mCoinTossMsg2 = "The other player draws 4 cards, and gets 1 additional mana.";
        }
        else if (mCoinTossResult == 1) {
            mCoinTossMsg1 = "The coin landed on tails! The enemy plays first.";
            mCoinTossMsg2 = "You draw an extra card and additional mana for your troubles.";
        }
        else if (mCoinTossResult == 2) {
            mCoinTossMsg1 = "The coin landed on its edge!";
            mCoinTossMsg2 = "You automatically win the game for being lucky!";
        }
    }

    @Override
    public void update(ElapsedTime elapsedTime) {
        // Process any touch events occurring since the update
        Input input = mGame.getInput();

        mCurrentTime = System.currentTimeMillis();
        mTimeRemaining = 10 - ((mCurrentTime - mTimeOnCreate)/1000);

        if (!mCoin.isComplete()) {
            mCoin.coinAnimation();
        }

        if (mCurrentTime - mTimeOnCreate >= mCoinToss_Timeout) {
            if (mOpponent.getYourTurn()) {
                mEnemyTurnBegins = System.currentTimeMillis();
            }

            mGame.getScreenManager().getCurrentScreen().dispose();
           mScreenManager.changeScreenButton(new colosseumDemoScreen(mPlayer, mOpponent, mCurrentTurn,
                    mUserWhoStarted, mEnemyTurnBegins, mPlayerDeck, mEnemyDeck, mGame));
        }

        List<TouchEvent> touchEvents = input.getTouchEvents();
        if (touchEvents.size() > 0) {
            mSkipButton.update(elapsedTime);

            //If the 'skip animation' button is pressed, then go straight to game:
            if (mSkipButton.isPushTriggered()) {
                mCoinToss_Timeout = 0;
            }
        }
    }

    @Override
    public void draw(ElapsedTime elapsedTime, IGraphics2D graphics2D) {
        // Clear the screen
        graphics2D.clear(Color.WHITE);

        //Draw the background
        mCTSBackground.draw(elapsedTime, graphics2D, mDefaultLayerViewport,
                mDefaultScreenViewport);

        //Draw the title image
        mCoinTossTitle.draw(elapsedTime, graphics2D, mDefaultLayerViewport, mDefaultScreenViewport);

        //Draw the skip button
        mSkipButton.draw(elapsedTime, graphics2D, mDefaultLayerViewport, mDefaultScreenViewport);

        // Spacing that will be used to position the Paint object:
        float SCREEN_WIDTH = mGame.getScreenWidth();
        float SCREEN_HEIGHT = mGame.getScreenWidth();

        mCoin.draw(elapsedTime, graphics2D, mDefaultLayerViewport, mDefaultScreenViewport);

        if (mCurrentTime - mTimeOnCreate >= 3000) {
            graphics2D.drawText(mCoinTossMsg1, SCREEN_WIDTH * 0.24f, SCREEN_HEIGHT * 0.42f, mMessageText);
            graphics2D.drawText(mCoinTossMsg2, SCREEN_WIDTH * 0.18f, SCREEN_HEIGHT * 0.48f, mMessageText);

            graphics2D.drawText("Game will begin in " + mTimeRemaining + " seconds...", SCREEN_WIDTH * 0.46f, SCREEN_HEIGHT * 0.52f, mTimerText);
        }

        if(mGetPreference.getBoolean("FPS", true)) {
            fpsCounter.draw(elapsedTime, graphics2D);
        }
    }

    //Getters and setters:
    public int getmCoinTossResult() { return this.mCoinTossResult; }
    public String getmCoinTossMsg1() { return this.mCoinTossMsg1; }
    public String getmCoinTossMsg2() { return this.mCoinTossMsg2; }
}
