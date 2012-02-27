import com.etherpad.easysync2.Changeset;
import com.etherpad.easysync2.Operation;



public class EasySyncTest {

	static String cs = "Z:ph>1|8=pg*0+1$a";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Changeset myChange = Changeset.unpack(cs);
		System.out.println(myChange);
		for (Operation r : myChange.getOpIterator()) {
			System.out.println(r);
		}
	}

}
