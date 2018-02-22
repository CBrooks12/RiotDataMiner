import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.match.dto.Participant;
import net.rithms.riot.api.endpoints.match.dto.ParticipantIdentity;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;


public class GetData {
	private RiotAPICalls dataClass;
	private String CUR_PATCH = "7.24";
	private String USER_START = "Mutes All";
	private int MAX_GAMES = 10000;
	private int GAMES_PER_FILE = 200;
	private int SLEEP_TIME_MS = 1000;
	private String SAVE_FILE_PATH = "e:/";
	private int fileIndex = 0;
	public GetData(String fileName) {
		String API_KEY = null;
		try{
			Properties props = new Properties();
			File configFile = new File(fileName);
		    FileReader reader = new FileReader(configFile);
		    props.load(reader);
		    API_KEY = props.getProperty("API_KEY");
		    CUR_PATCH = props.getProperty("CUR_PATCH");
		    USER_START = props.getProperty("USER_START");
		    MAX_GAMES = Integer.valueOf(props.getProperty("MAX_GAMES"));
		    GAMES_PER_FILE = Integer.valueOf(props.getProperty("GAMES_PER_FILE"));
		    SLEEP_TIME_MS = Integer.valueOf(props.getProperty("SLEEP_TIME_MS"));
		    SAVE_FILE_PATH = props.getProperty("SAVE_FILE_PATH");
		    reader.close();
		}catch (FileNotFoundException ex) {
		    // file does not exist
		} catch (IOException ex) {
		    // I/O error
		}
		//System.out.println("API_KEY: "+API_KEY);
		dataClass = new RiotAPICalls(API_KEY);
	}
	
	@SuppressWarnings("unchecked")
	public HashSet<Long> loadLocalSavedGameIds(String filePath){
		int i = 0;
		ReadFromFile fR;
		HashMap<Long, Match> matchMap = new HashMap<Long, Match>();
		HashSet<Long> gameIds = new HashSet<Long>();
		try {
			while(true){
				String locFilePath = filePath + i + ".json";
				fR = new ReadFromFile(locFilePath);
				matchMap = (HashMap<Long, Match>) fR.getDataFromFile(matchMap.getClass());
				System.out.println("Retrieved games from file "+i+":" +matchMap.keySet().size());
				gameIds.addAll(matchMap.keySet());
				i++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Number of cached games retrieved: "+gameIds.size());
		fileIndex = i;
		return gameIds;
	}
	
	public int run() throws InterruptedException, RiotApiException{
		HashMap<Long,Match> matchMap = new HashMap<Long,Match>();
		Match firstGame = null;
		
		Summoner startingPoint = dataClass.getSummonerByName(USER_START);
		MatchList matchList = dataClass.getMatchList(startingPoint);

		//check if worm starts on current patch
		if(matchList.getTotalGames()>0){
			long gameId = matchList.getMatches().get(0).getGameId();
			firstGame = dataClass.getMatchById(gameId);
			if(!firstGame.getGameVersion().contains(CUR_PATCH)){ //TODO: get current patch from api
				System.out.println("Latest game " + firstGame + " not from current patch. Exiting...");
				return -1;
			}
		}
		int curGames = 0;
		
		//create caches
		HashSet<Long> userIds = new HashSet<Long>();
		HashSet<Long> gameIds = loadLocalSavedGameIds(SAVE_FILE_PATH); //NOTE: possibly add size of loaded games to curGame index
		userIds.add(startingPoint.getId()); //add starting user to set
		
		//create queue for bfs
		Queue<Long> participantsQueue = new LinkedList<Long>();
		participantsQueue.add(startingPoint.getAccountId());
		
		//loop while there are nodes to search
		while(!participantsQueue.isEmpty()){
			//pop user out of queue
			long curGameUserId = participantsQueue.poll();
			//wait 1 second for api
			Thread.sleep(SLEEP_TIME_MS);
			//get match list
			MatchList curMatchListTemp = dataClass.getMatchList(curGameUserId);
			//if users match list doesn't exist, go to next user
			if(curMatchListTemp==null){continue;}
			
			List<MatchReference> curMatchList = curMatchListTemp.getMatches();
			//iterate over match list for user
			for(MatchReference aMatch : curMatchList){ 
				//if game is already cached (node has been visited, skip)
				System.out.println("Checking match: "+ aMatch.getGameId());
				if(!gameIds.contains(aMatch.getGameId())){
					gameIds.add(aMatch.getGameId());
					//check if game is from classic summoners rift
					int [] queueTypes = {400,420,430,440};
					if(IntStream.of(queueTypes).anyMatch(x -> x == aMatch.getQueue())){
						Match locMatch = dataClass.getMatchById(aMatch.getGameId());
						Thread.sleep(SLEEP_TIME_MS);
						if(locMatch==null){continue;}
						if(locMatch.getGameVersion().contains(CUR_PATCH)){ //TODO: check if game is valid
							//if true, add to list
							matchMap.put(aMatch.getGameId(), locMatch);
							curGames++;
							//populate nodes for bfs by participants in game
							for(ParticipantIdentity p : locMatch.getParticipantIdentities()){
								long pId = p.getPlayer().getAccountId();
								if(!userIds.contains(pId)){
									userIds.add(pId);
									participantsQueue.add(pId);
								}
							}
							//System.out.println("Participant Queue Size: "+participantsQueue.size());
							if(matchMap.keySet().size()==GAMES_PER_FILE){
								System.out.println("Saving number of games: "+matchMap.keySet().size());
								saveData(matchMap);
								matchMap.clear();//delete local cache to save space. unnecessary to have so many games
							}
							//if gathered desired amount of game data
							if(curGames==MAX_GAMES){
								return curGames;
							}
						}
						else{ //end of most recent patch. return to queue
							System.out.println("End of current patch for "+curGameUserId);
							System.out.println("Number of games gathered for - "+curGameUserId+": " + curGames);
							curGames = 0;
							break;
						}
					}
					
				}
			}
			saveData(matchMap);
		}
		return curGames;
	}

	private boolean saveData(HashMap<Long, Match> matchMap) {
		SaveToFile saver = new SaveToFile(USER_START,SAVE_FILE_PATH,fileIndex);
		if(saver.saveData(matchMap)){
			fileIndex++;
			return true;
		}
		else return false;
	}

	public static void main(String[] args) throws InterruptedException, RiotApiException {
		GetData gd = new GetData("app.config");
		//gd.loadLocalData("e:/RiotDataV2/Latvian_Potato/Completed/SampleData");
		int ret = gd.run();
		System.out.println("Total number of games collected: "+ret);
	}

}
