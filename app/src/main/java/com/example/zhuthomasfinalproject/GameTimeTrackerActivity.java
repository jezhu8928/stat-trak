package com.example.zhuthomasfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;

public class GameTimeTrackerActivity extends AppCompatActivity {

    final int START_TIME = 8000*60;
    private TextView txt_points;
    private TextView txt_quarter;
    private TextView txt_fouls;

    private ListView lst_players;
    private TextView lst_item_text;
    public TextView txt_timer;
    private ToggleButton selectedButton;
    private int selectedPlayerIndex = 0; //index of currently selected player
    private ToggleButton playerButtons[] = new ToggleButton[5]; //array of player buttons
    private long startTime;
    private long currentTime = 0;
    private long timeDecrement;
    private boolean clockRunning = false;
    private Timer timer = new Timer( );
    private TextView txt_playDesc;

    private int t = 1;


    private TimerTask task;
    long minutes, seconds;
    String clockDisplayText="", minutesText="", secondsText="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_time_tracker);
        StatsManager.initStatsManager();
        txt_points = (TextView)findViewById(R.id.team_points);
        txt_quarter = (TextView)findViewById(R.id.quarter);
        txt_fouls = (TextView)findViewById(R.id.team_fouls);
        txt_timer = (TextView)findViewById(R.id.timer);
        playerButtons[0] = (ToggleButton)findViewById(R.id.btn_player1);
        playerButtons[1] = (ToggleButton)findViewById(R.id.btn_player2);
        playerButtons[2] = (ToggleButton)findViewById(R.id.btn_player3);
        playerButtons[3] = (ToggleButton)findViewById(R.id.btn_player4);
        playerButtons[4] = (ToggleButton)findViewById(R.id.btn_player5);
        selectedButton = playerButtons[0];
        lst_players = (ListView)findViewById(R.id.list_players);
        lst_item_text = (TextView)findViewById(R.id.list_item_text);
        txt_playDesc = (TextView)findViewById(R.id.play_desc);

