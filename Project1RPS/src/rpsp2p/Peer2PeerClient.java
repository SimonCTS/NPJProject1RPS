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
import java.util.logging.Level;
import java.util.logging.Logger;
import project1rps.Choice;

/**
 *
 * @author Simon Cathebras, Zoe Bellot
 */
public class Peer2PeerClient extends Thread {

    private String localPlayerName;
    private Integer localPort;
    private ArrayList<Peer> peerList;
    private Boolean listening;

    /**
     *
     * @param playerName
     * @param remoteIpAdress
     * @param remotePort
     * @param port
     */
    public Peer2PeerClient(String playerName,
            InetAddress remoteIpAdress,
            Integer remotePort,
            Integer port) {
        this.localPlayerName = playerName;
        this.localPort = port;
        this.peerList = new ArrayList<Peer>();
        peerList.add(new Peer(remoteIpAdress, remotePort));
        this.listening = true;
    }

    /**
     *
     * @param peerList
     * @return
     */
    public Integer connectToRing() throws IOException {
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
        out.write(toTarget, 0, toTarget.length);
        out.flush();

        Object newPeerList;
        try {
            newPeerList = in.readObject();
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
            return 1;
        } catch (OptionalDataException ode) {
            System.out.println(ode.toString());
            return 1;
        }

        Integer nextRound;
        try {
            nextRound = in.readInt();
        } catch (OptionalDataException ode) {
            System.out.println(ode.toString());
            return 1;
        }
        return 0;
    }

    /**
     *
     * @param clientSocket
     * @return
     */
    private Integer handleConnection(Socket clientSocket) {
        return 0;
    }

    /**
     *
     * @param peerList
     * @return error code
     *
     * Sends to each peer member within peerList an "end of game" message. Also
     * sets the listening boolean to false
     */
    public Integer disconnect() {
        return 0;
    }

    /**
     *
     * @param choice
     * @return Sends to all peers the choice of the local user
     */
    public Integer sendToPeers(Choice choice) {
        return 0;
    }

    public void run() {
        ServerSocket serverSocket = null;

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
