package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Main.Card;
import Main.CardGamePrimary;

public class ServerThread extends Thread {

	//Server socket used to act as server
	private ServerSocket serverSocket;

	//Thread used for the server to communicate with player 2
	private ServerToClientThread P2Thread;
	
	//Thread used for the server to communicate with player 3
	private ServerToClientThread P3Thread;
	
	//Simple solution to map player IDs to threads
	ServerToClientThread[] players = new ServerToClientThread[4];

	//Executor to make threading easier
	private ExecutorService executor = Executors.newFixedThreadPool(3);

	//Constructor
	public ServerThread() {
		//Start ourselves
		this.start();
	}
		
	
	//When the thread is run
	public void run(){
		
		//Start networking
		try {
			//Attempt to bind a server socket to the port
			serverSocket = new ServerSocket( CardGamePrimary.PORT );
		} catch (IOException e) {

			//If we can't do that, something has gone wrong

			//No server socket means no server so we need to "gracefully" stop here
			CardGamePrimary.ui.print( "Server failed to start" );

			//Stop any networking that has happened so far
			stopServer();

			return;
		}

		//If we got past the try catch then the server was started successfully
		CardGamePrimary.ui.print( "Server started" );

		//Listen for player 2 to connect
		Socket P2Socket;
		try {
			
			CardGamePrimary.ui.print( "Waiting for player 2 to connect..." );
			
			P2Socket = serverSocket.accept();
		} catch (IOException e) {

			//Something can go wrong while accepting so we have to handle that
			CardGamePrimary.ui.print( "Unable to connect to player 2" );

			//Game can't start without player 2

			//Stop any networking that has happened so far
			stopServer();
			
			//Stop trying to start the server
			return;
		}
		
		CardGamePrimary.ui.print( "Player 2 connected" );

		//Once we have player 2's socket connected, start a new server to client thread for it
		P2Thread = new ServerToClientThread( P2Socket, 2 );
		
		//Set up players array
		players[2] = P2Thread;
		
		executor.submit( P2Thread );
		
		//Listen for player 3 to connect
		Socket P3Socket;
		try {
			
			CardGamePrimary.ui.print( "Waiting for player 3 to connect..." );
			
			P3Socket = serverSocket.accept();
		} catch (IOException e) {

			//Something can go wrong while accepting so we have to handle that
			CardGamePrimary.ui.print( "Unable to connect to player 3" );

			//Game can't start without player 3

			//Stop any networking that has happened so far
			stopServer();

			//Stop trying to start the server
			return;
		}
		
		CardGamePrimary.ui.print( "Player 3 connected" );
		
		

		//Once we have player 2's socket connected, start a new server to client thread for it
		P3Thread = new ServerToClientThread( P3Socket, 3 );
		
		//Set up players array
		players[3] = P3Thread;
				
		executor.submit( P3Thread );
		
	}
	
	public void stopServer(){
		//Close the server socket
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Close threads
		executor.shutdown();
	}
	
	//Sends a card to all players
	public void broadcastCardPlayed( int playedByID, Card card ){
		
		//Send this card to player 2
		sendCardPlayed( 2, playedByID, card );
		
		//Send this card to player 3
		sendCardPlayed( 3, playedByID, card );
		
	}
	
	//Sends a played card to a specific player
	//Called by broadcastCard internally
	private void sendCardPlayed( int playerID, int playedByID, Card card ){
		
		//Send the card to the player via the player's thread
		players[ playerID ].sendPlayedCard( playedByID, card );
		
	}
	
	//Sends a dealt card to a player
	public void sendCardDealt( int playerID, Card card ){
		
		players[ playerID ].sendDealtCard( card );
	}
	
	//Broadcasts to every player that a new game has started
	public void broadcastGameStart( int startedByID ){
		
		players[2].sendGameStart( startedByID );
		players[3].sendGameStart( startedByID );
		
	}
	
	//Broadcasts to every player that a new round has started
	public void broadcastRoundStart( int startedByID ){
		
		//Tell each player that the game has started
		players[2].sendRoundStart( startedByID );
		players[3].sendRoundStart( startedByID );
		
	}
	
	//Begins listening for a player to play a card
	public void listenForCardPlayed( int playerID ) {
		players[ playerID ].ListenForCardPlayed();
	}
	
	//Begins listening for a player to start the round
	public void listenForRoundStart( int playerID ) {
		players[ playerID ].ListenForRoundStart();
	}
	
	//Begins listening for a player to start a new game
	public void listenForGameStart( int playerID ) {
		players[ playerID ].ListenForGameStart();
	}

}
