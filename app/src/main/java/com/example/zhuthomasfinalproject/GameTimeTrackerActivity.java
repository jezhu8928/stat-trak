/**
 * Sydney Thomas
 * January 6, 2020
 * Activity that tracks game time statistics
 * This code is associated with game_time_tracker.xml
 */
package com.example.zhuthomasfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
public class GameTimeTrackerActivity extends AppCompatActivity {
    final int START_TIME = 8000*60; //length of a quarter (8 minutes)
    private TextView txt_points; //text view for points
    private TextView txt_quarter; //text view for quarter
    private TextView txt_fouls; //text view for fouls
    private ListView lst_players; //list view for players to substitute
    private TextView lst_item_text; //text view for each item in the list view
    public TextView txt_timer; //text view for timer
    private ToggleButton selectedButton; //button for selected player number for current player
    private int selectedPlayerIndex = 0; //index of currently selected player
    private ToggleButton playerButtons[] = new ToggleButton[5]; //array of player buttons
    private long startTime; //start time of current interval clock is running
    private long currentTime = 0; //ending time for the interval the clock just ran
    private long timeDecrement; //how much the clock decreases by (1 second)
    private boolean clockRunning = false; //status of the clock (running or not)
    private Timer timer = new Timer(); //timer that triggers every second
    private TextView txt_playDesc; //text view for status display
    private int t = 1; //counter to count seconds
    private TimerTask task; //task that runs when the timer triggers every second
    long minutes; //calculated time for display (minutes)
    long seconds; //calculated time for display (seconds)
    String clockDisplayText = ""; //combined minutes and seconds
    String minutesText = ""; //time in minutes String
    String secondsText = ""; //time in seconds String
    private boolean gameOver = false;
    private Button btn_Undo; //button to under the last statics(s) captured by the user
    private Button btn_Next; //Next Quarter button /  Done button
    private Stack undoStack; //stack of commands completed by the user (in the current quarter). Used to undo



    /**
     * called when GameTimeTrackerActivity window starts up
     * @param savedInstanceState - used by the system
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set up screen from xml
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_time_tracker);

        //get a control object from the xml id of the control
        txt_points = (TextView)findViewById(R.id.team_points); //points display
        txt_quarter = (TextView)findViewById(R.id.quarter); //quarter display
        txt_fouls = (TextView)findViewById(R.id.team_fouls); //foul display
        txt_timer = (TextView)findViewById(R.id.timer); //clock display
        playerButtons[0] = (ToggleButton)findViewById(R.id.btn_player1); //first player slot
        playerButtons[1] = (ToggleButton)findViewById(R.id.btn_player2); //second player slot
        playerButtons[2] = (ToggleButton)findViewById(R.id.btn_player3); //third player slot
        playerButtons[3] = (ToggleButton)findViewById(R.id.btn_player4); //fourth player slot
        playerButtons[4] = (ToggleButton)findViewById(R.id.btn_player5); //fifth player slot
        selectedButton = playerButtons[0]; //currently selected player spot
        lst_players = (ListView)findViewById(R.id.list_players); //list view for player substitution
        lst_item_text = (TextView)findViewById(R.id.list_item_text); //text view for each item in list view
        txt_playDesc = (TextView)findViewById(R.id.play_desc); //status display
        btn_Undo = (Button)findViewById(R.id.btn_undo);
        //initializes data on GameTimeTracker window
        txt_points.setText("0");
        txt_quarter.setText("q1");
        txt_fouls.setText("0");
        playerButtons[0].setBackgroundColor(0xFF245300);
        playerButtons[0].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[0].getJerseyNum()));
        playerButtons[1].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[1].getJerseyNum()));
        playerButtons[2].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[2].getJerseyNum()));
        playerButtons[3].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[3].getJerseyNum()));
        playerButtons[4].setText(Integer.toString(StatsManager.getCurrentGame().getPlaying()[4].getJerseyNum()));
        btn_Next = (Button)findViewById(R.id.btn_next); // next quarter button

        btn_Next.setText("Next Quarter");

        //listView setup for all players, for substitution
        final ArrayList<Player> list = StatsManager.getCurrentGame().getTeam().getPlayers();
        Player[] array = list.toArray( new Player[list.size()] );
        lst_players.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //gets called when someone clicks on a player for substitution in the list of players
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player p = list.get(position); //selected player from list
                String display="You have clicked " + p.toString();
                System.out.println(display);
                //hide list view
                lst_players.setVisibility(View.INVISIBLE);
                //sets current player
                StatsManager.setCurrentPlayer(p);
                selectedButton.setText(Integer.toString(StatsManager.getCurrentPlayer().getJerseyNum()));
                StatsManager.getCurrentGame().getPlaying()[selectedPlayerIndex] = p;
            }
        });

        /*adapter between the list view and the array of players so if you add another player to
        the array, it will appear in the list view automatically*/
        ArrayAdapter <Player> adapter = new ArrayAdapter<Player>(this,
                R.layout.list_view_item, R.id.list_item_text, array);
        lst_players.setAdapter(adapter);

