/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpsp2p;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 *
 * @author fingolfin
 */
public class Peer implements Serializable{
    InetAddress ipAdress;
    Integer port;

    public Peer(InetAddress ipAdress, Integer localPort) {
        this.ipAdress = ipAdress;
        this.port = localPort;
    }
}