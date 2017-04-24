/* adapted from the University of Sussex Web Application and Services module(lab1).
* The class represents a TCP server that spawns server threads to handle clients
* connected to it.
*/
package modem;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MTTCPServer implements Runnable{
    int portNumber;
    ServerSocket masterSocket;
    Socket slaveSocket;
    MTTCPServerThread client;//TO-DO arrayList of clients
    
    public MTTCPServer(int port){
        this.portNumber = port;
    }
    
    @Override
    public void run() {
    

        try {
           
            masterSocket = new ServerSocket(portNumber);
            
            System.out.println("Server Started...");
            
            // the following will run forever (until interrupted by stopping the application through Netbeans)
            while (true) {                               
                slaveSocket = masterSocket.accept();                              
                System.out.println("Accepted TCP connection from: " + slaveSocket.getInetAddress() + ", " + slaveSocket.getPort() + "...");              
                client = new MTTCPServerThread(slaveSocket);              
                client.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(MTTCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void sendPacketToClient(String s) {
      client.interrupt();
      client.sendLine(s);
    }
    
   
}
