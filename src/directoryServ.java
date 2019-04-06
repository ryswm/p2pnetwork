import com.sun.istack.internal.NotNull;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.*;

public class directoryServ {

    public static void main(String argv[]) throws Exception {
        int servID = 1;


        Hashtable<String, String> table = new Hashtable<String, String>(); //File Name, IP Address



        //UDP Socket
        ClientConnection cli = new ClientConnection();
        cli.id = servID;
        cli.start();

        //TCP Socket
        PoolConnection dir = new PoolConnection();
        dir.id = servID;
        dir.start();
    }
}

//Threads

//UDP
class ClientConnection extends Thread {
    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];
    DatagramSocket sock;
    String data;
    int id;
    String response;


    public void ClientConnection() throws Exception {
        sock = new DatagramSocket(20270);
    }

    public void run() {
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                sock.receive(packet);
                data = new String(packet.getData()); //Received Data
                InetAddress IPAddress = packet.getAddress(); //Getting client ip and socket
                int port = packet.getPort();
                packet = new DatagramPacket(sendData, sendData.length, IPAddress, port); //Making response
                sock.send(packet); //Sending Response

                if(data == "init"){
                    init(IPAddress);
                }
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }

    private void init(InetAddress ip) throws Exception {
        ServerSocket sock = new ServerSocket(20271);
        Socket client = new Socket("localhost", sock.getLocalPort());

        DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
        BufferedReader inFromServ = new BufferedReader(new InputStreamReader(client.getInputStream()));

        outToServer.writeBytes("init" + '\n' + ip);

        String modSentence = inFromServ.readLine();

        client.close();
    }
}

//TCP
class PoolConnection extends Thread {
    String data;
    ServerSocket sock;
    int id;

    public void PoolConnection(int id) throws Exception{
        sock = new ServerSocket(20270);
    }

    public void run(){
        try{
            while(true){
                Socket connectionSocket = sock.accept();

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                data = inFromClient.readLine();
                outToClient.writeBytes("Received: " + data);

                if(data == "init"){
                    data = inFromClient.readLine();
                    init(data);
                }
            }
        } catch(Exception e){

        }
    }

    private String init(String ip) throws Exception {
        ServerSocket sock = new ServerSocket(20271);
        Socket client = new Socket("localhost", sock.getLocalPort());

        DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
        BufferedReader inFromServ = new BufferedReader(new InputStreamReader(client.getInputStream()));

        outToServer.writeBytes("init" + '\n');

        String modSentence = inFromServ.readLine();
        client.close();

        return modSentence;
    }

}
