package structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import utils.Clean;
import utils.Rule;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.AnnotatedTree;
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
	public HashMap<Tree, ArrayList<Tree>> corefHash;
	public boolean multiSentenceFlag;
	
	public ASentence(){
		this.sentenceString = "";
		this.sentenceTokenizedString = "";
		this.sentenceIndex = -1;
		this.bishanDirects = new ArrayList<DirectNode>();
		this.corefHash = new HashMap<Tree, ArrayList<Tree>>();
		this.multiSentenceFlag = false;
		
	}
	
	public void lastFiltering(){
		for (DirectNode direct:this.bishanDirects){
			for (Tree eTarget:direct.eTargets){
				findCoref(eTarget, direct);
			}
			
			direct.eTargets = Clean.removeDuplicate(direct.eTargets);
			/*
			Tree root = this.sentenceSyntax.get(TreeAnnotation.class);
			direct.eTargets = Clean.removeNonHead(direct.eTargets, root);
			*/
		}
	}
	
	public void expandETargetUsingGFBF() throws IOException{
		System.out.println("----- 2nd: adding gfbf rules -----");
		for (DirectNode bishan:this.bishanDirects){
			System.out.println("<"+bishan.agent+">");
			System.out.println(bishan.opinionSpan);
			
			bishan.eTargets = expandETargetUsingGFBF(bishan.eTargets);
			bishan.eTargets = Clean.removeDuplicate(bishan.eTargets);
			
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
				if (Rule.gfbfRulesJudgeGov(td, indexOfLeaf, govWord)){
					queue.offer(leaves.get(td.gov().index()-1));
				}
				if (Rule.gfbfRulesJudgeDep(td, indexOfLeaf, govWord)){
					queue.offer(leaves.get(td.dep().index()-1));
				}
			}
		}   // while queue isn't empty
		
		return newETargets;
	}
	
	
	public void alignGoldStandard() throws GateException{
		System.out.println("----- gold standard eTargets -----");
		for (DirectNode bishan:this.bishanDirects){
			System.out.println("<"+bishan.agent+">");
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
		ArrayList<DirectNode> bishans = this.bishanDirects;
		Tree root = this.sentenceSyntax.get(TreeAnnotation.class);
		//List<CoreLabel> words = this.sentenceSyntax.get(TokensAnnotation.class);
		
		System.out.println("----- 1st: eTargets in target span -----");
		for (DirectNode directNode:bishans){
			// first we find all etargets in the target span
			System.out.println("<"+directNode.agent+">");
			System.out.println(directNode.opinionSpan);
			System.out.println(directNode.targets);
			
			findAllHeadsInTargetSpan(directNode, root);
			directNode.eTargets = Clean.removeDuplicate(directNode.eTargets);
			
			findMoreByStanford(directNode);
			directNode.eTargets = Clean.removeDuplicate(directNode.eTargets);
			
			System.out.println(directNode.eTargets);
			
		}  // each direct node
		
		return;
	}
	
	public void extractAllETargetsInCon(){
		ArrayList<DirectNode> bishans = this.bishanDirects;
		Tree root = this.sentenceSyntax.get(TreeAnnotation.class);
		//List<CoreLabel> words = this.sentenceSyntax.get(TokensAnnotation.class);
		
		System.out.println("----- 1st: eTargets in target span -----");
		for (DirectNode directNode:bishans){
			int opinionStart = directNode.opinionStart;
			int opinionEnd = directNode.opinionStart + directNode.opinionSpan.split(" ").length-1;
			String conSpan = findConSpan(opinionStart, opinionEnd, root);
			ArrayList<Tree> trees = new ArrayList<Tree>();
			findTreeOfCon(conSpan, root, trees);
			Tree tree = trees.get(0);
			while (!tree.equals(root)&& !tree.label().value().toString().equals("VP") && !tree.label().value().toString().equals("NP")){
				tree = tree.parent(root);
				//System.out.println(tree.pennString());
			}
			findHeadInATree(tree, directNode.eTargets);
			
			for (int i=0;i<directNode.targets.size();i++){
				int targetStart = directNode.targetStarts.get(i);
				int targetEnd = targetStart + directNode.targets.get(i).split(" ").length-1;
				String targetConSpan = findConSpan(targetStart, targetEnd, root);
				ArrayList<Tree> targetTrees = new ArrayList<Tree>();
				findTreeOfCon(targetConSpan, root, targetTrees);
				Tree targetTree = targetTrees.get(0);
				while (!targetTree.equals(root)&& !targetTree.label().value().toString().equals("VP") && !targetTree.label().value().toString().equals("NP")){
					targetTree = targetTree.parent(root);
					//System.out.println(tree.pennString());
				}
				findHeadInATree(targetTree, directNode.eTargets);
				
			}
			
			findMoreByStanford(directNode);
			
			System.out.println(directNode.eTargets);
		}  // each direct node
		
		return;
		
	}
	
	private void findMoreByStanford(DirectNode direct){
		if (!direct.eTargets.isEmpty() && direct.eTargets.size() > 0)
			return;
		
		int subjStart = direct.opinionStart;
		int subjEnd = subjStart + direct.opinionSpan.split(" ").length-1;
		
		// find the subtree corresponding to the constituent
		Tree sentiTreeRoot = this.sentenceSyntax.get(AnnotatedTree.class);
		ArrayList<Tree> treesOfCon = new ArrayList<Tree>();
		
		String conSpan = findConSpan(subjStart, subjEnd, this.sentenceSyntax.get(TreeAnnotation.class));
		findTreeOfCon(conSpan, sentiTreeRoot, treesOfCon);
		
		Queue<Tree> queue = new LinkedList<Tree>();
		queue.offer(treesOfCon.get(0));
		while ( !queue.isEmpty() ){
			Tree subTree = queue.poll();
			if (!subTree.isLeaf()){
				// put eTargets in
				int polarityNumStanford = RNNCoreAnnotations.getPredictedClass(subTree);
				if ( (direct.polarity.startsWith("pos") && polarityNumStanford>2) 
						|| (direct.polarity.startsWith("neg") && polarityNumStanford<2) )
					direct.eTargets.addAll(subTree.getLeaves());
				// put children in the queue
				for (Tree child:subTree.getChildrenAsList()){
					queue.offer(child);
				}
			}
		}
		
		return;
	}
	
	private void findAllHeadsInTargetSpan(DirectNode directNode, Tree root) throws IOException{
		if (directNode.targets.isEmpty()){
			ArrayList<Tree> headsInSubjSpan =  findAllHeadsInSubjSpan(directNode, root);
			
			directNode.eTargets.addAll(findETargetMyselfByCon(directNode,root));
			
			
			for (Tree head:headsInSubjSpan){
				int indexOfLeaf = root.getLeaves().indexOf(head)+1;
				directNode.eTargets.addAll(findETargetMyselfByDep(indexOfLeaf,headsInSubjSpan));
				
			}
			
		}
		
		else{
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
	
	private ArrayList<Tree> findETargetMyselfByDep(int indexOfLeaf, ArrayList<Tree> headsInSubjSpan) throws IOException{
		ArrayList<Tree> eTargets = new ArrayList<Tree>();
		ArrayList<Tree> leaves = (ArrayList<Tree>) this.sentenceSyntax.get(TreeAnnotation.class).getLeaves();
		
		for (TypedDependency td:this.tdl){
			if (td.gov().index() == 0)
				continue;
			
			String govWord = this.sentenceSyntax.get(TokensAnnotation.class).get(td.gov().index()-1).lemma();
			//if (Rule.targetRulesJudgeGov(td, indexOfLeaf, govWord) && !headsInSubjSpan.contains(leaves.get(td.gov().index()-1)) ){
			if (Rule.targetRulesJudgeGov(td, indexOfLeaf, govWord)  ){
				//Tree leaf = leaves.get(td.gov().index()-1);
				//System.out.println(leaf.parent().label().value());
				//if ( judgeHead(leaf.parent()) )
				eTargets.add(leaves.get(td.gov().index()-1));
			}
			if (Rule.targetRulesJudgeDep(td, indexOfLeaf, govWord)  ){
				//Tree leaf = leaves.get(td.dep().index()-1);
				//System.out.println(leaf.parent().label().value());
				//if ( judgeHead(leaf.parent()) )
				eTargets.add(leaves.get(td.dep().index()-1));
			}
		}
		
		return eTargets;
	}
	
	private ArrayList<Tree> findETargetMyselfByCon(DirectNode direct, Tree root) throws IOException{
		ArrayList<Tree> eTargets = new ArrayList<Tree>();
		
		int opinionStart = direct.opinionStart;
		int opinionEnd = opinionStart + direct.opinionSpan.split(" ").length-1;
		String conSpan = findConSpan(opinionStart, opinionEnd, root);
		ArrayList<Tree> treesOfCon = new ArrayList<Tree>();
		findTreeOfCon(conSpan, root, treesOfCon);
		Tree tree = treesOfCon.get(0);
		
		// the subj span is an NP
		if (tree.label().value().startsWith("NP")){
			findHeadInATree(tree, eTargets);
		}
		
		// the subj span is a VP
		else if (tree.label().value().startsWith("VP")){
			// deal with siblings
			ArrayList<Tree> siblings = (ArrayList<Tree>) tree.siblings(root);
			for (Tree sibling:siblings){
				if (sibling.label().value().startsWith("S") || sibling.label().value().startsWith("NP")){
					findHeadInATree(sibling, eTargets);
				}
			}
			// NP in the VP
			
			
		}
		
		else if (tree.label().value().startsWith("ADJ")){
			ArrayList<Tree> headsInSubjSpan =  findAllHeadsInSubjSpan(direct, root);
			for (Tree head:headsInSubjSpan){
				int indexOfLeaf = root.getLeaves().indexOf(head)+1;
				eTargets.addAll(findETargetMyselfByDep(indexOfLeaf,headsInSubjSpan));
				
				for (TypedDependency td:this.tdl){
					if (td.gov().index() == 0)
						continue;
					
					String govWord = this.sentenceSyntax.get(TokensAnnotation.class).get(td.gov().index()-1).lemma();
					//if (Rule.targetRulesJudgeGov(td, indexOfLeaf, govWord) && !headsInSubjSpan.contains(leaves.get(td.gov().index()-1)) ){
					if (Rule.targetRulesJudgeGovNounOfAdj(td, indexOfLeaf, govWord)  ){
						eTargets.add(root.getLeaves().get(td.gov().index()-1));
					}
					if (Rule.targetRulesJudgeDepNounOfAdj(td, indexOfLeaf, govWord)  ){
						eTargets.add(root.getLeaves().get(td.dep().index()-1));
					}
				}  // dependency
			}  // each head in subj span
		}  // adj
		
		return eTargets;
		
		
	}
	
	private void findHeadInATree(Tree root, ArrayList<Tree> heads){
		// if the current node is the parent node of a particular token, and it is either noun (NN) or verb (VB), or prounoun (PRP)
		// print it
		if (root.isPreTerminal() && judgeHead(root) ){
			heads.add(root.children()[0]);
		}
		
		
		for (Tree child: root.children()){
			findHeadInATree(child, heads);
		}
		
		return;
	}
	
	private boolean judgeHead(Tree root){
		if (root.isPreTerminal() && (root.label().value().startsWith("NN") || root.label().value().startsWith("VB") || root.label().value().equals("PRP") ) ){
			return true;
		}
		
		return false;
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
	
	private void findCoref(Tree leaf, DirectNode direct){
		if (this.corefHash.containsKey(leaf))
			direct.eTargets.addAll(this.corefHash.get(leaf));
		
		
		return;
	}
	
	
	
	
	
	

}
