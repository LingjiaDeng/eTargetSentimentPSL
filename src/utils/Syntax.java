package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import structure.ASentence;


import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencePositionAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class Syntax {
	public static StanfordCoreNLP pipeline;
	//public CoreMap sentenceCoreMap;
	//public Map<Integer, CorefChain> corefHash;
	//public List<CoreMap> sentences;
	//public Map<Integer, CorefChain> corefHash;
	//public String sentenceIds;
	private static TreebankLanguagePack tlp;
	private static GrammaticalStructureFactory gsf;
	
	public Syntax(){
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    this.pipeline = new StanfordCoreNLP(props);
	    this.tlp = new PennTreebankLanguagePack();
		this.gsf = tlp.grammaticalStructureFactory();
	}
	
	public void parseDoc(String docContent){
		Annotation document = new Annotation(docContent);
	    
        this.pipeline.annotate(document);
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        Map<Integer, CorefChain> corefHash = document.get(CorefChainAnnotation.class);
        List<CoreMap> tmps = document.get(SentencesAnnotation.class);
        
        /*
        for (Integer chainId: corefHash.keySet()){
        	System.out.println(chainId);
        	System.out.println(corefHash.get(chainId));
        	CorefChain chain = corefHash.get(chainId);
        }
        */
        
        
        return;
	}
	
	public void parseSentence(String sentence){
        Annotation document = new Annotation(sentence);
        this.pipeline.annotate(document);
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        Map<Integer, CorefChain> corefHash = document.get(CorefChainAnnotation.class);
        
        CoreMap sentenceCoreMap = sentences.get(0);
        
        return;
	}
	
	public void parseSentence(ASentence sentence){
		String span = sentence.sentenceString;
        Annotation document = new Annotation(span);
        this.pipeline.annotate(document);
        
        // syntax
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        sentence.sentenceSyntax = sentences.get(0);
        // add dependency parser
        GrammaticalStructure gs = this.gsf.newGrammaticalStructure(sentence.sentenceSyntax.get(TreeAnnotation.class));
        sentence.tdl = gs.typedDependencies();
        
        // translate the co-references from stanford parser into my representation
        Map<Integer, CorefChain> corefHash = document.get(CorefChainAnnotation.class);
        for (Integer chainId:corefHash.keySet()){
        	CorefChain chain = corefHash.get(chainId);
        	if (chain.getMentionsInTextualOrder().size() < 2)
        		continue;
        	
        	ArrayList<Tree> cmLeaves = new ArrayList<Tree>();
        	for (CorefMention cm:chain.getMentionsInTextualOrder()){
        		int index = cm.startIndex;
        		Tree cmLeaf = sentence.sentenceSyntax.get(TreeAnnotation.class).getLeaves().get(index);
        		cmLeaves.add(cmLeaf);
        	}
        	
        	for (Tree cmLeaf:cmLeaves){
        		if (sentence.corefHash.containsKey(cmLeaf)){
        			ArrayList<Tree> tmp = sentence.corefHash.get(cmLeaf);
        			tmp.addAll(cmLeaves);
        			sentence.corefHash.put(cmLeaf, tmp);
        		}
        	}
        	
        }
        
        return;
	}
	
	
}
