package iss.workshops.memorygame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends AppCompatActivity  {

    private ProgressBar mProgressBar;
    private TextView mLoadingText;
    private int mProgressStatus = 0;

    ImageView currentView = null;

    TextView timerText;
    Button pauseBtn;
    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;
    boolean timerStarted = false;

    //counting cards for game logic

    private int countPair = 0;
    private int cardsOpen = 0;
    private TextView mScore;
    List<Bitmap> matchedCards = new ArrayList<>();

    //create integer array to hardcode position of each image item; duplicate since there's 2 of each image
    int[] position = { 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5 };

    //set current position to -1 at start of game; so it won't overlap with the above positions
    int currentPosition = -1;

    //use this to prevent false clicking
    private long mLastClickTime = 0;

    //pause screen
    private ImageButton mPauseScreen;

    String filepath;
    ArrayList<Bitmap> ids ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);


        shuffleImages();
        Intent intent1=new Intent(this,MainActivity.class);
        GridView gridView = (GridView) findViewById(R.id.gridView);
        GameBoard gameBoard = new GameBoard(this);
        gridView.setAdapter(gameBoard);

        filepath = getIntent().getStringExtra("img_path");
        ids = loadImageFromFile(filepath);

        mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        //score is invisible at first
        mScore = (TextView) findViewById(R.id.Score);
        mScore.setVisibility(View.INVISIBLE);


        //attributes for pause screen
        mPauseScreen = (ImageButton) findViewById(R.id.pauseScreen);
        mPauseScreen.setVisibility(View.INVISIBLE);

        Bitmap bitmap = null;
        Intent intent = new Intent();

       String filepath = getIntent().getStringExtra("img_path");

         loadImageFromFile(filepath);
         MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.audio2);
         gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

          @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

                if (matchedCards.contains(ids.get(position[pos])) || currentPosition == pos) {
                    return;
                }

                if (currentPosition < 0) {
                    cardsOpen = 1;
                    int oneOpen=position[pos];

                    //prevent false clicking for 1 second after every 2 cards are selected
                    long currentClickTime = SystemClock.uptimeMillis();
                    long elapsedTime = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;
                    if (elapsedTime <= 1000) {
                        return;
                    }

                    currentPosition = pos;
                    currentView = (ImageView) view;
                    currentView.setImageBitmap(ids.get(oneOpen));

                }

                else {
                    cardsOpen = 2;
                    int twoOpen=position[pos];

                    if (position[currentPosition] != position[pos]) {
                        ((ImageView) view).setImageBitmap(ids.get(twoOpen));
                        Toast toast = Toast.makeText(getApplicationContext(), "Not a match", Toast.LENGTH_SHORT);
                        toast.setGravity(0, 0, 0);
                        toast.show();
                        ((ImageView)view).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                toast.cancel();
                            }
                        },500);

                        //after showing both unmatched cards for 0.5second, flip both back
                        ((ImageView) view).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                currentView.setImageResource(R.drawable.placeholder1);
                                ((ImageView) view).setImageResource(R.drawable.placeholder1);
                            }
                        }, 500);
                    }

                    else if (position[currentPosition] == position[pos] && cardsOpen == 2){
                        //int i=position[pos];
                        currentView.setImageBitmap(ids.get(twoOpen));
                        ((ImageView) view).setImageBitmap(ids.get(twoOpen));
                        if (!matchedCards.contains(ids.get(twoOpen))) {
                            matchedCards.add(ids.get(twoOpen));
                            countPair++;
                            //display score
                            mScore.setText(countPair + " " + getString(R.string.score_count));
                            mScore.setVisibility(View.VISIBLE);

                            //PROGRESS BAR increases by 17 with every match
                            mProgressStatus += 17;
                            mProgressBar.setProgress(mProgressStatus);
                            mProgressBar.setVisibility(View.VISIBLE);

                            //after showing both matched cards for 0.5second, matched cards disappear
                            ((ImageView) view).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    currentView.setImageAlpha(100);
                                    ((ImageView) view).setImageAlpha(100);
                                }
                            }, 500);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    mPlayer.start();
                                }
                            }).start();
                        }

                        if (countPair < 6) {
                            Toast toast1 = Toast.makeText(getApplicationContext(), "Matched!", Toast.LENGTH_SHORT);
                            toast1.setGravity(0, 0, 0);
                            toast1.show();
                            ((ImageView) view).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    toast1.cancel();
                                }
                            }, 500);
                        }

                        else {
                            Toast toast2 = Toast.makeText(getApplicationContext(), "YOU WIN!", Toast.LENGTH_SHORT);
                            toast2.setGravity(0, 0, 0);
                            toast2.show();
                          //stop timer once player wins
                            timerStarted = false;
                            timer.cancel();
                             finishAffinity();
                            startActivity(intent1);
                        }
                    }
                    currentPosition = -1;
                    cardsOpen = 0;
                }
            }
        });

        //TIMER
        timerText = (TextView) findViewById(R.id.Timer);
        pauseBtn = (Button) findViewById(R.id.PauseBtn);

        timer = new Timer();
        startTimer();
    }

    public void pauseTapped(View view) {
        GridView gv = findViewById(R.id.gridView);

        if (!timerStarted) {
            timerStarted = true;
            pauseBtn.setText("PAUSE");
            pauseBtn.setTextColor(ContextCompat.getColor(this, R.color.black));

            startTimer();

            //remove pause screen
            mPauseScreen.setVisibility(View.INVISIBLE);
            pauseBtn.setVisibility(View.VISIBLE);
        }
        else {
            timerStarted = false;

            timerTask.cancel();

            //display pause screen
            mPauseScreen.setVisibility(View.VISIBLE);
            pauseBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void startTimer() {
        timerStarted = true;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        time++;
                        timerText.setText(getTimerText());
                    }
                });
            }
        };
        //delay 0 = timer will start straightaway; period 1000 = 1000ms = 1s
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private String getTimerText() {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    private String formatTime(int seconds, int minutes, int hours) {
        return String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%02d", seconds);
    }

    //shuffling images
    private int[] shuffleImages() {
        int noOfElements = position.length;
        for (int i = 0; i < noOfElements; i++) {
            int s = i + (int)(Math.random() * (noOfElements - i));

            int temp = position[s];
            position[s] = position[i];
            position[i] = temp;
        }

        return position;
    }


    private ArrayList<Bitmap> loadImageFromFile(String path) {
        ArrayList<Bitmap> listOfBitMaps = new ArrayList<>();

        //Should not change these
        String filepath = path;
        String[] filenames = {"1", "2", "3", "4", "5", "6", "1", "2", "3", "4", "5", "6"};

        for (int j = 0; j < filenames.length; j++) {

            File f = new File(filepath + "/" + filenames[j]);
            Bitmap b = BitmapFactory.decodeFile(String.valueOf(f));
            listOfBitMaps.add(b);
        }

        return listOfBitMaps;
    }

}