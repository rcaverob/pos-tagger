package cs10;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Viterbi Decoder Class. Functions as a part of speech tagger. 
 *
 * @author Rodrigo A. Cavero Blades, Dartmouth CS 10, Winter 2018
 */

public class Viterbi {
	private Map<String, Map<String, Double>> transitions;  		// word type - > [next word type -> probability]
	private Map<String, Map<String, Double>> observations;  	// word type -> [word -> probability]
	private Double unobserved = -100.0;                     	// penalty for unobserved words
	
	private Map<String, Double> obsTotal;                       //used to normalize observation probabilities
	private Map<String, Double> transTotal;						//used to normalize transition probabilities
	private Set<String> tagSet;									//stores all the tags that can be used to label words 
	private List<Map<String, String>> backtracker;              //used to backtrack the best path in Viterbi decoding
	
	public Viterbi() {
		observations = new HashMap<String, Map<String, Double>>();
		transitions = new HashMap<String, Map<String, Double>>();
		backtracker = new ArrayList<Map<String, String>>();
		
		obsTotal = new HashMap<String, Double>();
		transTotal = new HashMap<String, Double>();
		tagSet = new HashSet<String>();
	}
	
	/**
	 * Given a List of Sentences and a List of Lines of Tags, Trains the Viterbi Decoder on them.
	 * Stores the necessary information on the observations and transitions maps.
	 */
	public void train(List<String> sentences, List<String> tagList) {
		while(!sentences.isEmpty()) {
			// First need to format the given data so it can be used easily.
			String sentence = sentences.remove(0).toLowerCase();
			String tagLine = tagList.remove(0);
			String[] words = sentence.split(" ");
			String[] tags = tagLine.split(" ");
			
			String prevTag = null;		// keeps track of the previous tag for transition probability calculation
			
			// Fill the observations and transitions maps with the counts found on the training files.
			// Also keeps track of the total counts to normalize later
			for(int i = 0; i < words.length; i++) {
				String word = words[i];
				String tag = tags[i];
				Map<String, Double> wordCount = observations.get(tag);
				obsTotal.put(tag, obsTotal.get(tag) + 1.0);
				String tagKey = prevTag == null? "#" : prevTag;
				transTotal.put(tagKey, transTotal.get(tagKey) + 1.0);
				if (!wordCount.containsKey(word)) {
					observations.get(tag).put(word, 1.0);	
				}else {
					observations.get(tag).put(word, wordCount.get(word) + 1.0);
				}	

				Map<String, Double> curProb = prevTag == null? transitions.get("#") : transitions.get(prevTag);
				
				if (!curProb.containsKey(tag)) {
					curProb.put(tag, 1.0);
				} else {
					curProb.put(tag, curProb.get(tag) + 1.0);
				}
				prevTag = tag;
			}
		}
		
		
		// Translate the probabilities in the observations map from a count 
		// to an actual probability fraction, then use log on that to avoid
		// computational problems with fractions.
		for(Map.Entry<String, Map<String, Double>> entry : observations.entrySet()) {
			String tag = entry.getKey();
			Map<String, Double> m = entry.getValue();
			
			for (Map.Entry<String, Double> inner : m.entrySet()) {
				String word = inner.getKey();
				Double count = inner.getValue();
				Double total = obsTotal.get(tag);
				
				m.put(word, Math.log(count/total));
			}
		}
		
		// Translate the probabilities in the transitions map from a count 
		// to an actual probability fraction, then use log on that to avoid
		// computational problems with fractions.
		for(Map.Entry<String, Map<String, Double>> entry : transitions.entrySet()) {
			String tag = entry.getKey();
			Map<String, Double> m = entry.getValue();
			
			for (Map.Entry<String, Double> inner : m.entrySet()) {
				String nextTag = inner.getKey();
				Double count = inner.getValue();
				Double total = transTotal.get(tag);
				
				m.put(nextTag, Math.log(count/total));
			}
		}
	}

	/**
	 * Given a sentence (array of strings), returns an array of corresponding tags.
	 * Uses the Viterbi algorithm for tagging
	 */
	public String[] decodeLine(String[] line) {
		String[] result = new String[line.length];
		Set<String>curStates = new HashSet<String>();
		curStates.add("#");
		Map<String, Double>curScores = new HashMap<String, Double>();
		curScores.put("#", 0.0);
		for (int i = 0; i < line.length; i++) {
			Set<String>nextStates = new HashSet<String>(); 
			Map<String, Double> nextScores = new HashMap<String, Double>();
			for (String tag : curStates) {
				for (String nextState : transitions.get(tag).keySet()) {
					nextStates.add(nextState);
					Double nextScore = curScores.get(tag) + transitions.get(tag).get(nextState)
						+(observations.get(nextState).get(line[i]) == null? unobserved : observations.get(nextState).get(line[i]));
					if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) {
						nextScores.put(nextState, nextScore);
						// Set predecesor of nextstate at i be curState
						if (backtracker.size() < i + 1) {
							Map<String, String> pred = new HashMap<String, String>();
							pred.put(nextState, tag);
							backtracker.add(pred);
						} else {
							backtracker.get(i).put(nextState, tag);
						}
					}
				}
			}
			curStates = nextStates;
			curScores = nextScores;
		}
		
