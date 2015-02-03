package readBishan;

import java.util.ArrayList;

public class DirectNode {
  public String agent;
  public int agentStart;
  public String sentence;
  public String opinionSpan;
  public int opinionStart;
  public ArrayList<String> targets;
  public ArrayList<Integer> targetStarts;
  public String polarity;
  public boolean overlapped;
  
  
  public DirectNode(){
    this.agent = "";
    this.agentStart = -1;
    this.sentence = "";
    this.opinionSpan = "";
    this.opinionStart = -1;
    this.targets = new ArrayList<String>();
    this.targetStarts = new ArrayList<Integer>();
    this.overlapped = false;
    this.polarity = "";
  }

}
