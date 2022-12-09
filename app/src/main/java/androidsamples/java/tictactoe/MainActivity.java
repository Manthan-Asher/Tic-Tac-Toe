package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  /**
   * activity lifecycle method, where set the content view and initialising toolbar
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }

  /**
   * Runs when logout is clicked in menu bar
   * @param item
   * @return
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_logout) {
      Log.d(TAG, "logout clicked");
      // TODO handle log out
      FirebaseAuth.getInstance().signOut();
      NavController mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
      mNavController.navigate(R.id.loginFragment);
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Credit - Shameek Baranwal
   *  this is to accommodate for the fact that the stack is not cleared when navigating to the login or dashboard,
   *  so pressing back button leads to confusing behaviour (e.g. pressing back from login leads back to game)
   */
  @Override
  public void onBackPressed() {
    Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
    assert navHostFragment != null;
    Fragment current = navHostFragment.getChildFragmentManager().getFragments().get(0);
    if (current.getClass().getSimpleName().equals("DashboardFragment") || current.getClass().getSimpleName().equals("LoginFragment")) {
      finish();
    } else {
      super.onBackPressed();
    }
    Log.d(TAG, "onBackPressed: " + current.getClass().toString());
  }
}