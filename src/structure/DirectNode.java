package structure;

import java.util.ArrayList;

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
	
	public HashSet<String> unigramCon;
	public HashSet<String> bigramCon;
	public HashSet<String> unigramDep;
	public HashSet<String> bigramDep;
	
	
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
	  
	  this.unigramCon = new HashSet<String>();
	  this.bigramCon = new HashSet<String>();
	  this.unigramDep = new HashSet<String>();
	  this.bigramDep = new HashSet<String>();
	}
	
	public void countItself(){
		for (Tree eTarget:this.eTargets){
			Feature feature = new Feature();
			if (this.opinionTree.getLeaves().contains(eTarget)){
				feature.inOpinionSpan = 1;
			}
			
			for (String target:this.targets){
				if (target.contains(eTarget.nodeString()))
					feature.inTargetSpan = 1;
			}
		}
	}
	
	public void countCon(ArrayList<String> unigramCon, ArrayList<String> bigramCon){
		int[] unigramCount = new int[unigramCon.size()];
		int[] bigramCount = new int[bigramCon.size()];
		
		for (int i=0;i<unigramCon.size();i++){
			if (this.unigramCon.contains(unigramCon.get(i)))
				unigramCount[i] = 1;
			else
				unigramCount[i] = 0;
		}
		
		for (int i=0;i<bigramCon.size();i++){
			if (this.bigramCon.contains(bigramCon.get(i)))
				bigramCount[i] = 1;
			else
				bigramCount[i] = 0;
		}
		
		feature.unigramCon = unigramCount;
		feature.bigramCount = bigramCount;
	}
	
	public void countDep(ArrayList<String> unigramDep, ArrayList<String> bigramDep){
		int[] unigramCount = new int[unigramDep.size()];
		int[] bigramCount = new int[bigramDep.size()];
		
		for (int i=0;i<unigramDep.size();i++){
			if (this.unigramDep.contains(unigramDep.get(i)))
				unigramCount[i] = 1;
			else
				unigramCount[i] = 0;
		}
		
		for (int i=0;i<bigramDep.size();i++){
			if (this.bigramDep.contains(bigramDep.get(i)))
				bigramCount[i] = 1;
			else
				bigramCount[i] = 0;
		}
		
		feature.unigramDep = unigramCount;
		feature.bigramDep = bigramDep;
	}

}
