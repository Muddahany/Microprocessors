public class SetAssociativeCache extends Cache {
	CacheLine[] set;
	String mode;
	int m;
	int level;

	public SetAssociativeCache(int s, int l, int m,int level) {
		super(s, l, m);
		set = new CacheLine[s / l];
		this.m = m;
		for (int i = 0; i < set.length; i++) {
			set[i] = new CacheLine(l);
		}
		offsetsize = (int) (Math.log(l) / Math.log(2));
		indexsize = (int) (Math.log((s / (l*m))) / Math.log(2));
		tagsize = 16 - offsetsize - indexsize;
		this.level=level;
	}

	public String readData(String line) {
		String tag = line.substring(0, tagsize);
		int index = Integer.parseInt(line.substring(tagsize, indexsize + tagsize), 2)*m;
		int offset = Integer.parseInt(line.substring(indexsize + tagsize), 2);
		// -------------------------------------------------------------------------------------
		for (int i = 0; i < m && set[index + i].tag.equals(tag); i++) {
			if (set[index].validityBit) {
				set[index+i].used++;
				hit++;
				return set[index + i].data[offset];
			} else {
				return null;
			}
		}
		// Miss
		miss++;
		int LRU = this.getLRUindex(index);
		if (mode == "write-back") {
			// ----------------------------------------/write-back\-----------------------------------
			if (set[LRU].dirty) { // check dirty bit
				Memory.writeToMemory(line, set[LRU].data,level); // Write
															// its
															// previous
															// data
															// back
															// to
															// the
															// lower
															// memory
			}
			// Read data from memory into the cache block
			set[LRU].data[offset] = Memory.readData(line, level);
			set[LRU].tag=tag;
			set[LRU].dirty = false; // mark block as non dirty
		} else {
			// ---------------------------------------/write-through\--------------------------------
			// Read data from memory into the cache block
			set[LRU].data[offset] = Memory.readData(line, level);
			set[LRU].tag=tag;
		}
		// -------------------------------------------------------------------------------------
		set[LRU].used++;
		return set[LRU].data[offset];
	}

	public void writeData(String line, String data) {
		String tag = line.substring(0, tagsize);
		int index = Integer.parseInt(line.substring(tagsize, indexsize), 2)*m;
		int offset = Integer.parseInt(line.substring(indexsize + tagsize), 2);

		if (mode == "write-back") {
			// ----------------------------------------/write-back\-----------------------------------
			for (int i = 0; i < m && set[index + i].tag.equals(tag); i++) {
				set[index+i].used++;
				set[index + i].data[offset] = data; // write new data to the
													// cache black
				set[index + i].dirty = true;
				hit++;
				return;
			}
			miss++;
			int LRU = this.getLRUindex(index);
			if (set[LRU].dirty) { // check dirty bit
				Memory.writeToMemory(line, set[LRU].data,level); // Write its
															// previous
															// data
															// back to
															// the lower
															// memory
			}
			// Read data from memory into the cache block
			set[LRU].data[offset] = Memory.readData(line, level);
			set[LRU].tag=tag;
			set[LRU].used++;

		} else {
			// ---------------------------------------/write-through\--------------------------------
			for (int i = 0; i < m && set[index + i].tag.equals(tag); i++) {
				set[index+i].used++;
				set[index + i].data[offset] = data; // write new data to the
													// cache black
				hit++;
				return;
			}
			miss++;
			Memory.writeToMemory(line, set[index].data,level); // write data to
															// lower memory
		}
		// -------------------------------------------------------------------------------------
	}

	public int getLRUindex(int index) {
		int min = set[index].used;
		int indexx = index;
		for (int i = 0; i < m && set[index + i].used < min; i++) {
			if (set[index + i].used == 0) {
				return index + i;
			}
			min = set[index + i].used;
			indexx = index + i;
		}
		return indexx;

	}

	@Override
	public void writeData(String line,String[] data) {
		for (int i = 0; i < data.length; i++) {
			this.writeData(line, data[i]);
		}
		
	}
}
