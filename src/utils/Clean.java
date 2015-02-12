package utils;

import java.util.ArrayList;

import edu.stanford.nlp.trees.Tree;

public class Clean {
	
	public Clean(){
		
	}
	
	public static ArrayList<Tree> removeDuplicate(ArrayList<Tree> oldList){
		ArrayList<Tree> newList = new ArrayList<Tree>();
		for (Tree tree:oldList){
			if (!newList.contains(tree))
				newList.add(tree);
		}
		
		return newList;
	}
	
	public static ArrayList<Tree> removeNonHead(ArrayList<Tree> oldList, Tree root){
		ArrayList<Tree> newList = new ArrayList<Tree>();
		
		for (Tree tree:oldList){
			if ( tree.parent(root).label().value().startsWith("NN") ||
					tree.parent(root).label().value().startsWith("VB") ||
					tree.parent(root).label().value().startsWith("PRP") )
				newList.add(tree);
		}
		
		return newList;
	}

}
