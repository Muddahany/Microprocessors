public class trial {
	public static void main(String[] args) {
		Memory m = new Memory();
		DirectCache l1=new DirectCache(4096,16,1);
		FullyAssociativeCache l2=new FullyAssociativeCache(4096,16,2);
		SetAssociativeCache l3=new SetAssociativeCache(4096,16,4,3);
		m.addcache(l1);
		m.addcache(l2);
		m.addcache(l3);
		
	}
}
