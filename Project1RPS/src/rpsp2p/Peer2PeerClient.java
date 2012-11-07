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
        
        /*ENVOYER LE JOIN AUX AUTRES NODES*/

        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException ex) {
            System.err.println(ex.toString());
        }

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
    private void doPlay(Peer peer) {
        throw new UnsupportedOperationException("Not yet implemented");
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
            doPlay(new Peer(
                    clientSocket.getInetAddress(),
                    clientSocket.getPort()));
        }
        try {
            in.close();
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

            BufferedOutputStream out = null;

            try {
                out = new BufferedOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println(e.toString());
                System.exit(1);
            }

            byte[] toTarget = "QUIT".getBytes();
            try {
                out.write(toTarget, 0, toTarget.length);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex);
            }

            out.close();
            clientSocket.close();

        }
        
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
    
    @Override
    public void interrupt(){
        super.interrupt();
        try {
            this.serverSocket.close();
        } catch (IOException ex) {
            System.err.println("erreur while closing");
        }
    }

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
