
public class ROBEntry {
	String type;
	int destination,value;
	boolean ready;
	public ROBEntry(){
		ready=false;
		type=null;
		value=destination=-1;
	}
}
