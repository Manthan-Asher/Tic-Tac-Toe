# Tic Tac Toe

This is a starter code for the Tic Tac Toe multiplayer game app assignment.

It uses Android Navigation Component, with a single activity and three fragments:

- The DashboardFragment is the home screen. If a user is not logged in, it should navigate to the
LoginFragment. (See the TODO comment in code.)

- The floating button in the dashboard creates a dialog that asks which type of game to create and
passes that information to the GameFragment (using SafeArgs).

- The GameFragment UI has a 3x3 grid of buttons. They are initialized in the starter code.
Appropriate listeners and game play logic needs to be provided.

- Pressing the back button in the GameFragment opens a dialog that confirms if the user wants to
forfeit the game. (See the FIXME comment in code.)

- A "log out" action bar menu is shown on both the dashboard and the game fragments. Clicking it
should log the user out and show the LoginFragment. This click is handled in the MainActivity.

# A5- Tic Tac Toe

### Submitted by-

- Name - **Manthan Asher**
- ID - **2019A7PS0144G**
- Email - **f20190144@goa.bits-pilani.ac.in**

---

## Overview

- **Description of Application**

  This app simulates a Tic Tac Toe game between two-player or a single player on a device. The games and their data are stored in the Realtime Database in Firebase. I am using Firebase Authentication to signup/log in a user in the application.

  There is 1 Main Activity and 3 known Fragments - Login Fragment, Dashboard Fragment and Game Fragment.

- **Known Bugs**

  On back pressed for forfeit results does not update Wins and Losses. I tried to implement that feature but the app was crashing for some reason for that person, althought the wins and losses were getting updated in database and for other player. I felt crashing might be a bigger issue hence I commented the code that handled that. The app crashed with a "Fragment not attached to context" issue. I tried finding fixes for it but it didn't work work.
  
  Although there are no other known bugs in the application, I could not resolve some accessibility scanner issues of the Menu Bar. Below are some screenshots of the application.

  ![sdpd-b.jpeg](A5-%20Tic%20Tac%20Toe%20e3ac2939b7a64dbe9de7eb382a615d01/sdpd-b.jpeg)

![sdpd=2.jpeg](A5-%20Tic%20Tac%20Toe%20e3ac2939b7a64dbe9de7eb382a615d01/sdpd2.jpeg)

![sdpd-a.jpeg](A5-%20Tic%20Tac%20Toe%20e3ac2939b7a64dbe9de7eb382a615d01/sdpd-a.jpeg)

![sdpd=e.jpeg](A5-%20Tic%20Tac%20Toe%20e3ac2939b7a64dbe9de7eb382a615d01/sdpde.jpeg)

![sdpd=c.jpeg](A5-%20Tic%20Tac%20Toe%20e3ac2939b7a64dbe9de7eb382a615d01/sdpdc.jpeg)

## Tasks Completion Description

- **Task 1**
    - Sign-in was implemented by using the FirebaseAuth instance which provides two methods, `createAccountWithEmailAndPassword` and `signInWithEmailAndPassword`
    - After setting up auth in firebase console, we check if the user exists with the credentials and accordingly sign up or sign in.
    - There is a signout button in the Toolbar which when pressed logs the user out and takes them to the login fragment.
    - The dashboard shows the wins, losses, and open games, where each open game is in a recycler view list that shows the game id and the user who created it.
    - There is a floating action button that shows a dialog to create a one player game or a two player game.
    - A one player game is played between the computer and the user while a two player game uses the real-time database to actually add another user.
- **Task 2**

  I am storing a user instance and a game instance in the database. I am using a general object, hence my database stores single player games as well. By default the player who created the game is Player 1 with symbol “X” and Player 2 is Computer or the other player who joins with symbol “O”.

    - The single-player mode plays the user against the computer.
    - The user has their turn to click. I check if the cell is not filled already. The computer then picks up the first empty button out of the remaining ones and marks it with an “O”.
    - At every move, I check for the result. If a game ends by win, loss or draw, I show a dialog box and end the game and takes back to dashboard.
    - If the user wins, a dialog shows the win message and then the user’s score gets updated in real time in the database in Firebase.
    - Similarly if the user loses, then their score is automatically incremented in the loss count and this is reflected in real time.
    - All of this leads the user back to the dashboard which shows the available new scores.
- **Task 3**
    - As mentioned earlier, two-player game works in a similar way as single-player, except that some other user has to join. This meant that there were listeners used to check for live data changes and accordingly data on the application was updated.
    - We wait for the game to play until another user joins. Once they join, we begin the game.
    - Clicking logic was the same for the game as the single player logic, but there were certain caveats in the way the players joined the game.
    - A listener was needed to check for when a user joined an ongoing game, and also listeners to check for when data was being changed on the database.
    - Checks for win, loss and draw were done very much in the same manner as before but we do it for both users.
    - The major chunk of work was in setting up how the model must behave and when do updates to the database happen.
    - To enable easy addition into the database, I created two models for `UserInfo` and `GameInfo` respectively.
    - Both those endpoints were also present in the database which in essence stored Player and Game objects respectively.

## Accessibility

1. **Talk-Back Experience -**

   I ran the application using **TalkBack** service. The service is fairly easy to use and guides the user on each and every step. Elements on the screen were read aloud. Overall, TalkBack is a great accessibility tool which can help blind people or people with vision impairments to use apps. For this app, I had some difficulties in using talkback especially in playing game because we need to check everytime where the other user made a move and then think of the specific cell to click in response to that and so on.

2. **Accessibility Scanner -**

   The accessibility scanner revealed 3 suggesstions that can improve my app.

    1. `Touch Target` : The touch target height for Email and Password EditText widgets was less than normal. Changed *`android:layout_height*="48dp"` to fix that.
    2. `No speakable Text Present` - There was no speakable text present for the tictactoe cells. Fixed that using *`android:contentDescription*="@string/cell"`
    3. `Text contrast`: Inappropriate text contrast ratio. This issue was fixed by adding and setting the `android:textColor` property.

   I ignored the Text Scaling accessibility suggestions.


## Testing Procedure

- I only used the monkey testing tool with this assignment this time.
- The way to do this is by running `adb shell monkey -p androidsamples.java.tictactoe -v n`
- Where n is the number of iterations.
- I ran the monkey tool successfully for 2500, 5000, and 10000 iterations. The app did not crash on any run.

## Hosting the App

App is hosted on firebase and uses realtime database. To run the app simply clone the repo and run using android studio.

## Time taken

It took approximately **30 hours** to complete the assignment. Most of the time went in two player game logic (15 hours)

## Difficulty

In my opinion, I would rate this assignment 10/10 in terms of difficulty. Had to read about firebase and implementing and testing Two player game was an issue.

## Sources

- Android docs: [https://developer.android.com/docs](https://developer.android.com/docs)
- Firebase docs: [https://firebase.google.com/docs/](https://firebase.google.com/docs/)
- Rajath V(f20190122) helped me with Readme and accessibility testing.
- Arun Ganti(f20190021) helped me in two player game logic and debugging bugs.
- Shameek Baranwal(f20191098) helped me resolve onBackPressed issues and helped me debug.
