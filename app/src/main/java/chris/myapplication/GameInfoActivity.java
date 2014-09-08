package chris.myapplication;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class GameInfoActivity extends ActionBarActivity {

    LinearLayout linearLayoutScoringPlays;
    TextView textViewGameInfoHomeTeam;
    TextView textViewGameInfoAwayTeam;
    TextView textViewGameInfoHomeScore;
    TextView textViewGameInfoAwayScore;
    TextView textViewGameInfoMinutes;
    int homeScore = 0;
    int awayScore = 0;
    String gameID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_info);

        // Hide actionbar to make activity full screen
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        linearLayoutScoringPlays = (LinearLayout) findViewById(R.id.linearLayoutScoringPlaysActivity);
        textViewGameInfoHomeTeam = (TextView) findViewById(R.id.textViewGameInfoHomeTeam);
        textViewGameInfoAwayTeam = (TextView) findViewById(R.id.textViewGameInfoAwayTeam);
        textViewGameInfoHomeScore = (TextView) findViewById(R.id.textViewGameInfoHomeScore);
        textViewGameInfoAwayScore = (TextView) findViewById(R.id.textViewGameInfoAwayScore);
        textViewGameInfoMinutes = (TextView) findViewById(R.id.textViewGameInfoMinutes);

        homeScore = 0;
        awayScore = 0;
        gameID = getIntent().getStringExtra("gameID");

        // Get game object to be displayed
        Game game = getGameInfo();
        textViewGameInfoHomeTeam.setText(game.getHomeTeamName());
        textViewGameInfoAwayTeam.setText(game.getAwayTeamName());
        textViewGameInfoHomeScore.setText(String.valueOf(game.getHomeTeamScore()));
        textViewGameInfoAwayScore.setText(String.valueOf(game.getAwayTeamScore()));
        if (game.getMinutesPlayed() == 0) {
            textViewGameInfoMinutes.setText(game.getStartTime());
        } else {
            textViewGameInfoMinutes.setText(String.valueOf(game.getMinutesPlayed()) + "mins");
        }
        ArrayList<ScoringPlay> scoringPlays = game.getScoringPlays();

        // Add each scoring play one by one by inflating a scoringplay_linearlayout LinearLayout and
        // setting the text for all the textviews
        for (ScoringPlay s : scoringPlays) {
            // Inflate layout
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.scoringplay_linearlayout,
                    this.linearLayoutScoringPlays, false);
            // Get scoring play then split it into team and play
            String scoringPlay = s.getPlay();
            String team = scoringPlay.substring(0, 4);
            String play = scoringPlay.substring(4, scoringPlay.length());
            if (team.equals("home")) {
                TextView textViewHomePlay = (TextView) linearLayout.findViewById(R.id.textViewHomePlay);
                textViewHomePlay.setText(play.equals("DropGoal") ? "Drop Goal" : play);
            } else {
                TextView textViewAwayPlay = (TextView) linearLayout.findViewById(R.id.textViewAwayPlay);
                textViewAwayPlay.setText(play.equals("DropGoal") ? "Drop Goal" : play);
            }

            // Change score to score at this point in the game then display it along with the
            // minutes played when this scoring play happened
            changeScore(team, play);
            TextView textViewMinutes = (TextView) linearLayout.findViewById(R.id.textViewMinutes);
            textViewMinutes.setText(String.valueOf(homeScore) + "-" + String.valueOf(awayScore) +
                    " (" + String.valueOf(s.getMinutes()) + "')");
            TextView textViewDescription = (TextView) linearLayout.findViewById(R.id.textViewPlayDescription);
            if (s.getDescription().equals("")) {
                textViewDescription.setVisibility(View.GONE);
            } else {
                if (team.equals("away")) {
                    textViewDescription.setGravity(Gravity.RIGHT);
                }
                textViewDescription.setText(s.getDescription());
            }

            // Add this linear layout to main linear layout
            linearLayoutScoringPlays.addView(linearLayout);
        }
    }

    // Update score based on team and scoring play
    private void changeScore(String team, String play) {
        if (play.equals("Try")) {
            if (team.equals("home")) {
                homeScore += 5;
            } else {
                awayScore += 5;
            }
        } else if (play.equals("Penalty") || play.equals("DropGoal")) {
            if (team.equals("home")) {
                homeScore += 3;
            } else {
                awayScore += 3;
            }
        } else if (play.equals("Conversion")) {
            if (team.equals("home")) {
                homeScore += 2;
            } else {
                awayScore += 2;
            }
        }
    }

    // Returns game object based on gameID
    private Game getGameInfo() {
        Game result = null;

        // Iterate through all games until matching gameID is found
        for (int i = 0; i < DrawFragmentActivity.games.size(); i++) {
            if (DrawFragmentActivity.games.get(i).getGameID() == Long.parseLong(gameID)) {
                result = DrawFragmentActivity.games.get(i);
            }
        }

        return result;
    }

}
