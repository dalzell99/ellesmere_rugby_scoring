package chris.myapplication;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;

public class DrawFragmentActivity extends ActionBarActivity {

    /**
     * Sets the date for the start of the season. This is used for displaying
     * games by week.
     */
    public static final Calendar startFirstWeek = new GregorianCalendar(2015, 3, 2);

    /**
     * Contains all the game objects to be used in this activity
     */
    public static ArrayList<Game> games;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public static DrawPagerAdapter mDrawPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    public static SlidingTabLayout mSlidingTabLayout;

    /**
     * Create the activity. Sets up an {@link android.app.ActionBar} with tabs, and then configures the
     * {@link ViewPager} contained inside R.layout.activity_draw_fragment.
     *
     * <p>A {@link DrawPagerAdapter} will be instantiated to hold the different pages of
     * fragments that are to be displayed. A
     * {@link android.support.v4.view.ViewPager.SimpleOnPageChangeListener} will also be configured
     * to receive callbacks when the user swipes between pages in the ViewPager.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_fragment);

        try {
            final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
            if (!(activeNetwork != null && activeNetwork.isConnected())) {
                // if yes is clicked and user is offline, redirect them to network settings
                displayToast("Please connect to either wifi or a mobile network then reopen the draw");
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            } else {
                // Retrieve arraylist of every game stored in database from asynctask
                // and store it in static games variable
                games = (ArrayList<Game>) new GetAllGames().execute(MainActivity.SERVER_ADDRESS + "get_all_games.php").get();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Displays a toast with passed in message
    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * The games must be retrieved before the fragments can be created.
     */
    private void finishCreate() {
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mDrawPagerAdapter = new DrawPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDrawPagerAdapter);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    // Retrieves all the games from the database
    private class GetAllGames extends AsyncTask {

        @Override
        protected ArrayList<Game> doInBackground(Object[] objects) {
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
            return games;
        }

        @Override
        protected void onPostExecute(Object o) {
            // Finish creating view. This prevents fragments being created before
            // data has been retrieved.
            finishCreate();
        }
    }
}