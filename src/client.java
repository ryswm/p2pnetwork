import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    public static void main(String[] args) throws Exception {
        Hashtable<Integer, String>  pool = new Hashtable<>();
        pool.put(new Integer(1),"10.17.179.115");
        Hashtable<String, Hashtable<Integer, String>> records = new Hashtable<String, Hashtable<Integer, String>>();
        String clientRequest;
        Server node = new Server();
        DirConnection connect = new DirConnection();

        Scanner in = new Scanner(System.in);



        clientRequest = in.nextLine();
        if(clientRequest == "init"){
            connect.msg = "init";
            connect.start();

        }


    }
}

class DirConnection extends Thread {
    DatagramSocket sock;
     String msg;
    String response;
    private byte[] buffer;
    InetAddress address;


    DirConnection() throws Exception {
        sock = new DatagramSocket(20270);
        address = InetAddress.getByName("10.17.179.115");
    }

    public void run(){
        try {
            //Send request
            System.out.println(msg);
            buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, sock.getLocalPort());
            sock.send(packet);

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
        sock = new ServerSocket(20270);
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
