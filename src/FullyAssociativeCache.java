public class FullyAssociativeCache extends Cache {
	CacheLine[] full;
	private String mode;
	int level;

	public FullyAssociativeCache(int s, int l, int level) {
		super(s, l, s / l);
		full = new CacheLine[s / l];
		for (int i = 0; i < full.length; i++) {
			full[i] = new CacheLine(l);
		}
		offsetsize = (int) (Math.log(l) / Math.log(2));
		tagsize = 16 - offsetsize;
		this.level = level;
	}

	public CacheLine readData(String line) {
		String tag = line.substring(0, tagsize);
		int offset = Integer.parseInt(line.substring(tagsize), 2);
		// ---------------------------------------//Hit\\----------------------------------------------
		for (int i = 0; i < full.length && full[i].validityBit && full[i].tag == tag; i++) {
			full[i].used++;
			hit++;
			return full[i];
		}
		// ........................................Miss...........................................
		int LRU = this.getLRUindex(); // first free space or least recently used
		miss++;						  // index
		if (mode == "write-back") {
			// -------------------------------/write-back\-----------------------------------
			if (full[LRU].dirty) { // check dirty bit
				Memory.writeToMemory(line, full[LRU].data, level); // write
																	// to
				// lower memory
			}
			full[LRU] = Memory.readData(line, level); // Read data from
														// lower
			// memory into the cache
			// block
			full[LRU].dirty = false; // mark block as non dirty
		} else {
			// -----------------------------/write-through\--------------------------------
			full[LRU] = Memory.readData(line, level); // Read data from
														// lower
			// memory into the cache
			// block
		}
		// -------------------------------------------------------------------------------------
		return full[LRU]; // return data
	}

	public void writeData(String line, String data) {
		String tag = line.substring(0, tagsize);
		int offset = Integer.parseInt(line.substring(tagsize), 2);
		int LRU = this.getLRUindex();
		// ----------------------------------------/write-back\--------------------------------
		if (mode == "write-back") {
			// ------------------------------------//Hit\\-------------------------------
			for (int i = 0; i < full.length && full[i].validityBit && full[i].tag == tag; i++) {
				full[i].used++;
				full[i].data[offset] = data;
				full[i].dirty = true;
				return;
			}
			// ------------------------------------//Miss\\-------------------------------
			if (full[LRU].dirty) { // check dirty bit
				Memory.writeToMemory(line, full[LRU].data, level); // write to
																	// lower
				// memory
			}
			full[LRU] = Memory.readData(line, level); // Read data from lower
														// memory
			// into the cache block
			return;
		}
		// ---------------------------------------/Hit-write-through\--------------------------------
		for (int i = 0; i < full.length && full[i].validityBit && full[i].tag == tag; i++) {
			full[i].used++;
			full[i].data[offset] = data;
			Memory.writeToMemory(line, full[i].data, level);
			return;
		}
		// ---------------------------------------/Miss-write-through\--------------------------------
		String[] dataa = { data };
		Memory.writeToMemory(line, dataa, level); // write data to lower memory
	}
	// -------------------------------------------------------------------------------------

	public int getLRUindex() {
		int min = full[0].used;
		int index = 0;
		for (int i = 1; i < full.length && full[i].used < min; i++) {
			if (full[i].used == 0) {
				return i;
			}
			min = full[i].used;
			index = i;
		}
		return index;
	}

	public void writeData(String line, String[] data) {
		for (int i = 0; i < data.length; i++) {
			this.writeData(line, data[i]);
		}

	}

}