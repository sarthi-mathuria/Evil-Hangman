

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
 * Manages the details of EvilHangman. This class keeps tracks of the possible
 * words from a dictionary during rounds of hangman, based on guesses so far.
 *
 */
public class HangmanManager {

	// instance variables / fields
	private HangmanDifficulty difficulty;
	private ArrayList<String> dictionary;
	private ArrayList<String> activeWords;
	private ArrayList<Character> guessesMade;
	private char guess;
	private String activePattern;
	private int wordLength;
	private int numGuesses;
	private int numGuessesMade;
	private static final char DASH = '-';

	/**
	 * Create a new HangmanManager from the provided set of words and phrases. pre:
	 * words != null, words.size() > 0
	 * 
	 * @param words   A set with the words for this instance of Hangman.
	 * @param debugOn true if we should print out debugging to System.out.
	 * @throws FileNotFoundException
	 */
	public HangmanManager(Set<String> words, boolean debugOn) throws FileNotFoundException {

		if (words == null || words.size() <= 0) {
			throw new IllegalArgumentException("Violation of preconditions.");
		}

		dictionary = new ArrayList<String>();
		Iterator<String> wordIterator = words.iterator();
		while (wordIterator.hasNext()) {
			dictionary.add(wordIterator.next());
		}

	}

	/**
	 * Create a new HangmanManager from the provided set of words and phrases.
	 * Debugging is off. pre: words != null, words.size() > 0
	 * 
	 * @param words A set with the words for this instance of Hangman.
	 * @throws FileNotFoundException
	 */
	public HangmanManager(Set<String> words) throws FileNotFoundException {

		if (words == null || words.size() <= 0) {
			throw new IllegalArgumentException("Violation of preconditions.");
		}

		dictionary = new ArrayList<String>();
		Iterator<String> wordIterator = words.iterator();
		while (wordIterator.hasNext()) {
			dictionary.add(wordIterator.next());
		}

	}

