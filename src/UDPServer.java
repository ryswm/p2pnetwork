import java.io.*;
import java.net.*;

public class UDPServer {

    public static void main(String argv[]) throws Exception {
        String receiveData;
        String capSentence;

        DatagramSocket FirstContactSocket = new DatagramSocket(6789);

        while(true){
        	DatagramPacket = new DatagramPacket (reveiveData,receiveData.);
            FirstContactSocket.receive(receivePacket);

            String sentence = new String(receivePacket.getData());
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
            
            serverSocket.send(sendPacket);
        }
    }
}