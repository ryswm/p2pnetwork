import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    public static void main(String[] args) throws Exception {
        Hashtable<Integer, String>  pool = new Hashtable<>();
        Hashtable<String, Hashtable<Integer, String>> records = new Hashtable<String, Hashtable<Integer, String>>();
        String clientRequest;

        //Server node = new Server(); //Socket to wait for connection from other client
        DirConnection connect = new DirConnection(); //Socket to make connections with dirServer pool on request

        Scanner in = new Scanner(System.in);
        //System.out.println("Please enter the IP of the first dirServer in pool: ");
        //String dest = in.nextLine();
        //pool.put(new Integer(1),dest);


        clientRequest = in.nextLine();
        if(clientRequest.equals("init")){
            connect.msg = "init";
            connect.start();
            if(!connect.response.equals(null)){
                connect.sock.close();
            }

        }

    }
}

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
