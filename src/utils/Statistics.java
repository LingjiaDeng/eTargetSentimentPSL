package utils;

import java.util.HashSet;

public class Statistics {
	public static HashSet<String> unigramCon;
	public static HashSet<String> bigramCon;
	public static HashSet<String> unigramDep;
	public static HashSet<String> bigramDep;
	
	public Statistics(){
		this.unigramCon = new HashSet<String>();
		this.bigramCon = new HashSet<String>();
		this.unigramDep = new HashSet<String>();;
		this.bigramDep = new HashSet<String>();;
		
	}

}
