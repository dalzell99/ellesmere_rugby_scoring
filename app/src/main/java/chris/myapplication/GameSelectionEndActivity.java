package chris.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class GameSelectionEndActivity extends ActionBarActivity {

    Context mContext;
    ArrayAdapter<String> divisionAdapter;
    ArrayAdapter<String> teamsAdapter;
    Spinner spinnerDivision;
    Spinner spinnerHomeTeam;
    Spinner spinnerAwayTeam;
    EditText editTextHomeEndScore;
    EditText editTextAwayEndScore;
    Button buttonSubmitScoreGameEnd;
    Button buttonSetDate;
    TextView textViewDate;
    TextView textViewDateString;

    public static int homeScore;
    public static int awayScore;
    String gameID;
    String message = "";

    // Data to be used in spinners
    public static List<String> divisions = Arrays.asList("Div 1", "Div 2", "Div 3", "Colts", "U18", "U16", "U14.5", "U13", "U11.5");
    List<String> teamsDiv1 = Arrays.asList("Hornby", "Waihora", "Lincoln", "Raikaia", "Methven", "Southbridge", "Burn/Duns/Irwell", "Glenmark", "Darfield",
            "Ashley", "Prebbleton", "Celtic", "Saracens", "Oxford", "Ohoka", "Kaiapoi", "West Melton", "Southern", "Hampstead", "Rolleston");
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
        setContentView(R.layout.activity_score_game_end);

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

        spinnerDivision = (Spinner) findViewById(R.id.spinnerDivisionSelectEnd);
        spinnerHomeTeam = (Spinner) findViewById(R.id.spinnerHomeTeamSelectEnd);
        spinnerAwayTeam = (Spinner) findViewById(R.id.spinnerAwayTeamSelectEnd);
        editTextHomeEndScore = (EditText) findViewById(R.id.editTextHomeEndScore);
        editTextAwayEndScore = (EditText) findViewById(R.id.editTextAwayEndScore);
        buttonSubmitScoreGameEnd = (Button) findViewById(R.id.buttonSubmitScoreGameEnd);
        buttonSetDate = (Button) findViewById(R.id.buttonChangeDateEnd);
        textViewDate = (TextView) findViewById(R.id.textViewDateEnd);
        textViewDateString = (TextView) findViewById(R.id.textViewDateStringEnd);

        teamsAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, teamsDiv1);
        teamsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerHomeTeam.setAdapter(teamsAdapter);
        spinnerAwayTeam.setAdapter(teamsAdapter);

        divisionAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerDivision.setAdapter(divisionAdapter);
        spinnerDivision.setOnItemSelectedListener(divisionItemClickListener);

        buttonSubmitScoreGameEnd.setOnClickListener(new View.OnClickListener() {
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

        // Initialise dates to todays date. textViewDate displays user friendly date
        // and textViewDateString contains date used for gameID.
        Calendar calendar = Calendar.getInstance();
        textViewDate.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " " + Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov", "Dec").get(calendar.get(Calendar.MONTH)) + " " + String.valueOf(calendar.get(Calendar.YEAR)));
        String date = "";
        date += String.valueOf(calendar.get(Calendar.YEAR));
        date += pad(calendar.get(Calendar.MONTH) + 1);
        date += pad(calendar.get(Calendar.DAY_OF_MONTH));
        textViewDateString.setText(date);

        // Initialise DatePickerDialog with todays date and then display it.
        buttonSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(GameSelectionEndActivity.this, onDateSet,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
    }

    // Create OnDateSetListener which sets hidden textViewDateString to date in YYYYMMDD format and
    // sets textViewDate to user friendly date in format DD MMM YY
    private DatePickerDialog.OnDateSetListener onDateSet = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            String date = "";
            date += String.valueOf(year);
            date += pad(month + 1);
            date += pad(day);
            textViewDateString.setText(date);

            date = String.valueOf(day) + " " + Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                    "Aug", "Sep", "Oct", "Nov", "Dec").get(month) + " " + String.valueOf(year).substring(2,4);
            textViewDate.setText(date);
        }
    };

    // Pads single digit ints with a leading zero to keep 2 character length
    private String pad(int c) {
        return c >= 10 ? String.valueOf(c) : "0" + String.valueOf(c);
    }

    // AsyncTask which checks if game exists using php script on server
    private class CheckGameExists extends AsyncTask {

        @Override
        protected Boolean doInBackground(Object[] objects) {
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
            if (message.equals("Doesn't Exist")) {
                // If game doesn't exist in database, then give the option to create it
                final String division = spinnerDivision.getSelectedItem().toString();
                final String homeTeam = spinnerHomeTeam.getSelectedItem().toString();
                final String awayTeam = spinnerAwayTeam.getSelectedItem().toString();

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
                            message = "";
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close the dialog box
                            // and do nothing
                            dialog.cancel();
                            message = "";
                        }
                    })
                    .create()
                    .show();
            } else {
                // If game exists, check if it game has been live scored.
                new CheckForLiveScoring().execute(MainActivity.SERVER_ADDRESS + "get_game.php");
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

        // Add date to gameID.
        gameID += textViewDateString.getText();

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

    // Retrieves game info from server based on gameID
    private class CheckForLiveScoring extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            String result = "";
            HttpClient httpclient = new DefaultHttpClient();

            // Create HttpPost with script server address passed to asynctask
            HttpPost httppost = new HttpPost((String) objects[0]);
            try {
                // Add all game details to List<NameValuePair> and add to HttpPost
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("gameID", gameID));
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
                result = sb.toString();
            } catch (Exception e) {
                System.out.println("ScoreGameFragmentActivity: " + e.toString());
            }

            try{
                // Check if any games were retrieved. This prevents most JSONExceptions.
                if (!result.equals("")) {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject json = jsonArray.getJSONObject(0);
                    homeScore = json.getInt("homeTeamScore");
                    awayScore = json.getInt("awayTeamScore");
                }
            }catch(JSONException e){
                Log.e("log_tag", "Error parsing data " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            checkIfLiveScored();
        }
    }

    private void checkIfLiveScored() {
        if (homeScore != 0 || awayScore != 0) {
            // Create and show alert dialog asking the user if the game info is correct.
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Game Details")
                    .setMessage("Current score stored online:" + "\n" +
                            spinnerHomeTeam.getSelectedItem() + " " + homeScore + "\n" +
                            spinnerAwayTeam.getSelectedItem() + " " + awayScore + "\n\n" +
                            "Do you want to change the score to:" + "\n" +
                            spinnerHomeTeam.getSelectedItem() + " " + editTextHomeEndScore.getText() + "\n" +
                            spinnerAwayTeam.getSelectedItem() + " " + editTextAwayEndScore.getText())
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new UploadGameEndScore().execute(MainActivity.SERVER_ADDRESS + "final_game_score.php");
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if "No" button is clicked, just close the dialog box
                            // and do nothing
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
        } else {
            new UploadGameEndScore().execute(MainActivity.SERVER_ADDRESS + "final_game_score.php");
        }
    }

    // AsyncTask which uploads final game score to php script
    private class UploadGameEndScore extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient httpclient = new DefaultHttpClient();

            // Create HttpPost with script server address passed to asynctask
            HttpPost httppost = new HttpPost((String) objects[0]);
            try {
                // Add all game details to List<NameValuePair> and add to HttpPost
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("gameID", gameID));
                nameValuePairs.add(new BasicNameValuePair("homeScore", String.valueOf(editTextHomeEndScore.getText())));
                nameValuePairs.add(new BasicNameValuePair("awayScore", String.valueOf(editTextAwayEndScore.getText())));
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
            if (message.equals("success")) {
                displayToast("Game Updated");
                message = "";
            } else {
                displayToast("Game wasn't updated. Please contact app creator.");
                message = "";
            }
        }
    }

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
            new UploadGameEndScore().execute(MainActivity.SERVER_ADDRESS + "final_game_score.php");
        }
    }
}

















