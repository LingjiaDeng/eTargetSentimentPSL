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

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import structure.ASentence;
import structure.DirectNode;
import utils.Overlap;
import utils.Syntax;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.FeatureMap;
import gate.util.GateException;

public class ReadGATE {
	private Document doc;
	public HashMap<Integer, ArrayList<DirectNode>> bishanSentenceHash;
	public HashMap<Integer,ASentence> sentenceHash;
	//private static LexicalizedParser lp;
	private static TreebankLanguagePack tlp;
	private static GrammaticalStructureFactory gsf;
	private static StanfordCoreNLP pipeline;
	 
	
	public ReadGATE(String docId) throws MalformedURLException, GateException{
		IntiateGATE.go(docId);
		this.doc = IntiateGATE.doc;
		this.sentenceHash = new HashMap<Integer,ASentence>();
		this.bishanSentenceHash = new HashMap<Integer,ArrayList<DirectNode>>();
		//this.lp =  LexicalizedParser.loadModel(
			//	"/afs/cs.pitt.edu/usr0/lid29/Documents/RESOURCES/stanford-corenlp-full-2013-06-20/englishPCFG.ser.gz","-maxLength", "80");
		this.tlp = new PennTreebankLanguagePack();
		this.gsf = tlp.grammaticalStructureFactory();
		
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
	    this.pipeline = new StanfordCoreNLP(props);
		
		readGATE();
	}
	
	
	public ArrayList<ASentence> addBishanResults(HashMap<Integer,ArrayList<DirectNode>> bishans,ArrayList<ASentence> sentences) throws GateException{
		this.bishanSentenceHash = bishans;
		return addBishanResults(sentences);
	}
	
	private ArrayList<ASentence> addBishanResults(ArrayList<ASentence> sentences){
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
			
			// get syntax info (tokens, lemma, POS, parseTree)
			aSentence.sentenceSyntax = Syntax.parse(sentence, this.pipeline);
			// dependency parser
			GrammaticalStructure gs = this.gsf.newGrammaticalStructure(aSentence.sentenceSyntax.get(TreeAnnotation.class));
			aSentence.tdl = gs.typedDependencies();
			
			/*
			// tokenize and get the parse
			PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(new StringReader(sentence));
			List<Word> words = ptb.tokenize();
			
			String sentenceTokenized = StringUtils.join(words).trim();
			aSentence.sentenceTokenizedString = sentenceTokenized;
			aSentence.tokens = words;
			
			// parse sentence
			Tree parseTree = this.lp.apply(words);
			aSentence.parseTree = parseTree;
			
			// dependency parser
			GrammaticalStructure gs = this.gsf.newGrammaticalStructure(parseTree);
			aSentence.tdl = gs.typedDependencies();
			*/
			
			// get the gold standard nodes
			AnnotationSet nodesInSentence = markups.get(markup.getStartNode().getOffset(), markup.getEndNode().getOffset());
			aSentence.annotations = nodesInSentence;
			
			
			this.sentenceHash.put(aSentence.sentenceIndex, aSentence);
		}
		
		System.out.println("# sentence from GATE: "+markups.get("inside").size());
		
		return;
	}
}
