

import java.io.*;
import java.net.*;
public class UDPClient {

    public static void main(String argv[]) throws Exception {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        DatagramSocket clientSocket = new DatagramSocket();

        InetAddress IPAddress = InetAddress.getByName("141.117.117.48");

        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        String sentence = inFromUser.readLine();

        sendData = sentence.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,20270);

        clientSocket.send(sendPacket);

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        clientSocket.receive(receivePacket);

        String modS = new String(receivePacket.getData());

        System.out.println("FROM SERVER:" + modS);
        clientSocket.close();
    }


}
