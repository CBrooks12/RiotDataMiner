import net.rithms.riot.api.RiotApiException;


public class Main {
	public static void main(String[] args) {
		GetData gd = new GetData("app.config");
		try {
			int ret = gd.run();
			System.out.println("Total number of games collected: "+ret);
		} catch (InterruptedException | RiotApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
