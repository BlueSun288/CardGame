package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import Main.Card;
import Main.CardGamePrimary;

public class ClientThread extends Thread {
	
	//The server's IP address
	String serverAddress = "";
	
	//Where the client events are
	private ClientEvents events = CardGamePrimary.gameLogic.clientEvents;
	
	//The server's port
	int port = CardGamePrimary.PORT;
	
	//Socket connecting this client to the server
	private Socket socket;
	
	//Sends data to the server
	PrintWriter out;

	//Gets data from the server
	BufferedReader in;
	
	//Our player ID
	public int playerID;
	
	//Constructor
	public ClientThread( String serverAddress ) {
		this.serverAddress = serverAddress;
		
		//Start ourselves
		this.start();
	}
	
	//Main thread code
	public void run(){
		
		CardGamePrimary.ui.print( "Connecting to server..." );
		
		//Try to connect to the server
		try {
			socket = new Socket( serverAddress, port );
			
		} catch (IOException e) {
			
			//If we can't connect, display an error and stop the thread
			error( "Could not connect to server at " + serverAddress + " on port " + port );
			
			return;
			
		}
		
		//Try to set up the input and output streams
		try {
			//Create the in and out streams
			in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			out = new PrintWriter(socket.getOutputStream(), true);
			
			CardGamePrimary.ui.print( "Waiting for playerID" );
			
			//Listen for our player number
			String playerIDString = in.readLine();
			playerID = Integer.parseInt( playerIDString );
			
			CardGamePrimary.ui.print( "We are player " + playerID );
			
		} catch (IOException e) {
			
			//If we can't create the streams, display an error and stop the thread
			error( "Could not create streams" );
			
			return;
		}
		
		CardGamePrimary.ui.print( "Connected." );
		
		//Call the client connection event
		events.connectedToServerOnClient();
		
	}
	//Sends a line to the server
	private void writeLine( String str ){
		out.write( str + "\n" );
		out.flush();
	}
	
	//Writes an integer as a string for convenience sake
	private void writeLine( int num ){
		writeLine( Integer.toString( num ) );
	}
	
	//Listens for dealt cards
	public void listenForDealtCard() {
		
		CardGamePrimary.ui.print( "Listening for dealt card from server..." );
		
		//Start listening for a card from the server for our hand
			
		String line;
		
		//Try to read the next line
		try {
			line = in.readLine();
		} catch (IOException e) {
			error( "Encountered an error while reading initial card deal line. Exiting..." );
			return;
		}
		
		//If this line contains "carddealt" then the server is dealing us a card
		if( line.contains( "carddealt" ) ){
			
			//Read in the next two lines, which will be ints for card value and card suit, in that order
			try {
				
				//Read and parse the value
				String cardValueString = in.readLine();
				int cardValue = Integer.parseInt( cardValueString );
				
				//Read and parse the suit
				String cardSuitString = in.readLine();
				int cardSuit = Integer.parseInt( cardSuitString );
				
				//Recreate the dealt card object
				Card dealtCard = new Card( cardValue, cardSuit );
				
				//Call the dealt card event
				CardGamePrimary.gameLogic.clientEvents.cardDealtOnClient( dealtCard );
				
			} catch (IOException e) {
				error( "Encountered an error while reading dealt card values. Exiting..." );
				return;
			}
		}
	}
	
	//Listens for someone to play a card
	public void listenForPlayedCard() {
		
		CardGamePrimary.ui.print( "Listening for someone to play a card..." );
		
		//Try to read the next line
		String line;
		try {
			line = in.readLine();
		} catch (IOException e) {
			error( "Encountered an error while reading initial card deal line. Exiting..." );
			return;
		}
		
		if( line.contains( "cardplayed" ) ){
			
			//Read in the next three lines, which will be ints for playedByID, card value and card suit, in that order
			try {
				
				//Read and parse the playedByID
				String playedByIDString = in.readLine();
				int playedByID = Integer.parseInt( playedByIDString );
				
				//Read and parse the value
				String cardValueString = in.readLine();
				int cardValue = Integer.parseInt( cardValueString );
				
				//Read and parse the suit
				String cardSuitString = in.readLine();
				int cardSuit = Integer.parseInt( cardSuitString );
				
				//Recreate the dealt card object
				Card card = new Card( cardValue, cardSuit );
				
				//Call card played event
				events.cardPlayedOnClient(playedByID, card);
				
			} catch (IOException e) {
				error( "Encountered an error while reading played card values. Exiting..." );
				return;
			}
		}
		
	}
	
	//Listens for someone to start the next round
	public void listenForRoundStart() {
		
		CardGamePrimary.ui.print( "Listening for someone to start the next round..." );
		
		//Try to read the next line
		String line;
		try {
			line = in.readLine();
		} catch (IOException e) {
			error( "Encountered an error while reading round start line. Exiting..." );
			return;
		}
		
		if( line.contains( "roundstart" ) ){
			
			//Read in the next line to get the ID of who started the round
			try {
				
				//Read and parse the startedByID
				String startedByIDString = in.readLine();
				int startedByID = Integer.parseInt( startedByIDString );
				
				//Fire the event
				events.roundStartedOnClient( startedByID );
				
			} catch (IOException e) {
				error( "Encountered an error while reading round start. Exiting..." );
				return;
			}
		}
		
	}
	
	//Listens for someone to start the next game
	public void listenForGameStart() {
		
		CardGamePrimary.ui.print( "Listening for someone to start the next game..." );
		
		//Try to read the next line
		String line;
		try {
			line = in.readLine();
		} catch (IOException e) {
			error( "Encountered an error while reading game start line. Exiting..." );
			return;
		}
		
		if( line.contains( "gamestart" ) ){
			
			//Read in the next line to get the ID of who started the game
			try {
				
				//Read and parse the startedByID
				String startedByIDString = in.readLine();
				int startedByID = Integer.parseInt( startedByIDString );
				
				//Fire the event
				events.gameStartedOnClient( startedByID );
				
			} catch (IOException e) {
				error( "Encountered an error while reading game start. Exiting..." );
				return;
			}
		}
		
	}
	
	//Logs the error and calls the error event
	private void error( String error ) {
		
		//Log the error
		CardGamePrimary.ui.print( error );
		
		//Call the event
		events.error( error );
	}
	
	//Tries to play a card
	public void playCard( Card card ) {
		
		writeLine( "cardplayed" );
		writeLine( card.getValue() );
		writeLine( card.getSuit() );
	}
	
	//Tries to start the round
	public void startRound() {
		
		writeLine( "roundstart" );
		
	}
	
	//Tries to start the game
	public void startGame() {
		
		writeLine( "gamestart" );
		
	}
	
}
