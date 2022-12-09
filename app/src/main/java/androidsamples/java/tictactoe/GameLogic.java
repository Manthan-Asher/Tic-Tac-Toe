package androidsamples.java.tictactoe;

import androidx.lifecycle.ViewModel;

import java.util.List;

/**
 * Model class for Game Objects. Holds an instance of game object.
 * The current status of the match that we receive from the database, we're storing that status here.
 * It holds one additional field "turnToGo" which essentially tells is the current player player1 or 2
 * and decides when to make a move in 2 player. It also holds the logic for single player game.
 */
public class GameLogic extends ViewModel{
    private static final String TAG = "GameLogic";

    public boolean singlePlayer;
    public boolean open;
    public List<String> tictactoe;
    public String status;
    public String matchUUID;
    public String player1;
    public String player2;
    public String winner;
    public Integer turn;
    public Integer turnToGo;

    /**
     * Since it is a view model, had to define an initialisation function instead of constructor
     * @param matchUUID
     * @param singlePlayer
     */
    public void initialise1(String matchUUID,boolean singlePlayer){
        this.matchUUID = matchUUID;
        this.singlePlayer = singlePlayer;
    }


    public void initialise2(GameInfo game) {
        open = game.getOpen();
        singlePlayer = game.getSinglePlayer();
        winner = game.getWinner();
        status = game.getStatus();
        matchUUID = game.getUUID();
        player1 = game.getPlayer1();
        player2 = game.getPlayer2();
        turn = game.getTurn();
        tictactoe = game.getTictactoe();
    }

    /**
     * This function is called when the current user makes a move(player1 in case of single player).
     * It checks if the current cell is empty and assigns the symbol to that cell.
     * Symbol is always X if player1 and O for player2.
     * @param index in list where move is to be made
     * @param player index
     * @return true if valid move, false otherwise
     */
    public boolean play(Integer index,Integer player){
        String symbol = player == 1 ? "X" : "O";

        if(tictactoe.get(index).equals("")){
            tictactoe.set(index,symbol);
            return true;
        }
        return false;
    }

    /**
     * Checks if the game is concluded after every move.
     *
     * @return 0 if game not finished yet, 1 if player1 won, 2 if player2 won, 3 if draw
     */
    public int checkResult(){
        String winChar = "";
        boolean isFilled = true;
        for(int i = 0; i <9; i++){
            isFilled = !tictactoe.get(i).equals("");
            if (!isFilled) break;
        }

        if  (tictactoe.get(0).equals(tictactoe.get(1)) && tictactoe.get(1).equals(tictactoe.get(2)) && !tictactoe.get(0).isEmpty()) winChar = tictactoe.get(0);
        else if (tictactoe.get(3).equals(tictactoe.get(4)) && tictactoe.get(4).equals(tictactoe.get(5)) && !tictactoe.get(3).isEmpty()) winChar = tictactoe.get(3);
        else if (tictactoe.get(6).equals(tictactoe.get(7)) && tictactoe.get(7).equals(tictactoe.get(8)) && !tictactoe.get(6).isEmpty()) winChar = tictactoe.get(6);
        else if (tictactoe.get(0).equals(tictactoe.get(3)) && tictactoe.get(3).equals(tictactoe.get(6)) && !tictactoe.get(0).isEmpty()) winChar = tictactoe.get(0);
        else if (tictactoe.get(4).equals(tictactoe.get(1)) && tictactoe.get(1).equals(tictactoe.get(7)) && !tictactoe.get(1).isEmpty()) winChar = tictactoe.get(1);
        else if (tictactoe.get(2).equals(tictactoe.get(5)) && tictactoe.get(5).equals(tictactoe.get(8)) && !tictactoe.get(2).isEmpty()) winChar = tictactoe.get(2);
        else if (tictactoe.get(0).equals(tictactoe.get(4)) && tictactoe.get(4).equals(tictactoe.get(8)) && !tictactoe.get(0).isEmpty()) winChar = tictactoe.get(0);
        else if (tictactoe.get(6).equals(tictactoe.get(4)) && tictactoe.get(4).equals(tictactoe.get(2)) && !tictactoe.get(2).isEmpty()) winChar = tictactoe.get(2);
        else if(isFilled) return 3;
        else return 0;

        return (winChar.equals("X")) ? 1 : 2;
    }

    /**
     * This functions basically fetches computer move in single player game. The logic is to find first empty cell and
     * fill it.
     */
    public void getComputerMove(){
        for(int i=0;i<9;i++){
            if(tictactoe.get(i).equals("")){
                tictactoe.set(i,"O");
                return;
            }
        }
    }

    public void reset(){

    }
}
