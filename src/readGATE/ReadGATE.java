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
import utils.WTF;
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
	//private static TreebankLanguagePack tlp;
	//private static GrammaticalStructureFactory gsf;
	//private static StanfordCoreNLP pipeline;
	
	
	public ReadGATE(String docId) throws MalformedURLException, GateException{
		
		
		IntiateGATE.go(docId);
		this.doc = IntiateGATE.doc;
		this.sentenceHash = new HashMap<Integer,ASentence>();
		this.bishanSentenceHash = new HashMap<Integer,ArrayList<DirectNode>>();
		//this.lp =  LexicalizedParser.loadModel(
			//	"/afs/cs.pitt.edu/usr0/lid29/Documents/RESOURCES/stanford-corenlp-full-2013-06-20/englishPCFG.ser.gz","-maxLength", "80");
		//this.tlp = new PennTreebankLanguagePack();
		//this.gsf = tlp.grammaticalStructureFactory();
		
		
		
		readGATE();
	}
	
	
	public ArrayList<ASentence> addBishanResults(HashMap<Integer,ArrayList<DirectNode>> bishans,ArrayList<ASentence> sentences) throws GateException{
		this.bishanSentenceHash = bishans;
		
		for (Integer sentenceIndex:this.sentenceHash.keySet()){
			ASentence aSentence = this.sentenceHash.get(sentenceIndex);
			
			if (this.bishanSentenceHash.containsKey(sentenceIndex)){
				ArrayList<DirectNode> bishanAlls = this.bishanSentenceHash.get(sentenceIndex);
				
				aSentence.sentenceTokenizedString = bishanAlls.get(0).sentence;
				
				if (bishanAlls.get(0).opinionStart == -1)
					aSentence.bishanDirects = new ArrayList<DirectNode>();
				else{
					aSentence.bishanDirects = new ArrayList<DirectNode>();
					for (DirectNode directNode:bishanAlls){
						ArrayList<Annotation> subjAnnos = findMatchingSubjMarkup(aSentence.sentenceTokenizedString,
								directNode, aSentence.annotations);
						//if (!subjAnnos.isEmpty() && subjAnnos.size() ==1){
						//	aSentence.bishanDirects.add(directNode);
						//}
						if (judgeSingleSourceSinglePolarity(subjAnnos)){
							aSentence.bishanDirects.add(directNode);
						}
						//aSentence.bishanDirects.add(directNode);
					}
				}  // else
			}   //  if has sentence
			
			sentences.add(aSentence);
		}
		
		return sentences;
	}
	
	private boolean judgeSingleSourceSinglePolarity(ArrayList<Annotation> subjAnnos){
		int pos = 0;
		int neg = 0;
		String source = "";
		boolean multiSourceflag = false;
		for (Annotation subjAnno:subjAnnos){
			String polarity = "";
			if (subjAnno.getFeatures().containsKey("polarity")){
				polarity = subjAnno.getFeatures().get("polarity").toString();
			}
			else if (subjAnno.getFeatures().containsKey("attitude-type")){
				polarity = subjAnno.getFeatures().get("attitude-type").toString();
			}
			
			if (polarity.contains("pos"))
				pos++;
			else if (polarity.contains("neg"))
				neg++;
			
			if (subjAnno.getFeatures().containsKey("nested-source")){
				if (source.isEmpty())
					source = subjAnno.getFeatures().get("nested-source").toString();
				else
					if (!source.equals(subjAnno.getFeatures().get("nested-source").toString()))
						multiSourceflag = true;
			}
		}
		if ( (pos > 0 && neg > 0) || (multiSourceflag) )
			return false;
		else
			return true;
	}
	
	private ArrayList<Annotation> findMatchingSubjMarkup(String tokenizedSentence, DirectNode direct, AnnotationSet markups) throws GateException{
		ArrayList<Annotation> subjs  = new ArrayList<Annotation>();
		
		String opinionSpan = direct.opinionSpan;
		int opinionStart = tokenizedSentence.indexOf(opinionSpan);
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
				//System.out.println("!!!"+this.content.getContent(subjAnno.getStartNode().getOffset(), subjAnno.getEndNode().getOffset()).toString());
				subjs.add(subjAnno);
			}
		}
		
		return subjs;
	}
	
	/*
	public void alignSentenceWithStanfordSyntax(List<CoreMap> sentencesSytax){
		for (Integer sentenceIndex:this.sentenceHash.keySet()){
			ASentence aSentence = this.sentenceHash.get(sentenceIndex);
			
			for (CoreMap sentenceSyntax:sentencesSytax){
			}
		}
		
		return;
	}
	*/
	
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
			aSentence.content = content;
			
			/*
			if (aSentence.sentenceIndex != 1)
				continue;
			
			*/
			
			
			/*
			Syntax stanfordParser = new Syntax();
			stanfordParser.parseSentence(sentence);
			// get syntax info (tokens, lemma, POS, parseTree)
			aSentence.sentenceSyntax = stanfordParser.sentenceCoreMap;
			// dependency parser
			GrammaticalStructure gs = this.gsf.newGrammaticalStructure(aSentence.sentenceSyntax.get(TreeAnnotation.class));
			aSentence.tdl = gs.typedDependencies();
			aSentence.corefHash = stanfordParser.corefHash;
			*/
			
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
