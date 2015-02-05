package featureExtraction;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.ConstituentFactory;
import edu.stanford.nlp.trees.Tree;

import gate.Annotation;
import gate.AnnotationSet;
import readBishan.DirectNode;
import run.ASentence;
import utils.Overlap;

public class FindETarget {
	
	public ASentence s;
	
	public FindETarget(ASentence s){
		this.s = s;
		run();
	}
	
	private void run(){
		ASentence s = this.s;
		System.out.println(s.sentenceTokenizedString);
		
		AnnotationSet markups = s.annotations;
		ArrayList<DirectNode> bishans = s.bishanDirects;
		Tree root = s.parseTree;
		List<Word> words = s.tokens;
		String sentenceString = s.sentenceString;
		Annotation inside = markups.get("inside").get(0);
		
		for (DirectNode d:bishans){
			String opinionSpan = d.opinionSpan;
			ArrayList<String> targets = d.targets;
			System.out.println(opinionSpan);
			System.out.println(targets);
			
			if (targets.size() == 0)
				continue;
			
			for (String target:targets){
				int targetStart = d.targetStarts.get(targets.indexOf(target));
				int targetEnd = targetStart + target.split(" ").length-1;
				List<Tree> leaves = root.getLeaves();
				System.out.print(targetStart);
				System.out.println(targetEnd);
				
				/*
				for (Tree leave:leaves){
					System.out.print(leave.nodeNumber(root));
					System.out.println(" "+leave.nodeString());
					System.out.println(leave.pathNodeToNode(leave, root));
				}
				*/
				ArrayList<Constituent> tmp = new ArrayList<Constituent>();
				for (Constituent con:root.constituents()){
					if ( Overlap.intervalContains(con.start(), con.end(), targetStart, targetEnd) ){
						if (tmp.size()==0)
							tmp.add(con);
						else{
							if (tmp.get(0).contains(con) )
								tmp.add(0, con);
						}
					}
				}  // each constituent
				System.out.println(tmp.get(0).start()+":"+tmp.get(0).end());
				
				ArrayList<Tree> treesOfCon = new ArrayList<Tree>();
				findTreeOfCon(tmp.get(0) , root, treesOfCon);
				System.out.println(treesOfCon.get(0).toString());
				
				
				
				
			}
			
			
			
		}  // each direct node
		
	}
	
	private void findTreeOfCon(Constituent con, Tree root, ArrayList<Tree> treesOfCon){
		System.out.println(root.children().length);
		System.out.println(root.constituents());
		if (!root.constituents().contains(con))
			return;
		
		boolean childHasCon = false;
		for (Tree child:root.children()){
			System.out.println("...");
			System.out.println(child.constituents());
			if (child.constituents().contains(con)){
				childHasCon = true;
				findTreeOfCon(con, child, treesOfCon);
			}	
		}
		
		if (!childHasCon)
			treesOfCon.add(root);
		
		return;
		
	}
}
