package chris.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class GameSelectionActivity extends Activity {

    Context mContext;
    ArrayAdapter<String> divisionAdapter;
    ArrayAdapter<String> teamsAdapter;
    Spinner spinnerDivision;
    Spinner spinnerHomeTeam;
    Spinner spinnerAwayTeam;
    Button buttonSelectGame;
    String gameID;
    String message;

    List<String> divisions = Arrays.asList("Div 1", "Div 2", "Div 3", "Colts", "U18", "U16", "U14.5", "U13", "U11.5");
    List<String> teamsDiv1 = Arrays.asList("Waihora", "Lincoln", "Raikaia", "Methven", "Southbridge", "Burn/Duns/Irwell", "Glenmark", "Darfield",
            "Ashley", "Prebbleton", "Celtic", "Saracens", "Oxford", "Ohoka", "Kaiapoi", "West Melton", "Southern", "Hampstead");
    List<String> teamsDiv2 = Arrays.asList("Springston", "West Melton", "Diamond Harbour", "Leeston", "Darfield", "Selwyn", "Banks Peninsula",
            "Southbridge", "Hornby", "Kirwee", "Rolleston", "Lincoln", "Prebbleton", "Burn/Duns/Irwell");
    List<String> teamsDiv3 = Arrays.asList("Hornby", "Waihora", "Kirwee", "Springston", "Burn/Duns/Irwell", "Lincoln");
    List<String> teamsColts = Arrays.asList("Banks Peninsula", "Waihora", "Prebbleton", "Celtic", "Rolleston", "Lincoln", "West Melton", "Darfield",
            "Springston", "Kirwee");
    List<String> teamsU18 = Arrays.asList("Malvern Combined", "Waihora", "Springston/Southbridge", "Meth/Allen/Rak", "Tinwald/Celtic");
    List<String> teamsU16 = Arrays.asList("Ashley", "Oxford", "Waihora", "Woodend/Ohoka", "Rolleston", "Prebbleton", "West Melton", "Celtic",
            "Malvern Combined", "Lincoln", "Kaiapoi", "Harlequins");
    List<String> teamsU145 = Arrays.asList("Rolleston", "Prebbleton", "Malvern Combined", "West Melton", "Waihora", "Lincoln", "Duns/Southbr/Leest/Irwell");
    List<String> teamsU13 = Arrays.asList("Rolleston Black", "Rolleston Gold", "West Melton", "Lincoln", "Waihora", "Duns/Irwell/Leeston",
            "Prebbleton White", "Springston/Southbridge", "Prebbleton Blue", "Darfield");
    List<String> teamsU115 = Arrays.asList("Prebbleton Black", "Rolleston Black", "Rolleston Gold", "Lincoln", "Southbridge", "Waihora White",
            "Duns/Irwell/Sprinst", "Selwyn/Sheffield", "West Melton", "Prebbleton Blue", "Prebbleton White", "Waihora Black", "Banks Peninsula",
            "Leeston", "Darfield/Kirwee");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selection);

        // Make the activity full screen by removing actionbar
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Store reference to activity context
        mContext = this;

        // Sort all the team collections alphabetically
        Collections.sort(teamsDiv1);
        Collections.sort(teamsDiv2);
        Collections.sort(teamsDiv3);
        Collections.sort(teamsColts);
        Collections.sort(teamsU18);
        Collections.sort(teamsU16);
        Collections.sort(teamsU145);
        Collections.sort(teamsU13);
        Collections.sort(teamsU115);

        spinnerDivision = (Spinner) findViewById(R.id.spinnerDivisionSelect);
        spinnerHomeTeam = (Spinner) findViewById(R.id.spinnerHomeTeamSelect);
        spinnerAwayTeam = (Spinner) findViewById(R.id.spinnerAwayTeamSelect);
        buttonSelectGame = (Button) findViewById(R.id.buttonSelectGame);

        teamsAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, teamsDiv1);
        teamsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerHomeTeam.setAdapter(teamsAdapter);
        spinnerAwayTeam.setAdapter(teamsAdapter);

        divisionAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerDivision.setAdapter(divisionAdapter);
        spinnerDivision.setOnItemSelectedListener(divisionItemClickListener);

        buttonSelectGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get current internet connection status
                final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
                if (!(activeNetwork != null && activeNetwork.isConnected())) {
                    // If user not connected to internet then redirect them to wifi settings
                    displayToast("Please connect to either wifi or a mobile network then click button again");
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else {
                    // If user is connected to internet then start ScoreGameFragmentActivity
                    gameID = createGameID();
                    new CheckGameExists().execute(MainActivity.SERVER_ADDRESS + "game_exists.php", gameID);
                }
            }
        });
    }

    // AsyncTask which checks if game exists using php script on server
    private class CheckGameExists extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            // Create gameID and retrieve game details
            String gameID = (String) objects[1];

            HttpClient httpclient = new DefaultHttpClient();
            // Create HttpPost with script server address passed to asynctask
            HttpPost httppost = new HttpPost((String) objects[0]);
            try {
                // Add gameID to List<NameValuePair> and add to HttpPost
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("gameID", gameID));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HttpPost and store response
                HttpResponse response = httpclient.execute(httppost);

                // Convert response into String
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                String line = reader.readLine();
                is.close();
                System.out.println(line);

                // Trim unnecessary characters from response String
                message = line.trim();
            } catch (Exception e) {
                System.out.println("CreateGameActivity: " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            // Display a toast based on whether game was added to database. If success isn't returned
            // then there is a problem with the php script
            if (!message.equals("success")) {
                // If game doesn't exist in database, then give the option to create it
                String division = spinnerDivision.getSelectedItem().toString();
                String homeTeam = spinnerHomeTeam.getSelectedItem().toString();
                String awayTeam = spinnerAwayTeam.getSelectedItem().toString();

                // Create and show alert dialog asking the user if the game info is correct.
                new AlertDialog.Builder(mContext)
                    .setTitle("Confirm Game Details")
                    .setMessage("Division: " + division + "\nHome Team: " + homeTeam +
                            "\nAway Team: " + awayTeam + "\n\nIs this correct?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Add the new game to the database by calling the SendTask asynctask
                            new SendTask().execute(MainActivity.SERVER_ADDRESS + "create_game.php");
                            // Then start ScoreGameFragmentActivity
                            Intent intent = new Intent(GameSelectionActivity.this, ScoreGameFragmentActivity.class);
                            intent.putExtra("gameID", gameID);
                            intent.putExtra("homeTeam", spinnerHomeTeam.getSelectedItem().toString());
                            intent.putExtra("awayTeam", spinnerAwayTeam.getSelectedItem().toString());
                            startActivity(intent);
                            message = "";
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close the dialog box
                            // and do nothing
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
            } else {
                // If game exists, start ScoreGameFragmentActivity
                Intent intent = new Intent(GameSelectionActivity.this, ScoreGameFragmentActivity.class);
                intent.putExtra("gameID", gameID);
                intent.putExtra("homeTeam", spinnerHomeTeam.getSelectedItem().toString());
                intent.putExtra("awayTeam", spinnerAwayTeam.getSelectedItem().toString());
                startActivity(intent);
                message = "";
            }
        }
    }

    // Displays a toast with passed in message
    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Create gameID from todays date, teams and division
    private String createGameID() {
        String gameID = "";

        // Add date to gameID
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        gameID += timeStamp.substring(0,8);

        // Add teamIDs to gameID. If teamID less than 10,
        // add a 0 to preserve gameID length
        if (spinnerHomeTeam.getSelectedItemPosition() < 10) { gameID += "0"; }
        gameID += String.valueOf(spinnerHomeTeam.getSelectedItemPosition());
        if (spinnerAwayTeam.getSelectedItemPosition() < 10) { gameID += "0"; }
        gameID += String.valueOf(spinnerAwayTeam.getSelectedItemPosition());

        // Add divisionID to gameID.
        if (spinnerDivision.getSelectedItemPosition() < 10) { gameID += "0"; }
        gameID += String.valueOf(spinnerDivision.getSelectedItemPosition());

        System.out.println(gameID);
        return gameID;
    }

    // Whenever the division is changed, the team lists in the spinners are changed
    private AdapterView.OnItemSelectedListener divisionItemClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            switch (position) {
                case 0:
                    teamsAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, teamsDiv1);
                    break;
                case 1:
                    teamsAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, teamsDiv2);
                    break;
                case 2:
                    teamsAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, teamsDiv3);
                    break;
                case 3:
                    teamsAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, teamsColts);
                    break;
                case 4:
                    teamsAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, teamsU18);
                    break;
                case 5:
                    teamsAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, teamsU16);
                    break;
                case 6:
                    teamsAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, teamsU145);
                    break;
                case 7:
                    teamsAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, teamsU13);
                    break;
                case 8:
                    teamsAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, teamsU115);
                    break;
            }

            spinnerHomeTeam.setAdapter(teamsAdapter);
            spinnerAwayTeam.setAdapter(teamsAdapter);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    };

    // AsyncTask which uploads data to php script
    private class SendTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            // Create gameID and retrieve game details
            String homeTeam = spinnerHomeTeam.getSelectedItem().toString();
            String awayTeam = spinnerAwayTeam.getSelectedItem().toString();
            String location = homeTeam;
            String time = "12pm";

            // Check if all necessary details have been entered
            if (!homeTeam.equals("") && !awayTeam.equals("") && !location.equals("") && !time.equals("")) {
                HttpClient httpclient = new DefaultHttpClient();

                // Create HttpPost with script server address passed to asynctask
                HttpPost httppost = new HttpPost((String) objects[0]);
                try {
                    // Add all game details to List<NameValuePair> and add to HttpPost
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("gameID", gameID));
                    nameValuePairs.add(new BasicNameValuePair("homeTeam", homeTeam));
                    nameValuePairs.add(new BasicNameValuePair("awayTeam", awayTeam));
                    nameValuePairs.add(new BasicNameValuePair("location", location));
                    nameValuePairs.add(new BasicNameValuePair("time", time));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HttpPost and store response
                    HttpResponse response = httpclient.execute(httppost);

                    // Convert response into String
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                    String line = reader.readLine();
                    is.close();
                    System.out.println(line);

                    // Trim unnecessary characters from response String
                    message = line.trim();
                } catch (Exception e) {
                    System.out.println("CreateGameActivity: " + e.toString());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            // Display a toast based on whether game was added to database. If success isn't returned
            // then there is a problem with the php script
            if (message.equals("success")) {
                displayToast("Game Created");
                message = "";
            } else {
                displayToast("Game wasn't Created. Please contact app creator.");
                message = "";
            }
        }
    }
}

















