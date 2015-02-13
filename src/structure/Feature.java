package structure;

import java.util.HashSet;

public class Feature {
	public int inOpinionSpan;
	public int inTargetSpan;
	public int lengthOnConTree;
	public int lengthOnDep;
	//public int containsSthInDep;
	
	public HashSet<String> unigramCon;
	public HashSet<String> bigramCon;
	public HashSet<String> unigramDep;
	public HashSet<String> bigramDep;
	
	public int[] unigramConCount;
	public int[] bigramConCount;
	public int[] unigramDepCount;
	public int[] bigramDepCount;
	
	
	public Feature(){
		this.inOpinionSpan = 0;
		this.inTargetSpan = 0;
		this.lengthOnConTree = 0;
		this.lengthOnDep = 0;
		//this.containsSthInDep = 0;
		
		this.unigramCon = new HashSet<String>();
		this.bigramCon = new HashSet<String>();
		this.unigramDep = new HashSet<String>();;
		this.bigramDep = new HashSet<String>();
		
	}
	
	public void print(){
		System.out.println("uniCon: "+this.unigramCon);
		System.out.println(" biCon: "+this.bigramCon);
		System.out.println("uniDep: "+this.unigramDep);
		System.out.println(" biDep: "+this.bigramDep);
		System.out.println("------------");
	}

}
