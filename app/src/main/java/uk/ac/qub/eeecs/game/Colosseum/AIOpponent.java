package uk.ac.qub.eeecs.game.Colosseum;

import android.graphics.Bitmap;

import java.util.ArrayList;

import uk.ac.qub.eeecs.gage.world.GameScreen;
import uk.ac.qub.eeecs.game.Colosseum.Regions.ActiveRegion;
import uk.ac.qub.eeecs.game.Colosseum.Regions.HandRegion;

/*
   Authored by Scott Barham & Kyle Corrigan
 * An extension of the player class, with the addition of AI behaviors
 */

public class AIOpponent extends Player {

    private float opponentPortraitXPos = getPortraitXPos(); // Centers the player portrait x coordinate to the center of the screen, same as the player portrait - Kyle
    private float opponentPortraitYPos = getGameScreen().getDefaultLayerViewport().getTop()-(getPortraitHeight()/2)-(70.0f/1.5f); // Displays the portrait at the top of the screen - Kyle
    private float abilityFrameXPos = getAbilityFrameXPos(); //-Kyle
    private float abilityFrameYPos = opponentPortraitYPos - (getPortraitHeight()/2) + (getAbilityFrameHeight()/2); //-Kyle

    //Pass in the regions via the constructor, so the data can be grabbed from each, and then output - Scott
    private HandRegion playerHandRegion,opponentHandRegion; //-Scott
    private ActiveRegion playerActiveRegion,opponentActiveRegion; //-Scott

    //Create a list of cards in the AI hand, board and player board region -Scott
    private ArrayList<Card> cardsInAIHandRegion, cardsInAIBoardRegion, cardsInPlayerBoardRegion;
    private int playerHealth, aiHealth, boardspaceRemaining, handspaceRemaining, manaRemaining; //Values of current player and ai attributes. -Scott
    private int[] playerCardValues = {0, 0}, aiBoardCardValues = {0, 0}; //List of the values of played card values with notation of {attack,health}. -Scott
    private int[] cardInHandCategory = {0,0,0}; //different card types, in order of "minion", "spell", "weapon" for cards in hand/board. -Scott

    public AIOpponent(GameScreen gameScreen, String hero, HandRegion playerHandRegion, HandRegion opponentHandRegion, ActiveRegion playerActiveRegion, ActiveRegion opponentActiveRegion){
        super(gameScreen, hero); //-Kyle

        setPortraitYPos(opponentPortraitYPos); //-Kyle
        setAbilityFrameYPos(abilityFrameYPos); //-Kyle
        createHeroAbilityButton(hero); //-Kyle

        //Rest of code after this by Scott
        this.playerHandRegion = playerHandRegion;
        this.opponentHandRegion = opponentHandRegion;
        this.playerActiveRegion = playerActiveRegion;
        this.opponentActiveRegion = opponentActiveRegion;
    }

    public void playRandom() { //Temporary code to play a random minion to test attacking functionality - Scott
        opponentActiveRegion.addCard(opponentHandRegion.getCardsInRegion().get(0));
        opponentHandRegion.removeCard(opponentHandRegion.getCardsInRegion().get(0));
    }

    private void cardLists(ArrayList<Card> cardsInRegion, int[] cardValues, boolean handRegion) { //Create lists of cards values, depending on regions chosen  - Scott
        for(int i=0; i<cardsInRegion.size(); i++){ //If it is from the board region, handCategory is false, else if hand region, its true.
            if(cardsInRegion.get(i) instanceof MinionCard) { //Checking if the current card looked at is an instance of "MinionCard"
                if(!handRegion) { //If we are looking at the board region..
                    MinionCard minion = (MinionCard) cardsInRegion.get(i); //Get the card as a minion
                    cardValues[0] += minion.getAttack(); //calculate the total attack values for all minions that player controls
                    cardValues[1] += minion.getHealth(); //calculate the total health values for all minions that player controls
                } else { //Else we are looking at hand region
                    cardValues[0]++; //Increase the amount of minion card count
                }
            } else if(handRegion) { //If we are looking at the hand region..
                if(cardsInRegion.get(i) instanceof SpellCard) { //Checking if the current card looked at is an instance of "SpellCard"
                    cardValues[1]++; //Increase the amount of spell card count
                } else if(cardsInRegion.get(i) instanceof WeaponCard) { //Checking if the current card looked at is an instance of "WeaponCard"
                    cardValues[2]++; //Increase the amount of weapon card count
                }
            }
        }
    }

    private void resetValues(int[] valuesToReset) { //Reset values of integer arrays to prevent carried over values - Scott
        for(int i=0; i < valuesToReset.length; i++) {
            valuesToReset[i] = 0;
        }
    }

    private void aiTurnSetup() { //Setup an up to date version of all the information the AI will use to make decisions upon what moves it takes - Scott

        //reset these to default values to prevent carry over values
        resetValues(playerCardValues); resetValues(aiBoardCardValues); resetValues(cardInHandCategory);


        cardsInAIHandRegion = opponentHandRegion.getCardsInRegion(); //get a copy of all the cards the AI has in hand
        cardsInAIBoardRegion = opponentActiveRegion.getCardsInRegion(); //get a copy of all the cards the AI has in play
        cardsInPlayerBoardRegion = playerActiveRegion.getCardsInRegion(); //get a copy of all the cards the player has in play

        playerHealth = Player.getCurrentHealth(); //get the players health
        aiHealth = getCurrentHealth(); //get the ai health
        boardspaceRemaining = opponentActiveRegion.getMaxNumCardsInRegion()-cardsInAIBoardRegion.size(); //calculate how many cards the ai can play
        handspaceRemaining = opponentHandRegion.getMaxNumCardsInRegion()-cardsInAIHandRegion.size(); //calculate how many cards you can draw before they burn
        manaRemaining = getCurrentMana(); //get the current amount of mana the ai has

        cardLists(cardsInAIBoardRegion,aiBoardCardValues, false);
        cardLists(cardsInPlayerBoardRegion,playerCardValues, false);
        cardLists(cardsInAIHandRegion,cardInHandCategory, true);


    }

    private void aiMovesSetup() { // Scott
        //do a check if a card is a weapon
        if(isWeaponEquipped()) {
            //only equip a new weapon if lethal or preventing enemy lethal.
        } else {
            //equip a weapon is a possible move
        }
    }

    public void aiTurn() { // Scott
        aiTurnSetup();
        //cards that can be played (weapons if no weapon slot filled, spells always unless mana)
        //cards that can attack
        //any taunt minions in the way
        //if ai can attack with weapon

    }
}
