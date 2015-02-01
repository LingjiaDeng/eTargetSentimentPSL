package run;

import gate.util.GateException;

import java.io.IOException;
import readBishan.*;
import readGATE.ReadETarget;

public class Run {
	public static void main(String[] args) throws IOException, GateException{
		String docId = "temp_fbis/21.10.31-12974";
		
		//ReadBishanHolder r = new ReadBishanHolder(docId);
		//ReadBishanSentiment s = new ReadBishanSentiment(docId);
		ReadBishanTogether t = new ReadBishanTogether(docId);
		
		ReadETarget g = new ReadETarget(docId);
		
	}
}
