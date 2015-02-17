package structure;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.AnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import gate.Annotation;
import gate.AnnotationSet;
import gate.util.GateException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import readBishan.ReadBishanTogether;
import readGATE.ReadGATE;
import utils.GFBF;
import utils.Overlap;
import utils.Path;
import utils.Statistics;
import utils.Syntax;

public class Doc {
	public ArrayList<ASentence> sentences;
	public String docId;
	
	public int gsNum;
	public int autoNum;
	public int corretNum;
	private static Syntax parse;
	//private List<CoreMap> sentencesSyntax;
	
	//public HashSet<String> unigramCon;
	//public HashSet<String> bigramCon;
	//public HashSet<String> unigramDep;
	//public HashSet<String> bigramDep;
	
	public Doc(String docId) throws IOException, GateException{
		this.docId = docId;
		this.sentences = new ArrayList<ASentence>();
		this.gsNum = 0;
		this.autoNum = 0;
		this.corretNum = 0;
		this.parse = new Syntax();
		
		//this.unigramCon = new HashSet<String>();
		//this.bigramCon = new HashSet<String>();
		//this.unigramDep = new HashSet<String>();
		//this.bigramDep = new HashSet<String>();
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
			sentence.docId = this.docId;
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
			//aSentence.addAllHeadAsETarget();
			aSentence.addETarget();
			aSentence.addMoreByStanford();
			aSentence.addMoreByGFBF();
			aSentence.lastFiltering();
		}  // each aSentence
	}
	
	private void initializeFeature(DirectNode directNode){
		for (Tree eTarget:directNode.eTargets)
			directNode.features.add(new Feature());
		
		return;
	}
	
	public void countFeatures() throws IOException, GateException{
		for (ASentence aSentence:this.sentences){
			if (aSentence.multiSentenceFlag)
				continue;
			
			if (aSentence.sentenceSyntax == null)
				parse();
			
			List<CoreLabel> tokens = aSentence.sentenceSyntax.get(TokensAnnotation.class);
			SemanticGraph depGraph = aSentence.sentenceSyntax.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        	Set<IndexedWord> words = depGraph.vertexSet();
			
			for (DirectNode directNode:aSentence.bishanDirects){
				if (directNode.features.isEmpty() || directNode.features.size() == 0)
					initializeFeature(directNode);
				
				Tree root = directNode.root;
				if (directNode.eTargetsGS.isEmpty() || directNode.eTargetsGS.size() == 0)
					continue;
				
				for (Tree eTarget:directNode.eTargets){
					int eTargetIndex = directNode.eTargets.indexOf(eTarget);
					int indexOfLeaf = root.getLeaves().indexOf(eTarget);
					
					// is GFBF
					int gfSenseCount = 0;
					int bfSenseCount = 0;
					String eTargetLemma = tokens.get(indexOfLeaf).lemma();
					if ( GFBF.isGF(eTargetLemma) ){
						gfSenseCount = GFBF.countGF(eTargetLemma);
					}
					if ( GFBF.isBF(eTargetLemma) ){
						bfSenseCount = GFBF.countBF(eTargetLemma);
					}
					if ( gfSenseCount+bfSenseCount != 0){
						directNode.features.get(eTargetIndex).isGF = (gfSenseCount+1)/(gfSenseCount+bfSenseCount+1);
						directNode.features.get(eTargetIndex).isBF = (bfSenseCount+1)/(gfSenseCount+bfSenseCount+1);
					}
					
							
					// GFBF-target: calculate the counts on constituency parser
					for (int targetIndex = 0;targetIndex<directNode.targets.size();targetIndex++){
						int targetStart = directNode.targetStarts.get(targetIndex);
						String targetLemma = tokens.get(targetStart).lemma();
						
						if ( !(GFBF.isGF(eTargetLemma) || GFBF.isBF(eTargetLemma) ||
								GFBF.isGF(targetLemma) || GFBF.isBF(targetLemma)) )
							continue;
						
						
						Tree targetTree = directNode.targetTrees.get(targetIndex);
						List<Tree> path = root.pathNodeToNode(targetTree, eTarget.parent(root));
						if (!path.isEmpty() && path.size() != 0){
							for (int i=0;i<path.size();i++){
								Tree treeOnPath = path.get(i);
								
								HashSet<String> tmp = directNode.features.get(eTargetIndex).unigramConGFBF;
								tmp.add(treeOnPath.label().value());
								if (i==0)
									continue;
								
								tmp = directNode.features.get(eTargetIndex).bigramConGFBF;
								tmp.add(path.get(i-1).label().value()+"-"+treeOnPath.label().value());
							}
						}  // if path is not empty
					}  // each target
					
					// opinion: calculate the counts on constituency parser
					List<Tree> pathCon = root.pathNodeToNode(directNode.opinionTree, eTarget.parent(root));
					if (!pathCon.isEmpty() && pathCon.size()!=0){
						directNode.features.get(eTargetIndex).lengthOnConTree = pathCon.size();
						for (int i=0;i<pathCon.size();i++){
							Tree treeOnPath = pathCon.get(i);
							
							HashSet<String> tmp = directNode.features.get(eTargetIndex).unigramCon;
							tmp.add(treeOnPath.label().value());
							Statistics.unigramCon.add(treeOnPath.label().value());
							if (i==0)
								continue;
							
							tmp = directNode.features.get(eTargetIndex).bigramCon;
							tmp.add(pathCon.get(i-1).label().value()+"-"+treeOnPath.label().value());
							Statistics.bigramCon.add(pathCon.get(i-1).label().value()+"-"+treeOnPath.label().value());
						}
					}  // if
					
					// calculate the counts on dependency parser
					IndexedWord eTargetWord = null;
					IndexedWord opinionWord = null;
					for (int ig=0;ig<words.size();ig++){
						IndexedWord word = (IndexedWord) words.toArray()[ig];
						if (word.index()-1==indexOfLeaf)
							eTargetWord = word;
						if (word.index()-1==directNode.opinionStart)
							opinionWord = word;
					}
					
					// GFBF-target: calculate the counts on dependency parser
					for (int targetIndex = 0;targetIndex<directNode.targets.size();targetIndex++){
						int targetStart = directNode.targetStarts.get(targetIndex);
						String targetLemma = tokens.get(targetStart).lemma();
						if ( !(GFBF.isGF(eTargetLemma) || GFBF.isBF(eTargetLemma) ||
								GFBF.isGF(targetLemma) || GFBF.isBF(targetLemma)) )
							continue;
						
						IndexedWord targetWord = null;
						for (int ig=0;ig<words.size();ig++){
							IndexedWord word = (IndexedWord) words.toArray()[ig];
							if (word.index()-1==targetStart)
								targetWord = word;
						}
						
						if (eTargetWord != null && targetWord != null){
							List<SemanticGraphEdge> pathDep = depGraph.getShortestUndirectedPathEdges(eTargetWord, targetWord);
				        	directNode.features.get(eTargetIndex).lengthOnDep = pathDep.size();
				        	for (int i=0;i<pathDep.size();i++){
				        		HashSet<String> tmp = directNode.features.get(eTargetIndex).unigramDepGFBF;
								tmp.add(pathDep.get(i).getRelation().getShortName());
				        		if (i==0)
				        			continue;
				        				
				        		tmp = directNode.features.get(eTargetIndex).bigramDepGFBF;
								tmp.add(pathDep.get(i-1).getRelation().getShortName()+"-"+pathDep.get(i).getRelation().getShortName());	
				        	}
						}  // if
					}  // each target
					
					// opinion: calculate the counts on dependency parser
					if (eTargetWord !=  null && opinionWord !=  null){
						List<SemanticGraphEdge> pathDep = depGraph.getShortestUndirectedPathEdges(eTargetWord, opinionWord);
			        	directNode.features.get(eTargetIndex).lengthOnDep = pathDep.size();
			        	for (int i=0;i<pathDep.size();i++){
			        		HashSet<String> tmp = directNode.features.get(eTargetIndex).unigramDep;
							tmp.add(pathDep.get(i).getRelation().getShortName());
							Statistics.unigramDep.add(pathDep.get(i).getRelation().getShortName());
			        		if (i==0)
			        			continue;
			        				
			        		tmp = directNode.features.get(eTargetIndex).bigramDep;
							tmp.add(pathDep.get(i-1).getRelation().getShortName()+"-"+pathDep.get(i).getRelation().getShortName());
							Statistics.bigramDep.add(pathDep.get(i).getRelation().getShortName()+"-"+pathDep.get(i).getRelation().getShortName());
			        			
			        	}
					}  // if
				}  // each eTarget
			}  // each direct node
		} // each sentence
	}
	
	public void generateFeatures() throws IOException, GateException{
		if (Statistics.unigramCon.isEmpty() || Statistics.unigramCon.size() == 0 ||
				Statistics.bigramCon.isEmpty() || Statistics.bigramCon.size() == 0 ||
				Statistics.unigramDep.isEmpty() || Statistics.unigramDep.size() == 0 ||
				Statistics.bigramDep.isEmpty() || Statistics.bigramDep.size() == 0)
			countFeatures();
		
		for (ASentence aSentence:this.sentences){
			if (aSentence.multiSentenceFlag)
				continue;
			
			if (aSentence.sentenceSyntax == null)
				parse();
			
			System.out.println("====== sentence ======");
			for (DirectNode directNode:aSentence.bishanDirects){
				if (directNode.eTargetsGS.isEmpty() || directNode.eTargetsGS.size() == 0)
					continue;
				
				directNode.countFeatures();
				
				System.out.println(directNode.opinionSpan);
				System.out.println(directNode.eTargetsGS);
				
				for (int i=0;i<directNode.eTargets.size();i++){
					System.out.println(directNode.eTargets.get(i).nodeString());
					Feature feature = directNode.features.get(i);
					feature.print();
				}
				
			}
		}
		
		return;
	}
	
	public void writerFeatures() throws IOException, GateException{
		File f = new File(Path.getFeatureRoot()+docId+"/"+"svmFeatures.all");
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		
		File tf = new File(Path.getFeatureRoot()+docId+"/"+"svmFeatures.etargetId");
		FileWriter tfw = new FileWriter(tf);
		BufferedWriter tbw = new BufferedWriter(tfw);
		
		File df = new File(Path.getFeatureRoot()+docId+"/"+"svmFeatures.docId");
		FileWriter dfw = new FileWriter(df);
		BufferedWriter dbw = new BufferedWriter(dfw);
		
		
		if (Statistics.unigramCon.isEmpty() || Statistics.unigramCon.size() == 0 ||
				Statistics.bigramCon.isEmpty() || Statistics.bigramCon.size() == 0 ||
				Statistics.unigramDep.isEmpty() || Statistics.unigramDep.size() == 0 ||
				Statistics.bigramDep.isEmpty() || Statistics.bigramDep.size() == 0)
			countFeatures();
		
		for (ASentence aSentence:this.sentences){
			if (aSentence.multiSentenceFlag)
				continue;
			
			if (aSentence.sentenceSyntax == null)
				parse();
			
			for (DirectNode directNode:aSentence.bishanDirects){
				if (directNode.eTargetsGS.isEmpty() || directNode.eTargetsGS.size() == 0)
					continue;
				
				directNode.countFeatures();
				
				
				for (int i=0;i<directNode.eTargets.size();i++){
					Feature feature = directNode.features.get(i);
					//feature.print();
					feature.write(bw);
					Integer id = directNode.root.getLeaves().indexOf(directNode.eTargets.get(i));
					tbw.write(String.valueOf(aSentence.sentenceIndex));
					tbw.write("\t");
					tbw.write(String.valueOf(-1*directNode.opinionStart));
					tbw.write("\t");
					tbw.write(String.valueOf(id));
					tbw.newLine();
					
					dbw.write(this.docId);
					dbw.newLine();
				}
				
			}
		}
		
		bw.close();
		fw.close();
		
		tbw.close();
		tfw.close();
		
		dbw.close();
		dfw.close();
	}
	
	
	
	public void statistics(){
		for (ASentence aSentence:this.sentences){
			for (DirectNode directNode:aSentence.bishanDirects){
				if (directNode.eTargetsGS.isEmpty() || directNode.eTargetsGS.size() == 0)
					continue;
				
				Statistics.gsNum += directNode.eTargetsGS.size();
				this.gsNum += directNode.eTargetsGS.size();
				Statistics.autoNum += directNode.eTargets.size();
				this.autoNum += directNode.eTargets.size();
				for (Tree eTarget:directNode.eTargetsGS){
					if (directNode.eTargets.contains(eTarget)){
						this.corretNum += 1;
						Statistics.correctNum += 1;
					}
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
	
	public void writeForPSL() throws IOException{
		System.out.println(" ===== write into PSL files =====");
		
		File f = new File(Path.getPSLRoot()+this.docId+"/"+"svmFeatures.etargetId");
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		ArrayList<String> ids = new ArrayList<String>();
		String line = "";
		while ( (line=br.readLine()) != null ){
			ids.add(line);
		}
		br.close();
		fr.close();
		
		f = new File(Path.getPSLRoot()+this.docId+"/"+"svmFeatures.output");
		fr = new FileReader(f);
		br = new BufferedReader(fr);
		ArrayList<Double> scores = new ArrayList<Double>();
		line = "";
		while ( (line=br.readLine()) != null ){
			scores.add(Double.parseDouble(line));
		}
		br.close();
		fr.close();
		
		for (ASentence aSentence:this.sentences){
			/*
			 * write targets output from SVM here
			 */
			HashMap<Integer, HashMap<Integer,Double>> targets = new HashMap<Integer, HashMap<Integer, Double>>();
			for (int l=0;l<ids.size();l++){
				if (Integer.parseInt(ids.get(l).split("\t")[0]) != aSentence.sentenceIndex)
						continue;
				
				int directNodeId = Integer.parseInt(ids.get(l).split("\t")[1]);
				int etargetId = Integer.parseInt(ids.get(l).split("\t")[2]);
				Double score = scores.get(l);
				
				if (targets.containsKey(directNodeId) ){
					HashMap<Integer, Double> tmp = targets.get(directNodeId);
					tmp.put(etargetId, score);
					targets.put(directNodeId, tmp);
				}
				else{
					HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
					tmp.put(etargetId, score);
					targets.put(directNodeId, tmp);
				}
			}
			
			
			/*
			 * write the other components
			 */
			aSentence.writeForPSL(targets);
		}
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
