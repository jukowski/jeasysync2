import com.etherpad.lite.EtherpadLiteConnection;

public class ConnectionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		EtherpadLiteConnection conn = new EtherpadLiteConnection("http://localhost:9001", "test");
	    System.out.println("Exiting");
	}

}
