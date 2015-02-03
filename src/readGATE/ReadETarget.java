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
import run.ASentence;
import utils.Overlap;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.FeatureMap;
import gate.util.GateException;

public class ReadETarget {
	private Document doc;
	public HashMap<String,ArrayList<DirectNode>> bishanSentenceHash;
	public HashMap<String,ASentence> sentenceHash;
	 
	
	public ReadETarget(String docId) throws MalformedURLException, GateException{
		IntiateGATE.go(docId);
		this.doc = IntiateGATE.doc;
		this.sentenceHash = new HashMap<String,ASentence>();
		this.bishanSentenceHash = new HashMap<String,ArrayList<DirectNode>>();
		readGATE();
	}
	
	
	public ArrayList<ASentence> addBishanResults(HashMap<String,ArrayList<DirectNode>> bishans,ArrayList<ASentence> sentences) throws GateException{
		this.bishanSentenceHash = bishans;
		return mergeBishanIntoGATE(sentences);
	}
	
	private ArrayList<ASentence> mergeBishanIntoGATE(ArrayList<ASentence> sentences){
		for (String sentence:this.sentenceHash.keySet()){
			ASentence aSentence = this.sentenceHash.get(sentence);
			if (this.bishanSentenceHash.containsKey(sentence)){
				aSentence.bishanDirects = this.bishanSentenceHash.get(sentence);	
			}
			sentences.add(aSentence);
		}
		
		return sentences;
	}
	
	
	private void readGATE() throws GateException{
		DocumentContent content = this.doc.getContent();
		JudgeETarget j = new JudgeETarget();
		
		AnnotationSet markups = this.doc.getAnnotations("MPQA");
		markups.addAll(this.doc.getAnnotations());
		
		for (Annotation markup:markups.get("inside")){
			String sentence = content.getContent(markup.getStartNode().getOffset(), markup.getEndNode().getOffset()).toString();
			ASentence aSentence = new ASentence();
			aSentence.sentenceString = sentence;
			
			// tokenize and get the parse
			PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(new StringReader(sentence));
			List<Word> words = ptb.tokenize();
			String sentenceTokenized = StringUtils.join(words).trim();
			aSentence.sentenceTokenizedString = sentenceTokenized;
			aSentence.tokens = words;
			
			
			// parse sentence
			
			// get the gold standard nodes
			AnnotationSet nodesInSentence = markups.get(markup.getStartNode().getOffset(), markup.getEndNode().getOffset());
			aSentence.annotations = nodesInSentence;
			/*
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
			//AnnotationSet heads = nodesInSentence.get("head");
			*/
			
			
			this.sentenceHash.put(sentenceTokenized, aSentence);
		}
		
		System.out.println(markups.get("inside").size());
		
		return;
	}
}
