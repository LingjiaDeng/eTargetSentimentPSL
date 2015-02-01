package readGATE;

import java.io.File;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Path;

import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.Utils;
import gate.util.GateException;

public final class IntiateGATE {
	
	public static Document doc;
	
	private IntiateGATE(){
	}
	
	public static void go(String docId) throws GateException, MalformedURLException{
		Pattern pattern = Pattern.compile("([a-zA-Z0-9\\-_\\.]+/[a-zA-Z0-9\\-_\\.]+)$");
		Matcher match = pattern.matcher(docId);
		if (!match.find()){
			return;
		}
		
		File f = new File(Path.getGate_annoRoot()+docId+"/"+"eTarget.xml");
		Gate.init();			
		doc = (Document) 
				Factory.createResource("gate.corpora.DocumentImpl", 
				  Utils.featureMap(gate.Document.DOCUMENT_URL_PARAMETER_NAME, 
				f.toURI().toURL(),
				gate.Document.DOCUMENT_ENCODING_PARAMETER_NAME, "UTF-8"));
		doc.setMarkupAware(new Boolean(false));  // to prevent changing origial annotations
	
		
		return;
	}

}
