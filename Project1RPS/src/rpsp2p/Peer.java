/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpsp2p;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 *
 * @author fingolfin
 */
public class Peer {
    InetAddress ipAdress;
    Integer Port;

    public Peer(InetAddress ipAdress, Integer localPort) {
        this.ipAdress = ipAdress;
        this.Port = localPort;
    }
}
