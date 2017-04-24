/*
 * Author: 135815
 * The main class of the program which acts as a middle man between the antennas
 * and the android application which connect to it. Effectivily making the program
 * be both a gateway and a server at the same time.
 */
package modem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author D7ooM
 */
public final class Modem {

    Antenna antenna1;
    Antenna antenna2;
    Antenna antenna3;

    Packet modPacket;//the last packet recieved by antenna(s).
    int modCounter;// count the number of diffrent payload packets; accros all antennas. 
    
    MTTCPServer server;//The server which handles the connection of android clients
    int portNumber = 11000;//The server port number
    
    BufferedReader stdIn;//used to read user input to terminal.

    /* 
     * Constructor that instiantiate a single input singal output modem.
     * and a server to handle android clients..
     * @param com The communication port number which the virtaul antenna board
     * is connected to, e.g. COM5.
     */
    public Modem(String com) {
        modCounter = 0;
        modPacket = new Packet(0, 0, "");
        antenna1 = new Antenna(com);
        antenna1.initialize();
        stdIn = new BufferedReader(new InputStreamReader(System.in));

        server = new MTTCPServer(11000);

        this.runModem();
    }

    /* 
     * Constructor that instiantiate a multiple input multiple output modem And
     * a server to handle android clients
     * @param com, com1, com2 The communication port number which the virtaul antenna board
     * is connected to, e.g. COM5.
     */
    public Modem(String com0, String com1, String com2) {
        modCounter = 0;
        modPacket = new Packet(0, 0, "");
        antenna1 = new Antenna(com0);
        antenna1.initialize();

        antenna2 = new Antenna(com1);
        antenna2.initialize();

        antenna3 = new Antenna(com2);
        antenna3.initialize();
        stdIn = new BufferedReader(new InputStreamReader(System.in));

        server = new MTTCPServer(11000);
        this.runModem();
    }

    /* 
     * Constructor that instiantiate a multiple input multiple output modem And
     * a server to handle android clients
     */
    

    /* 
     * This method is used for the preformance analysis. When invoked, it prints out
     * the results and resets the field releavant to the testing.
     */
    public void reset() {
        System.out.println("Modem recieved: " + (modCounter) + " different packets");
        System.out.println("Antenna at " + antenna1.getPortName() + " recieved: " + (antenna1.getCounter() - 1) + " packets");
        antenna1.resetAntenna();
        if (antenna2 != null) {
            System.out.println("Antenna at " + antenna2.getPortName() + " recieved: " + (antenna2.getCounter() - 1) + " packets");
            antenna2.resetAntenna();
            System.out.println("Antenna at " + antenna3.getPortName() + " recieved: " + (antenna3.getCounter() - 1) + " packets");
            antenna3.resetAntenna();
        }

        this.modCounter = 0;//
    }

    /* 
     * The main method of the modem. When the program is running and after 
     * instiating an instance of this class, the modem continously checks for 
     * new packets recieved by the antenna(s). After the reception of a packet 
     * the modem sends it  to all clients running the android application 
     * developed.
     */
    public void runModem() {
        Thread t;
        t = new Thread(server);
        t.start();

        while (!Thread.currentThread().isInterrupted()) {
            if (Antenna.ispAvailable()) {
                this.UpdateModPacket();
                modCounter++;
                if ((server.client != null)) {
                    server.sendPacketToClient(this.modPacket.getPayload());
                }
            }

            try {//manual reset
                if (stdIn.ready()) {
                    if ("reset".equals(stdIn.readLine())) {
                        this.reset();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, 
                        null, ex);
            }
        }
    }
    
    /**
     * Sets the modem packet as the static packet of the antenna class.
     */
    public void UpdateModPacket() {
        modPacket = Antenna.getLastPacket();
    }
    
    public Packet getModPacket() {
        return modPacket;
    }

    /*
     * This method serializes a string received from the client running the
     * android application to the antenna (NOT SUPPORTED YET).
     *
     * @param message The message to send to the antenna
     */
    public void serilizeToAntenna(String message) throws UnsupportedEncodingException, IOException {
        System.out.println("Modem got " + message);
        this.antenna1.send(message);
        if (antenna2 != null) {
            this.antenna2.send(message);
        }
        if (antenna3 != null) {
            this.antenna3.send(message);
        }
    }

    /*
     *Depending on the arguments proveded, The modem is either running as SISO 
     *or MIMO.
     */
    public static void main(String[] args) throws Exception {

        final BufferedReader stdIn;//used to read user input to terminal.
        stdIn = new BufferedReader(new InputStreamReader(System.in));

        if (args.length == 1) {
            Modem modem = new Modem(args[0]);
        } else if (args.length == 3) {
            Modem modem = new Modem(args[0], args[1], args[2]);
        }
    }
    //the thread below handles the user input.
       /* Thread t=new Thread() {
     @Override
     public void run() {
     /*using stdin, the below code reads what the user inputed,
     then converts the string in to byte[] and pass it as 
     a parameter for the serial.send method.
                            
     String buffer;
     while(true){
     try {
     if(stdIn.ready()){
     buffer =  stdIn.readLine();
     buffer = buffer +"\r";
     modem.getAntenna1().send(buffer.getBytes("UTF-8"));                                        
     }else{}
     } catch (IOException ex) {
     Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, null, ex);
     }
     }
     }
     };
     t.start(); */
}
