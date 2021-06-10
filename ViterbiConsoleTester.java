package cs10;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Allows testing of the Viterbi class through the console. 
 *
 * @author Rodrigo A. Cavero Blades, Dartmouth CS 10, Winter 2018
 */

public class ViterbiConsoleTester {

	public static void main(String[] args) throws FileNotFoundException {
		Viterbi v = new Viterbi();
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
			String[] result = v.decodeLine(words);
			for (int i = 0; i < result.length; i ++){
				System.out.print(result[i] + " ");
			}
			System.out.println("\nEnter another sentence to Decode:");
		}
		input.close();
	}

}
