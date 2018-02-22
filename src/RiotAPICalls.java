import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class RiotAPICalls {
	RiotApi api;
	public RiotAPICalls(String API_KEY) {
		ApiConfig config = new ApiConfig().setKey(API_KEY);
		api = new RiotApi(config);
	}

	public Summoner getSummonerByName(String name){
		try {
			System.out.println("getting summoner by name "+name);
			return api.getSummonerByName(Platform.NA, name);
		} catch (RiotApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public MatchList getMatchList(Summoner summoner){
		try {
			System.out.println("getting Match List by Summoner "+summoner);
			return api.getMatchListByAccountId(Platform.NA, summoner.getAccountId());
		} catch (RiotApiException e) {
			System.out.println("Error getting match list for summoner: "+summoner.getAccountId());
			return null;
		}
	}
	
	public MatchList getMatchList(long summonerId){
		try {
			System.out.println("getting Match List by Summonerid "+summonerId);
			return api.getMatchListByAccountId(Platform.NA, summonerId);
		} catch (RiotApiException e) {
			System.out.println("Error getting match list for summoner: "+summonerId);
			return null;
		}
	}
	
	public Match getMatchById(long gameId){
		try {
			System.out.println("getting Match by gameid "+gameId);
			return api.getMatch(Platform.NA, gameId);
		} catch (RiotApiException e) {
			System.out.println("Error getting match: "+gameId);
			return null;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
