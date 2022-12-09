package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment class for the dashboard in game. Handles recycler view, displays user email,wins losses
 * and lets a person join a game.
 */
public class DashboardFragment extends Fragment {

  private static final String TAG = "DashboardFragment";
  private NavController mNavController;
  private FirebaseAuth mAuth;
  private FirebaseDatabase mDatabase;
  private DatabaseReference mRef;
  private OpenGamesAdapter adapter;
  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {
  }

  /**
   * Activity lifecycle method where we initialze firebase auth instance
   * @param savedInstanceState
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    mAuth = FirebaseAuth.getInstance();
  }

  /**
   * On Create View, inflates the layout view
   * @param inflater
   * @param container
   * @param savedInstanceState
   * @return
   */
  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dashboard, container, false);
  }

  /**
   * Overrides the onViewCreated method of Fragment class. It checks if a user is logged in then fetches data from database and updates UI
   * and if not,takes them back to login fragment. It then gets the database reference. We add the recycler view here and set its adapter.
   * We also set the logic for floating action button and when a user presses button to create single or two player game.
   * @param view
   * @param savedInstanceState
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mNavController = Navigation.findNavController(view);

    // TODO if a user is not logged in, go to LoginFragment
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser == null) {
      NavDirections loginAction = DashboardFragmentDirections.actionNeedAuth();
      mNavController.navigate(loginAction);
      return;
    }
    Log.d(TAG, "currentUser: " + currentUser);
    TextView username = view.findViewById(R.id.txt_username);
    username.setText("Welcome, " + currentUser.getEmail());

    mDatabase = FirebaseDatabase.getInstance();
    mRef = mDatabase.getReference();

    // add dashboard code here
    valEventListener(view, currentUser);
    //add recylerview
    RecyclerView recycleView = view.findViewById(R.id.list);
    recycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
    adapter = new OpenGamesAdapter(view,requireContext());
    recycleView.setAdapter(adapter);
    getOpenMatches();
    // Show a dialog when the user clicks the "new game" button
    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {
      // A listener for the positive and negative buttons of the dialog
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        String gameType = "No type";
        String gameId = "";
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = getString(R.string.two_player);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = getString(R.string.one_player);
        }
        Log.d(TAG, "New Game: " + gameType);

        // Passing the game type as a parameter to the action
        // extract it in GameFragment in a type safe way
        NavDirections action = DashboardFragmentDirections.actionGame(gameType,gameId);
        mNavController.navigate(action);
      };

      // create the dialog
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
              .setCancelable(false)
          .setTitle(R.string.new_game)
          .setMessage(R.string.new_game_dialog_message)
          .setPositiveButton(R.string.two_player, listener)
          .setNegativeButton(R.string.one_player, listener)
          .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
          .create();
      dialog.show();
    });
  }

  /**
   * calls user reference and attaches a listener to the database and fetches wins,losses to update on dashboard.
   * @param view, currentUser
   */
  private void valEventListener(@NonNull View view, FirebaseUser currentUser) {
    mRef.child("User_Info").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
        assert userInfo != null;
        updateUI(userInfo, view);
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "onCancelled", databaseError.toException());
      }
    });
  }

  /**
   * Gets Open matches to display in the recycler view using a listener to game ref in database
    */
  private void getOpenMatches(){
    List<GameInfo> openGames = new ArrayList<>();
    mRef.child("Game_Info").addValueEventListener(new ValueEventListener() {
      /**
       * This method will be called with a snapshot of the data at this location. It will also be called
       * each time that data changes.
       *
       * @param snapshot The current data at the location
       */
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "onDataChange");
        openGames.clear();
        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
          // TODO: handle the post
          GameInfo game = dataSnapshot.getValue(GameInfo.class);
          assert game != null;
//          Log.d(TAG,"gameId" + game.getUUID());
          if(game.getOpen()) openGames.add(game);
        }
        adapter.setOpen(openGames);
      }

      /**
       * This method will be triggered in the event that this listener either failed at the server, or
       * is removed as a result of the security and Firebase Database rules.
       * @param error A description of the error that occurred
       */
      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "onCancelled" + error.getMessage());
      }
    });
  }

  /**
   * helper function to update UI
   * @param userInfo
   * @param v
   */
  private void updateUI(UserInfo userInfo,@NonNull View v){
    TextView score = v.findViewById(R.id.txt_score);
    score.setText("Wins: " + userInfo.getWins() + ", Losses: " + userInfo.getLosses());
  }

  /**
   * Override the onCreateOptionsMenu callback to inflate the menu bar icons
   * @param menu The menu on which the options is called
   * @param inflater The inflater that inflates the share_delete_menu resource
   */
  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }
}