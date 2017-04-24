/*
 * Author: 135815
 * class representing a packet, which will be recieved by the antennas and sent
 * to connected client, if any are connected.
 */
package modem;

/**
 * Class to model a packet
 */
public class Packet {

    int SNR;
    int RSSI;
    String payload;
    long time;

    public Packet(int RSSI, int SNR, String payload) {
        this.RSSI = RSSI;
        this.SNR = SNR;
        this.payload = payload;
        time = System.currentTimeMillis();
    }

    public int getSNR() {
        return SNR;
    }

    public int getRSSI() {
        return RSSI;
    }

    public String getPayload() {
        return payload;
    }

    public long getTime() {
        return time;
    }

}
