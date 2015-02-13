package structure;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.AnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import gate.Annotation;
import gate.AnnotationSet;
import gate.util.GateException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import readBishan.ReadBishanTogether;
import readGATE.ReadGATE;
import utils.Overlap;
import utils.Path;
import utils.Syntax;

public class Doc {
	public ArrayList<ASentence> sentences;
	public String docId;
	
	public int gsNum;
	public int autoNum;
	public int corretNum;
	private static Syntax parse;
	//private List<CoreMap> sentencesSyntax;
	
	public HashSet<String> unigramCon;
	public HashSet<String> bigramCon;
	public HashSet<String> unigramDep;
	public HashSet<String> bigramDep;
	
	public Doc(String docId) throws IOException, GateException{
		this.docId = docId;
		this.sentences = new ArrayList<ASentence>();
		this.gsNum = 0;
		this.autoNum = 0;
		this.corretNum = 0;
		this.parse = new Syntax();
		
		this.unigramCon = new HashSet<String>();
		this.bigramCon = new HashSet<String>();
		this.unigramDep = new HashSet<String>();
		this.bigramDep = new HashSet<String>();
	}
	
	public void parseAsAWholeDoc() throws IOException{
		File f = new File(Path.getDocRoot()+docId);
		
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		
		String docSpan = "";
		String line = "";
		while ( (line = br.readLine()) != null){
			docSpan += line;
		}
		
		br.close();
		fr.close();
		
		//this.parse.parseDoc(docSpan);
		//this.sentencesSyntax = this.parse.sentences;
		
		return;
	}
	
	public void parse() throws IOException, GateException{
		System.out.println("Parsing...");
		if (this.sentences.isEmpty())
			read();
		
		for (ASentence sentence:this.sentences){
			this.parse.parseSentence(sentence);
			
		}
	}
	
	public void read() throws IOException, GateException{
		ReadBishanTogether bishan = new ReadBishanTogether(this.docId);
		ReadGATE gate = new ReadGATE(docId);
		this.sentences = gate.addBishanResults(bishan.sentenceHash,this.sentences);
		
		System.out.println("after merging: "+sentences.size());
	}
	
	
	public void generateETarget() throws IOException, GateException{
		for (ASentence aSentence:this.sentences){
			if (aSentence.multiSentenceFlag)
				continue;
			
			if (aSentence.sentenceSyntax == null)
				parse();
			
			aSentence.preprocessing();
			
			System.out.println("=====  sentence ======");
			System.out.println(aSentence.sentenceTokenizedString);
			aSentence.alignGoldStandard();
			aSentence.addETarget();
			aSentence.addMoreByStanford();
			aSentence.addMoreByGFBF();
			aSentence.lastFiltering();
		}  // each aSentence
	}
	
	public void countFeatureNGram(){
		for (ASentence aSentence:this.sentences){
			if (aSentence.multiSentenceFlag)
				continue;
			
			if (aSentence.sentenceSyntax == null)
				parse();
			
			SemanticGraph depGraph = coreMap.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
        		Set<IndexedWord> words = depGraph.getLeafVertices();
			
			for (DirectNode directNode:aSentence.bishanDirects){
				Tree root = directNode.root;
				if (directNode.eTargetsGS.isEmpty() || directNode.eTargetsGS.size() == 0)
					continue;
				
				for (Tree target:directNode.targets){
					int targetIndex = directNode.targets.indexOf(target);
					// calculate the counts on constituency parser
					// find the list of path
					ArrayList<Tree> path = root.pathToNode(directNode.opinionTree, target);
					if (path.isEmpty()|| path.size()==0)
						continue;
					directNode.features.get(targetIndex).lengthOnConTree = path.size();
					for (int i=0;i<path.size();i++){
						Tree treeOnPath = path.get(i);
						HashSet<String> tmp = directNode.features.get(targetIndex).unigramCon;
						tmp.add(treeOnPath.label().value());
						this.unigramCon.add(treeOnPath.label().value());
						if (i==0)
							continue;
						
						tmp = directNode.features.get(targetIndex).bigramCon;
						tmp.add(path.get(i-1).label().value()+"-"+treeOnPath.label().value());
						this.bigramCon.add(path.get(i-1).label().value()+"-"+treeOnPath.label().value());
					}
					
					// calculate the counts on dependency parser
					int indexOfTarget = root.getLeaves().indexOf(target);
					IndexedWord targetWord = words.get(indexOfTarget);
					IndexedWord opinionWord = words.get(directNode.opinionStart);
					
					List<SemanticGraphEdge> path = depGraph.getShortestUndirectedPathEdges(targetWord, opinionWord);
		        		directNode.features.get(i).lengthOnDep = path.size();
		        		for (int i=0;i<path.size();i++){
		        			HashSet<String> tmp = directNode.features.get(targetIndex).unigramDep;
						tmp.add(path.get(i).getRelation().getLongName());
		        			this.unigramDep.add(path.get(i).getRelation().getLongName());
		        			if (i==0)
		        				continue;
		        				
		        			tmp = directNode.features.get(targetIndex).unigramCon;
						tmp.add(path.get(i-1).getRelation().getLongName()+"-"+path.get(i).getRelation().getLongName());
		        			this.bigramDep.add(path.get(i).getRelation().getLongName()+"-"+path.get(i).getRelation().getLongName());
		        			
		        		}
					
				}  // each target
			}  // each direct node
		} // each sentence
	}
	
