import java.io.*;
import java.net.*;
import java.util.*;

public class directoryServ {

    public static void main(String[] args) throws Exception{
        Scanner in = new Scanner(System.in);
        System.out.println("Please enter the Directory Server Pool ID: ");
        String servID = in.nextLine();


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
    public String data;
    String id;
    String response;
    boolean running;

    ClientConnection() throws Exception {
        sock = new DatagramSocket(20270);
    }

    public void run() {
        running = true;
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                sock.receive(packet);

                data = new String(packet.getData(), 0, packet.getLength()); //Received Data
                InetAddress IPAddress = packet.getAddress(); //Getting client ip and socket
                int port = packet.getPort();
                packet = new DatagramPacket(sendData, sendData.length, IPAddress, port); //Making response
                sock.send(packet); //Sending Response


                if (data.equals("init")) {
                    System.out.println("Hello");
                    init(IPAddress);
                }

            }  catch(Exception e){
                System.out.println(e);
            }
        }
    }

    private void init(InetAddress ip) throws Exception {
        ServerSocket sock = new ServerSocket(20271);
        Socket client = new Socket("localhost", sock.getLocalPort());

        DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
        BufferedReader inFromServ = new BufferedReader(new InputStreamReader(client.getInputStream()));

        outToServer.writeBytes("init" + '\n' + ip);

        //String modSentence = inFromServ.readLine();

        System.out.println("Trying to connect to dirServer 2");
        client.close();
    }
}





//TCP
class PoolConnection extends Thread {
    String data;
    ServerSocket sock;
    String id;

    PoolConnection() throws Exception{
        sock = new ServerSocket(20269);
    }

    public void run(){

        while(true){
            try{
                Socket connectionSocket = sock.accept();

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                data = inFromClient.readLine();
                outToClient.writeBytes("Received: " + data);
                System.out.println("Got here TCP");

                if(data == "init"){
                    data = inFromClient.readLine();
                    //init(data, connectionSocket.getInetAddress().toString());
                    System.out.println(data);
                }

            } catch(Exception e){
                //System.out.println(e);
            }
        }
    }

    private String init(String ip, String ipList) throws Exception {
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
