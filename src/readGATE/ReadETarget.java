package readGATE;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.StringUtils;

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
		DocumentContent content = this.doc.getContent();
		JudgeETarget j = new JudgeETarget();
		
		AnnotationSet markups = this.doc.getAnnotations("MPQA");
		markups.addAll(this.doc.getAnnotations());
		
		for (Annotation markup:markups.get("inside")){
			String sentence = content.getContent(markup.getStartNode().getOffset(), markup.getEndNode().getOffset()).toString();
			// tokenize and get the parse
			PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(new StringReader(sentence));
			List<Word> words = ptb.tokenize();
			String sentenceTokenized = StringUtils.join(words);
			
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
			if (!this.bishanSentenceHash.containsKey(sentenceTokenized)){
				continue;
			}
			
			ArrayList<DirectNode> bishans = this.bishanSentenceHash.get(sentenceTokenized);
			for (DirectNode bishan:bishans){
				int bishanSpanEnd = bishan.sentence.indexOf(bishan.span)+bishan.span.length();
				
				// add etarget candidates
				for (Annotation head:heads){
					
					if (j.judge(head,bishan))
					String headSpan = content.getContent(head.getStartNode().getOffset(), head.getEndNode().getOffset()).toString();
					
					if (bishan.targets.isEmpty() && head.getStartNode().getOffset() > bishanSpanEnd){
						bishan.targets.add(headSpan);
					}
					else if (!bishan.targets.isEmpty()){
						for (String target:bishan.targets){
							int bishanTargetStart = bishan.sentence.indexOf(target);
							int bishanTargetEnd = bishanTargetStart + target.length();
							
							if ( !(head.getStartNode().getOffset() > bishanTargetEnd) || !(head.getEndNode().getOffset() < bishanTargetStart) ){
								bishan.targets.add(headSpan);
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
