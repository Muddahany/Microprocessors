public class DirectCache extends Cache {
	CacheLine[] direct;
	String mode;
	int level;

	public DirectCache(int s, int l, int level) {
		super(s, l, 1);
		direct = new CacheLine[s / l];
		for (int i = 0; i < direct.length; i++) {
			direct[i] = new CacheLine(l);
		}
		offsetsize = (int) (Math.log(l) / Math.log(2));
		indexsize = (int) (Math.log(s / l) / Math.log(2));
		tagsize = 16 - offsetsize - indexsize;
		this.level = level;
	}

	public CacheLine readData(String line) {
		String tag = line.substring(0, tagsize);
		int index = Integer.parseInt(line.substring(tagsize, indexsize + tagsize), 2);
		int offset = Integer.parseInt(line.substring(indexsize + tagsize), 2);
		// -------------------------------------------------------------------------------------
		if (!direct[index].validityBit) {
			// Miss
			if (direct[index].tag != tag) {
				miss++;
				if (mode == "write-back") {
					// ----------------------------------------/write-back\-----------------------------------
					if (direct[index].dirty) { // check dirty bit
						Memory.writeToMemory(line, direct[index].data, level); // Write
						// its
						// previous
						// data
						// back
						// to
						// the
						// lower
						// memory
					}
					direct[index] = Memory.readData(line, level); // Read data
																	// from
																	// lower
																	// memory
																	// into
																	// the cache
																	// block

					direct[index].dirty = false;		 // mark block as non dirty
				} else {
					// ---------------------------------------/write-through\--------------------------------
					direct[index] = Memory.readData(line, level); // Read data
																	// from
					// lower memory into
					// the cache block
				}
				// -------------------------------------------------------------------------------------
			}
			return direct[index]; // return data
		}
		return null;

	}

	public void writeData(String line, String data) {
		String tag = line.substring(0, tagsize);
		int index = Integer.parseInt(line.substring(tagsize, indexsize), 2);
		int offset = Integer.parseInt(line.substring(indexsize + tagsize), 2);
		if (mode == "write-back") {
			// ----------------------------------------/write-back\-----------------------------------
			if (!(direct[index].tag == tag)) {
				miss++;
				if (direct[index].dirty) { // check dirty bit
					Memory.writeToMemory(line, direct[index].data, level); // Write
																			// its
					// previous
					// data
					// back to
					// the lower
					// memory
				}
				direct[index] = Memory.readData(line, level); // Read data from
																// lower
				// memory into the cache
				// block
			}
			// write new data to the cache black
			direct[index].tag = tag;
			direct[index].data[offset] = data;
			// mark cache block as dirty
			direct[index].dirty = true;
		} else {
			// ---------------------------------------/write-through\--------------------------------
			if ((direct[index].tag == tag)) { // Hit
				direct[index].data[offset] = data; // write new data to the
													// cache block
			}
			Memory.writeToMemory(line, direct[index].data, level); // write data
																	// to
																	// lower memory
		}
		// -------------------------------------------------------------------------------------
	}

	public int getLRUindex() {
		int min = direct[0].used;
		int indexx = 0;
		for (int i = 1; i < direct.length && direct[i].used < min; i++) {
			if (direct[i].used == 0) {
				return i;
			}
			min = direct[i].used;
			indexx = i;
		}
		return indexx;

	}

	@Override
	public void writeData(String line, String[] data) {
		for (int i = 0; i < data.length; i++) {
			this.writeData(line, data[i]);
		}

	}
}
