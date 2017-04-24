/*
*The code in this class was adapted from:
*ref:http://playground.arduino.cc/Interfacing/Java
*This class enables the program to read from the usb port of a computer using
*a custom library from http://rxtx.qbang.org/
*The major addition from the original program is the parser and the shared memory
*i.e. the static packet.
*/
package modem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Antenna implements SerialPortEventListener {

    SerialPort serialPort;
    /**
     * The port we're normally going to use.
     */
    private final String portName;
    /* 
     * Shared packet by all antennas
    */
    static Packet lastPacket = new Packet(0, 0, "");
    /*
     *counts number of packets recieved by one antenna. Used for testing purpases
    */
    int counter;
    /*
     *used just in case an antenna did not recieve the end packet.
    */
    static boolean done; 
    /**
     * A BufferedReader which will be fed by a InputStreamReader converting the
     * bytes into characters making the displayed results codepage independent
     */
    private BufferedReader input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 0;
    /**
     * Default bits per second for COM port.
     */
    private static final int DATA_RATE = 9600;

    //Used as means to check for the availablity of new packets by the modem.
    private static boolean pAvailable;
    
    /**
     * @param comName The port of the antenna Nucleo board
     */
    public Antenna(String comName) {
        portName = comName;
        Antenna.pAvailable = false;
        counter = 0;
    }

    public void initialize() {

        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();

            if (currPortId.getName().equals(portName)) {
                portId = currPortId;
                System.out.println("Antenna at " + portName + " is running...");
                break;
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);
            serialPort.disableReceiveTimeout();
            serialPort.enableReceiveThreshold(1);
            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (PortInUseException | UnsupportedCommOperationException | IOException | TooManyListenersException e) {
            System.err.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port. This will prevent
     * port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }
    
    /**
     * This method serializes a string to the board. 
     * It is not used in the current implementation
     * @param s 
     */
    public void send(String s) {
        try {
            System.out.println(s);
            String buffer = s;
            buffer = buffer + "\r";
            output.write(buffer.getBytes("UTF-8"));
            //Debug
        } catch (IOException ex) {
            Logger.getLogger(Antenna.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     * @param oEvent
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine;
                while ((inputLine = input.readLine()) != null) {
                    System.out.println(inputLine);
                    String rssi = "";
                    boolean rssiDone = false;
                    String snr = "";
                    boolean snrDone = false;
                    String payload = "";

                    //input parser of the form (rssi,snr,payload.)
                    for (int i = 0; i < inputLine.length(); i++) {

                        char c = inputLine.charAt(i);
                        if (!rssiDone) {
                            if (c == ',') {
                                rssiDone = true;
                            } else {
                                rssi = rssi + c;
                            }
                        } else if (rssiDone & !snrDone) {
                            if (c == ',') {
                                snrDone = true;
                            } else {
                                snr = snr + c;
                            }
                        } else {
                            if (c != '.') {
                                payload = payload + c;
                            }
                        }
                    }
                    Packet Buff = new Packet(Integer.parseInt(rssi), 
                            Integer.parseInt(snr), payload);
                    counter++;
                    if ((Buff.getTime()) > (lastPacket.getTime() + 100) || 
                            !(Buff.getPayload().equals(lastPacket.getPayload()))) {
                        lastPacket = Buff;
                        Antenna.pAvailable = true;
                    }
                }
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public static Packet getLastPacket() {
        Antenna.pRead();
        //System.out.println("(" + Antenna.lastPacket.getRSSI() + "/" + Antenna.lastPacket.getSNR() + "/" + Antenna.lastPacket.payload + ")");
        return lastPacket;

    }

    public static boolean ispAvailable() {
        return pAvailable;
    }

    public static void pRead() {
        Antenna.pAvailable = false;
    }

    public int getCounter() {
        return counter;
    }

    public static boolean isDone() {
        return done;
    }

    public String getPortName() {
        return portName;
    }

    public void resetAntenna() {
        counter = 0;
    }
}
