import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    public static void main(String[] args) throws Exception {
        boolean running = true;

        //Hashtables
        Hashtable<Integer, String>  pool = new Hashtable<>();
        Hashtable<String, Hashtable<Integer, String>> records = new Hashtable<String, Hashtable<Integer, String>>();

        //Client request
        String clientRequest;

        //For UDP connections
        byte[] buffer;
        byte[] responseBuf = new byte[2048];
        InetAddress address;
        int port;

        //Response from UDP connection
        String response;


        //Setting up UDP socket and user input
        DatagramPacket packet;
        DatagramSocket sock;

        Scanner in = new Scanner(System.in);



        while(running) {
             sock = new DatagramSocket(20270, InetAddress.getLocalHost());
            //Possible actions for client
            clientRequest = in.nextLine();
            if (clientRequest.equals("init")) { // INIT command, makes new udp connection and waits for a response on same socket
                buffer = clientRequest.getBytes();
                packet = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), 100);
                sock.send(packet);

                //Waiting for response
                packet = new DatagramPacket(responseBuf, responseBuf.length);
                sock.receive(packet);
                response = new String(packet.getData(), 0, packet.getLength());
                sock.close(); //Closing socket

                //Splitting response from server pool and adding to pool hashtable
                String[] temp = response.split(" ", 4);
                for (int i = 0; i < 4; i++) {
                    pool.put(i + 1, temp[i]);
                    System.out.println(pool.get(i + 1));
                }
            } else if(clientRequest.equals("inform")) {
                System.out.println("Please enter the file for which to upload: ");
                String fileName = in.nextLine();

                //Hashing filename
                int key = fileName.hashCode();
                key = (key % 4) + 1;

                //Extracting info for transmission
                String[] info = pool.get(key).split("#", 2);
                String[] ip = info[0].split("/", 2);

                //Sending inform message
                buffer = clientRequest.getBytes();
                packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip[1]), Integer.parseInt(info[1]));
                sock.send(packet);

                //Sending filename
                byte[] tempBuf = (fileName + ":" + InetAddress.getLocalHost().toString()).getBytes();
                packet = new DatagramPacket(tempBuf, tempBuf.length, InetAddress.getByName(ip[1]), Integer.parseInt(info[1]));
                sock.send(packet);

                sock.close(); //Closing socket

            }else if(clientRequest.equals("query")){


            }else if (clientRequest.equals("exit")){
                running = false;
            }

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
