package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GFBF {
	private static HashSet<String> GFLexicon;
	private static HashSet<String> BFLexicon;
	private static HashMap<String,Integer> GFCount;
	private static HashMap<String,Integer> BFCount;
	
	public GFBF() throws IOException{
		this.GFLexicon = new HashSet<String>();
		this.BFLexicon = new HashSet<String>();
		this.GFCount = new HashMap<String, Integer>();
		this.BFCount = new HashMap<String, Integer>();
		
		intializeLexicon();
	}
	
	private static void intializeLexicon() throws IOException{
		File f = new File(Path.getGFBFLexiconFile());
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		
		String line = "";
		while ( (line = br.readLine()) != null ){
			String[] items = line.split("\t");
			HashSet<String> words = new HashSet<String>();
			for (String word:items[2].split(",")){
				if (!word.contains("_"))
					words.add(word);
				if ( items[1].equals("+Effect") && GFCount.keySet().contains(word) ){
					GFCount.put(word, GFCount.get(word)+1);
				}
				if ( items[1].equals("+Effect") && !GFCount.keySet().contains(word) ) {
					GFCount.put(word, 1);
				}
				if ( items[1].equals("-Effect") && BFCount.keySet().contains(word) ){
					BFCount.put(word, BFCount.get(word)+1);
				}
				if ( items[1].equals("-Effect") && !BFCount.keySet().contains(word) ){
					BFCount.put(word, 1);
				}
			}   // each word
			
			if (items[1].equals("+Effect")){
				GFLexicon.addAll(words);
			}
			else if (items[1].equals("-Effect")){
				BFLexicon.addAll(words);
			}
		}
		
		br.close();
		fr.close();
	}
	
	public static int countGF(String word) throws IOException{
		if (GFLexicon.isEmpty() || BFLexicon.isEmpty())
			intializeLexicon();
		
		int count = 0;
		
		if (GFLexicon.contains(word))
			count = GFCount.get(word);
		
		return count;
	}
	
	public static int countBF(String word) throws IOException{
		if (GFLexicon.isEmpty() || BFLexicon.isEmpty())
			intializeLexicon();
		
		int count = 0;
		
		if (BFLexicon.contains(word))
			count = BFCount.get(word);
		
		return count;
	}
	
	public static boolean isGF(String word) throws IOException{
		if (GFLexicon.isEmpty() || BFLexicon.isEmpty())
			intializeLexicon();
		
		if (GFLexicon.contains(word) && !BFLexicon.contains(word))
			return true;
			
		return false;
	}
	
	public static boolean isBF(String word) throws IOException{
		if (GFLexicon.isEmpty() || BFLexicon.isEmpty())
			intializeLexicon();
		
		if (BFLexicon.contains(word) || !GFLexicon.contains(word))
			return true;
		
		return false;
	}

}
