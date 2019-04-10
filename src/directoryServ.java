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
        System.out.println("Setup complete, server #" + servID + " is in standby and awaiting connection");
        System.out.println("-------------------------------------------------------------------------------");




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
    String id; //Server ID
    int port; //Server port
    String nextIP; //next IP in pool
    int nextPort; //Next port in pool

    private byte[] bData = new byte[1024];

    private DatagramSocket sock;
    String data; //received data

    //Hashtable for this directory server
    //File Name, IP Address
    Hashtable<String, String> table = new Hashtable<String, String>();

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
                    init(IPAddress, port);
                }else if(data.equals("inform")){
                    sock.receive(packet);
                    data = new String(packet.getData(),0,packet.getLength());
                    addRecord(data);
                } else if(data.equals("query")){
                    sock.receive(packet);
                    data = new String(packet.getData(),0,packet.getLength());
                    query(data, IPAddress, port);
                }

            }  catch(Exception e){
                System.out.println("Exception UDP Server");
            }
        }
    }

    //Handles "init" command for server in pool with ID = 1, transforms UDP connection with client into TCP connection with dirServer pool
    private void init(InetAddress ip, int port) throws Exception {
        Socket connection = new Socket(nextIP, nextPort);

        DataOutputStream outToServer = new DataOutputStream(connection.getOutputStream());
        outToServer.writeBytes("init" + '\n' + ip.toString() + ':' + port + '\n' + sock.getInetAddress() + "#" + this.port); //init\n clientIP:clientPort\n dirServIP#dirServPort

        outToServer.close(); //Closing outward stream
        connection.close(); //Closing connection

        System.out.println("Trying to connect to 2nd dirServer in pool");
    }

    //Adds new file and location record to this server, initiated by receiving "inform" command
    private void addRecord(String data){
        //Extracting record
        String[] info = data.split(":", 2); //Stored as contentName:/IP
        String[] ip = info[1].split("/", 2); //Taking / off IP
        System.out.println("Split string");

        if(table.containsKey(info[0])){
            String ipList = table.get(info[0]);
            ipList += " " + ip[1];
        }

        //Adding to hashtable and displaying to server console
        table.put(info[0], ip[1]);
        System.out.println("Added new record to server: <" + info[0] + ", " + ip[1] + ">");
    }

    //Query Function to respond upon file query, initiated by query command from client
    private void query(String data, InetAddress ip, int port) throws Exception {
        byte[] msg;
        DatagramPacket queryMsg;
        if(table.containsKey(data)){
            msg = table.get(data).getBytes();
            queryMsg = new DatagramPacket(msg, msg.length, ip, port);
            sock.send(queryMsg);
        }else{
            msg = "404".getBytes();
            queryMsg = new DatagramPacket(msg, msg.length, ip, port);
            sock.send(queryMsg);
        }
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

                if(data.equals("init") && id.equals("1")) { //Server 1 only; Sends over UDP to client
                    data = inFromClient.readLine();
                    clientInit(data, inFromClient.readLine());
                } else if(data.equals("init")){ //Continues sending through pool over TCP
                    data = inFromClient.readLine();
                    init(data, inFromClient.readLine() + ' ' + connectionSocket.getInetAddress().toString() + '#' + connectionSocket.getLocalPort());
                }

                inFromClient.close();
            } catch(Exception e){
                System.out.println("Exception TCP Server");
            }
        }
    }

    //Init function for dirServers 2-4
    private void init(String ip, String ipList) throws Exception {
        Socket dest = new Socket(nextIP, nextPort);

        DataOutputStream outToServer = new DataOutputStream(dest.getOutputStream());
        outToServer.writeBytes("init" + '\n' + ip + '\n' + ipList);

        outToServer.close();
        dest.close();

        System.out.println("Sent through circle");
    }

    //Sends init response of entire dirServer pool back to client
    private void clientInit(String ip, String ipList) throws Exception{
        String[] info = ip.split(":", 2); //Splitting IP and port of client response will be sent to
        String[] fix = info[0].split("/", 2); //Taking / character off IP to convert to InetAddress
        InetAddress add = InetAddress.getByName(fix[1]); //Converting string IP to InetAddress

        byte[] bData = ipList.getBytes(); //Converting string of IPs to bytes for transmission


        //Sending packet back to client
        DatagramSocket temp = new DatagramSocket(9999);
        DatagramPacket pack = new DatagramPacket(bData, bData.length, add, Integer.parseInt(info[1])); //Making response
        temp.send(pack); //Sending Response


        temp.close(); //Closing temp UDP socket
        System.out.println("Got to end, trying to send back to client");
    }

}
