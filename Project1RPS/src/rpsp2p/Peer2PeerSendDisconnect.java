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
 *
 * @author fingolfin
 */
public class Peer2PeerSendDisconnect extends Peer2PeerClient{

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
