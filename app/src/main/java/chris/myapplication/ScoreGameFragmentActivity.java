package chris.myapplication;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.List;

public class ScoreGameFragmentActivity extends ActionBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public static ScoreGamePagerAdapter mScoreGamePagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * Variable to store gameID and team names for use by fragments
     */
    public static String gameID;
    public static String homeTeamName;
    public static String awayTeamName;
    public static int homeScore;
    public static int awayScore;
    public static ArrayList<ScoringPlay> scoringPlays;

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
        setContentView(R.layout.activity_score_game_fragment);

        gameID = getIntent().getStringExtra("gameID");
        homeTeamName = getIntent().getStringExtra("homeTeam");
        awayTeamName = getIntent().getStringExtra("awayTeam");
        scoringPlays = new ArrayList<ScoringPlay>();

        new GetGameInfo().execute(MainActivity.SERVER_ADDRESS + "get_game.php");
    }

    // Retrieves game info from server based on gameID
    private class GetGameInfo extends AsyncTask {

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
                    // Retrieve String containing JSONArray of JSONArrays each containing
                    // minutesPlayed, play and description
                    String jsonString = json.getString("scoringPlays");
                    // Get JSONArray from String
                    JSONArray jsonArray1 = new JSONArray(jsonString);
                    for (int i = 0; i < jsonArray1.length(); i++) {
                        // Get JSONArray from JSONArray
                        JSONArray object = jsonArray1.getJSONArray(i);
                        // Get minutesPlayed, scoring play and description from inner JSONArray
                        // and create a ScoringPlay object with them then add it to ArrayList of scoringPlays
                        // for this game
                        scoringPlays.add(new ScoringPlay(object.getInt(0), object.getString(1), object.getString(2)));
                    }
                }
            }catch(JSONException e){
                Log.e("log_tag", "Error parsing data " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            finishCreate();
        }
    }

    // Finishes onCreate. This prevents fragments being created before game info retrieved.
    public void finishCreate() {
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mScoreGamePagerAdapter = new ScoreGamePagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pagerScoreGame);
        mViewPager.setAdapter(mScoreGamePagerAdapter);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabsScoreGame);
        mSlidingTabLayout.setViewPager(mViewPager);
    }
}