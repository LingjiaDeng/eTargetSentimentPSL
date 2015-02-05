package readGATE;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.Tree;
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
	public HashMap<Integer, ArrayList<DirectNode>> bishanSentenceHash;
	public HashMap<Integer,ASentence> sentenceHash;
	public static LexicalizedParser lp;
	 
	
	public ReadETarget(String docId) throws MalformedURLException, GateException{
		IntiateGATE.go(docId);
		this.doc = IntiateGATE.doc;
		this.sentenceHash = new HashMap<Integer,ASentence>();
		this.bishanSentenceHash = new HashMap<Integer,ArrayList<DirectNode>>();
		this.lp =  LexicalizedParser.loadModel(
				"/afs/cs.pitt.edu/usr0/lid29/Documents/RESOURCES/stanford-corenlp-full-2013-06-20/englishPCFG.ser.gz","-maxLength", "80");
		
		readGATE();
	}
	
	
	public ArrayList<ASentence> addBishanResults(HashMap<Integer,ArrayList<DirectNode>> bishans,ArrayList<ASentence> sentences) throws GateException{
		this.bishanSentenceHash = bishans;
		return mergeBishanIntoGATE(sentences);
	}
	
	private ArrayList<ASentence> mergeBishanIntoGATE(ArrayList<ASentence> sentences){
		for (Integer sentenceIndex:this.sentenceHash.keySet()){
			ASentence aSentence = this.sentenceHash.get(sentenceIndex);
			if (this.bishanSentenceHash.containsKey(sentenceIndex)){
				aSentence.bishanDirects = this.bishanSentenceHash.get(sentenceIndex);	
			}
			sentences.add(aSentence);
		}
		
		return sentences;
	}
	
	
	private void readGATE() throws GateException{
		DocumentContent content = this.doc.getContent();
		
		AnnotationSet markups = this.doc.getAnnotations("MPQA");
		markups.addAll(this.doc.getAnnotations());
		
		// this list stores the sorted startnode offests, we can use this list to find the index of sentence in the document
		ArrayList<Integer> sortedStartNode = new ArrayList<Integer>();
		AnnotationSet insides = markups.get("inside");
		for (Annotation inside:insides){
			sortedStartNode.add(Integer.parseInt(inside.getStartNode().getOffset().toString()));
		}
		 Collections.sort(sortedStartNode);
		
		
		for (Annotation markup:markups.get("inside")){
			String sentence = content.getContent(markup.getStartNode().getOffset(), markup.getEndNode().getOffset()).toString();
			
			ASentence aSentence = new ASentence();
			aSentence.sentenceString = sentence;
			aSentence.sentenceIndex = sortedStartNode.indexOf(Integer.parseInt(markup.getStartNode().getOffset().toString()));
			
			if (aSentence.sentenceIndex != 16)
				continue;
			
			
			// tokenize and get the parse
			PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(new StringReader(sentence));
			List<Word> words = ptb.tokenize();
			
			String sentenceTokenized = StringUtils.join(words).trim();
			aSentence.sentenceTokenizedString = sentenceTokenized;
			aSentence.tokens = words;
			
			// parse sentence
			Tree parseTree = this.lp.apply(words);
			aSentence.parseTree = parseTree;
			/*
			System.out.println(sentence);
			System.out.println(parseTree.constituents());
			Constituent c = (Constituent) parseTree.constituents().toArray()[0];
			System.out.println("...."+c.start()+":"+c.end());
			*/
			
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
			
			
			this.sentenceHash.put(aSentence.sentenceIndex, aSentence);
		}
		
		System.out.println("# sentence from GATE: "+markups.get("inside").size());
		
		return;
	}
}
