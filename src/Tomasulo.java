import java.util.HashMap;

public class Tomasulo extends Memory {
	int head, tail;
	ROBEntry[] ROB;
	RS[] RS;
	Register[] RegisterStat = new Register[8];

	public Tomasulo(int ROBlength, int RSlength) {
		super();
		RS = new RS[RSlength];
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
		head = 0;
		tail = 0;
	}

	public void issue(int r, int rd, int rs, int rt, String opcode) {
		int h; // pointer
		if (RegisterStat[rs].busy) {
			h = RegisterStat[rs].reorder;
			if (ROB[h].ready) {
				RS[r].Vj = ROB[h].value;
				RS[r].Qj = 0;
			} else {
				RS[r].Qj = h ;
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
		execute(r, opcode, rd, rs, rt);
	}

	public void execute(int r, String opcode, int rd, int rs, int rt) {
		if (opcode != "sw" && opcode != "lw") {
			if (RS[r].Qj ==0 && RS[r].Qk == 0) {
				compute(opcode, rd, rs, rt);
			}
		}
		if (opcode == "lw") {
			if (RS[r].Qj == 0) { // and there are no sws earlier in the
									// queue
				RS[r].A = RS[r].Vj + RS[r].A; // Step One
				readData(RS[r].A + "", 1); // condition:lw step 1 done adn all
											// sws earlier in ROB have
											// different addresses
			}
		}
		if (opcode == "sw") {
			if (RS[r].Qj == 0) { // and  sw at queue head
				RS[r].A = RS[r].Vj + RS[r].A; // TYPO!!
			}
		}
		write(r,opcode,rd,rs,rt);
	}

	private void write(int r, String opcode, int rd, int rs, int rt) {
				
	}

	private void compute(String opcode, int rd, int rs, int rt) {
	}
}
