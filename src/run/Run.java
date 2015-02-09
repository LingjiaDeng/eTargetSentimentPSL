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
import structure.Doc;
import utils.GFBF;

public class Run {
	public static void main(String[] args) throws IOException, GateException{
		String docId = "20020206/20.31.05-16359";
		GFBF gfbfLexicon = new GFBF();
		
		Doc doc = new Doc(docId);
		doc.read();
		doc.parse();
		
		
		doc.generateETarget();
		doc.statistics();
		
		
		
	}
}
