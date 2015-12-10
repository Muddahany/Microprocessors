
public class Instruction {
	String address;
	String opcode;
	int rd;
	int rs;
	int rt;
	boolean stalled,executed;
	int r;
	int status;
	
	public Instruction(String address){
		this.address=address;
		stalled=false;
		executed=false;
		status=1;
	}
}
