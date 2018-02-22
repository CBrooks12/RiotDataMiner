import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.rithms.riot.api.endpoints.match.dto.Match;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class SaveToFile {
	FileWriter writer;
	BufferedWriter bf;
	String path = "e:/RiotDataV2/";
	public SaveToFile(String aPath)
	{
		path = aPath;
		try {
			writer = new FileWriter(path);
			bf = new BufferedWriter(writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public SaveToFile(String user, String filePath, int index)
	{
		String fileName = filePath + index +".json";
		try {
			writer = new FileWriter(fileName);
			bf = new BufferedWriter(writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public boolean saveData(HashMap<Long, Match> matchMap)
	{
		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(matchMap);
		JsonElement el = parser.parse(json);
		String jsonPretty = gson.toJson(el);
		try {
			bf.write(jsonPretty);
			bf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

		
}