		// Find the best-scored ending State
		Map.Entry<String, Double> bestEnd = null;
		for (Map.Entry<String, Double> entry : curScores.entrySet()){
		    if (bestEnd == null || entry.getValue() > bestEnd.getValue()){
		        bestEnd = entry;
		    }
		}
		
		// BackTrack from the best-scored ending State
		String pathNode = bestEnd.getKey();
		for (int i = line.length -1; i > -1; i --) {
			result[i] = pathNode;
			pathNode = backtracker.get(i).get(pathNode);
		}
		return result;
	}


	/**
	 * Given a list of sentences, returns an List of corresponding tag arrays.
	 */
	public List<String[]> deCode(List<String> sentenceLst) {
		List<String[]> result = new ArrayList<String[]>();
		while (!sentenceLst.isEmpty()) {
			String sentence = sentenceLst.remove(0).toLowerCase();
			String[] words = sentence.split(" ");
			String[] sentenceTags = decodeLine(words);
			result.add(sentenceTags);	
		}
		return result;
	}

	/**
	 * Given a List of lines containing tags separated by spaces, generates and stores a
	 * set of the tags, and adds them to the stored transition and observation maps 
	 * so they are used with Viterbi decoding.
	 */
	public void generateTags(List<String> tags) {
		tagSet.add("#");
		for (String tag:tags) {
			String[] tagLine = tag.split(" ");
			for (int i = 0; i < tagLine.length; i++) {
				tagSet.add(tagLine[i]);
			}
		}
		for (String tag : tagSet) {
			transitions.put(tag, new HashMap<String, Double>());
			observations.put(tag, new HashMap<String, Double>());
			transTotal.put(tag, 0.0);
			obsTotal.put(tag, 0.0);
		}
	}
	
	/**
	 * Runs a console test. Receives input sentences and outputs a sequence of corresponding tags.
	 */
	public void consoleTest() {
		Scanner input = new Scanner(System.in);
		
		System.out.println("Enter a sentence to Decode: (with spaces before punctuation and in lowercase)");
		System.out.println("Or enter q to quit");
		boolean active = true;
		while (active) {
			String sentence = input.nextLine();
			if (sentence.equals("q")) {
				active = false;
				System.out.println("Tester has been quit");
				break;
			}
			String[] words = sentence.split(" ");
			String[] result = decodeLine(words);
			for (int i = 0; i < result.length; i ++){
				System.out.print(result[i] + " ");
			}
			System.out.println("\nEnter another sentence to Decode:");
		}
		input.close();
	}
	
	/**
	 * Runs a file test utilizing a given testSentences file containing the sentences to be tested on 
	 * and a testTags file containing the correct tags for those sentences. 
	 * Prints the number of tags it gets right and wrong.
	 */
	public void fileTest(File testSentences, File testTags) throws FileNotFoundException {
		int correctTags = 0;
		int failedTags = 0;
		List<String> testingSentences = new ArrayList<String>();
		List<String> testingTags = new ArrayList<String>();
		Scanner s = new Scanner(testSentences);
		Scanner t = new Scanner(testTags);
		while (s.hasNextLine() && t.hasNextLine()) {
			testingSentences.add(s.nextLine());
			testingTags.add(t.nextLine());
		}
		s.close();
		t.close();
		ArrayList<String[]> givenTags = (ArrayList<String[]>) deCode(testingSentences);
		List<String[]> testTagArrays = new ArrayList<String[]>();
		
		for (String tag : testingTags) {
			testTagArrays.add(tag.split(" "));
		}
		
		for (int i = 0; i < givenTags.size(); i++) {
			for (int j = 0; j < givenTags.get(i).length; j ++) {
				if (givenTags.get(i)[j].equals(testTagArrays.get(i)[j])) {
					correctTags++;
				} else {
					failedTags++;
				}
			}
		}

		System.out.println("Correct tags: " + correctTags);
		System.out.println("Incorrect tags: " + failedTags);	
	}

	
}
