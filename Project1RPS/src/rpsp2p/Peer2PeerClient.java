/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpsp2p;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fingolfin
 */
public class Peer2PeerClient extends Thread {
    private String localPlayerName;
    private Integer localPort;
    private ArrayList<Peer> peerList;

    public Peer2PeerClient(String playerName, InetAddress ipAdress, Integer port) {
        this.localPlayerName = playerName;
        this.localPort = port;
        this.peerList = null;
    }
    
    private Integer ConnectToRing(InetAddress ipAdress, Integer port){
        return 0;
    }
    
    private Integer handleConnection(Socket clientSocket){
        return 0;
    }
    

    public void run(){
        Boolean listening = true;
        ServerSocket serverSocket = null;
        
        try {
            serverSocket = new ServerSocket(localPort);
        } catch (IOException e) {
            System.err.println("Could not listen on port: "+ localPort);
            System.exit(1);
        }
        
        while (listening) {            
            try {
                Socket clientSocket = serverSocket.accept();
                handleConnection(clientSocket);
            } catch (IOException ex) {
                System.err.print("Connextion error on port: " + localPort);
            }
        }
        
    }
    
    
}
