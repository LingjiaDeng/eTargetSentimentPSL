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

public class ReadBishanSentiment {
	public String filePath;
	public HashMap<String,ArrayList<DirectNode>> sentenceHash;
	
	
	public ReadBishanSentiment(String docId) throws IOException{
		this.filePath = Path.getRoot()+"Bishan_sentiment/allOutputs/"+docId;
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
		ArrayList<String> polarities = new ArrayList<String>();
		boolean flagNewOpinion = false;
		
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
						anno.polarity = polarities.get(opinionIndex);
												
						// add the anno into directs
						directs.add(anno);
					}  // for each opinion
				}  
			
				sentence = "";
				opinions = new ArrayList<String>();
				polarities = new ArrayList<String>();
				flagNewOpinion = true;
			}  // if line is empty
			else{
				String span = line.split("\t")[0];
				span = span.replace("-LRB-","").replace("-RRB-", "");
				span = span.replace("-LSB-","").replace("-RSB-", "");
				sentence += span+" ";
				
				// this is an opinion word, and it is new
				if ( !line.split("\t")[2].equals("O") && flagNewOpinion ){
					flagNewOpinion = false;
					opinions.add(span+" ");
					polarities.add(line.split("\t")[2].split("_")[0]);
				}
				// this is an opinion word, and its previous word is also an opinion word
				else if  ( !line.split("\t")[2].equals("O") && !flagNewOpinion ){
					// a new opinion, which does not have O word between this and previous opinion
					if ( !line.split("\t")[2].split("_")[0].equals(polarities.get(polarities.size()-1)) ){
						opinions.add(span+" ");
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
