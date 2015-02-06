package utils;

import java.util.ArrayList;
import java.util.List;


import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;
import gate.AnnotationSet;

public class ASentence {
	public String sentenceString;
	public String sentenceTokenizedString;
	public int sentenceIndex;
	public List<Word> tokens;
	public Tree parseTree;
	public AnnotationSet annotations;
	public ArrayList<DirectNode> bishanDirects;
	
	public ASentence(){
		this.sentenceString = "";
		this.sentenceTokenizedString = "";
		this.sentenceIndex = -1;
		this.tokens = new ArrayList<Word>();
		this.bishanDirects = new ArrayList<DirectNode>();
	}
	
	
	
	

}
