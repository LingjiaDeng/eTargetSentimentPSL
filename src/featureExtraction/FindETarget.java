package featureExtraction;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.ConstituentFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.StringUtils;

import gate.Annotation;
import gate.AnnotationSet;
import structure.ASentence;
import structure.DirectNode;
import utils.Overlap;

public class FindETarget {
	
	public ASentence s;
	private ArrayList<Tree> treesOfCon;
	
	public FindETarget(ASentence s){
		this.s = s;
		this.treesOfCon = new ArrayList<Tree>();
		run();
	}
	
	private void run(){
		ASentence s = this.s;
		//System.out.println(s.sentenceTokenizedString);
		
		AnnotationSet markups = s.annotations;
		ArrayList<DirectNode> bishans = s.bishanDirects;
		Tree root = s.parseTree;
		List<Word> words = s.tokens;
		
		for (DirectNode directNode:bishans){
			// first we find all etargets in the target span
			System.out.println(directNode.opinionSpan);
			System.out.println(directNode.targets);
			findAllHeadsInTargetSpan(directNode, words, root);
			System.out.println(directNode.eTargets);
			// next we fine/filter more eTargets in the sentence
			// next we print in PSL format
			
		}  // each direct node
	}
	
	private void findAllHeadsInTargetSpan(DirectNode directNode, List<Word> words, Tree root){
		if (directNode.targets.isEmpty())
			return;
		
		for (String target:directNode.targets){
			int targetStart = directNode.targetStarts.get(directNode.targets.indexOf(target));
			int targetEnd = targetStart + target.split(" ").length-1;
			
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
		
			
			ArrayList<Tree> treesOfCon = new ArrayList<Tree>();
			findTreeOfCon(tmp.get(0).toSentenceString((ArrayList) s.tokens) , root, treesOfCon);
			
			ArrayList<Tree> heads = new ArrayList<Tree>(); 
			findHead(treesOfCon.get(0), heads);
			directNode.eTargets = heads;
			
		} // each target
		
		return;
	}
	
	private void findHead(Tree root, ArrayList<Tree> heads){
		// if the current node is the parent node of a particular token, and it is either noun (NN) or verb (VB), or prounoun (PRP)
		// print it
		if (root.isPreTerminal() && (root.label().value().startsWith("NN") || root.label().value().startsWith("VB") || root.label().value().equals("PRP") ) ){
			heads.add(root.children()[0]);
		}
		
		
		for (Tree child: root.children()){
			findHead(child, heads);
		}
		
		return;
	}
	
	private void printTree(Tree root, Constituent con){
		if (root.isLeaf())
			return;
		else{
			System.out.println(root);
			System.out.println(root.constituents());
			System.out.println(root.constituents().contains(con));
		}
		
		for (Tree child:root.children()){
			printTree(child, con);
		}
	}
	
	private void findTreeOfCon(String constituentSpan, Tree root, ArrayList<Tree> treesOfCon){
		if ( StringUtils.join(root.getLeaves()).equals(constituentSpan) ){
			treesOfCon.add(root);
			return;
		}
		
		for (Tree child:root.children()){
			findTreeOfCon(constituentSpan, child, treesOfCon);
		}
		
		return;
	}
}
