public abstract class Cache {
	int tagsize;
	int indexsize;
	int offsetsize;
	int hit,miss;

	public Cache(int s, int l, int m) {
		hit=0;
		miss=0;
	}

	public abstract String readData(String line);

	public abstract void writeData(String line, String[] data);

	public void writeData(String[] data, String line) {
	}
}
