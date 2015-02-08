package structure;

import edu.stanford.nlp.trees.Tree;
import gate.util.GateException;

import java.io.IOException;
import java.util.ArrayList;

import readBishan.ReadBishanTogether;
import readGATE.ReadGATE;

public class Doc {
	public ArrayList<ASentence> sentences;
	public String docId;
	
	private int gsNum;
	private int autoNum;
	private int corretNum;
	
	public Doc(String docId) throws IOException, GateException{
		this.docId = docId;
		this.sentences = new ArrayList<ASentence>();
		this.gsNum = 0;
		this.autoNum = 0;
		this.corretNum = 0;
		
	}
	
	public void read() throws IOException, GateException{
		// read bishan's result (with holders and polarities)
		ReadBishanTogether bishan = new ReadBishanTogether(this.docId);
		// read gate's all (gold standard) annotations
		ReadGATE gate = new ReadGATE(docId);
		
		// create the sentence list where
		// each sentence is a structure
		this.sentences = gate.addBishanResults(bishan.sentenceHash,this.sentences);
		System.out.println("after merging: "+sentences.size());
	}
	
	public void generateETarget() throws IOException, GateException{
		// go through each sentence
		for (ASentence aSentence:this.sentences){
			System.out.println("sentence:");
			System.out.println(sentences.get(0).sentenceString);
			
			aSentence.alignGoldStandard();
			aSentence.findETarget();
			aSentence.expandETargetUsingGFBF();
		}  // each aSentence
	}
	
	public void statistics(){
		for (ASentence aSentence:this.sentences){
			for (DirectNode direct:aSentence.bishanDirects){
				this.gsNum += direct.eTargetsGS.size();
				this.autoNum += direct.eTargets.size();
				for (Tree eTarget:direct.eTargetsGS){
					if (direct.eTargets.contains(eTarget))
						this.corretNum += 1;
				}
			}
		}
		
		System.out.println("----- statistics -----");
		System.out.println(this.corretNum*1.0/this.gsNum);
		System.out.println(this.corretNum*1.0/this.autoNum);
	}
	
	
}
