package readGATE;

import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.Gate;
import gate.Utils;
import gate.util.GateException;

import java.io.File;
import java.net.MalformedURLException;

public class ReadMPQA3FinalVersion {
	
	public ReadMPQA3FinalVersion() throws GateException, MalformedURLException{
		// now we read the original xml file annotated by previous MPQA annotators
		File f = new File("");
		Gate.init();			
		Document doc = (Document) 
				Factory.createResource("gate.corpora.DocumentImpl", 
				  Utils.featureMap(gate.Document.DOCUMENT_URL_PARAMETER_NAME, 
				f.toURI().toURL(),
				gate.Document.DOCUMENT_ENCODING_PARAMETER_NAME, "UTF-8")); 
			
		doc.setMarkupAware(new Boolean(false));
		DocumentContent content = doc.getContent();
		AnnotationSet markups = doc.getAnnotations("MPQA");     // get all annotations in the MPQA annotation set
		
		
		
		
	}

}
