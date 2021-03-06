package structure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import utils.Path;
import utils.Rule;
import utils.Statistics;
import utils.WTF;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
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
	public String docId;
	public String sentenceString;
	public String sentenceTokenizedString;
	public int sentenceIndex;
	public AnnotationSet annotations;
	public DocumentContent content; 
	public ArrayList<DirectNode> bishanDirects;
	public Collection<TypedDependency> tdl;
	public Tree root;
	public CoreMap sentenceSyntax;
	public HashMap<Tree, ArrayList<Tree>> corefHash;
	public boolean multiSentenceFlag;
	
	public ASentence(){
		this.docId = "";
		this.sentenceString = "";
		this.sentenceTokenizedString = "";
		this.sentenceIndex = -1;
		this.bishanDirects = new ArrayList<DirectNode>();
		this.corefHash = new HashMap<Tree, ArrayList<Tree>>();
		this.multiSentenceFlag = false;
	}
	
	// add trees
	public void preprocessing(){
		Tree root = this.root;
		if (!this.multiSentenceFlag)
			Statistics.directNodeNum += this.bishanDirects.size();
		
		for (DirectNode directNode:this.bishanDirects){
			directNode.root = root;
			// find opinion tree
			int opinionStart = directNode.opinionStart;
			int opinionEnd = opinionStart + directNode.opinionSpan.split(" ").length-1;
			String conSpan = findConSpan(opinionStart, opinionEnd, root);
			ArrayList<Tree> trees = new ArrayList<Tree>(); 
			findTreeOfCon(conSpan, root, trees);
			Tree tree = trees.get(0);
			directNode.opinionTree = tree;
			
			// find agent tree
			if (directNode.agentStart != -1){
				int agentStart = directNode.agentStart;
				int agentEnd = agentStart + directNode.agent.split(" ").length-1;
				conSpan = findConSpan(agentStart, agentEnd, root);
				if (!conSpan.isEmpty() && conSpan.length() > 0){
					trees = new ArrayList<Tree>(); 
					findTreeOfCon(conSpan, root, trees);
					tree = trees.get(0);
					directNode.agentTree = tree;
				}
			}
			
			// find target trees
			for (int i=0;i<directNode.targets.size();i++){
				int targetStart = directNode.targetStarts.get(i);
				int targetEnd = targetStart + directNode.targets.get(i).split(" ").length-1;
				String targetConSpan = findConSpan(targetStart, targetEnd, root);
				ArrayList<Tree> targetTrees = new ArrayList<Tree>();
				findTreeOfCon(targetConSpan, root, targetTrees);
				Tree targetTree = targetTrees.get(0);
				directNode.targetTrees.add(targetTree);
			}  // each target tree
		}
		
		return;
	}
	
	public void lastFiltering(){
		for (DirectNode direct:this.bishanDirects){
			for (Tree eTarget:direct.eTargets){    // coref
				addCoref(eTarget, direct);
			}
			
			direct.eTargets = Clean.removeDuplicate(direct.eTargets);   // remove duplicate
			direct.eTargets = Clean.removeNonHead(direct.eTargets, this.root);
			
		}
	}
	
	
	
	
	public void alignGoldStandard() throws GateException{
		System.out.println("----- gold standard eTargets -----");
		for (DirectNode bishan:this.bishanDirects){
			ArrayList<Annotation> subjAnnos = findMatchingSubjMarkup(bishan, this.annotations);
			ArrayList<Annotation> sourceAnnos = new ArrayList<Annotation>();
			ArrayList<Annotation> eTargetAnnos = new ArrayList<Annotation>();
			
			
			if (subjAnnos.size()>1){
				WTF.contro++;
				int pos = 0;
				int neg = 0;
				String source = "";
				boolean flag = false;
				for (Annotation subjAnno:subjAnnos){
					String polarity = "";
					if (subjAnno.getFeatures().containsKey("polarity")){
						polarity = subjAnno.getFeatures().get("polarity").toString();
					}
					else if (subjAnno.getFeatures().containsKey("attitude-type")){
						polarity = subjAnno.getFeatures().get("attitude-type").toString();
					}
					
					if (polarity.contains("pos"))
						pos++;
					else if (polarity.contains("neg"))
						neg++;
					
					if (subjAnno.getFeatures().containsKey("nested-source")){
						if (source.isEmpty())
							source = subjAnno.getFeatures().get("nested-source").toString();
						else
							if (!source.equals(subjAnno.getFeatures().get("nested-source").toString()))
								flag = true;
					}
				}
				if (pos > 0 && neg > 0)
					WTF.polarityContro++;
				if (flag)
					WTF.sourceContro++;
			}
			
			
			
			for (Annotation subjAnno:subjAnnos){
				// find gs source
				Annotation source = findMathcingSourceMarkup(subjAnno, this.annotations);
				if (source != null)
					sourceAnnos.add(source);
				// fing gs etargets
				ArrayList<Annotation> tmp = findMatchingETargetMarkup(subjAnno, this.annotations);
				for (Annotation anno:tmp){
					if (!eTargetAnnos.contains(anno))
						eTargetAnnos.add(anno);
				}
			}
			bishan.agentGS.addAll(findMatchingHeads(sourceAnnos, this.root));
			bishan.eTargetsGS.addAll(findMatchingHeads(eTargetAnnos, this.root));
			
			System.out.println("<"+bishan.agentGS+">");
			System.out.println(bishan.opinionSpan);
			System.out.println(bishan.eTargetsGS);
		}
		
		return;
	}
	
	private ArrayList<Tree> findMatchingHeads(ArrayList<Annotation> eTargetAnnos, Tree root) throws GateException{
		ArrayList<Tree> heads = new ArrayList<Tree>();
		
		for (Annotation eTarget:eTargetAnnos){
			String eTargetSpan = this.content.getContent(eTarget.getStartNode().getOffset(), eTarget.getEndNode().getOffset()).toString();
			String lastName = "";
			if (eTargetSpan.contains(" "))
				lastName = eTargetSpan.split(" ")[eTargetSpan.split(" ").length-1];
			else
				lastName = eTargetSpan;
			
			for (Tree leaf:root.getLeaves()){
				if (lastName.equals(leaf.nodeString())  &&  !heads.contains(leaf))
					heads.add(leaf);
			}
		}
		
		return heads;
	}
	
	private Annotation findMathcingSourceMarkup(Annotation subjAnno, AnnotationSet markups){
		if (!subjAnno.getFeatures().containsKey("nested-source"))
			return null;
		
		ArrayList<Annotation> tmp = new ArrayList<Annotation>();
		
		FeatureMap params = subjAnno.getFeatures();
		String sourceId = params.get("nested-source").toString();
		
		Set<Annotation> sources = markups.get("agent");
		for (Annotation source:sources){
			FeatureMap paramSource = source.getFeatures();
			if (paramSource.containsKey("nested-source") && paramSource.get("nested-source").equals(sourceId))
				tmp.add(source);
		}
		
		if (!tmp.isEmpty() && tmp.size()>0)
			return tmp.get(0);
		else
			return null;
	}
	
	private ArrayList<Annotation> findMatchingETargetMarkup(Annotation subjAnno, AnnotationSet markups){
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
	
	public void addAllHeadAsETarget(){
		ArrayList<DirectNode> bishans = this.bishanDirects;
		Tree root = this.sentenceSyntax.get(TreeAnnotation.class);
		for (DirectNode directNode:bishans){
			addHeadInATree(root, directNode.eTargets);
		}
	}
	
	// extract all eTargets in the constituents...the recall is high
	public void addETarget(){
		ArrayList<DirectNode> bishans = this.bishanDirects;
		Tree root = this.sentenceSyntax.get(TreeAnnotation.class);
		//List<CoreLabel> words = this.sentenceSyntax.get(TokensAnnotation.class);
		
		System.out.println("----- 1st: eTargets in target span -----");
		for (DirectNode directNode:bishans){
			// first we find all etargets in the target span
			System.out.println("<"+directNode.agent+">");
			System.out.println(directNode.opinionSpan);
			System.out.println(directNode.targets);
			
			//Tree tree = directNode.opinionTree.deepCopy();
			Tree tree = directNode.opinionTree;
			while (!tree.equals(root)&& !tree.label().value().toString().equals("VP") && !tree.label().value().toString().equals("NP")){
				tree = tree.parent(root);
				//System.out.println(tree.pennString());
			}
			addHeadInATree(tree, directNode.eTargets);
			
			for (int i=0;i<directNode.targets.size();i++){
				//Tree targetTree = directNode.targetTrees.get(i).deepCopy();
				Tree targetTree = directNode.targetTrees.get(i);
				while (!targetTree.equals(root)&& !targetTree.label().value().toString().equals("VP") && !targetTree.label().value().toString().equals("NP")){
					targetTree = targetTree.parent(root);
					//System.out.println(tree.pennString());
				}
				addHeadInATree(targetTree, directNode.eTargets);
			}
			
			System.out.println(directNode.eTargets);
		}  // each direct node
		
		return;
		
	}
	
	public void addMoreByStanford(){
		for (DirectNode direct:this.bishanDirects){
			if (!direct.eTargets.isEmpty() && direct.eTargets.size() > 0)
				return;
			
			int subjStart = direct.opinionStart;
			int subjEnd = subjStart + direct.opinionSpan.split(" ").length-1;
			
			// find the subtree corresponding to the constituent
			Tree sentiTreeRoot = this.sentenceSyntax.get(AnnotatedTree.class);
			ArrayList<Tree> treesOfCon = new ArrayList<Tree>();
			
			String conSpan = findConSpan(subjStart, subjEnd, this.root);
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
		}
		
		return;
	}
	
	
	// using myself herustics...the recall is low
	public void old_addETarget() throws IOException{
		ArrayList<DirectNode> bishans = this.bishanDirects;
		Tree root = this.sentenceSyntax.get(TreeAnnotation.class);
		//List<CoreLabel> words = this.sentenceSyntax.get(TokensAnnotation.class);
		
		System.out.println("----- 1st: eTargets in target span -----");
		for (DirectNode directNode:bishans){
			// first we find all etargets in the target span
			System.out.println("<"+directNode.agent+">");
			System.out.println(directNode.opinionSpan);
			System.out.println(directNode.targets);
			
			old_addAllHeadsInTargetSpan(directNode, root);
			directNode.eTargets = Clean.removeDuplicate(directNode.eTargets);
			
			System.out.println(directNode.eTargets);
			
		}  // each direct node
		
		return;
	}
	
	private void old_addAllHeadsInTargetSpan(DirectNode directNode, Tree root) throws IOException{
		if (directNode.targets.isEmpty()){
			ArrayList<Tree> headsInSubjSpan =  old_findAllHeadsInSubjSpan(directNode, root);
			
			directNode.eTargets.addAll(old_findETargetMyselfByCon(directNode,root));
			
			
			for (Tree head:headsInSubjSpan){
				int indexOfLeaf = root.getLeaves().indexOf(head)+1;
				directNode.eTargets.addAll(old_findETargetMyselfByDep(indexOfLeaf,headsInSubjSpan));
				
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
				addHeadInATree(treesOfCon.get(0), heads);
				for (Tree head:heads){
					if (target.contains(head.nodeString() )){
						directNode.eTargets.add(head);
					}
				}
				
			} // each target
		}
		
		return;
	}
	
	private ArrayList<Tree> old_findAllHeadsInSubjSpan(DirectNode directNode, Tree root){
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
	
	
	
	private ArrayList<Tree> old_findETargetMyselfByDep(int indexOfLeaf, ArrayList<Tree> headsInSubjSpan) throws IOException{
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
	
	private ArrayList<Tree> old_findETargetMyselfByCon(DirectNode direct, Tree root) throws IOException{
		ArrayList<Tree> eTargets = new ArrayList<Tree>();
		
		int opinionStart = direct.opinionStart;
		int opinionEnd = opinionStart + direct.opinionSpan.split(" ").length-1;
		String conSpan = findConSpan(opinionStart, opinionEnd, root);
		ArrayList<Tree> treesOfCon = new ArrayList<Tree>();
		findTreeOfCon(conSpan, root, treesOfCon);
		Tree tree = treesOfCon.get(0);
		
		// the subj span is an NP
		if (tree.label().value().startsWith("NP")){
			addHeadInATree(tree, eTargets);
		}
		
		// the subj span is a VP
		else if (tree.label().value().startsWith("VP")){
			// deal with siblings
			ArrayList<Tree> siblings = (ArrayList<Tree>) tree.siblings(root);
			for (Tree sibling:siblings){
				if (sibling.label().value().startsWith("S") || sibling.label().value().startsWith("NP")){
					addHeadInATree(sibling, eTargets);
				}
			}
			// NP in the VP
			
			
		}
		
		else if (tree.label().value().startsWith("ADJ")){
			ArrayList<Tree> headsInSubjSpan =  old_findAllHeadsInSubjSpan(direct, root);
			for (Tree head:headsInSubjSpan){
				int indexOfLeaf = root.getLeaves().indexOf(head)+1;
				eTargets.addAll(old_findETargetMyselfByDep(indexOfLeaf,headsInSubjSpan));
				
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
	
	public void addMoreByGFBF() throws IOException{
		System.out.println("----- 2nd: adding gfbf rules -----");
		for (DirectNode bishan:this.bishanDirects){
			System.out.println("<"+bishan.agent+">");
			System.out.println(bishan.opinionSpan);
			
			bishan.eTargets = findMoreByGFBF(bishan);
			bishan.eTargets = Clean.removeDuplicate(bishan.eTargets);
			
			System.out.println(bishan.eTargets);
		}  // each direct node
		
		return;
	}
	
	private ArrayList<Tree> findMoreByGFBF(DirectNode directNode) throws IOException{
		ArrayList<Tree> eTargets = directNode.eTargets;
		
		if (eTargets.isEmpty() || eTargets.size()==0)
			return eTargets;
		
		ArrayList<Tree> leaves = (ArrayList<Tree>) this.root.getLeaves();
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
					Rule.makeItATriple(td, leaves, directNode.gfbfTriples);
				}
				if (Rule.gfbfRulesJudgeDep(td, indexOfLeaf, govWord)){
					queue.offer(leaves.get(td.dep().index()-1));
					Rule.makeItATriple(td, leaves, directNode.gfbfTriples);
				}
			}
		}   // while queue isn't empty
		
		return newETargets;
	}
	
	/*
	 * functions below as used as helpers
	 */
	
	private void addHeadInATree(Tree root, ArrayList<Tree> heads){
		// if the current node is the parent node of a particular token, and it is either noun (NN) or verb (VB), or prounoun (PRP)
		// print it
		if ( judgeHead(root) ){
			heads.add(root.children()[0]);
		}
		
		
		for (Tree child: root.children()){
			addHeadInATree(child, heads);
		}
		
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
		
		if (tmp.isEmpty() || tmp.size()==0)
			return "";
		
		String conSpan = tmp.get(0).toSentenceString(wordsTmp);
		
		return conSpan;
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
	
	private void addCoref(Tree leaf, DirectNode direct){
		if (this.corefHash.containsKey(leaf))
			direct.eTargets.addAll(this.corefHash.get(leaf));
		
		
		return;
	}
	
	private boolean judgeHead(Tree root){
		if (root.isPreTerminal() && (root.label().value().startsWith("NN") || root.label().value().startsWith("VB") || root.label().value().equals("PRP") ) ){
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("unused")
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
	
public void writeForPSL(HashMap<Integer, HashMap<Integer,Double>> targets) throws IOException{
		
		HashSet<Integer> etargets = new HashSet<Integer>();
		
		HashMap<Integer, Double> gfs = new HashMap<Integer, Double>();
		HashMap<Integer, Double> bfs = new HashMap<Integer, Double>();
		HashMap<Integer, HashMap<Integer, Double>> agents = new HashMap<Integer, HashMap<Integer, Double>>();
		HashMap<Integer, HashMap<Integer, Double>> themes = new HashMap<Integer, HashMap<Integer, Double>>();
		
		HashMap<Integer, HashMap<Integer, Double>> sources = new HashMap<Integer, HashMap<Integer, Double>>();
		
		HashMap<Integer, Double> positives = new HashMap<Integer, Double>();
		HashMap<Integer, Double> negatives = new HashMap<Integer, Double>();
		
		/*
		 * open the output file from SVM classifier
		 */
		System.out.println("===============");
		System.out.println(this.sentenceTokenizedString);
		for (DirectNode directNode:this.bishanDirects){
			if (directNode.eTargetsGS.isEmpty() || directNode.eTargetsGS.size() == 0)
				continue;
			
			ArrayList<Feature> features = directNode.features;
			
			List<Tree> leaves = directNode.root.getLeaves();
			etargets.add(-1*directNode.opinionStart);
			for (int t=0;t<directNode.eTargets.size();t++){
				Tree etarget = directNode.eTargets.get(t);
				Feature feature = features.get(t);
				
				int eTargetIndex = leaves.indexOf(etarget);
				etargets.add(eTargetIndex);
				
				// gfbf triples
				if (feature.isGF != 0){
					gfs.put(eTargetIndex, feature.isGF);
				}
				if (feature.isBF != 0){
					bfs.put(eTargetIndex, feature.isBF);
				}
				
				Triple triple = Overlap.tripleListContains(etarget, directNode.gfbfTriples);
				if (triple != null){
					// agents
					for (Tree agentTree:triple.agent){
						if ( agents.containsKey(eTargetIndex) ){
							HashMap<Integer, Double> tmp = agents.get(eTargetIndex);
							tmp.put(leaves.indexOf(agentTree), 1.0);
						}
						else{
							HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
							tmp.put(leaves.indexOf(agentTree), 1.0);
							agents.put(eTargetIndex, tmp);
						}
					}
					// themes
					for (Tree themeTree:triple.theme){
						if ( themes.containsKey(eTargetIndex) ){
							HashMap<Integer, Double> tmp = themes.get(eTargetIndex);
							tmp.put(leaves.indexOf(themeTree), 1.0);
						}
						else{
							HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
							tmp.put(leaves.indexOf(themeTree), 1.0);
							themes.put(eTargetIndex, tmp);
						}
					}
				}
				
				
				/*
				// targets
				// read from Doc class input
				*/
				
			}   // each etarget
			
			// source
			if (!directNode.agentGS.isEmpty() && directNode.agentGS.size()>0){
				int tmpSourceIndex = directNode.root.getLeaves().indexOf(directNode.agentGS.get(0));
				etargets.add(tmpSourceIndex);
				HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
				tmp.put(tmpSourceIndex, 1.0);
				sources.put(-1*directNode.opinionStart, tmp);
			}
			else{
				etargets.add(0);
				HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
				tmp.put(0, 1.0);
				sources.put(-1*directNode.opinionStart, tmp);
			}
		
			// positive
			if (directNode.polarity.equals("positive")){
				positives.put(-1*directNode.opinionStart, 1.0);
			}
			if (directNode.polarity.equals("negative")){
				negatives.put(-1*directNode.opinionStart, 1.0);
			}
			
			System.out.println("-----------");
			System.out.println(-1*directNode.opinionStart+" "+directNode.opinionSpan);
			System.out.print("sources: ");
			System.out.println(sources);
			System.out.print("targets: ");
			System.out.println(targets);
			System.out.print("positives: ");
			System.out.println(positives);
			System.out.print("negatives: ");
			System.out.println(negatives);
		
		}  // each direct node
		
		
		
		if (etargets.isEmpty())
			return;
		
		// write etarget
		File f = new File(Path.getPSLRoot()+this.docId+"/"+"PSL"+String.valueOf(this.sentenceIndex)+".etargets");
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		
		if (!etargets.isEmpty()){
			for (Integer id:etargets){
				bw.write(String.valueOf(id));
				bw.newLine();
			}
		}
		
		bw.close();
		fw.close();
		
		// write gf
		f = new File(Path.getPSLRoot()+this.docId+"/"+"PSL"+String.valueOf(this.sentenceIndex)+".gfs");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		
		if (!gfs.isEmpty()){
			for (Integer id:gfs.keySet()){
				bw.write(String.valueOf(id)+"\t"+String.valueOf(gfs.get(id)));
				bw.newLine();
			}
		}
		
		bw.close();
		fw.close();
		
		// write bf
		f = new File(Path.getPSLRoot()+this.docId+"/"+"PSL"+String.valueOf(this.sentenceIndex)+".bfs");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		
		if (!bfs.isEmpty()){
			for (Integer id:bfs.keySet()){
				bw.write(String.valueOf(id)+"\t"+String.valueOf(bfs.get(id)));
				bw.newLine();
			}
		}
		
		bw.close();
		fw.close();
		
		// write positive
		f = new File(Path.getPSLRoot()+this.docId+"/"+"PSL"+String.valueOf(this.sentenceIndex)+".positives");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		
		if (!positives.isEmpty()){
			for (Integer id:positives.keySet()){
				bw.write(String.valueOf(id)+"\t"+String.valueOf(positives.get(id)));
				bw.newLine();
			}
		}
		
		bw.close();
		fw.close();
		
		// write negative
		f = new File(Path.getPSLRoot()+this.docId+"/"+"PSL"+String.valueOf(this.sentenceIndex)+".negatives");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		
		if (!negatives.isEmpty()){
			for (Integer id:negatives.keySet()){
				bw.write(String.valueOf(id)+"\t"+String.valueOf(negatives.get(id)));
				bw.newLine();
			}
		}
		
		bw.close();
		fw.close();
		
		// write agent
		f = new File(Path.getPSLRoot()+this.docId+"/"+"PSL"+String.valueOf(this.sentenceIndex)+".agents");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		
		if (!agents.isEmpty()){
			for (Integer id1:agents.keySet()){
				if (!agents.get(id1).isEmpty()){
					for (Integer id2:agents.get(id1).keySet()){
						bw.write( String.valueOf(id1)+"\t"+String.valueOf(id2)+"\t"+String.valueOf(agents.get(id1).get(id2)) );
						bw.newLine();
					}
				}
				
			}
		}
		
		bw.close();
		fw.close();
		
		// write theme
		f = new File(Path.getPSLRoot()+this.docId+"/"+"PSL"+String.valueOf(this.sentenceIndex)+".themes");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		
		if (!themes.isEmpty()){
			for (Integer id1:themes.keySet()){
				if (!themes.get(id1).isEmpty()){
					for (Integer id2:themes.get(id1).keySet()){
						bw.write( String.valueOf(id1)+"\t"+String.valueOf(id2)+"\t"+String.valueOf(themes.get(id1).get(id2)) );
						bw.newLine();
					}
				}
				
			}
		}
		
		bw.close();
		fw.close();
		
		// write source
		f = new File(Path.getPSLRoot()+this.docId+"/"+"PSL"+String.valueOf(this.sentenceIndex)+".sources");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		
		if (!sources.isEmpty()){
			for (Integer id1:sources.keySet()){
				if (!sources.get(id1).isEmpty()){
					for (Integer id2:sources.get(id1).keySet()){
						bw.write( String.valueOf(id1)+"\t"+String.valueOf(id2)+"\t"+String.valueOf(sources.get(id1).get(id2)) );
						bw.newLine();
					}
				}
				
			}
		}
		
		bw.close();
		fw.close();
		
		// write target
		f = new File(Path.getPSLRoot()+this.docId+"/"+"PSL"+String.valueOf(this.sentenceIndex)+".targets");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		
		if (!targets.isEmpty()){
			for (Integer id1:targets.keySet()){
				if (!targets.get(id1).isEmpty()){
					for (Integer id2:targets.get(id1).keySet()){
						bw.write( String.valueOf(id1)+"\t"+String.valueOf(id2)+"\t"+String.valueOf(targets.get(id1).get(id2)) );
						bw.newLine();
					}
				}
				
			}
		}
		
		bw.close();
		fw.close();
		
		
		
		return;
	}
}
