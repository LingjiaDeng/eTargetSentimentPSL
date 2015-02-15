package utils;

import java.io.IOException;
import java.util.ArrayList;

import structure.Triple;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;

public final class Rule {
	
	private Rule(){
		
	}
	
	// if indexOfLeaf == dep, then judge gov
	public static boolean gfbfRulesJudgeGov(TypedDependency td, int indexOfLeaf, String govWord) throws IOException{
		if (td.gov().index() == 0)
			return false;
		
		//String govWord = this.sentenceSyntax.get(TokensAnnotation.class).get(td.gov().index()-1).lemma();
		
		if ( td.dep().index()==indexOfLeaf && td.reln().toString().equals("nsubj") )   // sentiment(agent) -> sentiment(event)
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().equals("dobj") && GFBF.isGF(govWord))  // sentiment(theme) -> sentiment(event): gov must be a goodfor 
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().equals("conj"))  //  ``and'' 
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().equals("ccomp")  &&  GFBF.isGF(govWord))  //  sentiment(event) -> sentiment(retainer): gov must be a retainer 
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().equals("xcomp")  &&  GFBF.isGF(govWord))  //  sentiment(event) -> sentiment(retainer): gov must be a retainer 
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().contains("mod"))   //  find object of a modifier
			return true;
		
		return false;
	}
	
	// if indexOfLeaf == gov, then judge dep
	public static boolean gfbfRulesJudgeDep(TypedDependency td, int indexOfLeaf, String govWord) throws IOException{
		if (td.gov().index() == 0)
			return false;
		
		//String govWord = this.sentenceSyntax.get(TokensAnnotation.class).get(td.gov().index()-1).lemma();
		
		if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("nsubj") )   // sentiment(event) -> sentiment(agent)
			return true;
		else if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("dobj")  &&  GFBF.isGF(govWord))   //  sentiment(event) -> sentiment(theme): gov must be a goodfor
			return true;
		else if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("conj"))   // ``and''
			return true;
		else if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("ccomp")  &&  GFBF.isBF(govWord))    // sentiment(retainer) -> sentiment(event): gov must be a retainer
			return true;
		else if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("xcomp")  &&  GFBF.isBF(govWord))    // sentiment(retainer) -> sentiment(event): gov must be a retainer
			return true;
		
		
		return false;
	}
	
	public static void makeItATriple(TypedDependency td, ArrayList<Tree> leaves, ArrayList<Triple> triples){
		Tree govLeaf = leaves.get(td.gov().index()-1);
		Tree depLeaf = leaves.get(td.dep().index()-1);
		String relation = td.reln().toString();
		
		Triple govInList = Overlap.tripleListContains(govLeaf, triples);
		Triple depInList = Overlap.tripleListContains(depLeaf, triples);
		
		if (relation.equals("nsubj")){
			if (govInList != null){
				govInList.agent = depLeaf;
			}
		}
		else if (relation.equals("dobj")){
			if (govInList != null){
				govInList.theme = depLeaf;
			}
		}
		else if (relation.equals("conj") || relation.equals("ccomp") || relation.equals("xcomp")){
			if (govInList == null){
				Triple newTriple = new Triple();
				newTriple.gfbf = govLeaf;
				triples.add(newTriple);
			}
			if (depInList == null){
				Triple newTriple = new Triple();
				newTriple.gfbf = depLeaf;
				triples.add(newTriple);
			}
		}
		
		return;
	}
	
	// if indexOfLeaf == dep, then judge gov
	public static boolean targetRulesJudgeGov(TypedDependency td, int indexOfLeaf, String govWord) throws IOException{
		if (td.gov().index() == 0)
			return false;
		
		//String govWord = this.sentenceSyntax.get(TokensAnnotation.class).get(td.gov().index()-1).lemma();
		
		/*
		if ( td.dep().index()==indexOfLeaf && td.reln().toString().equals("nsubj") )   
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().equals("dobj")  )  
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().equals("conj")) 
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().equals("ccomp")  )   
			return true;
		*/
		if ( td.dep().index()==indexOfLeaf && td.reln().toString().contains("mod"))   //  find object of a modifier
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().contains("prep_of"))  
			return true;
		else if ( td.dep().index()==indexOfLeaf && td.reln().toString().contains("prep_as"))   
			return true;
		return false;
	}
	
	// if indexOfLeaf == gov, then judge dep
	public static boolean targetRulesJudgeDep(TypedDependency td, int indexOfLeaf, String govWord) throws IOException{
		if (td.gov().index() == 0)
			return false;
		
		//String govWord = this.sentenceSyntax.get(TokensAnnotation.class).get(td.gov().index()-1).lemma();
		
		if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("dobj") )   //  find object of a verb
			return true;
		if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("ccomp")  )    // find ccomp of a verb
			return true;
		if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("xcomp")  )    // find xcomp of a verb
			return true;
		else if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("prep_of")  )   
			return true;
		else if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("prep_as")  )    
			return true;
		
		return false;
	}
	
	public static boolean targetRulesJudgeGovNounOfAdj(TypedDependency td, int indexOfLeaf, String govWord) throws IOException{
		if (td.gov().index() == 0)
			return false;
		
		if ( td.dep().index()==indexOfLeaf && td.reln().toString().contains("mod") )   
			return true; 
		
		return false;
	}
	
	public static boolean targetRulesJudgeDepNounOfAdj(TypedDependency td, int indexOfLeaf, String govWord) throws IOException{
		if (td.gov().index() == 0)
			return false;
		
		if ( td.gov().index()==indexOfLeaf && td.reln().toString().equals("nsubj") )   
			return true; 
		
		return false;
	}
	
	

}
