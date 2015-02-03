package readBishan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Overlap;

public class ReadBishanTogether {
	public HashMap<String,ArrayList<DirectNode>> sentenceHash; 
	
	
	public ReadBishanTogether(String docId) throws IOException{
		Pattern pattern = Pattern.compile("([a-zA-Z0-9\\-_\\.]+/[a-zA-Z0-9\\-_\\.]+)$");
		Matcher match = pattern.matcher(docId);
		if (match.find()){
			run(docId);
		}
		else{
			System.out.println("Invalid DocId:"+docId);
		}
		
	}
	
	private void run(String docId) throws IOException{
		ReadBishanSentiment s = new ReadBishanSentiment(docId);
		ReadBishanHolder h = new ReadBishanHolder(docId);
		
		HashMap<String,ArrayList<DirectNode>>  sentimentSens = s.sentenceHash;
		HashMap<String,ArrayList<DirectNode>>  holderSens = h.sentenceHash;
		
		if (sentimentSens.size()==0 || holderSens.size() == 0)
			return;
		
		for (String sentence:sentimentSens.keySet()){
			ArrayList<DirectNode> sentiments  = new ArrayList<DirectNode>(sentimentSens.get(sentence));
			if (holderSens.containsKey(sentence)){
				ArrayList<DirectNode> holders = holderSens.get(sentence);
				
				// match the agent and target
				for (int i=0;i<sentiments.size();i++){
					DirectNode sentiment = sentiments.get(i);
					for (DirectNode holder:holders){
						if ( Overlap.intervalOverlap(holder.opinionStart,holder.opinionStart+holder.opinionSpan.split(" ").length,sentiment.opinionStart,sentiment.opinionStart+sentiment.opinionSpan.split(" ").length) ){;
							holder.overlapped = true;
							sentiment.agent = holder.agent;
							sentiment.agentStart = holder.agentStart;
							sentiment.targets = holder.targets;
							sentiment.targetStarts = holder.targetStarts;
							// update in the original sentimentSens
							ArrayList<DirectNode> tmp = sentimentSens.get(sentence);
							tmp.set(i, sentiment);
							sentimentSens.put(sentence, tmp);
						}  // if overlap
					}
				}  // for each sentiment DirectNode
				
				// add the remaining unused holder DirectNode
				for (DirectNode holder:holders){
					if (!holder.overlapped){
						ArrayList<DirectNode> tmp = sentimentSens.get(holder.sentence);
						tmp.add(holder);
						sentimentSens.put(holder.sentence, tmp);
					}
				}  // for each unused holder DirectNode
			}  // if has sentence
		}  // for each sentence
		
		this.sentenceHash = sentimentSens;
		System.out.println("sentences:"+sentimentSens.size());
	}

}
