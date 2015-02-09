package utils;

import java.io.IOException;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
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
		
		return false;
	}
	
	

}
