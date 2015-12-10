import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Engine {
	public static void main(String[] args) {
		Memory m = new Memory();
		DirectCache l1 = new DirectCache(4096, 16, 0);
		FullyAssociativeCache l2 = new FullyAssociativeCache(4096, 16, 1);
		SetAssociativeCache l3 = new SetAssociativeCache(4096, 16, 4, 2);
		l1.mode = "write-back";
		l2.mode = "write-back";
		l3.mode = "write-back";
		m.addcache(l1, 0);
		m.addcache(l2, 1);
		m.addcache(l3, 2);
		m.physicalMemory.put("0000000000000000", "lw RegA, RegB, 15");
		Tomasulo t = new Tomasulo(5, 2, 2, 2, 1);
		boolean flag = true;
		String address = "";
		String firstAddress = "";
		BufferedReader reader = null;
		try {
			File file = new File("program.txt");
			reader = new BufferedReader(new FileReader(file));

			String line;

			int z;
			while ((line = reader.readLine()) != null) {
				if (flag) {
					firstAddress = convertToBinaryString(Integer.parseInt(line));
					address = convertToBinaryString(Integer.parseInt(line));
					flag = false;
				} else {
					m.physicalMemory.put(address, line);
					z = Integer.parseInt(address, 2);
					z++;
					address = convertToBinaryString(z);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int i = 0;
		ArrayList<Instruction> instructions = new ArrayList<Instruction>();
		address = firstAddress;
		while (i < 100) {
			instructions.add(new Instruction(address));
			for (int j = 0; j < instructions.size(); j++) {
				if (instructions.get(j).status == 1) {
					t.fetch(instructions.get(j),m);
				}
				if (instructions.get(j).status == 2) {
					t.issue(instructions.get(j),m);
				}
				if (instructions.get(j).status == 3) {
					t.execute(instructions.get(j),m);
				}
				if (instructions.get(j).status == 4) {
					t.write(instructions.get(j),m);
				}
				if (instructions.get(j).status == 5) {
					t.commit(instructions.get(j),m);
				}
				instructions.get(j).stalled=false;
			}
		}

	}

	public static String convertToBinaryString(int x) {
		String s = Integer.toBinaryString(x);
		for (int i = 0; s.length() != 16; i++) {
			s = "0" + s;
		}
		return s;
	}
}