	/**
	 * Get the number of words in this HangmanManager of the given length. pre: none
	 * 
	 * @param length The given length to check.
	 * @return the number of words in the original Dictionary with the given length
	 */
	public int numWords(int length) {

		int count = 0;

		for (int x = 0; x < dictionary.size(); x++) {
			if (dictionary.get(x).length() == length) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Get for a new round of Hangman. Think of a round as a complete game of
	 * Hangman.
	 * 
	 * @param wordLen    the length of the word to pick this time. numWords(wordLen)
	 *                   > 0
	 * @param numGuesses the number of wrong guesses before the player loses the
	 *                   round. numGuesses >= 1
	 * @param diff       The difficulty for this round.
	 * @throws FileNotFoundException
	 */
	public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) 
			throws FileNotFoundException {

		if (numWords(wordLen) <= 0 || numGuesses < 1) {
			throw new IllegalArgumentException("Violation of precondition.");
		}

		activeWords = new ArrayList<String>();
		guessesMade = new ArrayList<Character>();
		this.numGuesses = numGuesses;
		numGuessesMade = 0;
		wordLength = wordLen;
		difficulty = diff;

		updateListByLength();

		// resets active pattern to all dashes
		StringBuilder word = new StringBuilder();
		for (int x = 0; x < wordLen; x++) {
			word.append(DASH);
		}

		activePattern = word.toString();

	}

	/**
	 * The number of words still possible (live) based on the guesses so far.
	 * Guesses will eliminate possible words.
	 * 
	 * @return the number of words that are still possibilities based on the
	 *         original dictionary and the guesses so far.
	 */
	public int numWordsCurrent() {
		return activeWords.size();
	}

	/**
	 * Get the number of wrong guesses the user has left in this round (game) of
	 * Hangman.
	 * 
	 * @return the number of wrong guesses the user has left in this round (game) of
	 *         Hangman.
	 */
	public int getGuessesLeft() {
		return numGuesses - numGuessesMade;
	}

	/**
	 * Return a String that contains the letters the user has guessed so far during
	 * this round. The characters in the String are in alphabetical order. The
	 * String is in the form [let1, let2, let3, ... letN]. For example [a, c, e, s,
	 * t, z]
	 * 
	 * @return a String that contains the letters the user has guessed so far during
	 *         this round.
	 */
	public String getGuessesMade() {

		Collections.sort(guessesMade);
		return guessesMade.toString();
	}

	/**
	 * Check the status of a character.
	 * 
	 * @param guess The characater to check.
	 * @return true if guess has been used or guessed this round of Hangman, false
	 *         otherwise.
	 */
	public boolean alreadyGuessed(char guess) {

		for (int x = 0; x < guessesMade.size(); x++) {
			if (guessesMade.get(x) == guess) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get the current pattern. The pattern contains '-''s for unrevealed (or
	 * guessed) characters and the actual character for "correctly guessed"
	 * characters.
	 * 
	 * @return the current pattern.
	 */
	public String getPattern() {
		return activePattern;
	}

	/**
	 * Update the game status (pattern, wrong guesses, word list), based on the give
	 * guess.
	 * 
	 * @param guess pre: !alreadyGuessed(ch), the current guessed character
	 * @return return a tree map with the resulting patterns and the number of words
	 *         in each of the new patterns. The return value is for testing and
	 *         debugging purposes.
	 */
	public TreeMap<String, Integer> makeGuess(char guess) {

		if (alreadyGuessed(guess)) {
			throw new IllegalStateException();
		}

		this.guess = guess;
		guessesMade.add(guess);

		TreeMap<String, Integer> familyValues = new TreeMap<String, Integer>();
		TreeMap<String, ArrayList<String>> familyWords = new TreeMap<String, ArrayList<String>>();

		// fills both treemaps with same keys and different corresponding integers /
		// arrayLists of words
		fillValues(familyValues, familyWords);

		// chooses appropriate key by choosing certain family based on current
		// difficult which is based on what current turn is
		String correctDifficultyKey = chooseFamilyDiff(familyValues, familyWords);

		// cuts active words down to proper arrayList of words according to key
		activeWords = familyWords.get(correctDifficultyKey);

		// checks whether an incorrect guess has been made in order to
		// increment numGuessesMade
		String oldPattern = activePattern;
		activePattern = updatePattern(correctDifficultyKey);
		if (oldPattern.equals(activePattern)) {
			numGuessesMade++;
		}

		return familyValues;
	}

	// gets keys and stores appropriate integers and arrayLists of strings into
	// treemaps
	private void fillValues(TreeMap<String, Integer> familyValues, TreeMap<String, 
			ArrayList<String>> familyWords) {

		for (int word = 0; word < activeWords.size(); word++) {
			String key = getKey(activeWords.get(word));
			Integer k = familyValues.get(key);

			if (k == null) {
				familyValues.put(key, 1);
				ArrayList<String> temp = new ArrayList<String>();
				temp.add(activeWords.get(word));
				familyWords.put(key, temp);

			} else {
				familyValues.put(key, k + 1);
				familyWords.get(key).add(activeWords.get(word));
			}

		}

	}

	// chooses appropriate difficulty level and returns correct families of integers
	// and words accordingly
	private String chooseFamilyDiff(TreeMap<String, Integer> familyValues,
			TreeMap<String, ArrayList<String>> familyWords) {

		final int MEDIUM = 4;
		final int EASY = 2;

		if (difficulty == HangmanDifficulty.HARD) {
			return hardestFam(familyValues, familyWords);
		} else if (difficulty == HangmanDifficulty.MEDIUM) {

			if (guessesMade.size() % MEDIUM == 0 && familyValues.size() > 1) {
				return secondHardestFam(familyValues, familyWords);
			} else {
				return hardestFam(familyValues, familyWords);
			}
		} else if (difficulty == HangmanDifficulty.EASY) {

			if (guessesMade.size() % EASY == 0 && familyValues.size() > 1) {
				return secondHardestFam(familyValues, familyWords);
			} else {
				return hardestFam(familyValues, familyWords);

			}

		}

		return null;

	}

	// returns most difficult key according to both treemaps' values
	private String hardestFam(TreeMap<String, Integer> familyValues, TreeMap<String, 
			ArrayList<String>> familyWords) {

		int max = 0;
		String hardestFam = "";

		Iterator<String> resultsValues = familyValues.keySet().iterator();

		while (resultsValues.hasNext()) {
			String key = resultsValues.next();
			int value = familyValues.get(key);

			if (value >= max) {
				max = value;
				hardestFam = key;
			}
		}

		// checks for ties
		hardestFam = checkTies(familyValues, familyWords, max, hardestFam);
		return hardestFam;

	}

	// returns second hardest key according to both treemaps' values
	// removes hardest key in treemaps and then finds hardest key
	// in resulting treemaps
	private String secondHardestFam(TreeMap<String, Integer> familyValues,
			TreeMap<String, ArrayList<String>> familyWords) {

		String hardestKey = hardestFam(familyValues, familyWords);
		TreeMap<String, Integer> familyValues2 = new TreeMap<String, Integer>();
		TreeMap<String, ArrayList<String>> familyWords2 = new TreeMap<String, ArrayList<String>>();

		familyValues2.putAll(familyValues);
		familyWords2.putAll(familyWords);
		familyValues2.remove(hardestKey);
		familyWords2.remove(hardestKey);

		String fam = "";
		int max = 0;
		Iterator<String> wordIterator = familyValues.keySet().iterator();
		while (wordIterator.hasNext()) {
			String key = wordIterator.next();
			Integer value = familyValues.get(key);

			if (value >= max && key != hardestKey) {
				max = value;
				fam = key;
			}
		}

		// check for ties for second hardest
		String secondHardest = checkTies(familyValues2, familyWords2, max, fam);
		return secondHardest;

	}

	// finds any keys that are tied
	private String checkTies(TreeMap<String, Integer> familyValues, TreeMap<String, 
			ArrayList<String>> familyWords,
			int max, String hardestFam) {
		String longestFam = hardestFam;

		Iterator<String> checkTies = familyValues.keySet().iterator();

		while (checkTies.hasNext()) {
			String key = checkTies.next();
			int val = familyValues.get(key);

			if (max == val) {
				if (key != longestFam) {

					// if tie again, check difficulty based on lexicographical order
					longestFam = checkDashes(key, longestFam);
				}
			}
		}

		return longestFam;
	}

	// returns harder key based on lexicographical order
	private String checkDashes(String key, String longestFam) {
		int count1 = countDashes(key);
		int count2 = countDashes(longestFam);

		if (count1 > count2) {
			return key;
		} else if (count1 < count2) {
			return longestFam;
		} else {
			int compareValue = key.compareTo(longestFam);

			if (compareValue < 0) {
				return key;
			} else if (compareValue > 0) {
				return longestFam;
			}
		}

		return null;
	}

	// helper method for determinging tie breakers
	// more dashes = harder key
	private int countDashes(String key) {

		int count = 0;
		for (int x = 0; x < key.length(); x++) {

			if (key.charAt(x) == DASH) {
				count++;
			}
		}

		return count;
	}

	// updates active pattern to change any dashes with corresponding letters
	private String updatePattern(String key) {

		StringBuilder sb = new StringBuilder();

		for (int x = 0; x < wordLength; x++) {

			if (activePattern.charAt(x) != DASH) {
				sb.append(activePattern.charAt(x));
			} else if (key.charAt(x) != DASH) {
				sb.append(key.charAt(x));
			} else {
				sb.append(DASH);
			}

		}

		return sb.toString();
	}

	// return a key that stores all non dash instances of corresponding indexes
	// between activePattern and word
	private String getKey(String word) {
		StringBuilder key = new StringBuilder();
		for (int letter = 0; letter < wordLength; letter++) {

			if (word.charAt(letter) == guess) {
				key.append(guess);
			} else if (word.charAt(letter) == activePattern.charAt(letter)) {
				key.append(activePattern.charAt(letter));
			} else {
				key.append(DASH);
			}
		}

		return key.toString();
	}

	private void updateListByLength() {

		for (int x = 0; x < dictionary.size(); x++) {
			if (dictionary.get(x).length() == wordLength) {
				activeWords.add(dictionary.get(x));
			}
		}
	}

	/**
	 * Return the secret word this HangmanManager finally ended up picking for this
	 * round. If there are multiple possible words left one is selected at random.
	 * <br>
	 * pre: numWordsCurrent() > 0
	 * 
	 * @return return the secret word the manager picked.
	 */
	public String getSecretWord() {

		if (numWordsCurrent() <= 0) {
			throw new IllegalStateException();
		}

		Random r = new Random();
		int choice = r.nextInt(numWordsCurrent());
		return activeWords.get(choice);
	}

}
