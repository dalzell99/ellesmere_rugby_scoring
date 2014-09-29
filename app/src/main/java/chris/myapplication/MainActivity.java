package chris.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    Button buttonAddGames;
    Button buttonScoreGame;
    Button buttonDraw;

    public static String SERVER_ADDRESS = "http://www.possumpam.com/rugby-scoring-app-scripts/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (!(activeNetwork != null && activeNetwork.isConnected())) {
            // If user is offline, redirect them to wifi settings
            displayToast("Please connect to either wifi or a mobile network");
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }

        buttonAddGames  = (Button) findViewById(R.id.buttonAddGames);
        buttonScoreGame = (Button) findViewById(R.id.buttonScoreGame);
        buttonDraw      = (Button) findViewById(R.id.buttonDraw);

        buttonAddGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CreateGameActivity.class));
            }
        });

        buttonDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DrawFragmentActivity.class));
            }
        });

        buttonScoreGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GameSelectionActivity.class));
            }
        });
    }

    // Displays a toast with passed in message
    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
