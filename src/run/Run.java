package run;

import gate.util.GateException;

import java.io.IOException;
import java.util.ArrayList;

import readBishan.*;
import readGATE.ReadETarget;

public class Run {
	public static void main(String[] args) throws IOException, GateException{
		String docId = "temp_fbis/21.10.31-12974";
		
		// read bishan's result
		ReadBishanTogether t = new ReadBishanTogether(docId);
		
		// add sources and targets into bishan's result
		
		// read gold standard
		ReadETarget g = new ReadETarget(docId);
		g.addBishanResults(t.sentenceHash);
		
		for (String sentence:t.sentenceHash.keySet()){
			ArrayList<DirectNode> nodes = t.sentenceHash.get(sentence);
			for (DirectNode node:nodes){
				System.out.println("---");
				System.out.println(node.agent);
				System.out.println(node.span);
				System.out.println(node.targets);
				
			}
		}
	}
}
