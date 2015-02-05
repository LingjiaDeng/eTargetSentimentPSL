package run;

import featureExtraction.FindETarget;
import gate.util.GateException;

import java.io.IOException;
import java.util.ArrayList;

import readBishan.*;
import readGATE.ReadETarget;

public class Run {
	public static void main(String[] args) throws IOException, GateException{
		String docId = "temp_fbis/21.10.31-12974";
		
		// read bishan's result (with holders and polarities)
		ReadBishanTogether bishan = new ReadBishanTogether(docId);
		// read gate's gold standard annotations
		ReadETarget gate = new ReadETarget(docId);
		
		// create the sentence list where
		// each sentence is a stucture
		// records the gold-standard annotations, and the bishan's results
		// and the tokenized result, and the parse tree
		ArrayList<ASentence> sentences = new ArrayList<ASentence>();
		sentences = gate.addBishanResults(bishan.sentenceHash,sentences);
		System.out.println("after merging: "+sentences.size());
		
		FindETarget find = new FindETarget(sentences.get(0));
		
		
		
	}
}
