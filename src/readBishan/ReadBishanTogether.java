package readBishan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import structure.DirectNode;
import utils.Overlap;

public class ReadBishanTogether {
	public HashMap<Integer,ArrayList<DirectNode>> sentenceHash; 
	
	
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
		
		HashMap<Integer,ArrayList<DirectNode>>  sentimentSens = s.sentenceHash;
		HashMap<Integer,ArrayList<DirectNode>>  holderSens = h.sentenceHash;
		
		if (sentimentSens.size()==0 || holderSens.size() == 0)
			return;
		
		for (Integer sentenceIndex:sentimentSens.keySet()){
			ArrayList<DirectNode> sentiments  = new ArrayList<DirectNode>(sentimentSens.get(sentenceIndex));
			DirectNode tmpNode = sentiments.get(0);
			
			if (holderSens.containsKey(sentenceIndex)){
				ArrayList<DirectNode> holders = holderSens.get(sentenceIndex);
				
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
							ArrayList<DirectNode> tmp = sentimentSens.get(sentenceIndex);
							tmp.set(i, sentiment);
							sentimentSens.put(sentenceIndex, tmp);
						}  // if overlap
					}
				}  // for each sentiment DirectNode
				
				// add the remaining unused holder DirectNode
				for (DirectNode holder:holders){
					if (!holder.overlapped){
						ArrayList<DirectNode> tmp = sentimentSens.get(holder.sentenceIndex);
						tmp.add(holder);
						sentimentSens.put(holder.sentenceIndex, tmp);
					}
				}  // for each unused holder DirectNode
				
				// filter out the neutral sentiments
				ArrayList<DirectNode> tmp = new ArrayList<DirectNode>();
				for (DirectNode direct:sentimentSens.get(sentenceIndex)){
					if (!direct.polarity.equals("neutral") || direct.opinionStart == -1)
						tmp.add(direct);
				}
				if (tmp.isEmpty() || tmp.size() == 0){
					tmp.add(tmpNode);
				}
				sentimentSens.put(sentenceIndex, tmp);	
			}  // if has sentence
		}  // for each sentence
		
		this.sentenceHash = sentimentSens;
		System.out.println("# sentence from Bishan: "+sentimentSens.size());
	}

}
