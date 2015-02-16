package structure;

import java.util.ArrayList;

import edu.stanford.nlp.trees.Tree;

public class Triple {
	public Tree gfbf;
	public ArrayList<Tree> agent;
	public ArrayList<Tree> theme;
	
	public Triple(){
		this.agent = new ArrayList<Tree>();
		this.theme = new ArrayList<Tree>();	
	}
	
	
	

}
