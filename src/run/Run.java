package run;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gate.util.GateException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import readBishan.*;
import readGATE.ReadGATE;
import structure.ASentence;
import structure.Doc;
import utils.GFBF;
import utils.Path;
import utils.Clean;
import utils.Statistics;
import utils.Syntax;

public class Run {
	public static void main(String[] args) throws IOException, GateException{
		int gsNum = 0;
		int autoNum = 0;
		int corretNum = 0;
		ArrayList<Doc> docs = new ArrayList<Doc>();
		GFBF gfbfLexicon = new GFBF();
		Clean remove = new Clean();
		Statistics statistics = new Statistics();
		
		Syntax parser = new Syntax();
		parser.multiSentenceNum = 0;
		
		File f = new File(Path.getDoclistFile());
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		
		int index = -1;
		
		String docId = "";
		while ( (docId=br.readLine())!= null ){
			index++;
			
			if (index > 0  )
				break;
			System.out.println("............"+index+"............."+docId);
			Doc doc = new Doc(docId);
			doc.read();
			doc.parse();
			doc.generateETarget();
			doc.countFeatures();
			
			doc.statistics();
			
			gsNum += doc.gsNum;
			autoNum += doc.autoNum;
			corretNum += doc.corretNum;
			
			docs.add(doc);
			
		}
		//String docId = "20020206/20.31.05-16359";
		//String docId = "temp_fbis/20.58.47-19000";
		
		br.close();
		fr.close();
		
		
		
		for (Doc doc:docs){
			//doc.generateFeatures();
			//doc.writerFeatures();
		}
		
		
		
		
		
		System.out.println("========== performance on corpus ========");
		double recall = corretNum*1.0/gsNum;
		double precision = corretNum*1.0/autoNum;
		System.out.println("recall: "+recall);
		System.out.println("precision: "+precision);
		System.out.println("F-measure:"+(2*recall*precision)/(recall+precision));
		
		System.out.println("multiSentence: "+parser.multiSentenceNum);
		
		
		
	}
}
