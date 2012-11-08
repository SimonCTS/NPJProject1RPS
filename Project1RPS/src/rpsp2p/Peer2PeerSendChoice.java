/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpsp2p;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import project1rps.Choice;
import rpsgui.Window;

/**
 * Threaded class, used to send a choice to all other peers in the ring.
 * @author Zoé Bellot, Simon Cathébras
 */
public class Peer2PeerSendChoice extends Peer2PeerClient {
    private Choice choice;

    /**
     * Creates a P2PSendChoice.
     * 
     * @param peer Peer to copy.
     * @param choice Choice to send.
     */
    public Peer2PeerSendChoice(Peer2PeerClient peer, Choice choice) {
        super(peer);
        this.choice = choice;
    }
    
    public void run(){
        try {
            this.sendToPeers(this.choice);
        } catch (IOException ex) {
            Logger.getLogger(Peer2PeerSendChoice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
