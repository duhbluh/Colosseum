package uk.ac.qub.eeecs.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import uk.ac.qub.eeecs.gage.Game;
import uk.ac.qub.eeecs.gage.engine.ElapsedTime;
import uk.ac.qub.eeecs.gage.engine.graphics.IGraphics2D;
import uk.ac.qub.eeecs.gage.engine.input.Input;
import uk.ac.qub.eeecs.gage.engine.input.TouchEvent;
import uk.ac.qub.eeecs.gage.ui.FPSCounter;
import uk.ac.qub.eeecs.gage.ui.PushButton;
import uk.ac.qub.eeecs.gage.world.GameScreen;
import uk.ac.qub.eeecs.gage.world.LayerViewport;
import uk.ac.qub.eeecs.game.Colosseum.colosseumDemoScreen;

public class PauseMenuScreen extends GameScreen {

    // /////////////////////////////////////////////////////////////////////////
    // Properties
    // /////////////////////////////////////////////////////////////////////////

    private List<PushButton> mButtons = new ArrayList<>();
    private PushButton mMenuScreen, mResume, mOptions, mMainMenu, mConcede, mStatsButton, mHTPButton;
    private LayerViewport mMenuViewport;

    //Information needed to set Music/SFX/FPS Preferences:
    private Context mContext = mGame.getActivity();
    private SharedPreferences mGetPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
    private SharedPreferences.Editor mPrefEditor = mGetPreference.edit();

    FPSCounter fpsCounter;

    public PauseMenuScreen(Game game)
    {
        super("PauseScreen",game);
        //Setting up  viewports method
        setupViewports();
        //Settign up pause menu objects
        SetUpPauseMenuOpbjects();
    }

    // /////////////////////////////////////////////////////////////////////////
    // Methods
    // /////////////////////////////////////////////////////////////////////////

    public void setupViewports() {
        // Setup the screen viewport to use the full screen:
        mDefaultScreenViewport.set(0, 0, mGame.getScreenWidth(), mGame.getScreenHeight());

        /* Calculate the layer height that will preserved the screen aspect ratio
         given an assume 480 layer width.*/
        float layerHeight = mGame.getScreenHeight() * (480.0f / mGame.getScreenWidth());

        mDefaultLayerViewport.set(240.0f, layerHeight / 2.0f, 240.0f, layerHeight / 2.0f);
        mMenuViewport = new LayerViewport(240.0f, layerHeight / 2.0f, 240.0f, layerHeight / 2.0f);
    }


    private void SetUpPauseMenuOpbjects()
    {
        int spacingX = (int) mDefaultLayerViewport.getWidth() / 5;
        int spacingY = (int) mDefaultLayerViewport.getHeight() / 3;

        //getting relevant images for this screen
        mGame.getAssetManager().loadAssets("txt/assets/PauseMenuAssets.JSON");

        //Creating the screen
        mMenuScreen = new PushButton(mDefaultLayerViewport.getWidth() / 2.0f,
                mDefaultLayerViewport.getHeight() / 2.0f, mDefaultLayerViewport.getWidth(),
                mDefaultLayerViewport.getHeight(),"Pause", this);
        //Creating resume button
        mResume = new PushButton(spacingX * 2.5f, spacingY * 1.95f,
                spacingX * 0.8f,spacingY * 0.39f,"Resume",
                "ResumeSelected",this );
        mButtons.add(mResume);
        //Creating the options button
        mOptions = new PushButton(spacingX * 2.5f, spacingY * 1.45f,
                spacingX * 0.8f,spacingY * 0.39f,"Options",
                "OptionsSelected",this );
        mButtons.add(mOptions);
        //Creating the 'main menu' button
        mMainMenu = new PushButton(spacingX * 2.5f, spacingY * 0.95f,
                spacingX * 0.8f,spacingY * 0.39f,"MainMenu",
                "MainMenuSelected",this );
        mButtons.add(mMainMenu);
        //Creating the 'concede' button
        mConcede = new PushButton(spacingX * 2.5f, spacingY * 0.45f,
                spacingX * 0.8f,spacingY * 0.39f,"Concede",
                "ConcedeSelected",this );
        mButtons.add(mConcede);
        mStatsButton = new PushButton(
                spacingX*0.5f, spacingY * 0.4f, spacingX*0.7f, spacingY*0.6f,
                "statsButton", "statsButtonSelected",this);
        mButtons.add(mStatsButton);
        //Create the Quit button
        mHTPButton = new PushButton(
                spacingX * 4.5f, spacingY * 0.4f, spacingX*0.8f, spacingY*0.5f,
                "HTPButton", "HTPButtonSelected",this);
        mButtons.add(mHTPButton);

        fpsCounter = new FPSCounter( mMenuViewport.getWidth() * 0.50f, mMenuViewport.getHeight() * 0.20f , this) { };
    }

    @Override
    public void update(ElapsedTime elapsedTime) {

        // Process any touch events occurring since the update
        Input input = mGame.getInput();

        List<TouchEvent> touchEvents = input.getTouchEvents();

        if (touchEvents.size() > 0) {
            for (PushButton button : mButtons)
                button.update(elapsedTime);

            if(mResume.isPushTriggered())
                mGame.getScreenManager().changeScreenButton(new colosseumDemoScreen(mGame));
            else if(mOptions.isPushTriggered())
                mGame.getScreenManager().changeScreenButton((new OptionsScreen(mGame)));
            else if (mMainMenu.isPushTriggered()){
                mGame.getScreenManager().changeScreenButton(new MenuScreen(mGame));
            } else if (mStatsButton.isPushTriggered()) {
                mGame.getScreenManager().changeScreenButton((new StatisticsScreen(mGame)));
            } else if (mHTPButton.isPushTriggered()) {
                mGame.getScreenManager().changeScreenButton((new HTPScreen(mGame)));
            } else if (mConcede.isPushTriggered()) {
                EndGameScreen.setMostRecentResult("loss");
                EndGameScreen.setConcedeResult(true);
                colosseumDemoScreen.setWasPaused(false); //Set this back to false for a new game
                mGame.getScreenManager().changeScreenButton(new EndGameScreen(mGame));
            }
        }
    }
    @Override
    public void draw(ElapsedTime elapsedTime, IGraphics2D graphics2D) {
        graphics2D.clear(Color.WHITE);
        graphics2D.clipRect(mDefaultScreenViewport.toRect());

        // Draw the background first of all
        mMenuScreen.draw(elapsedTime, graphics2D, mMenuViewport,
                mDefaultScreenViewport);

        for (PushButton button : mButtons) //Draw all the buttons stored in "mButtons"
            button.draw(elapsedTime, graphics2D, mDefaultLayerViewport, mDefaultScreenViewport);

        if(mGetPreference.getBoolean("FPS", true)) {
            fpsCounter.draw(elapsedTime, graphics2D);
        }
    }
}