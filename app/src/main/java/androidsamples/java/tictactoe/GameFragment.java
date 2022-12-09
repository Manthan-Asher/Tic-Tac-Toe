package androidsamples.java.tictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Game fragment class with main logic for playing game.
 */
public class GameFragment extends Fragment {
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;
  private final Button[] mButtons = new Button[GRID_SIZE];
  private NavController mNavController;
  private FirebaseUser currentUser;
  private FirebaseDatabase mDatabase;
  private boolean singlePlayer;
  private String matchUUID;
  DatabaseReference gameRef,userRef;
  private GameLogic mLogic;
  private Integer playerWins,playerLosses;
  private boolean threadNotComplete;
  private TextView gamePlayer1,gamePlayer2;
  private Integer activeColour,inactiveColour;

  /**
   * On create lifecycle method. All fields and references are initialized. We fetch arguments via safe args in bundles
   * Initializes the game object depending one player, new 2 player,joining 2 player game. Also add the on back pressed callback
   * @param savedInstanceState
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    mDatabase = FirebaseDatabase.getInstance();
    currentUser = FirebaseAuth.getInstance().getCurrentUser();
    gameRef = mDatabase.getReference("Game_Info");
    userRef = mDatabase.getReference("User_Info");

    // active and inactive are initialized this way because of the way the theme works
    // copied from stackoverflow {link @https://stackoverflow.com/questions/35417329/gettheme-resolveattribute-alternative-on-pre-lollipop}
    TypedValue typedValue1 = new TypedValue();
    requireActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue1, true);
    activeColour = ContextCompat.getColor(requireContext(), typedValue1.resourceId);

    TypedValue typedValue2 = new TypedValue();
    requireActivity().getTheme().resolveAttribute(R.attr.colorOnBackground, typedValue2, true);
    inactiveColour = ContextCompat.getColor(requireContext(), typedValue2.resourceId);

    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
    Log.d(TAG, "New game type = " + args.getGameType());
    if(args.getGameType().equals("One-Player")){
      singlePlayer = true;
    }else if(args.getGameType().equals("Two-Player")){
      singlePlayer = false;
    }

    // fetches matchUUID
    matchUUID = args.getGameID();
    mLogic = new ViewModelProvider(requireActivity()).get(GameLogic.class);

    if(matchUUID.isEmpty()){
      GameInfo game = makeGameObject();
      Log.d(TAG, "Game created: " + game.toString());
      mLogic.initialise2(game);
      mLogic.turnToGo = 1;
      gameRef.child(game.getUUID()).setValue(game);
      Log.d(TAG, "Player1: " + currentUser.getEmail() + " has created a " + args.getGameType() + " game : " + matchUUID);
    }else{
      mLogic.initialise1(matchUUID,singlePlayer);
      mLogic.turnToGo = 2;
      setTwoPlayerMatch(matchUUID);
      Log.d(TAG, "Player2: " + currentUser.getEmail() + " has joined game : " + matchUUID);
      Toast.makeText(requireContext(),"Joined Game as player 2", Toast.LENGTH_SHORT).show();
    }

    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");
        // TODO show dialog only when the game is still in progress
        handleBackPressedOrLogout(false);
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  /**
   * Initialise the game Info object and send it back
   * @return
   */
  @NonNull
  private GameInfo makeGameObject() {
    boolean open = !singlePlayer;
    String player1 = currentUser.getUid();
    String player2 = singlePlayer ? getString(R.string.computer) : getString(R.string.waiting);
    String status = singlePlayer ? getString(R.string.status_started) : getString(R.string.status_waiting);
    String winner = getString(R.string.undecided);
    Integer turn = 1;
    return new GameInfo(open, singlePlayer, player1,player2,status,winner,turn);
  }

