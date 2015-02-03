package run;

import gate.util.GateException;

import java.io.IOException;
import java.util.ArrayList;

import readBishan.*;
import readGATE.ReadETarget;

public class Run {
	public static void main(String[] args) throws IOException, GateException{
		String docId = "temp_fbis/21.10.31-12974";
		
		// read bishan's result (with holders and polarities)
		ReadBishanTogether bishan = new ReadBishanTogether(docId);
		/*
		for (String sentence:s=bishan.sentenceHash.keySet()){
			System.out.println("----------");
			System.out.println(sentence);
			System.out.println("----------");
			ArrayList<DirectNode> nodes = s.sentenceHash.get(sentence);
			
			for (DirectNode node:nodes){
				System.out.println(node.agentStart+"\t"+node.agent);
				System.out.println(node.opinionStart+"\t"+node.opinionSpan);
				System.out.println(node.polarity);
				System.out.print(node.targetStarts);
				System.out.println(node.targets);
			}
		}*/
		
		// read gold standard annotations
		ReadETarget gate = new ReadETarget(docId);
		
		ArrayList<ASentence> sentences = new ArrayList<ASentence>();
		sentences = gate.addBishanResults(bishan.sentenceHash,sentences);
		System.out.println(sentences.size());
		
		for (ASentence a:sentences){
			System.out.println(a.sentenceString);
		}
		
	}
}
