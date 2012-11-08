/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpsp2p;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rpsgui.Window;

/**
 * Threaded class, used to send disconnection message to all other peers in the ring.
 * @author Zoé Bellot, Simon Cathébras
 */
public class Peer2PeerSendDisconnect extends Peer2PeerClient{

    /**
     * Create a P2PDisconnect object.
     * @param peer 
     */
    public Peer2PeerSendDisconnect(Peer2PeerClient peer) {
        super(peer);
    }
    
    public void run(){
        try {
            this.disconnect();
        } catch (IOException ex) {
            Logger.getLogger(Peer2PeerSendDisconnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
