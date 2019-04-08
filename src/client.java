import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    public static void main(String[] args) throws Exception {
        //Hashtables
        Hashtable<Integer, String>  pool = new Hashtable<>();
        Hashtable<String, Hashtable<Integer, String>> records = new Hashtable<String, Hashtable<Integer, String>>();

        //Client request
        String clientRequest;

        //For UDP connections
        byte[] buffer;
        InetAddress address;
        int port;

        //Response from UDP connection
        String response;


        //Setting up UDP socket and user input
        DatagramSocket sock = new DatagramSocket(20270, InetAddress.getLocalHost());
        DatagramPacket packet;
        Scanner in = new Scanner(System.in);


        //Possible actions for client
        clientRequest = in.nextLine();
        if(clientRequest.equals("init")){ // INIT command, makes new udp connection and waits for a response on same socket
            buffer = clientRequest.getBytes();
            packet = new DatagramPacket(buffer, buffer.length,InetAddress.getLocalHost(),100);
            sock.send(packet);

            packet = new DatagramPacket(buffer, buffer.length);
            sock.receive(packet);
            response = new String(packet.getData(), 0, packet.getLength());
            //System.out.println(response);
        }



    }
}

//Kept if needed but not used
class DirConnection extends Thread {
    DatagramSocket sock;
     String msg;
    String response;
    private byte[] buffer;
    InetAddress address;
    int port;


    DirConnection() throws Exception {
        sock = new DatagramSocket();
        address = InetAddress.getLocalHost();
        port = 100;
    }

    public void run(){
        try {
            //Send request
            buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            sock.send(packet);
            System.out.println("Packet Sent");

            packet = new DatagramPacket(buffer, buffer.length);
            sock.receive(packet);
            response = new String(packet.getData(), 0, packet.getLength());
            sock.close();



        } catch (Exception e){
            System.out.println(e);
        }

    }
}

//For acting as server, not used yet
class Server extends Thread {
    ServerSocket sock;
    String data;

    Server() throws Exception{
        sock = new ServerSocket(20275);
    }

    public void run(){
        while(true){
            try{
                Socket connectionSocket = sock.accept();

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                data = inFromClient.readLine();
                outToClient.writeBytes("Received: " + data);

            } catch(Exception e){
                //System.out.println(e);
            }
        }
    }
}
