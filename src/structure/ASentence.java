package structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import gate.Annotation;
import gate.AnnotationSet;
import gate.DocumentContent;
import gate.FeatureMap;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

import utils.GFBF;
import utils.Overlap;
import utils.Rule;

import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.ConstituentFactory;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

public class ASentence {
	public String sentenceString;
	public String sentenceTokenizedString;
	public int sentenceIndex;
	public AnnotationSet annotations;
	public DocumentContent content; 
	public ArrayList<DirectNode> bishanDirects;
	public Collection<TypedDependency> tdl;
	public CoreMap sentenceSyntax;
	
	public ASentence(){
		this.sentenceString = "";
		this.sentenceTokenizedString = "";
		this.sentenceIndex = -1;
		this.bishanDirects = new ArrayList<DirectNode>();
	}
	
	public void expandETargetUsingGFBF() throws IOException{
		System.out.println("----- 2nd: adding gfbf rules -----");
		for (DirectNode bishan:this.bishanDirects){
			System.out.println(bishan.opinionSpan);
			bishan.eTargets = expandETargetUsingGFBF(bishan.eTargets);
			
			System.out.println(bishan.eTargets);
			
		}  // each direct node
		
		return;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Tree> expandETargetUsingGFBF(ArrayList<Tree> eTargets) throws IOException{
		if (eTargets.isEmpty() || eTargets.size()==0)
			return eTargets;
		
		ArrayList<Tree> leaves = (ArrayList<Tree>) this.sentenceSyntax.get(TreeAnnotation.class).getLeaves();
		Queue<Tree> queue = new LinkedList<Tree>();
		ArrayList<Tree> newETargets = new ArrayList<Tree>();
		ArrayList<Tree> visited = new ArrayList<Tree>();
		
		// put all the etargets from step 1 into the queue
		for (Tree eTarget:eTargets){
			queue.offer(eTarget);
		}
		
		while (!queue.isEmpty()){
			Tree newETarget = queue.poll();
			if (visited.contains(newETarget))
				continue;
			
			visited.add(newETarget);
			newETargets.add(newETarget);
			// find the qualified neighbors
			int indexOfLeaf = leaves.indexOf(newETarget)+1;
			
			for (TypedDependency td:this.tdl){
				if (td.gov().index()==0)
					continue;
				
				String govWord = this.sentenceSyntax.get(TokensAnnotation.class).get(td.gov().index()-1).lemma();
				if (Rule.gfbfRulesJudgeGov(td, indexOfLeaf, govWord))
					queue.offer(leaves.get(td.gov().index()-1));
				if (Rule.gfbfRulesJudgeDep(td, indexOfLeaf, govWord))
					queue.offer(leaves.get(td.dep().index()-1));
			}
		}   // while queue isn't empty
		
		return newETargets;
	}
	
	
	public void alignGoldStandard() throws GateException{
		System.out.println("----- gold standard eTargets -----");
		for (DirectNode bishan:this.bishanDirects){
			System.out.println(bishan.opinionSpan);
			ArrayList<Annotation> subjAnnos = findMatchingSubjMarkup(bishan, this.annotations);
			ArrayList<Annotation> eTargetAnnos = new ArrayList<Annotation>();
			for (Annotation subjAnno:subjAnnos){
				ArrayList<Annotation> tmp = findMatchingETargetMarkup(bishan, subjAnno, this.annotations);
				for (Annotation anno:tmp){
					if (!eTargetAnnos.contains(anno))
						eTargetAnnos.add(anno);
				}
			}
			bishan.eTargetsGS.addAll(findMatchingHeads(eTargetAnnos, this.sentenceSyntax.get(TreeAnnotation.class)));
			System.out.println(bishan.eTargetsGS);
		}
		
		return;
	}
	
	private ArrayList<Tree> findMatchingHeads(ArrayList<Annotation> eTargetAnnos, Tree root) throws GateException{
		ArrayList<Tree> heads = new ArrayList<Tree>();
		
		for (Annotation eTarget:eTargetAnnos){
			String eTargetSpan = this.content.getContent(eTarget.getStartNode().getOffset(), eTarget.getEndNode().getOffset()).toString();
			for (Tree leaf:root.getLeaves()){
				if (eTargetSpan.equals(leaf.nodeString())  &&  !heads.contains(leaf))
					heads.add(leaf);
			}
		}
		
		return heads;
	}
	
	private ArrayList<Annotation> findMatchingETargetMarkup(DirectNode direct, Annotation subjAnno, AnnotationSet markups){
		ArrayList<Annotation> eTargets = new ArrayList<Annotation>();
		ArrayList<String> eTargetIds = new ArrayList<String>(); 
		
		// find the ids of eTargets
		FeatureMap params = subjAnno.getFeatures();
		int targetNum = 1;
		while (params.containsKey(targetNum+"-eTarget-link")){
			String eTargetLinkSpan = params.get(targetNum+"-eTarget-link").toString();
			if (eTargetLinkSpan.startsWith("n")){
				targetNum++;
				continue;
			}
			
			if (eTargetLinkSpan.contains("[")){
				eTargetIds.add(eTargetLinkSpan.split("\\[")[1]);
			}
			else{
				eTargetIds.addAll(new ArrayList<String>(Arrays.asList(eTargetLinkSpan.replaceAll(" ","").split(","))));
			}
		
			targetNum++;
		}  // while
		if (params.containsKey("new-eTarget-link") && !params.get("new-eTarget-link").toString().startsWith("n")){
			eTargetIds.addAll(new ArrayList<String>(Arrays.asList(params.get("new-eTarget-link").toString().replaceAll(" ","").split(","))));
		}  // if new eTarget link
		
		// use the ids of eTargets to find the annotations
		Set<String> eTargetNames = new HashSet<String>();
		eTargetNames.add("eTarget");
		eTargetNames.add("eTarget-new");
		AnnotationSet eTargetAnnos  = markups.get(eTargetNames);
		for (String eTargetId:eTargetIds){
			for (Annotation eTargetAnno:eTargetAnnos){
				FeatureMap eTargetParams = eTargetAnno.getFeatures();
				if (eTargetParams.containsKey("id") && eTargetParams.get("id").toString().equals(eTargetId)){
					eTargets.add(eTargetAnno);
				}
			}
		}
		
		
		return eTargets;
	}
	
	private ArrayList<Annotation> findMatchingSubjMarkup(DirectNode direct, AnnotationSet markups) throws GateException{
		ArrayList<Annotation> subjs  = new ArrayList<Annotation>();
		
		String opinionSpan = direct.opinionSpan;
		int opinionStart = this.sentenceTokenizedString.indexOf(opinionSpan);
		int opinionEnd = opinionStart + opinionSpan.length();
		
		Set<String> subjNames = new HashSet<String>();
		subjNames.add("sentiment");
		subjNames.add("ESE-polar");
		AnnotationSet subjAnnos = markups.get(subjNames);
		
		// works as the same function:
		// insides.get(0)
		Long sentenceStart = (long) -1;
		for (Annotation inside:markups.get("inside")){
			sentenceStart =  inside.getStartNode().getOffset();
		}
		
		for (Annotation subjAnno:subjAnnos){
			int annoStart = (int) (subjAnno.getStartNode().getOffset() - sentenceStart);
			int annoEnd = (int) (subjAnno.getEndNode().getOffset() - sentenceStart);
			if (Overlap.intervalOverlap(opinionStart, opinionEnd, annoStart, annoEnd)){
				//System.out.println("!!!"+this.content.getContent(subjAnno.getStartNode().getOffset(), subjAnno.getEndNode().getOffset()).toString());
				subjs.add(subjAnno);
			}
		}
		
		return subjs;
	}
	
	
	public void findETarget() throws IOException{
		AnnotationSet markups = this.annotations;
		ArrayList<DirectNode> bishans = this.bishanDirects;
		Tree root = this.sentenceSyntax.get(TreeAnnotation.class);
		//List<CoreLabel> words = this.sentenceSyntax.get(TokensAnnotation.class);
		
		System.out.println("----- 1st: eTargets in target span -----");
		for (DirectNode directNode:bishans){
			// first we find all etargets in the target span
			System.out.println(directNode.opinionSpan);
			System.out.println(directNode.targets);
			findAllHeadsInTargetSpan(directNode, root);
			System.out.println(directNode.eTargets);
			// next we fine/filter more eTargets in the sentence
			// next we print in PSL format
			
		}  // each direct node
		
		return;
	}
	
	private void findAllHeadsInTargetSpan(DirectNode directNode, Tree root) throws IOException{
		if (directNode.targets.isEmpty()){
			ArrayList<Tree> headsInSubjSpan =  findAllHeadsInSubjSpan(directNode, root);
			
			for (Tree head:headsInSubjSpan){
				int indexOfLeaf = root.getLeaves().indexOf(head)+1;
				directNode.eTargets.addAll(findETargetMyself(indexOfLeaf));
			}
		}
		
		else{
			findAllHeadsInNonEmptyTargetSpan(directNode, root);
		}
		
		return;
	}
	
	private ArrayList<Tree> findAllHeadsInSubjSpan(DirectNode directNode, Tree root){
		ArrayList<Tree> returnedHeads = new ArrayList<Tree>();
		
		int subjStart = directNode.opinionStart;
		int subjEnd = subjStart + directNode.opinionSpan.split(" ").length-1;
		
		// find the subtree corresponding to the constituent
		ArrayList<Tree> treesOfCon = new ArrayList<Tree>();
		String conSpan = findConSpan(subjStart, subjEnd, root);
		findTreeOfCon(conSpan, root, treesOfCon);
		
		// treesOfCon.get(0) is the constituent containing the subj span
		// we then judge whether each leaf is in the subj span
		// if in, return it as heads in the subj span
		// regardless of the POS (i.e. head)
		for (Tree head:treesOfCon.get(0).getLeaves()){
			if (directNode.opinionSpan.contains(head.nodeString() )){
				// deal with each head in direct node span
				returnedHeads.add(head);
			}
		}
		
		return returnedHeads;
	}
	
	private void findAllHeadsInNonEmptyTargetSpan(DirectNode directNode, Tree root){
		for (String target:directNode.targets){
			int targetStart = directNode.targetStarts.get(directNode.targets.indexOf(target));
			int targetEnd = targetStart + target.split(" ").length-1;
			
			// find the subtree corresponding to the constituent
			ArrayList<Tree> treesOfCon = new ArrayList<Tree>();
			String conSpan = findConSpan(targetStart, targetEnd, root);
			findTreeOfCon(conSpan, root, treesOfCon);
			
			// find the heads in the subtree
			ArrayList<Tree> heads = new ArrayList<Tree>(); 
			findHeadInATree(treesOfCon.get(0), heads);
			for (Tree head:heads){
				if (target.contains(head.nodeString() )){
					directNode.eTargets.add(head);
				}
			}
			
		} // each target
		
		return;
	}
	
	private String findConSpan(int start, int end, Tree root){
		// record the original token
		ArrayList<String> wordsTmp = new ArrayList<String>();
		//for (CoreLabel pair:this.sentenceSyntax.get(TokensAnnotation.class)){    // token doesn't match with parse tree = =
		for (Tree leaf:root.getLeaves()){
			wordsTmp.add(leaf.nodeString());
		}
		
		// tmp.get(0) is the smallest constituent including the target span
		ArrayList<Constituent> tmp = new ArrayList<Constituent>();
		for (Constituent con:root.constituents()){
			if ( Overlap.intervalContains(con.start(), con.end(), start, end) ){
				if (tmp.size()==0)
					tmp.add(con);
				else{
					if (tmp.get(0).contains(con) )
						tmp.add(0, con);
				}
			}
		}  // each constituent
		
		
		String conSpan = tmp.get(0).toSentenceString(wordsTmp);
		
		return conSpan;
	}
	
	private ArrayList<Tree> findETargetMyself(int indexOfLeaf) throws IOException{
		ArrayList<Tree> eTargets = new ArrayList<Tree>();
		ArrayList<Tree> leaves = (ArrayList<Tree>) this.sentenceSyntax.get(TreeAnnotation.class).getLeaves();
		
		for (TypedDependency td:this.tdl){
			if (td.gov().index() == 0)
				continue;
			
			String govWord = this.sentenceSyntax.get(TokensAnnotation.class).get(td.gov().index()-1).lemma();
			if (Rule.targetRulesJudgeGov(td, indexOfLeaf, govWord))
				eTargets.add(leaves.get(td.gov().index()-1));
			if (Rule.targetRulesJudgeDep(td, indexOfLeaf, govWord))
				eTargets.add(leaves.get(td.dep().index()-1));
		}
		
		return eTargets;
	}
	
	private void findHeadInATree(Tree root, ArrayList<Tree> heads){
		// if the current node is the parent node of a particular token, and it is either noun (NN) or verb (VB), or prounoun (PRP)
		// print it
		if (root.isPreTerminal() && (root.label().value().startsWith("NN") || root.label().value().startsWith("VB") || root.label().value().equals("PRP") ) ){
			heads.add(root.children()[0]);
		}
		
		
		for (Tree child: root.children()){
			findHeadInATree(child, heads);
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
