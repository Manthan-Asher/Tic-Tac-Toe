package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Fragment to handle the sign up and log in logic.
 */
public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private NavController mNavController;
    private static final String TAG = "LoginFragment";

    /**
     * On create lifecycle method. initialises firebase auth instance
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // TODO if a user is logged in, go to Dashboard
        //initially implemented in login fragment but commented.
    }

    /**
     * On Create View, extracts the widgets and sets onclick listeners. Sign in logic also implemented.
     * If a user already exists, they will be logged in and navigated to dashboard automatically.
     * If a user does not exists, it will first check that via the exception and then sign up the new user in database.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {
                    // TODO implement sign in logic
                    EditText email = view.findViewById(R.id.edit_email);
                    EditText password = view.findViewById(R.id.edit_password);
                    String e = email.getText().toString();
                    String p = password.getText().toString();
                    if(e.isEmpty() || p.isEmpty()){
                        Toast.makeText(requireContext(), "Please enter Valid Email and Password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d(TAG, "email: " + e + ", password: " + p);
                    mAuth.signInWithEmailAndPassword(e, p)
                            .addOnCompleteListener(requireActivity(), task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                    Navigation.findNavController(v).navigate(action);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    if(Objects.requireNonNull(task.getException()).getClass().getSimpleName().equals("FirebaseAuthInvalidUserException")){
                                        Log.d(TAG, "User does not exist, creating a new user...");
                                        signUp(e,p);
                                    }else {
                                        Log.w(TAG, "signInWithEmailAndPassword:failure",task.getException());
                                        Toast.makeText(requireContext(), "Please enter Valid Email and Password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                });
        return view;
    }

    /**
     * Function is called for signing up a new user. Email must be valid and password must be at least 6 characters.
     * User is navigated to dashboard after successful signup process
     * @param email
     * @param password
     */
    private void signUp(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signUpWithEmail:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            // add to UserInfo
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference userRef = database.getReference("User_Info");
                            assert currentUser != null;
                            UserInfo user = new UserInfo(currentUser.getUid(),0,0, currentUser.getEmail());
                            userRef.child(currentUser.getUid()).setValue(user);
                            NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                            mNavController.navigate(action);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(requireContext(), "Please enter Valid Email and Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * On view created lifecycle method. It checks if user is logged in already and moves to dashboard.
     * Commented out because it is redundant. Home fragment is dashboard. If user is logged in, they will already be there
     * and if not they will be navigated to the login page
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNavController = Navigation.findNavController(view);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            NavDirections loginAction = LoginFragmentDirections.actionLoginSuccessful();
            mNavController.navigate(loginAction);
        }
    }

    // No options menu in login fragment.
}