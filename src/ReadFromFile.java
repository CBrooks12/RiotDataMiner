import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import net.rithms.riot.api.endpoints.match.dto.Match;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


public class ReadFromFile {
	private FileReader dataFile;
	private Gson gson;
	public ReadFromFile(String fileName) throws FileNotFoundException{
		dataFile = new FileReader(fileName);
		gson = new Gson();
	}
	public Object getDataFromFile(Class<?> type)
	{
		try {
			return gson.fromJson(dataFile, type);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException{
		ReadFromFile aReader = new ReadFromFile("e:/RiotDataV2/Latvian_Potato/Completed/SampleData.json");
		HashMap<Long, Match> matchMap = new HashMap<Long, Match>();
		matchMap = (HashMap<Long, Match>) aReader.getDataFromFile(matchMap.getClass());
	}
}
