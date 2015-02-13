package structure;

import java.util.ArrayList;
import java.util.HashSet;

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
	  
	  this.features = new ArrayList<Feature>();
	}
	
	public void countFeatures(HashSet<String> unigramCon, HashSet<String> bigramCon, HashSet<String> unigramDep, HashSet<String> bigramDep){
		for (int t=0;t<this.eTargets.size();t++){
			Tree eTarget = eTargets.get(t);
			Feature feature = new Feature();
			this.features.add(feature);
			
			
			// itself
			if (this.opinionTree.getLeaves().contains(eTarget)){
				feature.inOpinionSpan = 1;
			}
			
			for (String target:this.targets){
				if (target.contains(eTarget.nodeString()))
					feature.inTargetSpan = 1;
			}
			
			// constituency
			feature.unigramConCount = new int[unigramCon.size()];
			feature.bigramConCount = new int[bigramCon.size()];
			
			
			for (int i=0;i<unigramCon.size();i++){
				if (feature.unigramCon.contains(unigramCon.toArray()[i]))
					feature.unigramConCount[i] = 1;
				else
					feature.unigramConCount[i] = 0;
			}
			
			for (int i=0;i<bigramCon.size();i++){
				if (feature.bigramCon.contains(bigramCon.toArray()[i]))
					feature.bigramConCount[i] = 1;
				else
					feature.bigramConCount[i] = 0;
			}
			
			//feature.unigramCon = unigramCount;
			//feature.bigramCount = bigramCount;
			
			// dependency parser
			feature.unigramDepCount = new int[unigramDep.size()];
			feature.bigramDepCount = new int[bigramDep.size()];
			
			for (int i=0;i<unigramDep.size();i++){
				if (feature.unigramDep.contains(unigramDep.toArray()[i]))
					feature.unigramDepCount[i] = 1;
				else
					feature.unigramDepCount[i] = 0;
			}
			
			for (int i=0;i<bigramDep.size();i++){
				if (feature.bigramDep.contains(bigramDep.toArray()[i]))
					feature.bigramDepCount[i] = 1;
				else
					feature.bigramDepCount[i] = 0;
			}
			
			//feature.unigramDep = unigramCount;
			//feature.bigramDep = bigramCount;
		}
	}

}
