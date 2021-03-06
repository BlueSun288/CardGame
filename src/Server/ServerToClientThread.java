package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import Main.Card;
import Main.CardGamePrimary;

public class ServerToClientThread extends Thread {

	
	//The socket to the client
	private Socket socket;
	
	//Indicates which player this thread is for
	public int playerID;

	//Sends data to the client
	PrintWriter out;

	//Gets data from the client
	BufferedReader in;

	//Constructor
	public ServerToClientThread( Socket clientSocket, int playerID ){

		//Set socket to the socket that is already connected to this thread's client
		socket = clientSocket;
		
		//Set this player's player number
		this.playerID = playerID;

	}

	//Main thread code
	public void run(){

		//Try to set up the in and out streams to and from the client
		try {
			out = new PrintWriter( socket.getOutputStream(), true);
			in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			
			writeLine( playerID );
			
		} catch (IOException e) {

			//If we couldn't establish a proper connection to the client, we can't move forward
			error( "Could not open streams to player " + playerID );
			
			return;
		}

		CardGamePrimary.ui.print( "Player " + playerID + " connected successfully" );
		
		
		//Call the player connection event
		CardGamePrimary.gameLogic.serverEvents.playerConnectedOnServer( playerID );

	}
	
	//Sends a line to the client
	private void writeLine( String str ){
		out.write( str + "\n" );
		out.flush();
	}
	
	//Writes an integer as a string for convenience sake
	private void writeLine( int num ){
		writeLine( Integer.toString( num ) );
	}

	//Sends a dealt card to this player's hand
	public void sendDealtCard( Card card ){
		CardGamePrimary.ui.print( "Dealing " + card + " to player " + playerID );
		writeLine( "carddealt" );
		writeLine( card.getValue() );
		writeLine( card.getSuit() );
	}
	
	//Sends a card played by another player to this player
	public void sendPlayedCard( int playedByID, Card card ){
		CardGamePrimary.ui.print( "Informing player " + playerID + " that player " + playedByID + " played " + card );
		
		writeLine( "cardplayed" );
		writeLine( playedByID );
		writeLine( card.getValue() );
		writeLine( card.getSuit() );
		
	}
	
	//Sends the player a notice that the game has started
	public void sendGameStart( int startedByID ){
		CardGamePrimary.ui.print( "Informing player " + playerID + " that a game is starting" );
		
		writeLine( "gamestart" );
		writeLine( startedByID );
	}
	
	//Sends the player a notice that the round has started
	public void sendRoundStart( int startedByID ){
		CardGamePrimary.ui.print( "Informing player " + playerID + " that a round is starting" );
		
		writeLine( "roundstart" );
		writeLine( startedByID );
	}
	
	//Listen for this player to play a card
	public void ListenForCardPlayed() {
		
		CardGamePrimary.ui.print( "Listening for a card from client..." );
		
		//No loop as we only need to listen for a single card	
		try {
		
			//Get the next string
			String line = in.readLine();
			
			//See if it's a played card
			if( line.contains( "cardplayed" ) ) {
				
				//Read in the value
				String stringValue = in.readLine();
				int value = Integer.parseInt( stringValue );
				
				//Read in the suit
				String stringSuit = in.readLine();
				int suit = Integer.parseInt( stringSuit );
				
				//Make a card out of that information
				Card card = new Card( value, suit );
				
				//Call the card played event
				CardGamePrimary.gameLogic.serverEvents.cardPlayedOnServer( playerID, card );
				
			}
		} catch( IOException e ) {
			error( "Listening for card failed" );
			
			return;
		}
		
	}
	
	//Listen for this player to start the round
	public void ListenForRoundStart() {
		
		CardGamePrimary.ui.print( "Listening for round start from player " + playerID );
		
		try {
		
			//Get the next string
			String line = in.readLine();
			
			//See if it's a played card
			if( line.contains( "roundstart" ) ) {
				
				//Call the round started event
				CardGamePrimary.gameLogic.serverEvents.roundStartedOnServer( playerID );
				
			}
		} catch( IOException e ) {
			error( "Listening for round start failed" );
			
			return;
		}
		
	}
	
	//Listen for the start of the next game
	public void ListenForGameStart() {
		CardGamePrimary.ui.print( "Listening for game start from player " + playerID );
		
		try {
		
			//Get the next string
			String line = in.readLine();
			
			//See if it's a played card
			if( line.contains( "gamestart" ) ) {
				
				//Call the round started event
				CardGamePrimary.gameLogic.serverEvents.gameStartedOnServer( playerID );
				
			}
		} catch( IOException e ) {
			error( "Listening for game start failed" );
			
			return;
		}
	}
	
	//Logs the error and calls the error event
	public void error( String error ) {
		
		//Log the error
		CardGamePrimary.ui.print( error );
		
		//Call the event
		CardGamePrimary.gameLogic.serverEvents.error( error );
	}
	
}
