package structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import readGATE.Annotation;
import readGATE.FeatureMap;
import utils.Overlap;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import featureExtraction.Constituent;
import gate.AnnotationSet;

public class ASentence {
	public String sentenceString;
	public String sentenceTokenizedString;
	public int sentenceIndex;
	public List<Word> tokens;
	public Tree parseTree;
	public AnnotationSet annotations;
	public ArrayList<DirectNode> bishanDirects;
	
	public ASentence(){
		this.sentenceString = "";
		this.sentenceTokenizedString = "";
		this.sentenceIndex = -1;
		this.tokens = new ArrayList<Word>();
		this.bishanDirects = new ArrayList<DirectNode>();
	}
	
	public void expandETargetUsingGFBF(){
		for (DirectNode bishan:this.bishanDirects){
			for (Tree eTarget:bishan.eTargets){
				findETargetUsingDep(eTarget);
			}
		}
		
		return;
	}
	
	private void findETargetUsingDep(Tree eTarget){
		ArrayList<Tree>  newETargets = new ArrayList<Tree>();
		ArrayList<Integer> newETargetIndice = new ArrayList<Integer>();
		
		int indexOfLeaf = this.tokens.indexOf(eTarget);
		
		for (TypedDependency td:tdl){
			if ( td.gov().index()-1==indexOfLeaf && td.reln().equals("nsubj") )
				newETargetIndice.add(td.dep().index()-1);
			else if ( td.dep().index()-1==indexOfLeaf && td.reln().equals("nsubj") )
				newETargetIndice.add(td.gov().index()-1);
			else if ( td.dep().index()-1==indexOfLeaf && td.reln().equals("dobj"))
				newETargetIndice.add(td.gov().index()-1);
			else if ( td.gov().index()-1==indexOfLeaf && td.reln().equals("dobj"))
				newETargetIndice.add(td.gov().index()-1);
		}
		
		ArrayList<Tree> leaves = this.parseTree.getLeaves();
		for (Integer index:newETargetIndice){
			newETargets.add(leaves.get(index));
		}
		
		
		return;
	}
	
	
	public void alignGoldStandard(){
		for (DirectNode bishan:this.bishanDirects){
			System.out.println("----------");
			System.out.println(bishan.opinionSpan);
			ArrayList<Annotation> subjAnnos = findMatchingSubjMarkup(bishan, this.annotations);
			System.out.println("# subjAnnos: "+subjAnnos.size());
			ArrayList<Annotation> eTargetAnnos = new ArrayList<Annotation>();
			for (Annotation subjAnno:subjAnnos){
				System.out.print(subjAnno.toString());
				ArrayList<Annotation> tmp = findMatchingETargetMarkup(bishan, subjAnno, markups);
				for (Annotation anno:tmp){
					if (!eTargetAnnos.contains(anno))
						eTargetAnnos.add(anno);
				}
			}
			System.out.println(eTargetAnnos.size());
			bishan.eTargetsGS.addAll(findMatchingETargetHeads(eTargetAnnos, aSentence.parseTree));
			
		}
		
		return;
	}
	
	private ArrayList<Tree> findMatchingETargetHeads(ArrayList<Annotation> eTargetAnnos, Tree root){
		ArrayList<Tree> heads = new ArrayList<Tree>();
		
		for (Tree leaf:root.getLeaves()){
			if (eTargetAnnos.contains(leaf.toString())){
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
	
	private ArrayList<Annotation> findMatchingSubjMarkup(DirectNode direct, AnnotationSet markups){
		ArrayList<Annotation> subjs  = new ArrayList<Annotation>();
		
		String opinionSpan = direct.opinionSpan;
		int opinionStart = this.aSentence.sentenceTokenizedString.indexOf(opinionSpan);
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
				subjs.add(subjAnno);
			}
		}
		
		return subjs;
	}
	
	
	public void findETarget(){
		AnnotationSet markups = this.annotations;
		ArrayList<DirectNode> bishans = this.bishanDirects;
		Tree root = this.parseTree;
		List<Word> words = this.tokens;
		
		for (DirectNode directNode:bishans){
			// first we find all etargets in the target span
			System.out.println(directNode.opinionSpan);
			System.out.println(directNode.targets);
			findAllHeadsInTargetSpan(directNode, words, root);
			System.out.println(directNode.eTargets);
			// next we fine/filter more eTargets in the sentence
			// next we print in PSL format
			
		}  // each direct node
		
		return;
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
