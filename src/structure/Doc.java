package structure;

import gate.util.GateException;

import java.io.IOException;
import java.util.ArrayList;

import readBishan.ReadBishanTogether;
import readGATE.ReadGATE;

public class Doc {
	public ArrayList<ASentence> sentences;
	public String docId;
	
	
	public Doc(String docId) throws IOException, GateException{
		this.docId = docId;
		this.sentences = new ArrayList<ASentence>();
		read();
		generateETarget();
		
	}
	
	private void read() throws IOException, GateException{
		// read bishan's result (with holders and polarities)
		ReadBishanTogether bishan = new ReadBishanTogether(this.docId);
		// read gate's all (gold standard) annotations
		ReadGATE gate = new ReadGATE(docId);
		
		// create the sentence list where
		// each sentence is a structure
		this.sentences = gate.addBishanResults(bishan.sentenceHash,this.sentences);
		System.out.println("after merging: "+sentences.size());
	}
	
	private void generateETarget() throws IOException{
		// go through each sentence
		for (ASentence aSentence:this.sentences){
			System.out.println("sentence:");
			System.out.println(sentences.get(0).sentenceString);
			
			aSentence.alignGoldStandard();
			aSentence.findETarget();
			aSentence.expandETargetUsingGFBF();
		}  // each aSentence
	}
}