	public void generateFeatures() throws IOException, GateException{
		for (ASentence aSentence:this.sentences){
			if (aSentence.multiSentenceFlag)
				continue;
			
			if (aSentence.sentenceSyntax == null)
				parse();
			
			for (DirectNode directNode:aSentence.bishanDirects){
				if (directNode.eTargetsGS.isEmpty() || directNode.eTargetsGS.size() == 0)
					continue;
				
				
				directNode.countItself();
				directNode.countCon(this.unigramCon, this.bigramCon);
				directNode.countDep(this.unigramDep, this.bigramDep);
				directNode.countGFBF();
				
			}
		}
	}
	
	public void statistics(){
		for (ASentence aSentence:this.sentences){
			for (DirectNode directNode:aSentence.bishanDirects){
				if (directNode.eTargetsGS.isEmpty() || directNode.eTargetsGS.size() == 0)
					continue;
				
				this.gsNum += directNode.eTargetsGS.size();
				this.autoNum += directNode.eTargets.size();
				for (Tree eTarget:directNode.eTargetsGS){
					if (directNode.eTargets.contains(eTarget))
						this.corretNum += 1;
				}
			}
		}
		
		System.out.println("----- statistics -----");
		double recall = this.corretNum*1.0/this.gsNum;
		double precision = this.corretNum*1.0/this.autoNum;
		System.out.println("recall: "+recall);
		System.out.println("precision: "+precision);
		System.out.println("F-measure:"+(2*recall*precision)/(recall+precision));
	}
	
	/*
	 * alignSentenceWithStanfordParser
	public void alignSentenceWithStanfordSyntax() throws IOException, GateException{
		if (this.sentencesSyntax.isEmpty()){
			parseDoc();
		}
		if (this.sentences.isEmpty()){
			read();
		}
		
		for (ASentence sentence:this.sentences){
			AnnotationSet insides = sentence.annotations.get("inside");
			int sentenceStart = -1;
			int sentenceEnd = -1;
			for (Annotation inside:insides){
				sentenceStart = inside.getStartNode().getOffset().intValue();
				sentenceEnd = inside.getEndNode().getOffset().intValue();
			}
			
			System.out.println(sentenceStart+" "+sentenceEnd);
			System.out.println(sentence.sentenceTokenizedString);
			
			for (CoreMap sentenceSyntax:this.sentencesSyntax){
				System.out.print(sentenceSyntax.get(CharacterOffsetBeginAnnotation.class)-1);
				System.out.print(" ");
				System.out.println(sentenceSyntax.get(CharacterOffsetEndAnnotation.class)-1);
				System.out.println(sentenceSyntax.toString());
				if (sentenceSyntax.get(CharacterOffsetBeginAnnotation.class)-1 == sentenceStart
						&& sentenceSyntax.get(CharacterOffsetEndAnnotation.class)-1 == sentenceEnd){
					sentence.sentenceSyntax = sentenceSyntax;
				}
			}
		}
		
		
		return;
	}
	*/
	
	
}