        txt_points.setText("0");
        txt_quarter.setText("q1");
        txt_fouls.setText("0");
        playerButtons[0].setBackgroundColor(0xFF245300);
        playerButtons[0].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[0].getJerseyNum()));
        playerButtons[1].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[1].getJerseyNum()));
        playerButtons[2].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[2].getJerseyNum()));
        playerButtons[3].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[3].getJerseyNum()));
        playerButtons[4].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[4].getJerseyNum()));

        //listView setup for all players, for substitution
        final ArrayList<Player> list = StatsManager.getCurrentGame().getTeam().getPlayers();
        Player[] array = list.toArray( new Player[list.size()] );
        lst_players.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player p=list.get(position);
                String display="You have clicked "+p.toString();
                System.out.println(display);
                lst_players.setVisibility(View.INVISIBLE);
                StatsManager.setCurrentPlayer(p);
                selectedButton.setText(Integer.toString(StatsManager.getCurrentPlayer().getJerseyNum()));
                StatsManager.getCurrentGame().getPlaying()[selectedPlayerIndex] = p;
            }
        });

        ArrayAdapter <Player> adapter = new ArrayAdapter<Player>(this,
                R.layout.list_view_item, R.id.list_item_text, array);

        lst_players.setAdapter(adapter);

        timeDecrement = 1000;
        startTime = START_TIME;
        currentTime = startTime;
    }
    public void onTwoPtMakes(View v){
        PlayerStats s = StatsManager.getCurrentPlayer().getCurrentStats();
        s.addTwoPtMakes();
        s.addPoints(2);

        StatsManager.getCurrentGame().addPoints(2);

        txt_points.setText(Integer.toString(StatsManager.getCurrentGame().getPoints()));


        txt_playDesc.setText("# " + StatsManager.getCurrentPlayer().getJerseyNum() + " scored 2 points");

    }
    public void onTwoPtMisses(View v) {
        PlayerStats s = StatsManager.getCurrentPlayer().getCurrentStats();
        s.addTwoPtMisses();

    }
    public void onThreePtMakes(View v){
        PlayerStats s = StatsManager.getCurrentPlayer().getCurrentStats();
        s.addThreePtMakes();
        s.addPoints(3);
        StatsManager.getCurrentGame().addPoints(3);

        txt_points.setText(Integer.toString(StatsManager.getCurrentGame().getPoints()));

        txt_playDesc.setText("# " + StatsManager.getCurrentPlayer().getJerseyNum() + " scored 3 points");
    }
    public void onThreePtMisses(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addThreePtMisses();
    }
    public void onFtMakes(View v){
        PlayerStats s = StatsManager.getCurrentPlayer().getCurrentStats();
        s.addFtMakes();

        s.addPoints(1);
        StatsManager.getCurrentGame().addPoints(1);

        txt_points.setText(Integer.toString(StatsManager.getCurrentGame().getPoints()));
        txt_playDesc.setText("# " + StatsManager.getCurrentPlayer().getJerseyNum() + " scored 1 point");

    }
    public void onFtMisses(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addFtMisses();
    }
    public void onAssists(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addAssists();
    }
    public void onOffRebs(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addOffRebs();
    }
    public void onDefRebs(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addDefRebs();
    }
    public void onSteals(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addSteals();
    }
    public void onTurnovers(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addTurnovers();
    }
    public void onBlocks(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addBlocks();
    }
    public void onFouls(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addFouls();
        StatsManager.getCurrentGame().addTeamFouls();

        txt_fouls.setText(Integer.toString(StatsManager.getCurrentGame().getTeamFouls()));

        // TEST
        System.out.println(StatsManager.getCurrentPlayer().getCurrentStats().toString());
    }
    public void onQuarter(View v) {
        String current = txt_quarter.getText().toString();
        if(current.equals("q1")) {
            txt_quarter.setText("q2");
        } else if( current.equals("q2")) {
            txt_quarter.setText("q3");
        } else if( current.equals("q3")) {
            txt_quarter.setText("q4");
        } else if( current.equals("q4")) {
            txt_quarter.setText("q1");
        }
    }

    //event handlers for if you click on a player
    public void onPlayer1(View v) {
        handlePlayerButtons(0);
    }
    public void onPlayer2(View v) {
        handlePlayerButtons(1);
    }
    public void onPlayer3(View v) {
        handlePlayerButtons(2);
    }
    public void onPlayer4(View v) {
        handlePlayerButtons(3);
    }
    public void onPlayer5(View v) {
        handlePlayerButtons(4);
    }
    public void handlePlayerButtons(int index) {
        Game game = StatsManager.getCurrentGame();
        //check if your clicking on currently selected player
        if(game.getPlaying()[index].equals(StatsManager.getCurrentPlayer())) {
            //bring up list of players for substitution
            lst_players.setVisibility(View.VISIBLE);
            lst_players.setZ(10);
        } else {
            //select the player
            StatsManager.setCurrentPlayer(StatsManager.getCurrentGame().getPlaying()[index]);
        }

        selectedButton = playerButtons[index];
        selectedPlayerIndex = index;
        playerButtons[index].setText(Integer.toString(StatsManager.getCurrentPlayer().getJerseyNum()));

        //set different colours for selected and not selected players
        for(int i = 0; i < playerButtons.length; i++) {
            if(i == index) {
                playerButtons[i].setBackgroundColor(0xFF245300);
            } else {
                playerButtons[i].setBackgroundColor(0xFF245354);
            }
        }

    }
    public void onNextQuarter(View v) {
        onQuarter(v);
        txt_timer.setText("08:00");
        //reset clock so it resets to 8 minutes on every new quarter
        resetClock();
        currentTime = 8000*60;
        //reset team fouls
        StatsManager.getCurrentGame().setTeamFouls(0);
        txt_fouls.setText(Integer.toString(0));

    }

    /**
     * method that is called when you click on the clock
     * @param v - current window
     */
    public void onClock(View v) {
        if(isClockRunning()) {
            pauseClock();
        } else {
            startClock();
        }
    }



    private void createClockTask() {
        task = new TimerTask() {
            public void run() {

                if(clockRunning) {
                    minutes = getMinutesRemaining();

                    if(minutes < 10) {
                        minutesText = "0" + minutes;
                    } else{
                        minutesText = Long.toString(minutes);
                    }
                    seconds = getSecondsRemaining();

                    if(seconds < 10) {
                        secondsText = "0" + seconds;
                    } else {
                        secondsText = Long.toString(seconds);
                    }

                    clockDisplayText = minutesText + ":" + secondsText;
                    runOnUiThread(new Runnable()  {
                        public void run() {

                            // Stuff that updates the UI
                            txt_timer.setText(clockDisplayText);
                        }

                    });
                    if((seconds == 0L) && (minutes == 0L)) {
                        pauseClock();
                        resetClock();
                        return;
                    }

                    currentTime = currentTime - timeDecrement;
                }
            }
        };
    }



    public void startClock() {
        createClockTask();
        timer.schedule(task, 0, timeDecrement);
        clockRunning = true;
    }

    public void pauseClock() {
        long elapsed;

        task.cancel();
        clockRunning = false;
        System.out.println("Paused");

        elapsed = startTime - currentTime - timeDecrement; // -1000 required because currentTime is 1 sec lower than display
        System.out.println("TIME Elapsed: " + elapsed);

        StatsManager.getCurrentGame().addPlayingTime(elapsed);

        startTime = currentTime+timeDecrement;


    }

    public void resetClock() {
        t = 1;
        currentTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long sT) {
        startTime = sT;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long cT) {
        currentTime = cT;
    }
    public long getMinutesRemaining() {
        long minutes;

        minutes = currentTime / (1000*60);
        return minutes;
    }
    public long getSecondsRemaining() {
        long minutes, seconds;
        minutes = currentTime / (1000*60);
        seconds = (currentTime - (minutes * 1000*60))/1000;
        return seconds;
    }
    public boolean isClockRunning() {
        return clockRunning;
    }


}

