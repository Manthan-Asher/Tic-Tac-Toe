package androidsamples.java.tictactoe;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

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
 * The Adapter class for the RecyclerView. This is used to link non compatible types and
 * also to show them on the view (UI).
 */

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {
  private static final String TAG = "OpenGamesAdapter";
  private List<GameInfo> openGames;
  private NavController mNavController;
  private DatabaseReference mRef;
  private String openGamePlayerEmail;
  private Context mContext;
  private String matchUUID;

  /**
   * Constructor. Initializes, navController and database reference
   * @param view
   * @param context
   */
  public OpenGamesAdapter(View view,Context context) {
    // FIXME if needed
    mNavController = Navigation.findNavController(view);
    mRef = FirebaseDatabase.getInstance().getReference("User_Info");
    openGamePlayerEmail = "";
    matchUUID = "";
    mContext = context;
  }

  /**
   * The overriden onCreateViewHolder method. It inflates the itemview and returns the ViewHolder object.
   * @param parent The parent ViewGroup
   * @param viewType the viewType of the view
   * @return an ViewHolder with the view
   */
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }


  /**
   * Overrides the onBindViewHolder method where we get the current open game
   * and binds it to the holder. I also fetch the user email from database to display which user created the game
   * @param holder The holder to bind to
   * @param position The position of the entry in the list.
   */
  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    // TODO bind the item at the given position to the holder
    if(openGames!=null){
      GameInfo game = openGames.get(position);
      matchUUID = game.getUUID();
      holder.mIdView.setText("Game ID - " + game.getUUID());
      mRef.child(game.getPlayer1()).addListenerForSingleValueEvent(
              new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                  Log.d(TAG, "onDataChange ");
                  UserInfo userInfo = snapshot.getValue(UserInfo.class);
                  assert userInfo != null;
                  Log.d(TAG, "getting user email to display in view holder - " + userInfo.getEmail());
                  openGamePlayerEmail = userInfo.getEmail();
                  holder.mContentView.setText(openGamePlayerEmail);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                  Log.w(TAG, "onCancelled", error.toException());
                }
              }
      );

    }
  }

  /**
   * Get the number of items in openGames.
   * @return
   */
  @Override
  public int getItemCount() {
    return (openGames == null) ? 0 : openGames.size();
  }


  /**
   * Set the openGames list to a new list and notifies that the DataSet has Changed.
   * @param openGames List of JournalEntry.
   */
  public void setOpen(List<GameInfo> openGames){
    this.openGames = openGames;
    Log.d(TAG, "openGames: " + openGames.size());
    notifyDataSetChanged();
  }


  /**
   * A Holder class for the RecyclerView. It is used to link the adapter and the
   * Recycler view to dynamically update UI.
   */
  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    /**
     * Constructor to fetch widgets by id and set on click listener
     * @param view
     */
    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.txt_gameID);
      mContentView = view.findViewById(R.id.txt_playerName);

      mView.setOnClickListener(this::launchGameFragment);
    }


    /**
     * To launch the particular game fragment
     * We pass the game type and match Id as safe args
     * @param v The view on which we do the navigation
     */
    private void launchGameFragment(View v) {
        Log.d(TAG, "launching the game fragment");
        FirebaseUser currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser());
        if(Objects.equals(currentUser.getEmail(), openGamePlayerEmail)){
          Toast.makeText(mContext, "Only Another Player can join the game", Toast.LENGTH_SHORT).show();
          return;
        }
      NavDirections gameAction = DashboardFragmentDirections.actionGame("Two-Player",matchUUID);
      mNavController.navigate(gameAction);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}