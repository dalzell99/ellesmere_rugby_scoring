package chris.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class GameInfoActivity extends ActionBarActivity {

    LinearLayout linearLayoutScoringPlays;
    TextView textViewGameInfoHomeTeam;
    TextView textViewGameInfoAwayTeam;
    TextView textViewGameInfoHomeScore;
    TextView textViewGameInfoAwayScore;
    TextView textViewGameInfoMinutes;
    SwipeRefreshLayout swipeLayout;
    int homeScore = 0;
    int awayScore = 0;
    String gameID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_info);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeGameInfo);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Check if user is connected to internet
                final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
                if (!(activeNetwork != null && activeNetwork.isConnected())) {
                    // if user is offline, redirect them to network settings
                    displayToast("Please connect to either wifi or a mobile network then try again");
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else {
                    // Update games if connected to internet
                    updateGames();
                }

                // Stop the refresh animation
                swipeLayout.setRefreshing(false);
            }
        });

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

    // Displays a toast with passed in message
    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    private void updateGames() {
        new GetAllGames().execute(MainActivity.SERVER_ADDRESS + "get_all_games.php");
    }

    // Retrieves all the games from the database
    private class GetAllGames extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            ArrayList<Game> games = new ArrayList<Game>();
            String result = "";

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost((String) objects[0]);

                // Return all games between start and end dates
                HttpResponse response = httpclient.execute(httppost);

                // Retrieve json data to be processed
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();
                result=sb.toString();
            } catch (Exception e) {
                Log.e("log_tag", "Error retrieving data " + e.toString());
            }

            try{
                // Check if any games were retrieved. This prevents most JSONExceptions.
                if (!result.equals("")) {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        ArrayList<ScoringPlay> scoringPlays = new ArrayList<ScoringPlay>();
                        JSONObject json = jsonArray.getJSONObject(i);
                        // Retrieve String containing JSONArray of JSONArrays each containing
                        // minutesPlayed, play and description
                        String jsonString = json.getString("scoringPlays");
                        // Get JSONArray from String
                        JSONArray jsonArray1 = new JSONArray(jsonString);
                        for (int j = 0; j < jsonArray1.length(); j++) {
                            // Get JSONArray from JSONArray
                            JSONArray object = jsonArray1.getJSONArray(j);
                            // Get minutesPlayed, scoring play and description from inner JSONArray
                            // and create a ScoringPlay object with them then add it to ArrayList of scoringPlays
                            // for this game
                            scoringPlays.add(new ScoringPlay(object.getInt(0), object.getString(1), object.getString(2)));
                        }
                        // Create Game from retrieved info and add it to games ArrayList
                        games.add(new Game(json.getLong("GameID"), json.getString("homeTeamName"), json.getInt("homeTeamScore"),
                                json.getString("awayTeamName"), json.getInt("awayTeamScore"), json.getString("location"),
                                json.getInt("minutesPlayed"), json.getString("time"), scoringPlays));
                    }
                }
            }catch(JSONException e){
                Log.e("log_tag", "Error parsing data " + e.toString());
            }

            // Return ArrayList with every game stored in database
            DrawFragmentActivity.games = games;

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            recreate();
            // Display toast informing user that all games have been updated
            displayToast("Game Updated");
        }
    }

}
