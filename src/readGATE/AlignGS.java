package readGATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import readBishan.DirectNode;
import run.ASentence;
import utils.Overlap;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;
import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;

public class AlignGS {
	
	private ASentence aSentence;
	
	public AlignGS(ASentence aSentence){
		this.aSentence = aSentence;
		AnnotationSet markups = aSentence.annotations;
		ArrayList<DirectNode> bishans = aSentence.bishanDirects;
		
		
		for (DirectNode bishan:bishans){
			System.out.println("----------");
			System.out.println(bishan.opinionSpan);
			ArrayList<Annotation> subjAnnos = findMatchingSubjMarkup(bishan, markups);
			System.out.println("# subjAnnos: "+subjAnnos.size());
			for (Annotation subjAnno:subjAnnos){
				System.out.print(subjAnno.toString());
				ArrayList<Annotation> eTargetAnnos = findMatchingETargetMarkup(bishan, subjAnno, markups);
				
				
			}
		}
		
		
		
	}
	
	private ArrayList<Tree> findMatchingETargetHeads(ArrayList<Annotation> eTargetAnnos, Tree root, List<Word> words){
		ArrayList<Tree> heads = new ArrayList<Tree>();
		
		for (Tree leaf:root.getLeaves()){
			
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
		
		
		System.out.println("eTargets: "+eTargets.size());
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

}
