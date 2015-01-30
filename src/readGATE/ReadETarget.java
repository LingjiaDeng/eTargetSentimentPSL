package readGATE;

import java.net.MalformedURLException;
import java.util.ArrayList;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.util.GateException;

public class ReadETarget {
	private Document doc;
	 
	
	public ReadETarget(String docId) throws MalformedURLException, GateException{
		IntiateGATE.go(docId);
		this.doc = IntiateGATE.doc;
	}
	
	private void run() throws GateException{
		ArrayList<String> sentences = new ArrayList<String>();
		DocumentContent content = this.doc.getContent();
		
		AnnotationSet markups = this.doc.getAnnotations("MPQA");
		//markups.addAll(this.doc.getAnnotations());
		for (Annotation markup:markups.get("inside")){
			String sentence = content.getContent(markup.getStartNode().getOffset(), markup.getEndNode().getOffset()).toString();
			// parse sentence
			
			AnnotationSet nodesInSentence = markups.get(markup.getStartNode().getOffset(), markup.getEndNode().getOffset());
			AnnotationSet eTargetsInSentence = nodesInSentence.get("eTarget");
			
			
			
			
			
		}
	}
}
