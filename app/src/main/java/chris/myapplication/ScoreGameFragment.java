package chris.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ScoreGameFragment extends Fragment {

    Button buttonHomeTry;
    Button buttonAwayTry;
    Button buttonHomeConversion;
    Button buttonAwayConversion;
    Button buttonHomePenalty;
    Button buttonAwayPenalty;
    Button buttonHomeDropGoal;
    Button buttonAwayDropGoal;
    Button sendButton;
    Button buttonHalfTime;
    Button buttonFullTime;
    EditText editTextMinutesPlayed;
    EditText editTextDescription;
    TextView textViewHomeTeam;
    TextView textViewAwayTeam;
    TextView textViewHomeScore;
    TextView textViewAwayScore;

    String gameID;
    String scoringPlay;
    String message;

    public ScoreGameFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // load the layout
        View rootView = inflater.inflate(R.layout.fragment_score_game, container, false);

        gameID = ScoreGameFragmentActivity.gameID;
        scoringPlay = "";
        message = "";

        textViewHomeTeam = (TextView) rootView.findViewById(R.id.textViewHomeTeam);
        textViewAwayTeam = (TextView) rootView.findViewById(R.id.textViewAwayTeam);
        textViewHomeScore = (TextView) rootView.findViewById(R.id.textViewHomeScore);
        textViewAwayScore = (TextView) rootView.findViewById(R.id.textViewAwayScore);
        textViewHomeTeam.setText(ScoreGameFragmentActivity.homeTeamName);
        textViewAwayTeam.setText(ScoreGameFragmentActivity.awayTeamName);
        textViewHomeScore.setText(String.valueOf(ScoreGameFragmentActivity.homeScore));
        textViewAwayScore.setText(String.valueOf(ScoreGameFragmentActivity.awayScore));

        editTextMinutesPlayed = (EditText) rootView.findViewById(R.id.editTextMinutesPlayed);
        editTextDescription = (EditText) rootView.findViewById(R.id.editTextDescription);
        sendButton = (Button) rootView.findViewById(R.id.sendButton);
        buttonHomeTry = (Button) rootView.findViewById(R.id.buttonHomeTry);
        buttonAwayTry = (Button) rootView.findViewById(R.id.buttonAwayTry);
        buttonHomeConversion = (Button) rootView.findViewById(R.id.buttonHomeConversion);
        buttonAwayConversion = (Button) rootView.findViewById(R.id.buttonAwayConversion);
        buttonHomePenalty = (Button) rootView.findViewById(R.id.buttonHomePenalty);
        buttonAwayPenalty = (Button) rootView.findViewById(R.id.buttonAwayPenalty);
        buttonHomeDropGoal = (Button) rootView.findViewById(R.id.buttonHomeDropGoal);
        buttonAwayDropGoal = (Button) rootView.findViewById(R.id.buttonAwayDropGoal);
        buttonHalfTime = (Button) rootView.findViewById(R.id.buttonHalfTime);
        buttonFullTime = (Button) rootView.findViewById(R.id.buttonFullTime);

        sendButton.setOnClickListener(otherClickListener);
        buttonHalfTime.setOnClickListener(otherClickListener);
        buttonFullTime.setOnClickListener(otherClickListener);
        buttonHomeTry.setOnClickListener(homeClickListener);
        buttonAwayTry.setOnClickListener(awayClickListener);
        buttonHomeConversion.setOnClickListener(homeClickListener);
        buttonAwayConversion.setOnClickListener(awayClickListener);
        buttonHomePenalty.setOnClickListener(homeClickListener);
        buttonAwayPenalty.setOnClickListener(awayClickListener);
        buttonHomeDropGoal.setOnClickListener(homeClickListener);
        buttonAwayDropGoal.setOnClickListener(awayClickListener);

        // Set button backgrounds to transparent
        resetButtonBackground();

        return rootView;
    }

    private View.OnClickListener otherClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button button = (Button) view;
            if (button.getText().equals("Send")) {
                // If send button clicked, call confirmPlay method
                confirmPlay();
            } else if (button.getText().equals("Half Time")) {
                // If half time button clicked, set minutesPlayed text to 40, set the scoringPlay
                // to halfTime, reset button background then set half time button to grey background
                editTextMinutesPlayed.setText("40");
                scoringPlay = "halfTime";
                resetButtonBackground();
                button.setBackgroundColor(Color.parseColor("#D0D0D0"));
            } else if (button.getText().equals("Full Time")) {
                // If full time button clicked, set minutesPlayed text to 80, set the scoringPlay
                // to fullTime, reset button background then set full time button to grey background
                editTextMinutesPlayed.setText("80");
                scoringPlay = "fullTime";
                resetButtonBackground();
                button.setBackgroundColor(Color.parseColor("#D0D0D0"));
            }
        }
    };

    private View.OnClickListener homeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Set scoring play to home + text value of button without spaces then
            // reset button background then set full time button to grey background
            Button button = (Button) view;
            scoringPlay = "home" + button.getText().toString().replace(" ", "");
            resetButtonBackground();
            button.setBackgroundColor(Color.parseColor("#D0D0D0"));
        }
    };

    private View.OnClickListener awayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Set scoring play to away + text value of button without spaces then
            // reset button background then set full time button to grey background
            Button button = (Button) view;
            scoringPlay = "away" + button.getText().toString().replace(" ", "");
            resetButtonBackground();
            button.setBackgroundColor(Color.parseColor("#D0D0D0"));
        }
    };

    // Display alertdialog with scoring play details so user can confirm they are correct
    private void confirmPlay() {
        if (!scoringPlay.equals("")) {
            String play = scoringPlay.substring(4, scoringPlay.length());
            String team = (scoringPlay.substring(0, 4).equals("home") ?
                    textViewHomeTeam.getText().toString() : textViewAwayTeam.getText().toString());
            String minutesPlayed = editTextMinutesPlayed.getText().toString();
            String description = editTextDescription.getText().toString();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

            // set title
            alertDialogBuilder.setTitle("Confirm Scoring Play");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Team: " + team + "\nPlay: " + play + "\nMinutes Played: " +
                            minutesPlayed + "\nDescription: " + description + "\n\nIs this correct?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final ConnectivityManager conMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
                            if (!(activeNetwork != null && activeNetwork.isConnected())) {
                                // if yes is clicked and user is offline, redirect them to network settings
                                dialog.cancel();
                                displayToast("Please connect to either wifi or a mobile network then try again");
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            } else {
                                // if yes clicked change score and call sendtack asynctask to update database
                                changeScore();
                                new SendTask().execute(MainActivity.SERVER_ADDRESS + "update_game.php");
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close the dialog box and
                            // display toast telling user game wasn't updated.
                            dialog.cancel();
                            displayToast("Game Not Updated");
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        } else {
            displayToast("Please select a scoring play");
        }
    }

    // Displays toast with message
    private void displayToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    // Sets button backgrounds to transparent
    private void resetButtonBackground() {
        buttonHomeTry.setBackgroundColor(Color.TRANSPARENT);
        buttonAwayTry.setBackgroundColor(Color.TRANSPARENT);
        buttonHomeConversion.setBackgroundColor(Color.TRANSPARENT);
        buttonAwayConversion.setBackgroundColor(Color.TRANSPARENT);
        buttonHomePenalty.setBackgroundColor(Color.TRANSPARENT);
        buttonAwayPenalty.setBackgroundColor(Color.TRANSPARENT);
        buttonHomeDropGoal.setBackgroundColor(Color.TRANSPARENT);
        buttonAwayDropGoal.setBackgroundColor(Color.TRANSPARENT);
        buttonHalfTime.setBackgroundColor(Color.TRANSPARENT);
        buttonFullTime.setBackgroundColor(Color.TRANSPARENT);
    }

    // Changes static score variables in ScoreGameFragmentActivity then updates
    // score textviews
    private void changeScore() {
        if (!scoringPlay.equals("")) {
            String team = scoringPlay.substring(0, 4);
            String play = scoringPlay.substring(4, scoringPlay.length());
            if (play.equals("Try")) {
                if (team.equals("home")) {
                    ScoreGameFragmentActivity.homeScore += 5;
                } else {
                    ScoreGameFragmentActivity.awayScore += 5;
                }
            } else if (play.equals("Penalty") || play.equals("DropGoal")) {
                if (team.equals("home")) {
                    ScoreGameFragmentActivity.homeScore += 3;
                } else {
                    ScoreGameFragmentActivity.awayScore += 3;
                }
            } else if (play.equals("Conversion")) {
                if (team.equals("home")) {
                    ScoreGameFragmentActivity.homeScore += 2;
                } else {
                    ScoreGameFragmentActivity.awayScore += 2;
                }
            }

            textViewHomeScore.setText(String.valueOf(ScoreGameFragmentActivity.homeScore));
            textViewAwayScore.setText(String.valueOf(ScoreGameFragmentActivity.awayScore));
        }
    }

    // Sends game info to php script on server to update database
    private class SendTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            String minutesPlayed = editTextMinutesPlayed.getText().toString();
            String description = editTextDescription.getText().toString();

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost((String) objects[0]);
            try {
                // Store game info in List and add to httppost
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("gameID", gameID));
                nameValuePairs.add(new BasicNameValuePair("scoringPlay", scoringPlay));
                nameValuePairs.add(new BasicNameValuePair("minutesPlayed", minutesPlayed));
                nameValuePairs.add(new BasicNameValuePair("description", description));
                nameValuePairs.add(new BasicNameValuePair("homeScore", String.valueOf(ScoreGameFragmentActivity.homeScore)));
                nameValuePairs.add(new BasicNameValuePair("awayScore", String.valueOf(ScoreGameFragmentActivity.awayScore)));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute httppost and retrieve response
                HttpResponse response = httpclient.execute(httppost);

                // Convert response to String
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                System.out.println(sb);
                // Get message informing of successfulness of update
                message = sb.substring(2, sb.length()).trim();
                if (message.equals("success")) {
                    // If update successful, add scoringplay to ScoreGameFragmentActivity.scoringPlays.
                    // Having a locally stored arraylist saves calling server each time scoring plays displayed.
                    ScoreGameFragmentActivity.scoringPlays.add(new ScoringPlay(
                            Integer.parseInt(sb.substring(0, 2)), scoringPlay, description));
                }
            } catch (Exception e) {
                // If there's a problem clientside, display error
                System.out.println("ScoreGameFragment: " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            // If update successful display toast. If not, then php script is broken and
            // person responsible for them should be notified
            scoringPlay = "";
            resetButtonBackground();
            editTextDescription.setText("");
            if (message.equals("success")) {
                displayToast("Game Updated");
                System.out.println(message);
                message = "";
            } else {
                displayToast("Error updating database. Please notify app creator.");
                System.out.println(message);
                message = "";
            }
        }
    }
}