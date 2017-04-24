/*
*adapted from the University of Sussex Web Application and Services module(lab1).
* A server thread which handles the communication between A client and this program.
*/
package modem;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MTTCPServerThread extends Thread {

    private Socket slaveSocket = null;
    String line;
    PrintWriter socketOutput;
    BufferedReader socketInput;

    
    public MTTCPServerThread(Socket socket) {
        super("MTTCPServerThread");
        this.slaveSocket = socket;
        
        
        try {            
            socketOutput = new PrintWriter(slaveSocket.getOutputStream(), true);
            socketInput = new BufferedReader(new InputStreamReader(slaveSocket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(MTTCPServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendLine(String s){
        socketOutput.println(s);
    }
    
   
}
