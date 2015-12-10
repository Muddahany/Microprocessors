import java.util.HashMap;

public class Tomasulo extends Memory {
	int head, tail;
	ROBEntry[] ROB;
	RS[] RS;
	Register[] RegisterStat = new Register[8];
	private int result;
	int addStations;
	int multiplyStations;
	int loadStations;
	int branchStations;

	public Tomasulo(int ROBlength, int addStations, int multiplyStations, int loadStations, int branchStations) {
		super();
		this.addStations = addStations;
		this.multiplyStations = multiplyStations;
		this.loadStations = loadStations;
		this.branchStations = branchStations;
		RS = new RS[addStations + multiplyStations + loadStations + branchStations];
		for (int i = 0; i < RS.length; i++) {
			RS[i] = new RS();
		}
		for (int i = 0; i < RegisterStat.length; i++) {
			RegisterStat[i] = new Register();
		}
		ROB = new ROBEntry[ROBlength];
		for (int i = 0; i < ROB.length; i++) {
			ROB[i] = new ROBEntry();
		}
		head = 1;
		tail = 1;
	}

	public void fetch(Instruction instruction, Memory mem) {
		String inst= Memory.readData(instruction.address, -1);
		String opcode = "";
		int i;
		for (i = 0; i < inst.length() && inst.charAt(i) != ' '; i++) {
			opcode = opcode + inst.charAt(i);
		}
		String rd = "";
		i = i + 2;
		while (inst.charAt(i) != ',' && i < inst.length()) {
			rd = rd + inst.charAt(i);
			i++;
		}
		String rs = "";
		i = i + 3;
		while (inst.charAt(i) != ',' && i < inst.length()) {
			rs = rs + inst.charAt(i);
			i++;
		}
		String rt = "";
		i = i + 3;
		while (i < inst.length()) {
			rt = rt + inst.charAt(i);
			i++;
		}
		int r = findStation(opcode);
		if (r == -1) {
			instruction.stalled=true;
		} else if (r == -2) {
			return;		// we don't support that function
		}
		int rdd=Integer.parseInt(rd);
		int rss=Integer.parseInt(rs);
		int rtt=Integer.parseInt(rt);
		instruction.rd=rdd;
		instruction.rs=rss;
		instruction.rt=rtt;
		instruction.r=r;
		instruction.status=2;
	}

	// 3yzeen n3ml 7aga esmha cycle
	private int findStation(String opcode) {
		if (opcode == "add" || opcode == "sub" || opcode == "addi" || opcode == "NAND") {
			for (int i = 0; i < this.addStations; i++) {
				if (i == this.addStations - 1 && RS[i].Busy) {
					return -1;
				}							//ASK ABOUT NAND
				if (!RS[i].Busy) {
					return i;
				}
			}
		} else if (opcode == "mult" || opcode == "div") {
			for (int i = this.addStations; i < this.addStations + this.multiplyStations; i++) {
				if (i == this.addStations + this.multiplyStations - 1 && RS[i].Busy) {
					return -1;
				}
				if (!RS[i].Busy) {
					return i;
				}
			}
		} else if (opcode == "lw" || opcode == "sw") {
			for (int i = this.addStations + this.multiplyStations; i < this.addStations + this.loadStations
					+ this.multiplyStations; i++) {
				if (i == this.addStations + this.loadStations + this.multiplyStations - 1 && RS[i].Busy) {
					return -1;
				}
				if (!RS[i].Busy) {
					return i;
				}
			}
		} else if (opcode == "jmp" || opcode == "beq" || opcode == "jalr" || opcode == "ret") {
			for (int i = this.addStations + this.multiplyStations + this.loadStations; i < RS.length; i++) {
				if (i == RS.length - 1 && RS[i].Busy) {
					return -1;
				}
				if (!RS[i].Busy) {
					return i;
				}
			}
		}
		return -2;
	}

	public void issue(Instruction instruction, Memory m) {
		int rs=instruction.rs;
		int rt=instruction.rt;
		int rd=instruction.rd;
		int r=instruction.r;
		String opcode=instruction.opcode;
		int h; // pointer
		if (RegisterStat[rs].busy) {
			instruction.stalled=true;
			h = RegisterStat[rs].reorder;
			if (ROB[h].ready) {
				RS[r].Vj = ROB[h].value;
				RS[r].Qj = 0;
			} else {
				
				RS[r].Qj = h;
			}
		} else {
			RS[r].Vj = RegisterStat[rs].value;
			RS[r].Qj = 0;
		}
		RS[r].Busy = true;
		RS[r].destination = tail;
		ROB[tail].type = opcode;
		ROB[tail].destination = rd;
		ROB[tail].ready = false;
		if (opcode != "lw") {
			if (RegisterStat[rt].busy) {
				instruction.stalled=true;
				h = RegisterStat[rt].reorder;
				if (ROB[h].ready) {
					RS[r].Vk = ROB[h].value;
					RS[r].Qk = 0;
				} else {
					RS[r].Qk = h;
				}
			} else {
				RS[r].Vk = RegisterStat[rt].value;
				RS[r].Qk = 0;
			}
		}
		if (opcode != "sw" && opcode != "lw") {
			RegisterStat[rd].reorder = tail;
			RegisterStat[rd].busy = true;
			ROB[tail].destination = rd;
		}
		if (opcode == "lw") {
			RS[r].A = rt;
			RegisterStat[rd].reorder = tail;
			RegisterStat[rd].busy = true;
			ROB[tail].destination = rd;
		}
		if (opcode == "sw") {
			RS[r].A = rt;
		}
		if(!instruction.stalled){
			instruction.status++;
		}
	}

	public void execute(Instruction instruction, Memory m) {
		int rs=instruction.rs;
		int rt=instruction.rt;
		int rd=instruction.rd;
		int r=instruction.r;
		String opcode=instruction.opcode;
		if (opcode != "sw" && opcode != "lw") {
			if (RS[r].Qj == 0 && RS[r].Qk == 0) {
				result = compute(opcode, rd, rs, rt);
			}
		}
		if (opcode == "lw") {
			if (RS[r].Qj == 0) { // and there are no sws earlier in the
									// queue
				RS[r].A = RS[r].Vj + RS[r].A; // Step One
				readData(RS[r].A + "", 1); // condition:lw step 1 done and all
											// sws earlier in ROB have
											// different addresses
			}
		}
		if (opcode == "sw") {
			if (RS[r].Qj == 0) { // and sw at queue head
				RS[r].A = RS[r].Vj + RS[r].A; // TYPO!!
			}
		}
	}

	public void write(Instruction instruction, Memory m) {
		int rs=instruction.rs;
		int rt=instruction.rt;
		int rd=instruction.rd;
		int r=instruction.r;
		String opcode=instruction.opcode;
		if (opcode != "sw") {
			int b = RS[r].destination;
			RS[r].Busy = false;
			for (int i = 0; i < RS.length; i++) {
				if (RS[i].Qj == b) {
					RS[i].Vj = result;
					RS[i].Qj = 0;
				}
				if (RS[i].Qk == b) {
					RS[i].Vk = result;
					RS[i].Qk = 0;
				}
			}
		}
	}

	private int compute(String opcode, int rd, int rs, int rt) {
		switch(opcode){
		case "add": return (rs+rt);
		case "addi": return (rs+rt);
		case "sub": return (rs-rt);
		case "nand": return (rs&rt);
		case "mult": return (rs*rt);
		case "div": return (rs/rt);
		}
	}

	public void commit(Instruction instruction, Memory m) {
		// TODO Auto-generated method stub
		
	}
}
