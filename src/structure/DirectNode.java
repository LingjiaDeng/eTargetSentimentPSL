package structure;

import java.util.ArrayList;
import java.util.HashSet;

import utils.GFBF;
import utils.Statistics;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.trees.Tree;

public class DirectNode {
	
	public String agent;
	public int agentStart;
	public String sentence;
	public int sentenceIndex;
	public String opinionSpan;
	public int opinionStart;
	public Tree opinionTree;
	public Tree stanfordOpinionTree;
	public ArrayList<String> targets;
	public ArrayList<Integer> targetStarts;
	public ArrayList<Tree> targetTrees;
	public String polarity;
	public boolean overlapped;
	public ArrayList<Tree> eTargets;
	public ArrayList<Tree> eTargetsGS;
	public Tree root;
	
	public ArrayList<Triple> gfbfTriples;
	public ArrayList<Feature> features;
	
	
	public DirectNode(){
	  this.agent = "";
	  this.agentStart = -1;
	  this.sentence = "";
	  this.sentenceIndex = -1;
	  this.opinionSpan = "";
	  this.opinionStart = -1;
	  this.targets = new ArrayList<String>();
	  this.targetStarts = new ArrayList<Integer>();
	  this.targetTrees = new ArrayList<Tree>();
	  this.overlapped = false;
	  this.polarity = "";
	  this.eTargets = new ArrayList<Tree>();
	  this.eTargetsGS = new ArrayList<Tree>();
	  
	  this.gfbfTriples = new ArrayList<Triple>();
	  this.features = new ArrayList<Feature>();
	}
	
	public void countFeatures(){
		for (int t=0;t<this.eTargets.size();t++){
			Tree eTarget = eTargets.get(t);
			Feature feature = this.features.get(t);
			
			// record whether it is the correct etarget
			if (this.eTargetsGS.contains(eTarget))
				feature.isCorrect = true;
			
			// in span
			if (this.opinionTree.getLeaves().contains(eTarget)){
				feature.inOpinionSpan = 1;
			}
			
			for (String target:this.targets){
				if (target.contains(eTarget.nodeString()))
					feature.inTargetSpan = 1;
			}
			
			// constituency
			feature.unigramConCount = new int[Statistics.unigramCon.size()];
			feature.bigramConCount = new int[Statistics.bigramCon.size()];
			
			
			for (int i=0;i<Statistics.unigramCon.size();i++){
				if (feature.unigramCon.contains(Statistics.unigramCon.toArray()[i]))
					feature.unigramConCount[i] = 1;
				else
					feature.unigramConCount[i] = 0;
			}
			
			for (int i=0;i<Statistics.bigramCon.size();i++){
				if (feature.bigramCon.contains(Statistics.bigramCon.toArray()[i]))
					feature.bigramConCount[i] = 1;
				else
					feature.bigramConCount[i] = 0;
			}
			
			// GFBF: constituency
			feature.unigramConCountGFBF = new int[Statistics.unigramCon.size()];
			feature.bigramConCountGFBF = new int[Statistics.bigramCon.size()];
			
			
			for (int i=0;i<Statistics.unigramCon.size();i++){
				if (feature.unigramConGFBF.contains(Statistics.unigramCon.toArray()[i]))
					feature.unigramConCountGFBF[i] = 1;
				else
					feature.unigramConCountGFBF[i] = 0;
			}
			
			for (int i=0;i<Statistics.bigramCon.size();i++){
				if (feature.bigramConGFBF.contains(Statistics.bigramCon.toArray()[i]))
					feature.bigramConCountGFBF[i] = 1;
				else
					feature.bigramConCountGFBF[i] = 0;
			}
			
			// dependency parser
			feature.unigramDepCount = new int[Statistics.unigramDep.size()];
			feature.bigramDepCount = new int[Statistics.bigramDep.size()];
			
			for (int i=0;i<Statistics.unigramDep.size();i++){
				if (feature.unigramDep.contains(Statistics.unigramDep.toArray()[i]))
					feature.unigramDepCount[i] = 1;
				else
					feature.unigramDepCount[i] = 0;
			}
			
			for (int i=0;i<Statistics.bigramDep.size();i++){
				if (feature.bigramDep.contains(Statistics.bigramDep.toArray()[i]))
					feature.bigramDepCount[i] = 1;
				else
					feature.bigramDepCount[i] = 0;
			}
			
			// GFBF: dependency parser
			feature.unigramDepCountGFBF = new int[Statistics.unigramDep.size()];
			feature.bigramDepCountGFBF = new int[Statistics.bigramDep.size()];
			
			for (int i=0;i<Statistics.unigramDep.size();i++){
				if (feature.unigramDepGFBF.contains(Statistics.unigramDep.toArray()[i]))
					feature.unigramDepCountGFBF[i] = 1;
				else
					feature.unigramDepCountGFBF[i] = 0;
			}
			
			for (int i=0;i<Statistics.bigramDep.size();i++){
				if (feature.bigramDepGFBF.contains(Statistics.bigramDep.toArray()[i]))
					feature.bigramDepCountGFBF[i] = 1;
				else
					feature.bigramDepCountGFBF[i] = 0;
			}
			
		}
	}

}
