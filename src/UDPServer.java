import java.io.*;
import java.net.*;

public class UDPServer {

    public static void main(String argv[]) throws Exception {
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        DatagramSocket FirstContactSocket = new DatagramSocket(6789);

        while(true){
        	DatagramPacket receivePacket = new DatagramPacket (receiveData,receiveData.length);
            FirstContactSocket.receive(receivePacket);

            String sentence = new String(receivePacket.getData());
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
            
            FirstContactSocket.send(sendPacket);
        }
    }
}