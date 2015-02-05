package readBishan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Path;

public class ReadBishanHolder {
	public String filePath;
	public HashMap<Integer,ArrayList<DirectNode>> sentenceHash;
	
	
	public ReadBishanHolder(String docId) throws IOException{
		Pattern pattern = Pattern.compile("([a-zA-Z0-9\\-_\\.]+/[a-zA-Z0-9\\-_\\.]+)$");
		Matcher match = pattern.matcher(docId);
		if (match.find()){
			this.filePath = Path.getBishanRoot()+"Bishan_holder/allOutputs/"+docId.replace("/","\\")+".bishan";
			this.sentenceHash = new HashMap<Integer,ArrayList<DirectNode>>();
			File f = new File(this.filePath);
			run(f);
		}
		else{
			System.out.println("Invalid DocId:"+docId);
		}
	}
	
	private void run(File f) throws IOException{
		// initialize
		ArrayList<DirectNode> directs = new ArrayList<DirectNode>();
		String sentence = "";
		ArrayList<String> opinions = new ArrayList<String>();
		ArrayList<String> opinionTags = new ArrayList<String>();
		ArrayList<Integer> opinionStarts = new ArrayList<Integer>();
		ArrayList<String> agents = new ArrayList<String>();
		ArrayList<String> agentTags = new ArrayList<String>();
		ArrayList<Integer> agentStarts = new ArrayList<Integer>();
		ArrayList<String> targets = new ArrayList<String>();
		ArrayList<String> targetTags = new ArrayList<String>();
		ArrayList<Integer> targetStarts = new ArrayList<Integer>();
		int tokenIndex = -1;
		int sentenceIndex = -1;
		
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while ( (line = br.readLine()) != null){
			if (line.isEmpty()){
				sentenceIndex++;
				this.sentenceHash.put(sentenceIndex, new ArrayList<DirectNode>());
				if (opinions.size()!= 0){
					for (int opinionIndex=0;opinionIndex<opinions.size();opinionIndex++){
						DirectNode anno = new DirectNode();
						anno.sentenceIndex = sentenceIndex;
						anno.sentence = sentence.trim();
						anno.opinionSpan = opinions.get(opinionIndex).trim();
						anno.opinionStart = opinionStarts.get(opinionIndex);
						
						// find the number of corresponding agent and target
						String opinionTag = opinionTags.get(opinionIndex);
						String[] tags = opinionTag.split("_");
						ArrayList<String> agentNumbers = new ArrayList<String>();
						ArrayList<String> targetNumbers = new ArrayList<String>();
						
						for (int i=2;i<tags.length;i++){
							if (Integer.parseInt(tags[i])<0)
								agentNumbers.add(tags[i]);
							else
								targetNumbers.add(tags[i]);
						}
						
						// add agent
						if (agentNumbers.size()!= 0){
							for (int agentIndex = 0;agentIndex<agents.size();agentIndex++){
								if (agentNumbers.contains(agentTags.get(agentIndex).split("_")[2])){
									if (anno.agent.isEmpty()){
										anno.agent = agents.get(agentIndex).trim();
										anno.agentStart = agentStarts.get(agentIndex);
									}
									else
										anno.agent += " "+agents.get(agentIndex);
										anno.agent = anno.agent.trim();
										anno.agentStart = agentStarts.get(agentIndex);
								}
							}
						}
						//else
						//	anno.agent = "N/A";
						
						// add target
						if (targetNumbers.size()!=0){
							for (int targetIndex=0;targetIndex<targets.size();targetIndex++){
								if (targetNumbers.contains(targetTags.get(targetIndex).split("_")[2])){
									anno.targets.add(targets.get(targetIndex).trim());
									anno.targetStarts.add(targetStarts.get(targetIndex));
								}
							}
						}
						
						// add the anno into directs
						directs.add(anno);
					}  // for each opinion
				}  
			
				sentence = "";
				opinions = new ArrayList<String>();
				opinionTags = new ArrayList<String>();
				opinionStarts = new ArrayList<Integer>();
				agents = new ArrayList<String>();
				agentTags = new ArrayList<String>();
				agentStarts  = new ArrayList<Integer>();;
				targets = new ArrayList<String>();
				targetTags = new ArrayList<String>();
				targetStarts  = new ArrayList<Integer>();
				tokenIndex = -1;
				
			}  // if line is empty
			else{
				tokenIndex++;
				String span = line.split("\t")[0];
				//span = span.replace("-LRB-","").replace("-RRB-", "");
				//span = span.replace("-LSB-","").replace("-RSB-", "");
				sentence += span + " ";
				
				// this is an opinion word, and it is new
				if ( !line.split("\t")[2].equals("O") && line.split("\t")[2].startsWith("B") ){
					if (line.split("\t")[2].contains("AGENT")){
						agents.add(span+" ");
						agentTags.add(line.split("\t")[2]);
						agentStarts.add(tokenIndex);
					}
					else if (line.split("\t")[2].contains("DSE")){
						opinions.add(span+" ");
						opinionTags.add(line.split("\t")[2]);
						opinionStarts.add(tokenIndex);
					}
					else if (line.split("\t")[2].contains("TARGET")){
						targets.add(span+" ");
						targetTags.add(line.split("\t")[2]);
						targetStarts.add(tokenIndex);
					}
				}
				// this is an opinion word, and its previous word is also an opinion word
				else if  ( !line.split("\t")[2].equals("O") && !line.split("\t")[2].startsWith("B") ){
					// a new opinion, which does not have O word between this and previous opinion
					if (line.split("\t")[2].contains("AGENT"))
						agents.set(agents.size()-1, agents.get(agents.size()-1)+span+" ");
					else if (line.split("\t")[2].contains("DSE"))
						opinions.set(opinions.size()-1, opinions.get(opinions.size()-1)+span+" ");
					else if (line.split("\t")[2].contains("TARGET"))
						targets.set(targets.size()-1, targets.get(targets.size()-1)+span+" ");
				}
			}  // else line is not empty
		}  // while each line
		
		br.close();
		fr.close();
		
		for (DirectNode d:directs){
			ArrayList<DirectNode> tmp = this.sentenceHash.get(d.sentenceIndex);
			tmp.add(d);
			this.sentenceHash.put(d.sentenceIndex, tmp);
		}  // for each directNode
		
		//System.out.println("holders: "+sentences.size());
		
	}

}
