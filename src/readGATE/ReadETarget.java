package readGATE;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.FeatureMap;
import gate.util.GateException;

public class ReadETarget {
	private Document doc;
	private String filePath;
	 
	
	public ReadETarget(String docId) throws MalformedURLException, GateException{
		IntiateGATE.go(docId);
		this.doc = IntiateGATE.doc;
		run();
	}
	
	private void run() throws GateException{
		ArrayList<String> sentences = new ArrayList<String>();
		DocumentContent content = this.doc.getContent();
		
		AnnotationSet markups = this.doc.getAnnotations("MPQA");
		markups.addAll(doc.getAnnotations());
		//markups.addAll(this.doc.getAnnotations());
		for (Annotation markup:markups.get("inside")){
			String sentence = content.getContent(markup.getStartNode().getOffset(), markup.getEndNode().getOffset()).toString();
			// parse sentence
			
			// get the gold standard nodes
			AnnotationSet nodesInSentence = markups.get(markup.getStartNode().getOffset(), markup.getEndNode().getOffset());
			Set<String> etargets = new HashSet<String>();
			etargets.add("eTarget");
			etargets.add("eTarget-new");
			AnnotationSet eTargetsInSentence = nodesInSentence.get(etargets);
			AnnotationSet sentiments = nodesInSentence.get("sentiment");
			AnnotationSet eses = nodesInSentence.get("ESE-polar");
			AnnotationSet agents = nodesInSentence.get("agent");
			
			
			
			
			
			
			
			
		}
		
		System.out.println(markups.get("inside").size());
		
		return;
	}
}
