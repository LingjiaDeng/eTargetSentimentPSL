package structure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import utils.Path;

public class Feature {
	public boolean isCorrect;
	public int inOpinionSpan;
	public int inTargetSpan;
	public int lengthOnConTree;
	public int lengthOnDep;
	
	public HashSet<String> unigramCon;
	public HashSet<String> bigramCon;
	public HashSet<String> unigramDep;
	public HashSet<String> bigramDep;
	
	public int[] unigramConCount;
	public int[] bigramConCount;
	public int[] unigramDepCount;
	public int[] bigramDepCount;
	
	public double isGF;
	public double isBF;
	public HashSet<String> unigramConGFBF;
	public HashSet<String> bigramConGFBF;
	public HashSet<String> unigramDepGFBF;
	public HashSet<String> bigramDepGFBF;
	
	public int[] unigramConCountGFBF;
	public int[] bigramConCountGFBF;
	public int[] unigramDepCountGFBF;
	public int[] bigramDepCountGFBF;
	
	
	public Feature(){
		this.isCorrect = false;
		this.inOpinionSpan = 0;
		this.inTargetSpan = 0;
		this.lengthOnConTree = -1;
		this.lengthOnDep = -1;
		//this.containsSthInDep = 0;
		
		this.unigramCon = new HashSet<String>();
		this.bigramCon = new HashSet<String>();
		this.unigramDep = new HashSet<String>();;
		this.bigramDep = new HashSet<String>();
		
		this.isGF = 0;
		this.isBF = 0;
		this.unigramConGFBF = new HashSet<String>();
		this.bigramConGFBF = new HashSet<String>();
		this.unigramDepGFBF = new HashSet<String>();
		this.bigramDepGFBF = new HashSet<String>();
		
	}
	
	public void print(){
		System.out.println(this.isCorrect);
		System.out.println("inOpinionSpan: "+this.inOpinionSpan);
		System.out.println("inTargetSpan: "+this.inTargetSpan);
		System.out.println("length-Con: "+this.lengthOnConTree);
		System.out.println("length-Dep: "+this.lengthOnDep);
		System.out.println("uniCon: "+this.unigramCon);
		System.out.println(" biCon: "+this.bigramCon);
		System.out.println("uniDep: "+this.unigramDep);
		System.out.println(" biDep: "+this.bigramDep);
		System.out.println("isGF: "+this.isGF);
		System.out.println("isBF: "+this.isBF);
		System.out.println("uniConGFBF: "+this.unigramConGFBF);
		System.out.println(" biConGFBF: "+this.bigramConGFBF);
		System.out.println("uniDepGFBF: "+this.unigramDepGFBF);
		System.out.println(" biDepGFBF: "+this.bigramDepGFBF);
		System.out.println("------------");
		
		
	}
	
	public void write(BufferedWriter bw) throws IOException{
		
		
		String str = "";
		if (this.isCorrect)
			str += "1 ";
		else
			str += "-1 ";
		
		if (this.inOpinionSpan != 0)
			str += "1:1 ";
		
		if (this.inTargetSpan != 0)
			str += "2:1 ";
		
		int startIndex = 3;
		for (int i=0;i<this.unigramConCount.length;i++){
			if (this.unigramConCount[i] == 0)
				continue;
			else
				str += (startIndex+i)+":1 ";
		}
		
		startIndex = startIndex+this.unigramConCount.length;
		for (int i=0;i<this.bigramConCount.length;i++){
			if (this.bigramConCount[i] == 0)
				continue;
			else
				str += (startIndex+i)+":1 ";
		}
		
		startIndex = startIndex+this.bigramConCount.length;
		for (int i=0;i<this.unigramDepCount.length;i++){
			if (this.unigramDepCount[i] == 0)
				continue;
			else
				str += (startIndex+i)+":1 ";
		}
		
		startIndex = startIndex+this.unigramDepCount.length;
		for (int i=0;i<this.bigramDepCount.length;i++){
			if (this.bigramDepCount[i] == 0)
				continue;
			else
				str += (startIndex+i)+":1 ";
		}
		
		startIndex = startIndex+this.bigramDepCount.length;
		if (this.isGF != 0)
			str += startIndex+":"+this.isGF+" ";
		
		startIndex += 1;
		if (this.isBF != 0)
			str += startIndex+":"+this.isBF+" ";
		
		startIndex += 1;
		for (int i=0;i<this.unigramConCountGFBF.length;i++){
			if (this.unigramConCountGFBF[i] == 0)
				continue;
			else
				str += (startIndex+i)+":1 ";
		}
		
		startIndex = startIndex+this.unigramConCountGFBF.length;
		for (int i=0;i<this.bigramConCountGFBF.length;i++){
			if (this.bigramConCountGFBF[i] == 0)
				continue;
			else
				str += (startIndex+i)+":1 ";
		}
		
		startIndex = startIndex+this.bigramConCountGFBF.length;
		for (int i=0;i<this.unigramDepCountGFBF.length;i++){
			if (this.unigramDepCountGFBF[i] == 0)
				continue;
			else
				str += (startIndex+i)+":1 ";
		}
		
		startIndex = startIndex+this.unigramDepCountGFBF.length;
		for (int i=0;i<this.bigramDepCountGFBF.length;i++){
			if (this.bigramDepCountGFBF[i] == 0)
				continue;
			else
				str += (startIndex+i)+":1 ";
		}
		
		
		
		
		bw.write(str);
		bw.newLine();
	}

}
