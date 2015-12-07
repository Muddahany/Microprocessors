public class CacheLine {
	boolean validityBit;
	String tag;
	String[] data;
	boolean dirty;
	int used;

	public CacheLine(int l) {
		data = new String[l];
		dirty = false;
		validityBit = true;
		used=0;
	}
}
