package readBishan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import structure.DirectNode;
import utils.Path;

public class ReadBishanSentiment {
	public String filePath;
	public HashMap<Integer,ArrayList<DirectNode>> sentenceHash;
	
	
	public ReadBishanSentiment(String docId) throws IOException{
		Pattern pattern = Pattern.compile("([a-zA-Z0-9\\-_\\.]+/[a-zA-Z0-9\\-_\\.]+)$");
		Matcher match = pattern.matcher(docId);
		if (match.find()){
			this.filePath = Path.getBishanRoot()+"Bishan_sentiment/allOutputs/"+docId.replace("/","\\")+".bishan";
			
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
		ArrayList<Integer> opinionStarts = new ArrayList<Integer>();
		ArrayList<String> polarities = new ArrayList<String>();
		boolean flagNewOpinion = true;
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
						anno.polarity = polarities.get(opinionIndex);
												
						// add the anno into directs
						directs.add(anno);
					}  // for each opinion
				}
				else{
					DirectNode anno = new DirectNode();
					anno.sentenceIndex = sentenceIndex;
					anno.sentence = sentence.trim();
					directs.add(anno);
				}
			
				sentence = "";
				opinions = new ArrayList<String>();
				opinionStarts = new ArrayList<Integer>();
				polarities = new ArrayList<String>();
				flagNewOpinion = true;
				tokenIndex = -1;
			}  // if line is empty
			else{
				tokenIndex++;
				String span = line.split("\t")[0];
				//span = span.replace("-LRB-","").replace("-RRB-", "");
				//span = span.replace("-LSB-","").replace("-RSB-", "");
				sentence += span+" ";
				
				// this is an opinion word, and it is new
				if ( !line.split("\t")[2].equals("O") && flagNewOpinion ){
					flagNewOpinion = false;
					opinions.add(span+" ");
					opinionStarts.add(tokenIndex);
					polarities.add(line.split("\t")[2].split("_")[0]);
				}
				// this is an opinion word, and its previous word is also an opinion word
				else if  ( !line.split("\t")[2].equals("O") && !flagNewOpinion ){
					// a new opinion, which does not have O word between this and previous opinion
					if ( !line.split("\t")[2].split("_")[0].equals(polarities.get(polarities.size()-1)) ){
						opinions.add(span+" ");
						opinionStarts.add(tokenIndex);
						polarities.add(line.split("\t")[2].split("_")[0]);
						flagNewOpinion = false;
					}
					else
						opinions.set(opinions.size()-1, opinions.get(opinions.size()-1)+span+" ");
				}
				else
					flagNewOpinion = true;
			}  // else line is not empty
		}  // while each line
		
		br.close();
		fr.close();
		
		for (DirectNode d:directs){
			ArrayList<DirectNode> tmp = this.sentenceHash.get(d.sentenceIndex);
			tmp.add(d);
			this.sentenceHash.put(d.sentenceIndex, tmp);
		}  // for each directNode
		
		//System.out.println("sentiments: "+sentences.size());
	}

}
