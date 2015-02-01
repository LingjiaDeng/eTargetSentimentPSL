package readGATE;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import readBishan.DirectNode;
import utils.Overlap;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.FeatureMap;
import gate.util.GateException;

public class ReadETarget {
	private Document doc;
	private String filePath;
	public HashMap<String,ArrayList<DirectNode>> bishanSentenceHash; 
	 
	
	public ReadETarget(String docId) throws MalformedURLException, GateException{
		this.bishanSentenceHash = new HashMap<String,ArrayList<DirectNode>>();
		IntiateGATE.go(docId);
		this.doc = IntiateGATE.doc;
		if (!this.bishanSentenceHash.isEmpty()){
			run();
		}
	}
	
	public void addBishanResults(HashMap<String,ArrayList<DirectNode>> bishans) throws GateException{
		this.bishanSentenceHash = bishans;
		run();
		
	}
	
	private void run() throws GateException{
		//ArrayList<String> sentences = new ArrayList<String>();
		DocumentContent content = this.doc.getContent();
		
		AnnotationSet markups = this.doc.getAnnotations("MPQA");
		markups.addAll(this.doc.getAnnotations());
		
		for (Annotation markup:markups.get("inside")){
			String sentence = content.getContent(markup.getStartNode().getOffset(), markup.getEndNode().getOffset()).toString();
			// parse sentence
			
			// get the gold standard nodes
			AnnotationSet nodesInSentence = markups.get(markup.getStartNode().getOffset(), markup.getEndNode().getOffset());
			Set<String> etargetNames = new HashSet<String>();
			etargetNames.add("eTarget");
			etargetNames.add("eTarget-new");
			AnnotationSet etargets = nodesInSentence.get(etargetNames);
			AnnotationSet sentiments = nodesInSentence.get("sentiment");
			AnnotationSet eses = nodesInSentence.get("ESE-polar");
			Set<String> subjNames = new HashSet<String>();
			subjNames.add("sentiment");
			subjNames.add("ESE-polar");
			AnnotationSet subjs = nodesInSentence.get(subjNames);
			AnnotationSet agents = nodesInSentence.get("agent");
			AnnotationSet heads = nodesInSentence.get("head");
			
			// match Bishan's result into gate annotations
			if (!this.bishanSentenceHash.containsKey(sentence.replaceAll("[^a-zA-Z0-9 ]+", "").replaceAll(" +", " ").trim())){
				continue;
			}
			
			ArrayList<DirectNode> bishans = this.bishanSentenceHash.get(sentence.replaceAll("[^a-zA-Z0-9 ]+", "").replaceAll(" +", " ").trim());
			for (DirectNode bishan:bishans){
				for (Annotation subj:subjs){
					String subjSpan = content.getContent(subj.getStartNode().getOffset(), subj.getEndNode().getOffset()).toString();
					if (Overlap.subStringOverlap(subjSpan, sentence)){
						// add more agents
						if (bishan.agent.isEmpty() || bishan.agent.equals("N/A") || bishan.agent.equals("")){
							
						}
						if (bishan.targets.isEmpty()){
							for (Annotation head:heads){
								if (head.getStartNode().getOffset() > subj.getEndNode().getOffset()){
									String headSpan = content.getContent(head.getStartNode().getOffset(), head.getEndNode().getOffset()).toString();
									bishan.targets.add(headSpan);
								}
							}
						}
					}
				}
			}
			
			
			
		}
		
		System.out.println(markups.get("inside").size());
		
		return;
	}
}
