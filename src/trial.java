public class trial {
	public static void main(String[] args) {
		Memory m = new Memory();
		DirectCache l1=new DirectCache(4096,16,0);
		FullyAssociativeCache l2=new FullyAssociativeCache(4096,16,1);
		SetAssociativeCache l3=new SetAssociativeCache(4096,16,4,2);
		l1.mode="write-back";
		l2.mode="write-back";
		l3.mode="write-back";
		m.addcache(l1,0);
		m.addcache(l2,1);
		m.addcache(l3,2);
		m.physicalMemory.put("0000000000000000", "lw RegA, RegB, 15");
		Tomasulo t = new Tomasulo(5,2,2,2,1);
	}
}
