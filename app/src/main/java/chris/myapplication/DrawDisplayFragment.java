package chris.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DrawDisplayFragment extends Fragment {

    // Stores current week number
    int weekNumber;

    // Sets week number to static weekNumber stored in DrawPagerAdapter
    public DrawDisplayFragment() {
        weekNumber = DrawPagerAdapter.weekNumber;
    }

    // Returns view to be displayed in viewpager
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get all games being played in this week
        ArrayList<Game> games = getThisWeeksGames(weekNumber);

        View rootView = inflater.inflate(R.layout.fragment_game_display, container, false);
        TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.tableLayoutScores);
        // Check if there are any games to display for this week.
        if (!games.isEmpty()) {
            // Group the games in each division together by adding games in order
            // based on which division they are in.
            for (int i = 0; i < CreateGameActivity.divisions.size(); i += 1) {
                // Add division title
                View divTitle = inflater.inflate(R.layout.result_division_title, container, false);
                TextView textViewDivTitle = (TextView) divTitle.findViewById(R.id.textViewDivTitle);
                textViewDivTitle.setText(CreateGameActivity.divisions.get(i));
                tableLayout.addView(divTitle);
                for (Game g : games) {
                    // Check if game is in division being displayed
                    if (String.valueOf(g.getGameID()).endsWith(pad(i))) {
                        View tableRow = inflater.inflate(R.layout.result_tablerow, container, false);
                        // Set gameID. This is used when the row is clicked to retrieve
                        // all the game info from the database.
                        final TextView textViewGameID = (TextView) tableRow.findViewById(R.id.textViewGameID);
                        textViewGameID.setText(String.valueOf(g.getGameID()));
                        // Set home team name
                        TextView textViewHomeName = (TextView) tableRow.findViewById(R.id.textViewHomeName);
                        textViewHomeName.setText(g.getHomeTeamName());
                        // Set home team score
                        TextView textViewHomeScore = (TextView) tableRow.findViewById(R.id.textViewHomeScore);
                        textViewHomeScore.setText(String.valueOf(g.getHomeTeamScore()));
                        // Set away team name
                        TextView textViewAwayName = (TextView) tableRow.findViewById(R.id.textViewAwayName);
                        textViewAwayName.setText(g.getAwayTeamName());
                        // Set away team score
                        TextView textViewAwayScore = (TextView) tableRow.findViewById(R.id.textViewAwayScore);
                        textViewAwayScore.setText(String.valueOf(g.getAwayTeamScore()));
                        // Set location of game
                        TextView textViewLocation = (TextView) tableRow.findViewById(R.id.textViewLocation);
                        textViewLocation.setText(String.valueOf(g.getLocation()));
                        // If game hasn't started, set to time game starts. Set to minutes played during game.
                        // If minutesPlayed equals 40, set to "Halftime" and set to "Final" if it equals 80.
                        TextView textViewTime = (TextView) tableRow.findViewById(R.id.textViewTime);
                        if (g.getMinutesPlayed() == 0) {
                            textViewTime.setText(g.getStartTime());
                        } else if (g.getMinutesPlayed() == 40) {
                            textViewTime.setText("Halftime");
                        } else if (g.getMinutesPlayed() == 80) {
                            textViewTime.setText("Final");
                        } else {
                            textViewTime.setText(String.valueOf(g.getMinutesPlayed()) + "mins");
                        }
                        // Add clicklistener to each row which opens activity
                        // with more information about game.
                        tableRow.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), GameInfoActivity.class);
                                intent.putExtra("gameID", textViewGameID.getText().toString());
                                startActivity(intent);
                            }
                        });
                        // Add tablerow to tablelayout
                        tableLayout.addView(tableRow);
                    }
                }
            }
        } else {
            // Display textview stating "No game to display" if games arraylist empty
            TextView textViewNoGames = (TextView) rootView.findViewById(R.id.textViewNoGames);
            textViewNoGames.setVisibility(View.VISIBLE);
            textViewNoGames.setText("No Games to display");
        }
        return rootView;
    }

    // Returns an ArrayList populated with a particular weeks games
    private ArrayList<Game> getThisWeeksGames(int weekNumber) {
        ArrayList<Game> result = new ArrayList<Game>();
        // Get date of first Monday of season
        Calendar calendar = DrawFragmentActivity.startFirstWeek;

        // Get date for start of week (Monday) as int in YYYYMMDD format
        calendar.add(Calendar.DAY_OF_MONTH, 7 * weekNumber);
        int startDate = Integer.parseInt(String.valueOf(calendar.get(Calendar.YEAR)) +
                pad(calendar.get(Calendar.MONTH)) +
                pad(calendar.get(Calendar.DAY_OF_MONTH)));

        // Get date for end of week (Sunday) as int in YYYYMMDD format
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        int endDate = Integer.parseInt(String.valueOf(calendar.get(Calendar.YEAR)) +
                pad(calendar.get(Calendar.MONTH)) +
                pad(calendar.get(Calendar.DAY_OF_MONTH)));

        calendar.add(Calendar.DAY_OF_MONTH, -(6 + 7 * weekNumber));

        // loop through games and find ones with dates between start and end dates
        for (Game g : DrawFragmentActivity.games) {
            // Extract date from gameID by converting to string, get a substring of first
            // 8 characters (YYYYMMDD) then convert back to int to be compared.
            int gameIDDate = Integer.parseInt(String.valueOf(g.getGameID()).substring(0,8));
            if (gameIDDate >= startDate && gameIDDate <= endDate) {
                result.add(g);
            }
        }

        return result;
    }

    // pads single digit ints with a leading zero
    private String pad(long c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}