package cs10;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Driver for the Viterbi class.
 * Handles the file reading necessary for training; also handles the calls to the testing methods. 
 *
 * @author Rodrigo A. Cavero Blades, Dartmouth CS 10, Winter 2018
 */

public class ViterbiDriver {

	public static void main(String[] args) throws FileNotFoundException {
		Viterbi v = new  Viterbi();
		List<String> sentences = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		Scanner s = new Scanner(new File("files/brown-train-sentences.txt"));
		Scanner t = new Scanner(new File("files/brown-train-tags.txt"));
		while (s.hasNextLine() && t.hasNextLine()) {
			sentences.add(s.nextLine());
			tags.add(t.nextLine());
		}
		s.close();
		t.close();
		v.generateTags(tags);
		v.train(sentences, tags);
		
		// Uncomment to run the console test
		// v.consoleTest();            
		v.fileTest(new File("files/brown-test-sentences.txt"), new File("files/brown-test-tags.txt"));
	}
}
