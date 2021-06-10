package cs10;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ViterbiFileTester {

	public static void main(String[] args) throws FileNotFoundException {
		Viterbi v = new  Viterbi();
		List<String> trainingSentences = new ArrayList<String>();
		List<String> trainingTags = new ArrayList<String>();
		Scanner s = new Scanner(new File("files/simple-train-sentences.txt"));
		Scanner t = new Scanner(new File("files/simple-train-tags.txt"));
		while (s.hasNextLine() && t.hasNextLine()) {
			trainingSentences.add(s.nextLine());
			trainingTags.add(t.nextLine());
		}
		s.close();
		t.close();
		v.generateTags(trainingTags);
		v.train(trainingSentences, trainingTags);
		
		List<String> testingSentences = new ArrayList<String>();
		List<String> testingTags = new ArrayList<String>();
		Scanner s2 = new Scanner(new File("files/simple-test-sentences.txt"));
		Scanner t2 = new Scanner(new File("files/simple-test-tags.txt"));
		while (s.hasNextLine() && t.hasNextLine()) {
			testingSentences.add(s.nextLine());
			testingTags.add(t.nextLine());
		}
		s.close();
		t.close();
		
		
		List<String> sentencesTst = new ArrayList<String>();
		Scanner sTst = new Scanner(new File("files/simple-test-sentences.txt"));
		while(sTst.hasNextLine()){
			sentencesTst.add(sTst.nextLine());
		}
		sTst.close();
		v.deCode(sentencesTst);
	}

}
