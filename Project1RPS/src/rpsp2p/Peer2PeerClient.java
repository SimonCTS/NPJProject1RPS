/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpsp2p;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import project1rps.Choice;
import rpsgui.Window;

/**
 *
 * @author Simon Cathebras, Zoe Bellot
 */
public class Peer2PeerClient extends Thread{

    private Integer localPort;
    private ArrayList<Peer> peerList;
    private ArrayList<Choice> choiceList;
    private ArrayList<Choice> nextChoiceList;
    private Integer game;
    private Choice playerChoice;
    private Boolean listening;
    private Window rpsWindow;

    /**
     *
     * @param playerName
     * @param remoteIpAdress
     * @param remotePort
     * @param port
     */
    public Peer2PeerClient(
            Integer port,
            Window window) {
        this.localPort = port;
        this.peerList = new ArrayList<Peer>();
        this.listening = true;
        this.rpsWindow = window;
    }

    
    public void addPeer(InetAddress ipAddress, Integer remotePort){
        this.peerList.add(new Peer(ipAddress,remotePort));
    }

    /**
     *
     * Connect the calling peer to an existing ring
     *
     * @since peerList is correctly initialised with remote port and remote Ip
     * adress
     *
     */
    private Integer connectToRing() {
        Peer target = peerList.get(0);
        Socket clientSocket = null;

        try {
            clientSocket = new Socket(target.ipAdress, target.port);
        } catch (IOException ex) {
            System.err.println("unable to connect "
                    + target.ipAdress.toString()
                    + ":"
                    + target.port);
        }

        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.toString());
            System.exit(1);
        }

        byte[] toTarget = "JOIN".getBytes();
        try {
            out.write(toTarget, 0, toTarget.length);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Peer2PeerClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        Object newPeerList = null;
        try {
            newPeerList = in.readObject();
        } catch (IOException ex) {
            System.err.println(ex.toString());
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
            return 1;
        }

        /*
         * Integer nextRound; try { nextRound = in.readInt(); } catch
         * (OptionalDataException ode) { System.out.println(ode.toString());
         * return 1; }
         */

        if (!(newPeerList instanceof ArrayList)) {
            System.err.println("Transmission error");
            System.exit(1);
        }

        for (Iterator<Peer> it = ((ArrayList) newPeerList).iterator();
                it.hasNext();) {
            Peer peer = it.next();
            peerList.add(peer);
        }
        
        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.out.println(e.toString());
            System.exit(1);
        }

        Object number = null;
        try
        {
            number = in.readObject();
        } catch (IOException ex) {
            System.err.println(ex.toString());
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
            return 1;
        }
        
        if (!(number instanceof Integer)) {
            System.err.println("Transmission error");
            System.exit(1);            
        }
        
        this.game = (Integer) number;
        
        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException ex) {
            System.err.println(ex.toString());
        }
        
        
        /*ENVOYER LE JOIN AUX AUTRES NODES*/

        return 0;
    }

    /**
     * do the required job to insert peer into the p2p ring Send the actual
     * peerList to this peer, using clientSocket.
     *
     * @param clientSocket
     * @param peer
     */
    private void doJoin(Socket clientSocket, Peer peer) {
        ObjectOutputStream out = null;

        /*
         * Send the peerList to the new host
         */
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.writeObject(peerList);
            out.flush();
            out.writeObject(game+1);
            out.flush();
        } catch (IOException ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }

        try {
            out.close();
        } catch (IOException ex) {
            System.err.println("error while closing the stream");
        }
        
        /*ADD THE RECEIVED PEER TO THE LIST*/
        peerList.add(peer);
        

    }

    /**
     * Remove peer from the peerList
     *
     * @param peer
     */
    private void doQuit(Peer peer) {
        peerList.remove(peer);
    }

    /**
     * Compute the result sended by peer
     * @param peer 
     */
    private void doPlay(Socket clientSocket){
        ObjectInputStream in = null;

        /* Receive the choie of the other host */   
        try {       
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }

        Object choice = null;
        try {
            choice = in.readObject();
        } catch (IOException ex) {
            System.err.println("error in reading");
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
        }
        
        if (! (choice instanceof Choice)) {
            System.err.println("Transmission error");
            System.exit(1);
        }
        
        /* Receive the game number */
        try {       
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }

        Object gameNumber = null;
        try {
            gameNumber = in.readObject();
        } catch (IOException ex) {
            System.err.println("error in reading");
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
        }
        
        if (! (gameNumber instanceof Integer)) {
            System.err.println("Transmission error");
            System.exit(1);
        }
          
        if (game == gameNumber) {
            choiceList.add((Choice)choice);
            if (choiceList.size() == peerList.size() - 1) {
                endOfGame();
            }      
        } else {
            nextChoiceList.add((Choice)choice);
        }
        
        try {
            in.close();
        } catch (IOException ex) {
            System.err.println("error while closing the input stream");
        }
    }
    
    /**
     * 
     * Compute the game score
     * 
     */
    private void endOfGame () {
        /*Compute the game score */
        Integer score = 0;
        for (Iterator<Choice> it = choiceList.iterator(); it.hasNext();){
            if (win(playerChoice, it.next())) {
                score = score + 1;
            }
        }
        game = game + 1;
        /* Update choices for the next game */
        choiceList = nextChoiceList;
        nextChoiceList.clear();
        rpsWindow.setScore(score);
    }
    
    /**
     * 
     * Return if p1 win or no
     * 
     * @param p1 : choice of the first player
     * @param p2 : choice of the second player
     * @return boolean that indiates if p1 win or no
     */
    private boolean win(Choice p1, Choice p2) {
        if (p1 == ROCK) {
            if (p2 == ROCK) {
                return false;
            }
            if (p2 == SCISSORS) {
                return true;
            }
            if (p2 == PAPER) {
                return false;
            }
        }
        if (p1 == SCISSORS)  {
            if (p2 == ROCK) {
                return false;
            }
            if (p2 == SCISSORS) {
                return false;
            }
            if (p2 == PAPER) {
                return true;
            }            
        }
        if (p1 == PAPER){
            if (p2 == ROCK) {
                return true;
            }
            if (p2 == SCISSORS) {
                return false;
            }
            if (p2 == PAPER) {
                return false;
            }            
        } else {
            System.err.println("Choice error");
            System.exit(1);
            return false;
        }            
    }

    /**
     * Handle all incoming request on this peer
     *
     * @param clientSocket
     * @return
     */
    private Integer handleConnection(Socket clientSocket) {
        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }

        byte[] input = new byte[4];
        try {
            in.read(input, 0, input.length);
        } catch (IOException ex) {
            System.err.println("error in reading");
        }

        if (input.equals("JOIN")) {
            doJoin(clientSocket, new Peer(
                    clientSocket.getInetAddress(),
                    clientSocket.getPort()));
        }
        if (input.equals("QUIT")) {
            doQuit(new Peer(
                    clientSocket.getInetAddress(),
                    clientSocket.getPort()));
        }
        if (input.equals("PLAY")) {
            doPlay(clientSocket);
        }
        try {
            in.close();
        } catch (IOException ex) {
            System.err.println("error while closing the input stream");
        }
        return 0;
    }

    /**
     * Sends to each peer member within peerList an "end of game" message Also
     * sets the listening boolean to false
     *
     * @param peerList
     * @return error code
     */
    public Integer disconnect() throws IOException {
        /*
         * broadcast to all peers that we are leaving
         */
        for (Iterator<Peer> it = peerList.iterator(); it.hasNext();) {
            Peer peer = it.next();
            /*
             * create socket
             */
            Socket clientSocket = null;
            try {
                clientSocket = new Socket(peer.ipAdress, peer.port);
            } catch (IOException ex) {
                System.err.println("unable to connect "
                        + peer.ipAdress.toString()
                        + ":"
                        + peer.port);
            }

            BufferedOutputStream out = null;

            try {
                out = new BufferedOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println(e.toString());
                System.exit(1);
            }

            /*
             * send bye
             */
            byte[] toTarget = "QUIT".getBytes();
            try {
                out.write(toTarget, 0, toTarget.length);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex);
            }

            /*
             * close streams and socket
             */
            out.close();
            clientSocket.close();

        }
        /*
         * stop listening
         */
        listening = false;
        return 0;
    }

    /**
     *
     * @param choice
     * @return Sends to all peers the choice of the local user
     */
    public Integer sendToPeers(Choice choice) throws IOException {
        for (Iterator<Peer> it = peerList.iterator(); it.hasNext();) {
            Peer peer = it.next();
            /*
             * create socket
             */
            Socket clientSocket = null;
            try {
                clientSocket = new Socket(peer.ipAdress, peer.port);
            } catch (IOException ex) {
                System.err.println("unable to connect "
                        + peer.ipAdress.toString()
                        + ":"
                        + peer.port);
            }

            ObjectOutputStream out = null;

            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println(e.toString());
                System.exit(1);
            }

            /*
             * send the choice
             */
            byte[] toTarget = "PLAY".getBytes();

            try {
                out.write(toTarget);
                out.flush();
                out.writeObject(choice);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex);
            }

            /*
             * close streams and socket
             */
            out.close();
            clientSocket.close();

        }
        return 0;
    }

    public void run() {
        ServerSocket serverSocket = null;

        /*
         * connection to the ring
         */
        connectToRing();

        try {
            serverSocket = new ServerSocket(localPort);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + localPort);
            System.exit(1);
        }


        while (listening) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleConnection(clientSocket);
            } catch (IOException ex) {
                System.err.print("Connection error on port: " + localPort);
            }
        }

        try {
            serverSocket.close();
        } catch (IOException ex) {
            System.err.println("Closing of the connection "
                    + "raised the following exception: "
                    + ex);
        }
    }
}