        //set up timer for game clock
        timeDecrement = 1000;
        startTime = START_TIME;
        currentTime = startTime;

        //reset gameOver flag
        gameOver = false;

        //initialize the undostack to store previous commands (buttons pushed) from the user
        undoStack = new Stack<UndoCommand>();
    }


    /* for each button handler below on<something>, a command is pushed onto the undoStack so the
     command can be undone */

    /**
     * method to update the score and player stats when the two point make button is pressed
     * @param v - current window
     */
    public void onTwoPtMakes(View v){
        PlayerStats s = StatsManager.getCurrentPlayer().getCurrentStats();
        //add to player stats
        s.addTwoPtMakes();
        //add to player points
        s.addPoints(2);
        //add to game points
        StatsManager.getCurrentGame().addPoints(2);
        //update screen
        txt_points.setText(Integer.toString(StatsManager.getCurrentGame().getPoints()));
        //update the status display
        txt_playDesc.setText("# " + StatsManager.getCurrentPlayer().getJerseyNum() + " scored 2 points");
        undoStack.push(new UndoCommand(UndoCommand.MAKE_2PT, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats when the two point miss button is pressed
     * @param v - current window
     */
    public void onTwoPtMisses(View v) {
        StatsManager.getCurrentPlayer().getCurrentStats().addTwoPtMisses();
        undoStack.push(new UndoCommand(UndoCommand.MISS_2PT, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the score and player stats when the three point make button is pressed
     * @param v - current window
     */
    public void onThreePtMakes(View v){
        PlayerStats s = StatsManager.getCurrentPlayer().getCurrentStats();
        //add to player stats
        s.addThreePtMakes();
        //add to player points
        s.addPoints(3);
        //add to game points
        StatsManager.getCurrentGame().addPoints(3);
        //update screen
        txt_points.setText(Integer.toString(StatsManager.getCurrentGame().getPoints()));
        //update the status display
        txt_playDesc.setText("# " + StatsManager.getCurrentPlayer().getJerseyNum() + " scored 3 points");
        undoStack.push(new UndoCommand(UndoCommand.MAKE_3PT, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats when the three point miss button is pressed
     * @param v - current window
     */
    public void onThreePtMisses(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addThreePtMisses();
        undoStack.push(new UndoCommand(UndoCommand.MISS_3PT, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update score and player stats when the free throw make button is pressed
     * @param v - current window
     */
    public void onFtMakes(View v){
        PlayerStats s = StatsManager.getCurrentPlayer().getCurrentStats();
        //add to player stats
        s.addFtMakes();
        //add to player points
        s.addPoints(1);
        //add to game points
        StatsManager.getCurrentGame().addPoints(1);
        //update screen
        txt_points.setText(Integer.toString(StatsManager.getCurrentGame().getPoints()));
        //update the status display
        txt_playDesc.setText("# " + StatsManager.getCurrentPlayer().getJerseyNum() + " scored 1 point");
        undoStack.push(new UndoCommand(UndoCommand.MAKE_FT, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats when the free throw miss button is pressed
     * @param v - current window
     */
    public void onFtMisses(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addFtMisses();
        undoStack.push(new UndoCommand(UndoCommand.MISS_FT, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats when the assist button is pressed
     * @param v - current window
     */
    public void onAssists(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addAssists();
        undoStack.push(new UndoCommand(UndoCommand.ASSIST, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats when the offensive rebound button is pressed
     * @param v - current window
     */
    public void onOffRebs(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addOffRebs();
        undoStack.push(new UndoCommand(UndoCommand.OFFR, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats when the defensive rebound button is pressed
     * @param v - current window
     */
    public void onDefRebs(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addDefRebs();
        undoStack.push(new UndoCommand(UndoCommand.DEFR, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats when the steal button is pressed
     * @param v - current window
     */
    public void onSteals(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addSteals();
        undoStack.push(new UndoCommand(UndoCommand.STL, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats when the turnover button is pressed
     * @param v - current window
     */
    public void onTurnovers(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addTurnovers();
        undoStack.push(new UndoCommand(UndoCommand.TO, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats when the block button is pressed
     * @param v - current window
     */
    public void onBlocks(View v){
        StatsManager.getCurrentPlayer().getCurrentStats().addBlocks();
        undoStack.push(new UndoCommand(UndoCommand.BLK, StatsManager.getCurrentPlayer()));
    }

    /**
     * method to update the player stats and team stats when the foul button is pressed
     * @param v - current window
     */
    public void onFouls(View v){
        //add to player fouls
        StatsManager.getCurrentPlayer().getCurrentStats().addFouls();
        //add to team fouls
        StatsManager.getCurrentGame().addTeamFouls();
        //update screen
        txt_fouls.setText(Integer.toString(StatsManager.getCurrentGame().getTeamFouls()));
        //save undo command
        undoStack.push(new UndoCommand(UndoCommand.FOUL, StatsManager.getCurrentPlayer()));

    }

    /**
     * method that goes to the next quarter when you click on the quarter display
     * @param v - current window
     */
    public void onQuarter(View v) {
        String current;

        current = txt_quarter.getText().toString();
        if(current.equals("q1")) {
            txt_quarter.setText("q2");
        }else if(current.equals("q2")){
            txt_quarter.setText("q3");
        }else if(current.equals("q3")){
            txt_quarter.setText("q4");
        }else if(current.equals("q4")){
            txt_quarter.setText("q1");  //necessary to correct quarter during game
        }
        //empty at the end of each quarter (no more undo)
        undoStack.empty();
        //save each quarter
        StatsManager.toFile();

    }

    /**
     * methods onPlayer1-5 will call handlePlayerButtons
     * @param v - current window
     */
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

    /**
     * method that processes the undo button
     * @param v - current window
     */
    public void onUndo(View v){
        UndoCommand undoCmd; //current command from stack
        Game g; //game to undo game stats
        PlayerStats s; //to retrieve player stats to undo
        //if there is no commands to undo
        if(undoStack.size() == 0){
            return;
        }
        //pops top undo command off the stack
        undoCmd = (UndoCommand)undoStack.pop();
        //get current game to undo in game stats
        g = StatsManager.getCurrentGame();
        //get player stats for the player in the undo command
        s = undoCmd.getPlayer().getCurrentStats();

        if(undoCmd.getCmd() == UndoCommand.ASSIST){ //undo an assist
            s.subtractAssists();
        }else if(undoCmd.getCmd() == UndoCommand.MAKE_2PT){ //undo a two point make
            //game points
            g.subtractPoints(2);
            //player points
            s.subtractPoints(2);
            //3pt makes count
            s.subtractTwoPtMakes();
            txt_points.setText(Integer.toString(g.getPoints()));
        }else if(undoCmd.getCmd() == UndoCommand.BLK){ //undo a block
            s.subtractBlocks();
        }else if(undoCmd.getCmd() == UndoCommand.STL){ //undo a steal
            s.subtractSteals();
        }else if(undoCmd.getCmd() == UndoCommand.TO){ //undo a turnover
            s.subtractTurnovers();
        }else if(undoCmd.getCmd() == UndoCommand.MAKE_3PT){ //undo a three point make
            //game points
            g.subtractPoints(3);
            //player points
            s.subtractPoints(3);
            //3 pt makes count
            s.subtractThreePtMakes();
            txt_points.setText(Integer.toString(g.getPoints()));
        }else if(undoCmd.getCmd() == UndoCommand.MAKE_FT){ //undo a free throw make
            //game points
            g.subtractPoints(1);
            //player points
            s.subtractPoints(1);
            //free throw makes count
            s.subtractFtMakes();
            txt_points.setText(Integer.toString(g.getPoints()));
        }else if(undoCmd.getCmd() == UndoCommand.MISS_2PT){ //undo a two point miss
            s.subtractTwoPtMisses();
        }else if(undoCmd.getCmd() == UndoCommand.MISS_3PT){ //undo a three point miss
            s.subtractThreePtMisses();
        }else if(undoCmd.getCmd() == UndoCommand.MISS_FT){ //undo a free throw miss
            s.subtractFtMisses();
        }else if(undoCmd.getCmd() == UndoCommand.FOUL){ //undo a foul
            //game fouls
            g.subtractTeamFouls();
            //player fouls
            s.subtractFouls();
            txt_fouls.setText(Integer.toString(g.getTeamFouls()));
        }else if(undoCmd.getCmd() == UndoCommand.OFFR){ //undo an offensive rebound
            s.subtractOffRebs();
        }else if(undoCmd.getCmd() == UndoCommand.DEFR){ //undo a defensive rebound
            s.subtractDefRebs();
        }
    }

    /**
     * method that either sets the current player on the first time you tap the player and then if you
     * tap the player again it brings up a list of players to substitute and complete the substitution
     * after you select a player from list
     * @param index - which player button is pressed
     */
    public void handlePlayerButtons(int index) {
        Game game = StatsManager.getCurrentGame();
        //check if your clicking on currently selected player
        if(game.getPlaying()[index].equals(StatsManager.getCurrentPlayer())){
            //bring up list of players for substitution
            lst_players.setVisibility(View.VISIBLE);
            lst_players.setZ(10);
        }else{
            //select the player
            StatsManager.setCurrentPlayer(StatsManager.getCurrentGame().getPlaying()[index]);
        }

        selectedButton = playerButtons[index];
        selectedPlayerIndex = index;
        playerButtons[index].setText(Integer.toString(StatsManager.getCurrentPlayer().getJerseyNum()));

        //set different colours for selected and not selected players
        for(int i = 0; i < playerButtons.length; i++){
            if(i == index){
                playerButtons[i].setBackgroundColor(0xFF245300);
            }else{
                playerButtons[i].setBackgroundColor(0xFF245354);
            }
        }

    }

    /**
     * method that goes to the next quarter when next quarter is pressed
     * @param v - current window
     */
    public void onNextQuarter(View v) {
        pauseClock();
        onQuarter(v);
        txt_timer.setText("08:00");
        //reset clock so it resets to 8 minutes on every new quarter
        resetClock();
        //reset team fouls
        StatsManager.getCurrentGame().setTeamFouls(0);
        txt_fouls.setText(Integer.toString(0));
        //saves data to a file each quarter
        StatsManager.toFile();
        if(gameOver){
            this.finish();
        }
    }

    /**
     * method that is called when you click on the clock
     * starts or stops the clock
     * @param v - current window
     */
    public void onClock(View v) {
        if(isClockRunning()){
            pauseClock();
        }else{
            startClock();
        }
    }

    /**
     * Runnable to execute gui code that needs to run on the UI thread.
     * This one sets the text on the clock display (each second when the timer goes off
     */
    Runnable updateClockDisplay = new Runnable()  {
        public void run() {
            //stuff that updates the UI
            txt_timer.setText(clockDisplayText);
        }

    };
    /**
     * Runnable to execute gui code that needs to run on the UI thread.
     * This one sets the text on the "Next Quarter" button to be "Done
     * and displays status messages to the user
     */
    Runnable setNextButtonText = new Runnable() {
        public void run() {
            btn_Next.setText("Done");
            Toast.makeText(getApplicationContext(),"The game is over. Your stats have been saved",
                    Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(),"Press 'Done' to close this game. Changes will be saved.",
                        Toast.LENGTH_LONG).show();
        }
    };

    /**
     * method that creates the task that gets called at 1 second intervals to
     * format and update the clock display with the current time.
     */
    private void createClockTask() {
        task = new TimerTask() {
            public void run() {
            if(clockRunning){
                //converts from milliseconds to minutes
                minutes = getMinutesRemaining();
                //put in a 0 first digit when only a single digit left in minutes
                if(minutes < 10){
                    minutesText = "0" + minutes;
                }else{
                    minutesText = Long.toString(minutes);
                }
                seconds = getSecondsRemaining();
                //puts in a 0 first digit when only a single digit left in minutes
                if(seconds < 10){
                    secondsText = "0" + seconds;
                }else{
                    secondsText = Long.toString(seconds);
                }
                /*update the clock display with the minutes and seconds remaining
                must use runOnUiThread or the gui update throws an exception*/
                clockDisplayText = minutesText + ":" + secondsText;

                //update clock display
                runOnUiThread(updateClockDisplay);
                //when time expires (ie. 0 minutes and 0 seconds left), stop the clock
                if((seconds == 0L) && (minutes == 0L)){
                    pauseClock();
                    resetClock();
                    if(txt_quarter.getText().equals("q4")) {
                        gameOver = true;
                        StatsManager.toFile(); //save game, and everything else
                        //change to "Done" button and display ending messages
                        runOnUiThread(setNextButtonText);

                    }
                    return;
                }
                //update currentTime (decrease by 1 second)
                currentTime = currentTime - timeDecrement;
            }
            }
        };
    }


    /**
     * method that starts the clock
     * sets a timer to go off every 1 second
     */
    public void startClock() {
        createClockTask();
        timer.schedule(task, 0, timeDecrement);
        clockRunning = true;
    }

    /**
     * method that pauses the clock and updates playing time for all players on the court
     */
    public void pauseClock() {
        long elapsed;
        //pause before timer initialized
        if(task ==null) {
            return;
        }
        task.cancel();
        clockRunning = false;
        System.out.println("Paused");
        //calculate elapsed time since clock was started and add to playing time for each player
        elapsed = startTime - currentTime - timeDecrement; //-1000 required because currentTime is 1 sec lower than display
        System.out.println("TIME Elapsed: " + elapsed);
        StatsManager.getCurrentGame().addPlayingTime(elapsed);
        //resets startTime
        startTime = currentTime + timeDecrement;
    }

    /**
     * method resets the clock to 8 minutes
     */
    public void resetClock() {
        t = 1;
        startTime = START_TIME;
        currentTime = startTime;
    }

    /**
     * accessor that gets the start time (which is the time currently on the clock when the timer is started)
     * @return - start time
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * mutator that sets the start time (which is the time currently on the clock when the timer is started)
     * @param sT - new start time
     */
    public void setStartTime(long sT) {
        startTime = sT;
    }

    /**
     * accessor that gets the current time (which is the time after the clock has been running)
     * @return - current time
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * mutator that sets the current time (which is the time after the clock has been running)
     * @param cT - new current time
     */
    public void setCurrentTime(long cT) {
        currentTime = cT;
    }

    /**
     * method that converts current time in milliseconds to minutes
     * @return - current time in minutes
     */
    public long getMinutesRemaining() {
        long minutes;
        minutes = currentTime / (1000*60);
        return minutes;
    }

    /**
     * method that calculate the remainder time in seconds
     * @return - remainder time in seconds
     */
    public long getSecondsRemaining() {
        long minutes, seconds;
        minutes = currentTime / (1000*60);
        seconds = (currentTime - (minutes * 1000*60))/1000;
        return seconds;
    }

    /**
     * method that checks if the clock is running
     * @return - if the clock is running or not
     */
    public boolean isClockRunning() {
        return clockRunning;
    }

    /**
     * Method that launches the instructions window when the Help button is clicked
     * @param v - the current window
     */
    public void launchInstructions(View v) {
        Intent i = new Intent(this, Instructions.class);
        Instructions.setIndex(2); // info about tracker on 7th slide
        startActivity(i);
    }

}

