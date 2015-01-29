package run;

import java.io.IOException;
import readBishan.*;

public class Run {
	public static void main(String[] args) throws IOException{
		String docId = "temp_fbis\\22.36.40-5626.bishan";
		
		ReadBishanHolder r = new ReadBishanHolder(docId);
		ReadBishanSentiment s = new ReadBishanSentiment(docId);
		ReadBishanTogether t = new ReadBishanTogether(docId);
		System.out.println(t.sentenceHash.size());
	}
}
