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
	}
	
	public void analyzeFeatures(){
		for (Tree eTarget:this.eTargets){
			Feature feature = new Feature();
			if (this.opinionTree.getLeaves().contains(eTarget)){
				feature.inOpinionSpan = 1;
			}
			
			System.out.println(this.opinionTree.nodeString());
			System.out.println(eTarget.nodeString());
			System.out.println(this.root.pathNodeToNode(this.opinionTree, eTarget).size());
			for (Tree tmp:this.root.pathNodeToNode(this.opinionTree, eTarget)){
				System.out.println(tmp.nodeString());
			}
				
		}
		
	}

}
