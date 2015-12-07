import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

public class Memory {
	HashMap<String,String> registers;
	String regA;
	String regB;
	String regC;
	String imm;
	static Cache[] memory;
	String[] mainMemory;
	int counter;
	

	public Memory() {
		mainMemory=new String[2^16];
		registers= new HashMap<String,String>(8);
		for (int i = 0; i < registers.size(); i++) {
			registers.put("R"+i, "0");
		}
		counter=0;
		memory = new Cache[20];
	}

	public void addcache(Cache cache) {
		memory[counter++]=cache;
	}

	public static void writeToMemory( String line,String[] data,int level) {
		memory[level+1].writeData(line,data);
	}

	public static CacheLine readData(String line,int level) {
		return memory[level+1].readData(line);
	}
}
