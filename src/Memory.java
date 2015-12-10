import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

public class Memory {
	String[] Regs;
	String regA;
	String regB;
	String regC;
	String imm;
	static Cache[] memory;
	static HashMap<String,String> physicalMemory;
	int counter;

	public Memory() {
		physicalMemory = new HashMap<String,String>();
		Regs = new String[8];
		for (int i = 0; i < Regs.length; i++) {
			Regs[i] = "0";
		}
		counter = 0;
		memory = new Cache[20];
	}

	public void addcache(Cache cache, int level) {
		memory[level] = cache;
	}

	public static void writeToMemory(String line, String[] data, int level) {
		memory[level + 1].writeData(line, data);
	}

	public static String readData(String line, int level) {
		if (memory[level + 1] != null) {
			return memory[level + 1].readData(line);
		}
		physicalMemory.put("0000000000000000", "lw RegA, RegB, 15");
		return physicalMemory.get(line);     //rakezz hnnaaa
	}
}
