package chris.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This activity allows the user to populate the database hosted on a server.
 */
public class CreateGameActivity extends ActionBarActivity {

    Context mContext;
    ArrayAdapter<String> divisionAdapter;
    ArrayAdapter<String> teamsAdapter;
    ArrayAdapter<String> locationAdapter;
    Spinner spinnerDivision;
    Spinner spinnerHomeTeam;
    Spinner spinnerAwayTeam;
    Spinner spinnerLocation;
    Button buttonCreateGame;
    Button buttonSetDate;
    Button buttonSetTime;
    TextView textViewTime;
    TextView textViewDate;
    TextView textViewDateString;

    // Stores message from database
    String message;

    // Data to be used in spinners
    public static List<String> divisions = Arrays.asList("Div 1", "Div 2", "Div 3", "Colts", "U18", "U16", "U14.5", "U13", "U11.5");
    List<String> locations = Arrays.asList("CC Upper 1", "Darfield 1", "Darfield 2", "Rolleston 1a", "Rolleston 1b", "Rolleston 2", "Rolleston 3", "Rolleston 4", "Rolleston 2a", "Rolleston 2b", "Rolleston 5b", "Rolleston 5a");
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
        setContentView(R.layout.activity_create_game);

//        // Make the activity full screen by removing actionbar
//        final ActionBar actionBar = getActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowHomeEnabled(false);
//            actionBar.setDisplayShowTitleEnabled(false);
//        }

        // Store reference to activity context
        mContext = this;

        // Sort all the collections alphabetically
        Collections.sort(teamsDiv1);
        Collections.sort(teamsDiv2);
        Collections.sort(teamsDiv3);
        Collections.sort(teamsColts);
        Collections.sort(teamsU18);
        Collections.sort(teamsU16);
        Collections.sort(teamsU145);
        Collections.sort(teamsU13);
        Collections.sort(teamsU115);

        // Create reference to all the controls in the layout
        spinnerDivision = (Spinner) findViewById(R.id.spinnerDivision);
        spinnerHomeTeam = (Spinner) findViewById(R.id.spinnerHomeTeam);
        spinnerAwayTeam = (Spinner) findViewById(R.id.spinnerAwayTeam);
        spinnerLocation = (Spinner) findViewById(R.id.spinnerLocation);
        buttonCreateGame = (Button) findViewById(R.id.buttonCreateGame);
        buttonSetDate = (Button) findViewById(R.id.buttonChangeDate);
        buttonSetTime = (Button) findViewById(R.id.buttonChangeTime);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        textViewDateString = (TextView) findViewById(R.id.textViewDateString);
        textViewTime = (TextView) findViewById(R.id.textViewTime);

        // Create adapters, populate them and connect them to the spinners
        teamsAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, teamsDiv1);
        teamsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerHomeTeam.setAdapter(teamsAdapter);
        spinnerAwayTeam.setAdapter(teamsAdapter);

        divisionAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerDivision.setAdapter(divisionAdapter);
        spinnerDivision.setOnItemSelectedListener(divisionItemClickListener);

        locationAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, locations);
        locationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);

        // Call validity check when Create Game button clicked
        buttonCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmGame();
            }
        });

        // Initialise DatePickerDialog with date stored in hidden
        // date string textview and then display it.
        buttonSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = textViewDateString.getText().toString();
                int year = Integer.parseInt(s.substring(0, 4));
                int month = Integer.parseInt(s.substring(4,6)) - 1;
                int day = Integer.parseInt(s.substring(6,8));
                new DatePickerDialog(CreateGameActivity.this, onDateSet, year, month, day).show();
            }
        });

        // Initialise TimePickerDialog with noon if textViewTime empty
        // or previously selected time and then display it.
        buttonSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = textViewTime.getText().toString();
                int hour = 12;
                int minute = 0;
                if (!s.equals("")) {
                    hour = Integer.parseInt(s.substring(0, s.lastIndexOf(":")));
                    hour += (s.substring(s.length() - 2, s.length()).equals("pm") ? 12 : 0);
                    minute = Integer.parseInt(s.substring(s.length() - 4, s.length() - 2));
                }
                new TimePickerDialog(CreateGameActivity.this, onTimeSet, hour, minute, false).show();
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

    // Create OnTimeSetListener which sets textViewTime to time in format HH:MM (12 hour)
    private TimePickerDialog.OnTimeSetListener onTimeSet = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            String time = (hour > 12 ? String.valueOf(hour - 12) : String.valueOf(hour));
            time +=  ":" + pad(minute);
            time += (hour >= 12 ? "pm" : "am");
            textViewTime.setText(time);
        }
    };

    // Confirmation of the game details to make sure it's all correct
    private void confirmGame() {
        // Retrieve game details
        String homeTeam = spinnerHomeTeam.getSelectedItem().toString();
        String awayTeam = spinnerAwayTeam.getSelectedItem().toString();
        String location = spinnerLocation.getSelectedItem().toString();
        String time = textViewTime.getText().toString();
        String date = textViewDate.getText().toString();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Confirm Game Details");

        // set dialog message
        alertDialogBuilder
                .setMessage("Home Team: " + homeTeam + "\nAway Team: " + awayTeam + "\nLocation: " +
                        location + "\nTime: " + time + "\nDate: " + date + "\n\nIs this correct?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
                        if (!(activeNetwork != null && activeNetwork.isConnected())) {
                            // if yes is clicked and user is offline, redirect them to network settings
                            displayToast("Please connect to either wifi or a mobile network");
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        } else {
                            new SendTask().execute(MainActivity.SERVER_ADDRESS + "create_game.php");
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close the dialog box
                        // and display toast notifying user game wasn't created
                        dialog.cancel();
                        displayToast("Game Not Created");
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    // Displays a toast with passed in message
    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // AsyncTask which uploads data to php script
    private class SendTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            // Create gameID and retrieve game details
            String gameID = createGameID();
            String homeTeam = spinnerHomeTeam.getSelectedItem().toString();
            String awayTeam = spinnerAwayTeam.getSelectedItem().toString();
            String location = spinnerLocation.getSelectedItem().toString();
            String time = textViewTime.getText().toString();

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

    // Create gameID from date, teams and division
    private String createGameID() {
        String gameID = "";

        // Add date to gameID
        gameID += textViewDateString.getText().toString();

        // Add teamIDs to gameID. If teamID less than 10,
        // add a 0 to preserve gameID length
        gameID += pad(spinnerHomeTeam.getSelectedItemPosition());
        gameID += pad(spinnerAwayTeam.getSelectedItemPosition());

        // Add divisionID to gameID.
        gameID += pad(spinnerDivision.getSelectedItemPosition());

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

    // Pads single digit ints with a leading zero to keep 2 character length
    private String pad(int c) {
        return c >= 10 ? String.valueOf(c) : "0" + String.valueOf(c);
    }
}