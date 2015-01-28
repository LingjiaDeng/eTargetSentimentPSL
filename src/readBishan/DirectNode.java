package readBishan;

import java.util.ArrayList;

public class DirectNode {
  public String agent;
  public String sentence;
  public String span;
  public ArrayList<String> targets;
  public boolean overlapped;
  
  public DirectNode(){
    this.agent = "";
    this.sentence = "";
    this.span = "";
    this.targets = new ArrayList<String>();
    this.overlapped = false;
  }

}
