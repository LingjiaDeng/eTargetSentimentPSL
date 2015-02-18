package utils;

import java.util.HashSet;

public class Statistics {
	public static HashSet<String> unigramCon;
	public static HashSet<String> bigramCon;
	public static HashSet<String> unigramDep;
	public static HashSet<String> bigramDep;
	
	public static int gsNum;
	public static int autoNum;
	public static int correctNum;
	public static int autoNumAfterPSL;
	public static int correctNumAfterPSL;
	
	public static int directNodeNum;
	
	public static double PSLthreshold;
	
	public Statistics(){
		Statistics.unigramCon = new HashSet<String>();
		Statistics.bigramCon = new HashSet<String>();
		Statistics.unigramDep = new HashSet<String>();
		Statistics.bigramDep = new HashSet<String>();
		
		Statistics.gsNum = 0;
		Statistics.autoNum = 0;
		Statistics.correctNum = 0;
		Statistics.autoNumAfterPSL = 0;
		Statistics.correctNumAfterPSL = 0;
		
		Statistics.directNodeNum = 0;
		
		Statistics.PSLthreshold = 0.0;
		
		
	}

}
