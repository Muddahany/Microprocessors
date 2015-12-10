public class CacheLine {
	boolean validityBit;
	String tag;
	String[] data;
	boolean dirty;
	int used;

	public CacheLine(int l) {
		data = new String[l];
		dirty = false;
		tag="-1";
		validityBit = true;
		used=0;
	}
}
