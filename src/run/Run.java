package run;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gate.util.GateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import readBishan.*;
import readGATE.ReadGATE;
import structure.ASentence;

public class Run {
	public static void main(String[] args) throws IOException, GateException{
		String docId = "20020206/20.31.05-16359";
		
		// read bishan's result (with holders and polarities)
		ReadBishanTogether bishan = new ReadBishanTogether(docId);
		// read gate's all (gold standard) annotations
		ReadGATE gate = new ReadGATE(docId);
		
		// create the sentence list where
		// each sentence is a structure
		ArrayList<ASentence> sentences = new ArrayList<ASentence>();
		sentences = gate.addBishanResults(bishan.sentenceHash,sentences);
		System.out.println("after merging: "+sentences.size());
		
		System.out.println("sentence:");
		System.out.println(sentences.get(0).sentenceString);
		ASentence aSentence = sentences.get(0);
		aSentence.alignGoldStandard();
		aSentence.findETarget();
		aSentence.expandETargetUsingGFBF();
		
		
	}
}
