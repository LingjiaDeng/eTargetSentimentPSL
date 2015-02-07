package utils;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public final class Syntax {
	private Syntax(){
		
	}
	
	public static CoreMap parse(String sentence, StanfordCoreNLP pipeline){
        Annotation document = new Annotation(sentence);
        pipeline.annotate(document);
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        CoreMap sentenceCoreMap = sentences.get(0);
        
        return sentenceCoreMap;
	}
}
