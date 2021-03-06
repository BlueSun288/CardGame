package UI;

import java.util.ArrayList;

public interface GUIInterface {

    //Server methods

    void playerConnected(int playerId);

    //Client methods

    void connectedToServer(int playerId);

    //General methods

    void roundStarted();

    void roundWinner(int playerId);

    void gameStarted();
    
    void gameWinner(int playerId);

    void error(String error);

    void startingHand(ArrayList<Main.Card> cards);

    void playableCards(ArrayList<Main.Card> cards);

    void updateScores(int player1, int player2, int player3);
}
