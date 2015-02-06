package run;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gate.util.GateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import readBishan.*;
import readGATE.ReadETarget;
import structure.ASentence;

public class Run {
	public static void main(String[] args) throws IOException, GateException{
		String docId = "20020206/20.31.05-16359";
		
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
		
		System.out.println("sentence:");
		System.out.println(sentences.get(0).sentenceString);
		ASentence aSentence = sentences.get(0);
		aSentence.findETarget();
		aSentence.alignGoldStandard();
		aSentence.expandETargetUsingGFBF();
		
		
	}
}
