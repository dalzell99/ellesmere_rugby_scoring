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
    Button buttonScoreGameEnd;
    Button buttonDraw;

    public static String SERVER_ADDRESS = "http://www.possumpam.com/rugby-scoring-app-scripts/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonAddGames  = (Button) findViewById(R.id.buttonAddGames);
        buttonScoreGame = (Button) findViewById(R.id.buttonScoreGame);
        buttonScoreGameEnd = (Button) findViewById(R.id.buttonScoreGameEnd);
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

        buttonScoreGameEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GameSelectionEndActivity.class));
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
