/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpsp2p;

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
    private ArrayList<Peer> nextPeerList;
    private ArrayList<Choice> choiceList;
    private ArrayList<Choice> nextChoiceList;
    private Integer game;
    private Choice playerChoice;
    private Boolean listening;
    private Window rpsWindow;
    private ServerSocket serverSocket;

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
        this.nextPeerList = new ArrayList<Peer>();
        this.choiceList = new ArrayList<Choice>();
        this.nextChoiceList = new ArrayList<Choice>();
        this.game = 1;
        this.playerChoice = null;
        this.listening = true;
        this.rpsWindow = window;
    }

    public Peer2PeerClient(Peer2PeerClient peer){
        this.localPort = peer.localPort;
        this.peerList = peer.peerList;
        this.rpsWindow = peer.rpsWindow;
        this.serverSocket = peer.serverSocket;
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
        
        System.out.println("connectToRing");
        if (peerList.isEmpty()){
            /*if the list is empty, this is the first peer in the system*/
            return 0;
        }
        
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
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.toString());
            System.exit(1);
        }
        
        /*
         * send JOIN to its first Peer
         */
        System.out.println("send JOIN");
        String toTarget = "JOIN";
        try {
            out.writeObject(toTarget);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Peer2PeerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            /*
             * receive a reponse of the first peer
             * with the list of new peers
             */
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            System.out.println(ex.toString());
            System.exit(1);
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

        /*
         * add the other peers
         */
        for (Iterator<Peer> it = ((ArrayList) newPeerList).iterator();
                it.hasNext();) {
            Peer peer = it.next();
            peerList.add(peer);
        }
        
        /*
         * update its game number
         */
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
        
        /*
         * ask other adds to add it
         */
        for(Iterator<Peer> it = ((ArrayList) newPeerList).iterator(); it.hasNext();){
            Peer peer = it.next();
            /*
             * create socket
             */
            Socket newClientSocket = null;
            try {
                newClientSocket = new Socket(peer.ipAdress, peer.port);
            } catch (IOException ex) {
                System.err.println("unable to connect "
                        + peer.ipAdress.toString()
                        + ":"
                        + peer.port);
            }

            try {
                out = new ObjectOutputStream(newClientSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println(e.toString());
                System.exit(1);
            }

            /*
             * send ADD ME (for the game number "game")
             */
            toTarget = "ADDME";

            try {
                out.writeObject(toTarget);
                out.flush();
                out.writeObject(game);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex);
            }

            /*
             * Close socket
             */
            try {
                out.close();
                newClientSocket.close();
                
            } catch (IOException ex) {
                System.err.println(ex.toString());
            }
        }      
        return 0;
    }

    /**
     * 
     * add the new peer for the current game or the next game
     * 
     * @param clientSocket
     * @param peer 
     */
    private void doAdd(ObjectInputStream in, ObjectOutputStream out, Peer peer) {

        /* 
         * Receive the game number of the new peer 
         */

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
        
        if ((Integer)gameNumber == game) {
            peerList.add(peer);
        } if ((Integer)gameNumber == (game + 1)) {
            /*
             * add the new peer for the next game
             */
            nextPeerList.add(peer);
        } else {
            System.err.println("Number game error in the connection");
            System.exit(1);
        }
        
    }
    
    /**
     * do the required job to insert peer into the p2p ring Send the actual
     * peerList to this peer, using clientSocket.
     *
     * @param clientSocket
     * @param peer
     */
    private void doJoin(ObjectInputStream in, ObjectOutputStream out, Peer peer) {

        /*
         * Send the peerList to the new host
         */
        try {
            out.writeObject(peerList);
            out.flush();
            out.writeObject(game+1);
            out.flush();
        } catch (IOException ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }
        
        /*ADD THE RECEIVED PEER TO THE LIST*/
        nextPeerList.add(peer);
        

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
    private void doPlay(ObjectInputStream in, ObjectOutputStream out){

        /* 
         * Receive the choie of the other peers
         */

        Object choice = null;
        try {
            choice = in.readObject();
        } catch (IOException ex) {
            System.err.println("error in reading. " + ex.toString());
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
        }
        
        if (! (choice instanceof Choice)) {
            System.err.println("Transmission error");
            System.exit(1);
        }
        
        /* 
         * Receive the game 
         */
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
            if ((choiceList.size() == peerList.size()) && (playerChoice != null)) {
                endOfGame();
            }      
        } else {
            nextChoiceList.add((Choice)choice);
        }
    }
    
    /**
     * 
     * Compute the game score
     * 
     */
    private void endOfGame () {
        /*
         * Compute the game score 
         */
        Integer score = 0;
        for (Iterator<Choice> it = choiceList.iterator(); it.hasNext();){
            if (win(playerChoice, it.next())) {
                score = score + 1;
            }
        }
        /*
         * Update number game
         */
        game = game + 1;
        /*
         * Upadate peer list
         */
        if (!(nextPeerList.isEmpty())){
            for (Iterator<Peer> it = nextPeerList.iterator(); it.hasNext();) {
                Peer newPeer = it.next();
                peerList.add(newPeer);
            }
            nextPeerList.clear();
        }
        /* 
         * Update choices for the next game 
         */
        choiceList = nextChoiceList;
        nextChoiceList.clear();
        playerChoice = null;
        /*
         * Update score
         */
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
        if (p1.equals(Choice.ROCK)) {
            if (p2.equals(Choice.ROCK)) {
                return false;
            }
            if (p2.equals(Choice.SCISSORS)) {
                return true;
            }
            if (p2.equals(Choice.PAPER)) {
                return false;
            }
        }
        if (p1.equals(Choice.SCISSORS))  {
            if (p2.equals(Choice.ROCK)) {
                return false;
            }
            if (p2.equals(Choice.SCISSORS)) {
                return false;
            }
            if (p2.equals(Choice.PAPER)) {
                return true;
            }            
        }
        if (p1.equals(Choice.PAPER)){
            if (p2.equals(Choice.ROCK)) {
                return true;
            }
            if (p2.equals(Choice.SCISSORS)) {
                return false;
            }
            if (p2.equals(Choice.PAPER)) {
                return false;
            }            
        } else {
            System.err.println("Choice error");
            System.exit(1);
        }            
        return false;
    }

    /**
     * Handle all incoming request on this peer
     *
     * @param clientSocket
     * @return
     */
    private Integer handleConnection(Socket clientSocket) {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }

        Object input = null;
        try {
            input =  in.readObject();
        } catch (IOException ex) {
            System.err.println("error in reading");
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
        }
        
        if (!(input instanceof String)){
            System.err.println("Transmission error");
            System.exit(1);
        }
        
        if (input.equals("JOIN")) {
            doJoin(in, out, new Peer(
                    clientSocket.getInetAddress(),
                    clientSocket.getPort()));
            System.out.println("Joined !");
        }
        if (input.equals("QUIT")) {
            doQuit(new Peer(
                    clientSocket.getInetAddress(),
                    clientSocket.getPort()));
        }
        if (input.equals("PLAY")) {
            doPlay(in, out);
        }
        if (input.equals("ADDME")) {
            doAdd(in, out ,new Peer(
                    clientSocket.getInetAddress(),
                    clientSocket.getPort()));
        }
        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            System.err.println("error while closing the input stream");
        }
        return 0;
    }

    /**
     * Sends to each peer member within peerList an "end of game" message 
     *
     * @param peerList
     * @return error code
     */
    public void disconnect() throws IOException {
        /*
         * broadcast to all peers that we are leaving
         */
        
        for (Iterator<Peer> it = peerList.iterator(); it.hasNext();) {
            Peer peer = it.next();
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
             * send bye
             */
            String toTarget = "QUIT";
            try {
                out.writeObject(toTarget);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex);
            }

            out.close();
            clientSocket.close();

        }
        
    }

    public void setChoice(Choice choice) {
        playerChoice = choice;
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
            String toTarget = "PLAY";

            try {
                out.writeObject(toTarget);
                out.flush();
                out.writeObject(choice);
                out.flush();
                out.writeObject(game);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex);
            }
            try {
                /*
                 * close streams and socket
                 */
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
            out.close();
            clientSocket.close();
        }
        return 0;
    }
    
    @Override
    public void interrupt(){
        super.interrupt();
        try {
            this.serverSocket.close();
        } catch (IOException ex) {
            System.err.println("erreur while closing");
        }
    }

    /*
     * 
     */
    @Override
    public void run() {
        this.serverSocket = null;

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


        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleConnection(clientSocket);
                clientSocket.close();
            }catch (InterruptedIOException ex){
                Thread.currentThread().interrupt();
                break;
            } catch (IOException ex) {
                if (!isInterrupted()){
                    System.err.print("Connection error on port: " + localPort);
                } else {
                    break;
                }
            }      
        }
        
    }
}
