/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpsp2p;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Class recording the element Peer. A peer includes an Ip Adress and a Port.
 * 
 * @author Zoé Bellot, Simon Cathébras
 */
public class Peer implements Serializable {

    InetAddress ipAdress;
    Integer port;

    public Peer(InetAddress ipAdress, Integer localPort) {
        this.ipAdress = ipAdress;
        this.port = localPort;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Peer) {
            return ((this.ipAdress.equals(((Peer) o).ipAdress)) && (this.port.equals(((Peer) o).port)));
        }
        return false;
    }


}
