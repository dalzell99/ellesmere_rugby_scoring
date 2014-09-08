package chris.myapplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class Game {
    long gameID;
    String homeTeamName;
    int homeTeamScore;
    String awayTeamName;
    int awayTeamScore;
    String location;
    int minutesPlayed;
    String time;
    ArrayList<ScoringPlay> scoringPlays;

    public Game(long gameID, String homeTeamName, int homeTeamScore, String awayTeamName,
                int awayTeamScore, String location, int minutesPlayed, String time,
                ArrayList<ScoringPlay> scoringPlays) {
        this.gameID = gameID;
        this.homeTeamName = homeTeamName;
        this.homeTeamScore = homeTeamScore;
        this.awayTeamName = awayTeamName;
        this.awayTeamScore = awayTeamScore;
        this.location = location;
        this.minutesPlayed = minutesPlayed;
        this.time = time;
        this.scoringPlays = scoringPlays;
    }

    public ArrayList<ScoringPlay> getScoringPlays() {
        return scoringPlays;
    }

    public long getGameID() {
        return gameID;
    }

    public String getLocation() {
        return location;
    }

    public int getMinutesPlayed() {
        return minutesPlayed;
    }

    public String getTime() {
        return time;
    }

    public String getStartTime() {
        String startTime = "";
        String today = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        // Check if game is being played today.
        if (String.valueOf(gameID).substring(0, 8).equals(today.substring(0, 8))) {
            startTime += "Today ";
        } else {
            try {
                Date date = new SimpleDateFormat("yyyyMMdd").parse(String.valueOf(gameID).substring(0, 8));
                startTime += new SimpleDateFormat("EE").format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            startTime += " ";
            startTime += String.valueOf(gameID).substring(6, 8);
            startTime += " ";
            startTime += Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                    "Aug", "Sep", "Oct", "Nov", "Dec").get(Integer.parseInt(String.valueOf(gameID).substring(4, 6)) - 1);
            startTime += " ";
        }
        startTime += time;
        return startTime;
    }

    public String getHomeTeamName() {
        return homeTeamName;
    }

    public int getHomeTeamScore() {
        return homeTeamScore;
    }

    public String getAwayTeamName() {
        return awayTeamName;
    }

    public int getAwayTeamScore() {
        return awayTeamScore;
    }
}
