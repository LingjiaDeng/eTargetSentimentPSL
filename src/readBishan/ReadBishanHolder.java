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
	public HashMap<String,ArrayList<DirectNode>> sentenceHash;
	
	
	public ReadBishanHolder(String docId) throws IOException{
		this.filePath = Path.getRoot()+"Bishan_holder/allOutputs/"+docId;
		this.sentenceHash = new HashMap<String,ArrayList<DirectNode>>();
		
		Pattern pattern = Pattern.compile("([a-zA-Z0-9\\-_\\.]+\\\\[a-zA-Z0-9\\-_\\.]+)\\.bishan$");
		Matcher match = pattern.matcher(docId);
		if (match.find()){
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
		ArrayList<String> agents = new ArrayList<String>();
		ArrayList<String> agentTags = new ArrayList<String>();
		ArrayList<String> targets = new ArrayList<String>();
		ArrayList<String> targetTags = new ArrayList<String>();
		
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while ( (line = br.readLine()) != null){
			if (line.isEmpty()){
				if (opinions.size()!= 0){
					for (int opinionIndex=0;opinionIndex<opinions.size();opinionIndex++){
						DirectNode anno = new DirectNode();
						anno.sentence = sentence;
						anno.span = opinions.get(opinionIndex);
						
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
									if (anno.agent.isEmpty())
										anno.agent = agents.get(agentIndex);
									else
										anno.agent += " "+agents.get(agentIndex);
								}
							}
						}
						else
							anno.agent = "N/A";
						
						// add target
						if (targetNumbers.size()!=0){
							for (int targetIndex=0;targetIndex<targets.size();targetIndex++){
								if (targetNumbers.contains(targetTags.get(targetIndex).split("_")[2]))
									anno.targets.add(targets.get(targetIndex));
							}
						}
						
						// add the anno into directs
						directs.add(anno);
					}  // for each opinion
				}  
			
				sentence = "";
				opinions = new ArrayList<String>();
				opinionTags = new ArrayList<String>();
				agents = new ArrayList<String>();
				agentTags = new ArrayList<String>();
				targets = new ArrayList<String>();
				targetTags = new ArrayList<String>();
				
			}  // if line is empty
			else{
				String span = line.split("\t")[0];
				span = span.replace("-LRB-","").replace("-RRB-", "");
				span = span.replace("-LSB-","").replace("-RSB-", "");
				sentence += span+" ";
				
				// this is an opinion word, and it is new
				if ( !line.split("\t")[2].equals("O") && line.split("\t")[2].startsWith("B") ){
					if (line.contains("AGENT")){
						agents.add(span+" ");
						agentTags.add(line.split("\t")[2]);
					}
					else if (line.contains("DSE")){
						opinions.add(span+" ");
						opinionTags.add(line.split("\t")[2]);
					}
					else if (line.contains("TARGET")){
						targets.add(span+" ");
						targetTags.add(line.split("\t")[2]);
					}
				}
				// this is an opinion word, and its previous word is also an opinion word
				else if  ( !line.split("\t")[2].equals("O") && !line.split("\t")[2].startsWith("B") ){
					// a new opinion, which does not have O word between this and previous opinion
					if (line.contains("AGENT"))
						agents.set(agents.size()-1, agents.get(agents.size()-1)+span+" ");
					else if (line.contains("DSE"))
						opinions.set(opinions.size()-1, opinions.get(opinions.size()-1)+span+" ");
					else if (line.contains("TARGET"))
						targets.set(targets.size()-1, targets.get(targets.size()-1)+span+" ");
				}
			}  // else line is not empty
		}  // while each line
		
		br.close();
		fr.close();
		
		HashMap<String,ArrayList<DirectNode>> sentences = new HashMap<String,ArrayList<DirectNode>>();
		for (DirectNode d:directs){
			if (!sentences.keySet().contains(d.sentence))
				sentences.put(d.sentence, new ArrayList<DirectNode>());
			ArrayList<DirectNode> tmp = sentences.get(d.sentence);
			tmp.add(d);
			sentences.put(d.sentence, tmp);
		}  // for each directNode
		
		this.sentenceHash = sentences;
		
	}

}
