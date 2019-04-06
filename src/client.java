import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    public static void main(String[] args){
        Hashtable<Integer, String>  pool = new Hashtable<>();
        Hashtable<String, Hashtable<Integer, String>> records = new Hashtable<String, Hashtable<Integer, String>>();
        pool.put(new Integer(1),"localhost");

        Scanner in = new Scanner(System.in);
        Server node = new Server();
        

    }
}

class DirConnection extends Thread {
    DatagramSocket sock;
     String msg;
    String response;
    private byte[] buffer;
    InetAddress address;


    public void DirConnection() throws Exception {
        sock = new DatagramSocket();
        address = InetAddress.getLocalHost();
    }

    public void run(){
        try {
            //Send request
            buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, sock.getLocalPort());
            sock.send(packet);

            packet = new DatagramPacket(buffer, buffer.length);
            sock.receive(packet);
            response = new String(packet.getData(), 0, packet.getLength());

        } catch (Exception e){
            System.out.println(e);
        }

    }
}

class Server extends Thread {
    ServerSocket sock;
    String data;

    public void Server(int id) throws Exception{
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
