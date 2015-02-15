package utils;

import java.util.ArrayList;

import structure.Triple;

import edu.stanford.nlp.trees.Tree;

public final class Overlap {
	
	private Overlap(){
	}
	
	public static boolean subStringOverlap(String s1,String s2){
		if (s1.contains(s2) || s2.contains(s1))
			return true;
		
		return false;
	}
	
	public static boolean pureStringOverlap(String s1, String s2){
		
		return subStringOverlap(s1.replaceAll("[^a-zA-Z0-9]+", ""),s2.replaceAll("[^a-zA-Z0-9]+", ""));
	}
	
	public static boolean intervalOverlap(int s1, int t1, int s2, int t2){
		
		if ( (s1<=s2 && s2<=t1) || (s2<=s1 && s1<=t2) )
			return true;
		else
			return false;
	}
	
	public static boolean intervalContains(int s1, int t1, int s2, int t2){
		
		if ( (s1<=s2) && (t2<=t1) )
			return true;
		else
			return false;
	}
	
	public static Triple tripleListContains(Tree tree, ArrayList<Triple> triples){
		for (Triple triple:triples){
			if (triple.gfbf.equals(tree))
				return triple;
		}
		
		return null;
	}

}
