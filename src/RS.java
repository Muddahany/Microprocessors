
public class RS {
	
	String Op; // - represents the operation being performed on operands
	int Qj, Qk; // - the reservation station that will produce the relevant
					// source operand (0 indicates the value is in Vj, Vk)
	int Vj, Vk; // - the value of the source operands
	int A; // - used to hold the memory address information for a load or
				// store
	int destination;
	boolean Busy; // - 1 if occupied, 0 if not occupied
	
	public RS(){
		Op=null;
		Qj=Qk=0;
		Vj=Vk=A=destination-1;
		Busy=false;
	}
}
