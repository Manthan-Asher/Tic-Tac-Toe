package androidsamples.java.tictactoe;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.espresso.accessibility.AccessibilityChecks;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Objects;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Accessibility check.
     */
    @BeforeClass
    public static void enableAccessibilityChecks() {
        AccessibilityChecks.enable();
    }


//    @Test
//    public void testNavigationToEntryListFragment() {
//        // Create a TestNavHostController
//        TestNavHostController navController = new TestNavHostController(
//                ApplicationProvider.getApplicationContext());
//
//        FragmentScenario<DashboardFragment> entryDetailsFragmentFragmentScenario
//                = FragmentScenario.launchInContainer(DashboardFragment.class, null, R.style.Theme_AppCompat, (FragmentFactory) null);
//
//        entryDetailsFragmentFragmentScenario.onFragment(fragment -> {
//            // Set the graph on the TestNavHostController
//            navController.setGraph(R.navigation.nav_graph);
//
//            // Make the NavController available via the findNavController() APIs
//            Navigation.setViewNavController(fragment.requireView(), navController);
//        });
//
//        // Verify that performing a click changes the NavController's state
//        onView(withId(R.id.fab_new_game)).perform(click());
//        assertThat(Objects.requireNonNull(navController.getCurrentDestination()).getId(), is(R.id.gameFragment));
//
//    }
}