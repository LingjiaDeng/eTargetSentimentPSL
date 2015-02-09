package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GFBF {
	private static ArrayList<String> GFLexicon;
	private static ArrayList<String> BFLexicon;
	
	public GFBF() throws IOException{
		this.GFLexicon = new ArrayList<String>();
		this.BFLexicon = new ArrayList<String>();
		intializeLexicon();
	}
	
	private static void intializeLexicon() throws IOException{
		File f = new File(Path.getGFBFLexiconFile());
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		
		String line = "";
		while ( (line = br.readLine()) != null ){
			String[] items = line.split("\t");
			ArrayList<String> words = new ArrayList<String>();
			for (String word:items[2].split(",")){
				if (!word.contains("_"))
					words.add(word);
			}
			
			if (items[1].equals("+Effect"))
				GFLexicon.addAll(words);
			else if (items[1].equals("-Effect"))
				BFLexicon.addAll(words);
		}
		
		br.close();
		fr.close();
	}
	
	public static boolean isGF(String word) throws IOException{
		if (GFLexicon.isEmpty() || BFLexicon.isEmpty())
			intializeLexicon();
		
		if (GFLexicon.contains(word))
			return true;
			
		return false;
	}
	
	public static boolean isBF(String word) throws IOException{
		if (GFLexicon.isEmpty() || BFLexicon.isEmpty())
			intializeLexicon();
		
		if (BFLexicon.contains(word))
			return true;
		
		return false;
	}

}
