import java.io.*;
import java.net.*;
import java.util.*;

public class directoryServ {

    public static void main(String[] args) throws Exception{
        //Server Setup
        Scanner in = new Scanner(System.in);
        System.out.println("Please enter the Directory Server Pool ID: ");
        String servID = in.nextLine();
        System.out.println("Please enter the desired port number for this server: ");
        String setPort = in.nextLine();
        System.out.println("Please enter the IP of the next server in pool (localhost for same device): ");
        String nextIP = in.nextLine();
        System.out.println("Please enter the desired port number for the next dirServer in pool: ");
        String nextPort = in.nextLine();


        //Hashtable for this directory server
        Hashtable<String, String> table = new Hashtable<String, String>(); //File Name, IP Address

        //UDP Socket
        ClientConnection cli = new ClientConnection(Integer.parseInt(setPort));
        cli.id = servID;
        cli.nextIP = nextIP;
        cli.nextPort = Integer.parseInt(nextPort);
        cli.start();

        //TCP Socket
        PoolConnection dir = new PoolConnection(Integer.parseInt(setPort));
        dir.id = servID;
        dir.nextIP = nextIP;
        dir.nextPort = Integer.parseInt(nextPort);
        dir.start();
    }
}

//Threads

//UDP
class ClientConnection extends Thread {
    String id;
    int port;
    String nextIP;
    int nextPort;

    private byte[] bData = new byte[1024];

    private DatagramSocket sock;
    String data;


    boolean running;

    ClientConnection(int port) throws Exception {
        sock = new DatagramSocket(port);
        this.port = port;
    }

    public void run() {
        running = true;
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(bData, bData.length);
                sock.receive(packet);

                data = new String(packet.getData(), 0, packet.getLength()); //Received Data
                InetAddress IPAddress = packet.getAddress(); //Getting client ip and socket
                int port = packet.getPort();

                if (data.equals("init")) {
                    System.out.println("Hello");
                    init(IPAddress, port, IPAddress);
                }

            }  catch(Exception e){
                System.out.println("Exception UDP Server");
            }
        }
    }

    private void init(InetAddress ip, int port, InetAddress first) throws Exception {
        Socket client = new Socket(nextIP, nextPort);

        DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
        outToServer.writeBytes("init" + '\n' + ip.toString() + ':' + port + '\n' + first);

        outToServer.close();
        client.close();

        System.out.println("Trying to connect to 2nd dirServer in pool");
    }
}





//TCP
class PoolConnection extends Thread {
    String data; //Message received from other server

    ServerSocket sock; //Socket for communication
    String id; //ID of server
    int port;
    String nextIP; //IP of next server
    int nextPort;

    boolean running;

    PoolConnection(int port) throws Exception{
        sock = new ServerSocket(port);
        this.port = port;
    }

    public void run(){
        running = true;
        while(running){
            try{
                Socket connectionSocket = sock.accept();

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                data = inFromClient.readLine(); //Request from other dirServer in pool
                if(data.equals("init") && id.equals("1")) {
                    data = inFromClient.readLine();
                    clientInit(data, inFromClient.readLine());
                } else if(data.equals("init")){
                    data = inFromClient.readLine();
                    init(data, inFromClient.readLine() + ' ' + connectionSocket.getInetAddress().toString());
                }

                inFromClient.close();
            } catch(Exception e){
                System.out.println("Exception TCP Server");
            }
        }
    }

    private void init(String ip, String ipList) throws Exception {
        Socket dest = new Socket(nextIP, nextPort);

        DataOutputStream outToServer = new DataOutputStream(dest.getOutputStream());
        outToServer.writeBytes("init" + '\n' + ip + '\n' + ipList);

        outToServer.close();
        dest.close();

        System.out.println("Sent through circle");
    }

    private void clientInit(String ip, String ipList) throws Exception{
        String[] info = ip.split(":", 2);
        String[] fix = info[0].split("/", 2);

        byte[] bData = ipList.getBytes();
        InetAddress add = InetAddress.getByName(fix[1]);

        DatagramSocket temp = new DatagramSocket(9999);
        DatagramPacket pack = new DatagramPacket(bData, bData.length, add, Integer.parseInt(info[1])); //Making response
        temp.send(pack); //Sending Response


        temp.close(); //Closing temp UDP socket
        System.out.println("Got to end, trying to send back to client");
    }

}
