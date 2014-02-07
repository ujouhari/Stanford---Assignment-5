/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.ArrayList;
import java.util.Arrays;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);

		playGame();
	}

	/**Initializes the game.*/
	private void playGame() {

		scoreSheet = new int[nPlayers][N_CATEGORIES];
		category = new int[nPlayers][N_CATEGORIES];

		for (int i = 1; i <= N_SCORING_CATEGORIES; i++) {
			for (int j = 1; j <= nPlayers; j++) {
				
				firstRoll(j);
				secondAndThirdRoll();
				updateCategory(j);
			}
		}
		gameOver();
	}

	/** Prompts the player to take their first roll, then displays the values of the dice. */
	private void firstRoll(int playerNumber) {
		
		display.printMessage(playerNames[playerNumber-1] + "'s turn! Click the " + "\"Roll Dice\" " + "button to roll the dice.");
		display.waitForPlayerToClickRoll(playerNumber);
		rollDice();
		display.displayDice(dice);
	}

	/** Gives each dice a random value from 1 to 6. */
	private void rollDice() {

		for (int i = 0; i < N_DICE; i++) {
			dice[i] = rgen.nextInt(1,6);
		}
	}

	/** Prompts the player to re-roll, then displays the updated values of the dice. */
	private void secondAndThirdRoll() {

		for (int i = 0; i < 2; i++) {
			display.printMessage("Select the dice you wish to re-roll and click." + "\"Roll Dice\" ");
			display.waitForPlayerToSelectDice();
			for (int j = 0; j < N_DICE; j++) {
				if (display.isDieSelected(j)) {
					dice[j] = rgen.nextInt(1,6);
				}
			}
			display.displayDice(dice);
		}
	}

	/**Prompts the player to pick a category, verifies the validity of the category, 
	 * assigns scores to the category and updates the score sheet. */
	private void updateCategory(int playerNumber) {

		display.printMessage("Select a category for this roll.");

		while(true) {
			int chosenCategory = display.waitForPlayerToSelectCategory();

			if (category[playerNumber-1][chosenCategory-1] == 0) {

				boolean check = checkCategory(dice, chosenCategory);
				int score = scoreCalculator(chosenCategory, dice, check);
				category[playerNumber-1][chosenCategory-1] = 1;
				checkScoreSheet(chosenCategory, playerNumber, score);
				display.updateScorecard(chosenCategory, playerNumber, score);
				break;
			}
			display.printMessage("Category already filled. Please choose another.");
		}
	}

	/**For each category in the game the method checks if it is valid.
	 * If the category chosen is correct, boolean value true is returned and if not, false is returned.*/
	private boolean checkCategory (int[] dice, int category) {

		int[] diceValues = Arrays.copyOf(this.dice, N_DICE);
		Arrays.sort (diceValues);

		if (category == ONES || category == TWOS || category == THREES || category == FOURS || category == FIVES || category == SIXES || category == CHANCE){
			return true;
		}

		else if (category == THREE_OF_A_KIND || category == FOUR_OF_A_KIND || category == YAHTZEE) {

			for (int i = 0; i < N_DICE; i++) {
				int maxCounter = 0;
				for (int j = 0; j < N_DICE; j++) {
					if (dice[i] == dice[j]) {
						maxCounter++;
					}
				}

				if ((category == THREE_OF_A_KIND && maxCounter >= 3) || (category == FOUR_OF_A_KIND && maxCounter >= 4) ||(category == YAHTZEE && maxCounter == 5)) {
					return true;
				}
			}
			return false; 
		}


		else if (category == FULL_HOUSE) {

			if (diceValues[0] == diceValues[1] && diceValues[1] == diceValues[2] && diceValues[3] == diceValues[4]){
				return true;
			}

			else if (diceValues[0] == diceValues[1] && diceValues[2] == diceValues[3] && diceValues[3] == diceValues[4]) {
				return true;
			}
			else return false;
		}

		else if (category == SMALL_STRAIGHT) {

			int counter = 0;
			int match = 0;
			int[] numberOfMatches;
			for (int i = 1; i < N_DICE; i++) { 
				if (diceValues[i] == diceValues[i - 1]) {
					counter++;
					match = i;
				}
			}

			if (counter == 1) {
				numberOfMatches = new int[4];
				numberOfMatches[0] = diceValues[0];
				for (int i = 1; i < N_DICE; i++) {
					if (i == match) continue;
					else if (i < match) numberOfMatches[i] = diceValues[i];
					else if (i > match) numberOfMatches[i - 1] = diceValues[i]; 
				}

				for (int i = 1; i < numberOfMatches.length; i++) {
					if (numberOfMatches[i] != numberOfMatches[i - 1] + 1) return false;
					if (i == numberOfMatches.length - 1) return true;
				}
				return false;
			}

			else if (counter == 0) {
				numberOfMatches = Arrays.copyOf(diceValues, N_DICE);
				for (int i = 1; i < numberOfMatches.length - 1; i++) {
					if (numberOfMatches[i] != numberOfMatches[i - 1] + 1) break;
					if (i == numberOfMatches.length - 2) return true;
				}
				for (int i = 2; i < numberOfMatches.length; i++) {
					if (numberOfMatches[i] != numberOfMatches[i - 1] + 1) break;
					if (i == numberOfMatches.length - 1) return true;
				}
				return false;
			}

			else return false;
		}

		else if (category == LARGE_STRAIGHT) {

			for (int i = 1; i < N_DICE; i++) {
				if (diceValues[i] != diceValues[i - 1] + 1) break;
				if (i == N_DICE - 1) return true;
			}
		}
		return false;
	}

	/**Defines the method to calculate the score for each category in the game.*/
	private int scoreCalculator(int category, int[] dice, boolean check) {

		int score = 0;

		if (!check) return score;

		if (category >= ONES && category <= SIXES) {
			for (int i = 0; i < N_DICE; i++) {
				if (dice[i] == category)
					score += category;
			}
		} 

		if (category == THREE_OF_A_KIND) {
			for (int i = 0; i < N_DICE; i++) {
				score += dice[i];
			}
		}

		if (category == FOUR_OF_A_KIND) {
			for (int i = 0; i < N_DICE; i++) {
				score += dice[i];
			}
		}

		if (category == CHANCE) {
			for (int i = 0; i < N_DICE; i++) {
				score += dice[i];
			}
		}	

		if (category == FULL_HOUSE) score = 25;

		if (category == SMALL_STRAIGHT) score = 30;

		if (category == LARGE_STRAIGHT) score = 40;

		if (category == YAHTZEE) score = 50;

		return score;

	}

	/**Calculates the score of each player and displays it on the score sheet*/
	private void checkScoreSheet(int category, int playerNumber, int score) {

		scoreSheet[playerNumber-1][category-1] = score;
		int upperScore = 0;
		int lowerScore = 0;
		int totalScore = 0; 

		for (int i = ONES; i <= SIXES; i++) {

			upperScore += scoreSheet[playerNumber-1][i-1];
			display.updateScorecard(UPPER_SCORE, playerNumber, upperScore);
		}
		scoreSheet[playerNumber-1][UPPER_SCORE-1] = upperScore;

		if (upperScore >= BONUS_CUTOFF) {

			display.updateScorecard(UPPER_BONUS, playerNumber, BONUS_VALUE);
			scoreSheet[playerNumber-1][UPPER_BONUS-1] = BONUS_VALUE;
			display.updateScorecard(UPPER_BONUS, playerNumber, BONUS_VALUE);
		}


		for (int j = THREE_OF_A_KIND; j <= CHANCE; j++) {

			lowerScore += scoreSheet[playerNumber-1][j-1];
		}
		scoreSheet[playerNumber-1][LOWER_SCORE-1] = lowerScore;
		display.updateScorecard(LOWER_SCORE, playerNumber, lowerScore);
		totalScore = upperScore + lowerScore + scoreSheet[playerNumber-1][UPPER_BONUS-1];
		scoreSheet[playerNumber-1][TOTAL-1] = totalScore;
		display.updateScorecard(TOTAL, playerNumber, totalScore);
	}

	/**Defines the method for finding and displaying the winner of the game. */
	private void gameOver() {

		int winner = 0;
		int winnerScore = 0;
		for (int i = 1; i <= nPlayers; i++) {
			if (scoreSheet[i-1][TOTAL-1] > winnerScore) {
				winner = i;
				winnerScore = scoreSheet[i-1][TOTAL-1];
			}
		}
		String gameWinner = playerNames[winner-1];
		display.printMessage("Congratulations " + gameWinner + ", you are the winner with a total score of " + scoreSheet[winner-1][TOTAL-1] + "!!!!");
	}



	/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[][] scoreSheet;
	private int[][] category;
	private int[] dice = new int[N_DICE];
	private int BONUS_VALUE = 35;
	private int BONUS_CUTOFF = 63;


}