  /**
   * On create view lifecycle method. We just inflate the ui
   * @param inflater
   * @param container
   * @param savedInstanceState
   * @return
   */
  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  /**
   * On View created lifecycle method. Extract the widgets,
   * set on click listeners on buttons and also set database listeners.
   * @param view
   * @param savedInstanceState
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mNavController = Navigation.findNavController(view);

    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);

    gamePlayer1 = view.findViewById(R.id.player1);
    gamePlayer2 = view.findViewById(R.id.player2);
    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        Log.d(TAG, "Button " + finalI + " clicked");
        // TODO implement listeners
        makeMove(finalI);
      });
    }

     if(!singlePlayer && mLogic.turnToGo == 1){
       notifyPlayer1ThatPlayer2Joined();
     }

    // listener which listens to the game changes in database
    listenerWhilePlayingGame();

    //listener when game ends
    listenerForStatus();
  }

  /**
   * Single value event listener for player 2 when they join an open game to update the game model fields
   * and updates the database as well
   * @param matchUUID
   */
  private void setTwoPlayerMatch(String matchUUID){
    Log.d(TAG, "setTwoPlayerMatch is called");
    Log.d(TAG, "matchUUID is " + matchUUID);
    //player2,open,status
    updateFieldsOnPlayerJoin(currentUser.getUid());
    gameRef.child(matchUUID).child("status").setValue(mLogic.status);
    gameRef.child(matchUUID).child("open").setValue(false);
    gameRef.child(matchUUID).child("player2").setValue(mLogic.player2);
    gameRef.child(matchUUID).addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange in setTwoPlayerMatch");
                GameInfo localGameInfo = snapshot.getValue(GameInfo.class);
                assert localGameInfo != null;
                mLogic.player1 = localGameInfo.getPlayer1();
                mLogic.turn = localGameInfo.getTurn();
                mLogic.tictactoe = localGameInfo.getTictactoe();
                mLogic.winner = localGameInfo.getWinner();
              }
              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled", error.toException());
              }
            }
    );
  }

  /**
   * Listener to notify player1 that player2 has joined and update fields and UI
   */
  private void notifyPlayer1ThatPlayer2Joined() {
    gameRef.child(mLogic.matchUUID).child("player2").addValueEventListener(new ValueEventListener() {

      /**
       * This method will be called with a snapshot of the data at this location. It will also be called
       * each time that data changes.
       *
       * @param snapshot The current data at the location
       */
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(snapshot.getValue() == null) return;
        updateFieldsOnPlayerJoin(snapshot.getValue().toString());
        updateUI();
      }

      /**
       * This method will be triggered in the event that this listener either failed at the server, or
       * is removed as a result of the security and Firebase Database rules.
       *
       * @param error A description of the error that occurred
       */
      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "onCancelled", error.toException());
      }
    });
  }

  /**
   * Listens for changes in tictactoe board and turn value while playing the game. Also updates the UI
   */
  private void listenerWhilePlayingGame(){
    gameRef.child(mLogic.matchUUID).addValueEventListener(new ValueEventListener() {
      /**
       * This method will be called with a snapshot of the data at this location. It will also be called
       * each time that data changes.
       *
       * @param snapshot The current data at the location
       */
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        GameInfo localGameInfo = (GameInfo) snapshot.getValue(GameInfo.class);
        if(localGameInfo == null) return;
        mLogic.tictactoe = localGameInfo.getTictactoe();
        mLogic.turn = localGameInfo.getTurn();
        updateUI();
      }

      /**
       * This method will be triggered in the event that this listener either failed at the server, or
       * is removed as a result of the security and Firebase Database rules.
       * @param error A description of the error that occurred
       */
      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "onCancelled", error.toException());
      }
    });
  }

  /**
   * Listens for status update in the database, mainly to check when game ends. Depending on the result and winner,
   * It will display the game over dialog and update wins,losses
   */
  private void listenerForStatus() {
    gameRef.child(mLogic.matchUUID).addValueEventListener(new ValueEventListener() {
      /**
       * This method will be called with a snapshot of the data at this location. It will also be called
       * each time that data changes.
       *
       * @param snapshot The current data at the location
       */
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        GameInfo localGameInfo = (GameInfo) snapshot.getValue(GameInfo.class);
        if(localGameInfo == null) return;
        mLogic.winner = localGameInfo.getWinner();
        mLogic.status = localGameInfo.getStatus();

        if(mLogic.status.equals(getString(R.string.status_finished))){
          String messageToDisplay = "";
          int result = 0;
          if(mLogic.winner.equals(getString(R.string.draw))){
            messageToDisplay = "It's a draw!";
            result = 3;
          }else if(mLogic.winner.equals("Player1")){
            result = 1;
            if(mLogic.turnToGo == 1) messageToDisplay = "Congratulations!";
            else messageToDisplay = "Sorry!";
          }else {
            result = 2;
            if(mLogic.turnToGo == 2) messageToDisplay = "Congratulations!";
            else messageToDisplay = "Sorry!";
          }

          addSingleEventListener(currentUser.getUid(),result);
          showDialogAndClose(messageToDisplay);
        }
      }

      /**
       * This method will be triggered in the event that this listener either failed at the server, or
       * is removed as a result of the security and Firebase Database rules.
       * @param error A description of the error that occurred
       */
      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "onCancelled", error.toException());
      }
    });
  }

  /**
   * Helper function to update fields when player 2 joins
   * @param player2Id player2 uuid
   */
  private void updateFieldsOnPlayerJoin(String player2Id) {
    mLogic.status = getString(R.string.status_started);
    mLogic.open = false;
    mLogic.player2 = player2Id;
  }

  /**
   * Handles the case when a player tries to make a move. Waits for other player to join and checks the turn and
   * validity of move. Then checks the result of game and takes an action accordingly.
   * If it is a single player, the computer move is also made here.
   * @param index where move is made in board
   */
  private void makeMove(Integer index){
    if(mLogic.status.equals(getString(R.string.status_waiting))){
      Toast.makeText(requireContext(), "Waiting for other player to join..", Toast.LENGTH_SHORT).show();
      return;
    }
    if(!Objects.equals(mLogic.turn, mLogic.turnToGo)){
      Toast.makeText(requireContext(), "Please wait for your turn", Toast.LENGTH_SHORT).show();
      return;
    }
    if(mLogic.play(index,mLogic.turnToGo)){
      mLogic.turn = ((mLogic.turn)%2) +1; // change turn
      gameRef.child(mLogic.matchUUID).child("tictactoe").setValue(mLogic.tictactoe);
      gameRef.child(mLogic.matchUUID).child("turn").setValue(mLogic.turn);
      updateUI();

      if(mLogic.checkResult() == 1){
        Log.d(TAG, "Player 1 won");
        updateDbEntryOnGameEnd("Player1");
      }

      if(mLogic.checkResult() == 3){
        Log.d(TAG, "Draw");
        updateDbEntryOnGameEnd(getString(R.string.draw));
      }

      if(singlePlayer && mLogic.checkResult() == 0){
        mLogic.getComputerMove();
        mLogic.turn = ((mLogic.turn)%2) +1; // change turn
        gameRef.child(mLogic.matchUUID).child("tictactoe").setValue(mLogic.tictactoe);
        gameRef.child(mLogic.matchUUID).child("turn").setValue(mLogic.turn);
        if(mLogic.checkResult() == 2){
          Log.d(TAG, "Computer Won");
          updateDbEntryOnGameEnd(getString(R.string.computer));
        }
      }else if(!singlePlayer && mLogic.checkResult() == 2){
        Log.d(TAG, "Player 2 won");
        updateDbEntryOnGameEnd("Player2");
      }
    }else{
      Toast.makeText(requireContext(), "Please select a different cell", Toast.LENGTH_SHORT).show();
      return;
    }
  }

  /**
   * When game ends we update the entries of game (winner and status) in database
   * @param winner
   */
  private void updateDbEntryOnGameEnd(String winner) {
    mLogic.status = getString(R.string.status_finished);
    mLogic.winner = winner;
    GameInfo newObj = new GameInfo(mLogic.open, mLogic.singlePlayer, mLogic.player1, mLogic.player2, mLogic.status, mLogic.winner, mLogic.turn,mLogic.tictactoe);
    gameRef.child(mLogic.matchUUID).setValue(newObj);
  }

  /**
   * Generic update UI method to update tictactoe board every time a move is made
   * and also displays player joining status
   */
  private void updateUI(){
    for(int i=0;i<GRID_SIZE;i++){
      mButtons[i].setText(mLogic.tictactoe.get(i));
    }
    gamePlayer1.setText("Player 1");

    String player2display = "";
    if(mLogic.player2.equals(getString(R.string.computer)) || mLogic.player2.equals(getString(R.string.waiting))){
      player2display = mLogic.player2;
    }else player2display = "Player 2";

    gamePlayer2.setText(player2display);

    if(mLogic.turn == 1){
      gamePlayer1.setTextColor(activeColour);
      gamePlayer2.setTextColor(inactiveColour);
    }else  {
      gamePlayer1.setTextColor(inactiveColour);
      gamePlayer2.setTextColor(activeColour);
    }
  }

  /**
   * Displays congratulations, draw or sorry message and navigates user back to dashboard
   * @param messageToDisplay
   */
  private void showDialogAndClose(String messageToDisplay) {
    AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setCancelable(false)
            .setTitle(R.string.game_over)
            .setMessage(messageToDisplay)
            .setPositiveButton("OK", (d, which) -> {
              // TODO update loss count
              // doing that already
              mNavController.navigate(R.id.dashboardFragment);
            })
            .create();
    dialog.show();
  }

  /**
   * Helper function to update win and loss count depending on player and result
   * @param playerId player uuid
   * @param result result of game
   */
  private void updateWinsAndLosses(String playerId,int result) {
      if(result == mLogic.turnToGo) {
        userRef.child(playerId).child("wins").setValue(playerWins+1);
      }else if(result != 3) {
        userRef.child(playerId).child("losses").setValue(playerLosses+ 1);
      }
  }

  /**
   * This is a single event listener to fetch wins and losses after game has finished to update in database
   * @param playerId player uuid
   * @param result result of game
   */
  private void addSingleEventListener(String playerId,int result) {
    Log.d(TAG, "addSingleEventListener is called");
    Log.d(TAG , "playerId: " + playerId);
    userRef.child(playerId).addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange ");
                UserInfo userInfo = snapshot.getValue(UserInfo.class);
                assert userInfo != null;
                Log.d(TAG, "getting wins and losses for " + userInfo.toString());
                playerWins = userInfo.getWins();
                playerLosses = userInfo.getLosses();
                updateWinsAndLosses(playerId,result);
              }
              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled", error.toException());
              }
            }
    );
  }

  /**
   * It handles the case of back button pressed or logout pressed depending on the status of the game.
   * If game is started, the player will lose
   * If player is waiting to join, player will be taken back on pressing back button, without losing
   * If game is already finished, nothing happens
   * @param logout true if logout is pressed, false otherwise
   */

  private void handleBackPressedOrLogout(boolean logout) {
    if(mLogic.status.equals(getString(R.string.status_started))) {
      if(!logout) showForfeitDialog();
      else endGameAsForfeit();
    }else if(mLogic.status.equals(getString(R.string.status_finished))) {
      Log.d(TAG, "Match is over, no need to show dialog");
      if(!logout) mNavController.navigate(R.id.dashboardFragment);
    }else if(mLogic.status.equals(getString(R.string.status_waiting))){
      Log.d(TAG, "Other player did not join, leaving..");
      mLogic.open = false;
      gameRef.child(mLogic.matchUUID).child("open").setValue(false);
      if(!logout) mNavController.navigate(R.id.dashboardFragment);
    }
  }

  /**
   * gives a warning message to user to see if they want to forfeit the game. If clicked yes, they lose
   * and wins,losses are updated
   */

  private void showForfeitDialog() {
    AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setTitle(R.string.confirm)
            .setMessage(R.string.forfeit_game_dialog_message)
            .setPositiveButton(R.string.yes, (d, which) -> {
              // TODO update loss count
              endGameAsForfeit();
            })
            .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
            .create();
    dialog.show();
  }


  /**
   * Used to end game when forfeited and update winner
   */
  private void endGameAsForfeit(){
    if(mLogic.turnToGo == 1){
      updateDbEntryOnGameEnd("Player2");
    }else updateDbEntryOnGameEnd("Player1");
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  /**
   * Runs when logout is clicked in menu bar
   * @param item
   * @return
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_logout) {
      Log.d(TAG, "logout clicked in game fragment");
      // TODO handle log out
      handleBackPressedOrLogout(true);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}