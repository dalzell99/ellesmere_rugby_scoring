package chris.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class ScoringPlaysFragment extends Fragment {

    LinearLayout tableLayoutScoringPlays;
    int homeScore = 0;
    int awayScore = 0;
    int id = 0;

    public ScoringPlaysFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game_info, container, false);
        tableLayoutScoringPlays = (LinearLayout) rootView.findViewById(R.id.linearLayoutScoringPlays);

        homeScore = 0;
        awayScore = 0;

        // Add each scoring play one by one by inflating a scoringplay_linearlayout LinearLayout and
        // setting the text for all the textviews
        for (ScoringPlay s : ScoreGameFragmentActivity.scoringPlays) {
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.scoringplay_linearlayout,
                    tableLayoutScoringPlays, false);
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

            // Set id so scoring play can be identified when clicked
            linearLayout.setId(id);
            id += 1;

            linearLayout.setOnClickListener(scoringPlayClickListener);

            // Add this linear layout to main linear layout
            tableLayoutScoringPlays.addView(linearLayout);
        }

        id = 0;

        return rootView;
    }

    private View.OnClickListener scoringPlayClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View view) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

            // set title
            alertDialogBuilder.setTitle("Delete Scoring Play");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Do you want to delete this scoring play?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if yes clicked call deleteScoringPlay method
                            deleteScoringPlay(view);
                        }
                    })
                    .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
    };

    // Deletes clicked scoring play from local scoringPlays arraylist and server
    private void deleteScoringPlay(View view) {
        // Get clicked rows id
        int viewID = view.getId();
        if (getView() != null) {
            // Get main LinearLayout containing all the scoring plays
            View v = ((ViewGroup) getView()).getChildAt(0);
            // Get specific scoring play LinearLayout to be deleted
            View v1 = ((ViewGroup) v).getChildAt(0);
            // Remove it from main LinearLayout
            ((ViewGroup) v1).removeViewAt(viewID);
            // Remove it from local arraylist
            ScoreGameFragmentActivity.scoringPlays.remove(viewID);
            // Recreate fragments to get correct score.
            ScoreGameFragmentActivity.mScoreGamePagerAdapter.notifyDataSetChanged();
            ScoreGameFragmentActivity.homeScore = homeScore;
            ScoreGameFragmentActivity.awayScore = awayScore;
            // Recreate fragments again to display corrected score. This is done because deleting a
            // scoring play makes the scores stored in ScoreGameFragmentActivity incorrect as they are
            // only changed when a new scoring play is added.
            ScoreGameFragmentActivity.mScoreGamePagerAdapter.notifyDataSetChanged();
            // Delete from server
            new DeleteScoringPlay().execute(MainActivity.SERVER_ADDRESS + "delete_scoring_play.php", viewID);
        }
    }

    // Changes the scores based on team and scoring play type
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

    // Deletes scoring play from server
    private class DeleteScoringPlay extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient httpclient = new DefaultHttpClient();
            // Create HttpPost with script server address passed to asynctask
            HttpPost httppost = new HttpPost((String) objects[0]);
            try {
                // Add all game details to List<NameValuePair> and add to HttpPost
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("gameID", ScoreGameFragmentActivity.gameID));
                nameValuePairs.add(new BasicNameValuePair("scoringPlayIndex", String.valueOf(objects[1])));
                nameValuePairs.add(new BasicNameValuePair("homeScore", String.valueOf(homeScore)));
                nameValuePairs.add(new BasicNameValuePair("awayScore", String.valueOf(awayScore)));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HttpPost and store response
                HttpResponse response = httpclient.execute(httppost);

                // Convert response into String
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                System.out.println(sb);
            } catch (Exception e) {
                System.out.println("GameInfoFragment: " + e.toString());
            }

            return null;
        }
    }
}