//Author: Ryan Woodworth : 500752821
//Client Program
import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    public static void main(String[] args) throws Exception {
        boolean running = true;

        //Hashtables
        Hashtable<Integer, String> pool = new Hashtable<>();


        //Client request
        String clientRequest;

        //For UDP connections
        byte[] buffer = new byte[2048];
        byte[] responseBuf = new byte[2048];
        byte[] tempBuf = new byte[1000000];
        InetAddress address;
        int port;

        String[] info;
        String[] ip;

        //Response from UDP connection
        String response;

        String fileName;        //Filename being added or requested
        int key; //key of hashed filename
        String downloadAdd;

        //Setting up UDP socket
        DatagramPacket packet;
        DatagramSocket sock;

        Scanner in = new Scanner(System.in);
        System.out.println("Please enter the port that will be used by this network client: ");
        port = Integer.parseInt(in.nextLine());
        System.out.println("Please enter the port that will be used by the first dirServ: ");
        int dirPort = Integer.parseInt(in.nextLine());
        System.out.println("Please enter the ip that will be used by the first dirServ: ");
        InetAddress dirIP = InetAddress.getByName(in.nextLine());


        //Starting TCP and server functions of client
        TCP client;
        Server serv = new Server(port);
        serv.start();

        sock = new DatagramSocket(port, InetAddress.getLocalHost());

        System.out.println("Please first type init");
        //Possible actions for client
        clientRequest = in.nextLine();
        if (clientRequest.equals("init")) { // INIT command, makes new udp connection and waits for a response on same socket
            buffer = clientRequest.getBytes();
            packet = new DatagramPacket(buffer, buffer.length, dirIP, dirPort);
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
        }
        else{
            running = false;
        }

        while (running) {
            sock = new DatagramSocket(port, InetAddress.getLocalHost());

            System.out.println("Type inform to upload, query to download, or exit to stop program: ");
            //Possible actions for client
            clientRequest = in.nextLine();

            if (clientRequest.equals("inform")) {
                System.out.println("Please enter the file for which to upload: ");
                fileName = in.nextLine();
                inform(fileName,sock, pool, serv);
                sock.close(); //Closing socket

            } else if (clientRequest.equals("query")) {
                System.out.println("Please enter the file for which to download: ");
                fileName = in.nextLine();

                //Hashing filename
                key = fileName.hashCode();
                key = Math.abs((key % 4) + 1);

                //Extracting info for transmission
                info = pool.get(key).split("#", 2);
                ip = info[0].split("/", 2);

                //Sending Query message
                buffer = clientRequest.getBytes();
                packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip[1]), Integer.parseInt(info[1]));
                sock.send(packet);

                //Checking and Sending filename that is being requested
                tempBuf = fileName.getBytes();

                File checkDir = new File(System.getProperty("user.dir", fileName));
                File dir = new File(checkDir.toString() + "/" + fileName);
                /*if(checkDir.exists()){
                    System.out.println("File already exists on local disk");
                }else {*/
                    packet = new DatagramPacket(tempBuf, tempBuf.length, InetAddress.getByName(ip[1]), Integer.parseInt(info[1]));
                    sock.send(packet);

                    //Waiting for response
                    packet = new DatagramPacket(responseBuf, responseBuf.length);
                    sock.receive(packet);
                    response = new String(packet.getData(), 0, packet.getLength());

                    if (!response.equals("404")) {
                        System.out.println("Please type the ip of peer to download from: (Please include the '#' and port number)");
                        System.out.println(response);
                        downloadAdd = in.nextLine();


                        client = new TCP(fileName, downloadAdd);
                        client.start();
                        inform(fileName, sock, pool, serv);

                    } else {
                        System.out.println("File not found");
                    }
                //}
                sock.close(); //Closing socket
            } else if (clientRequest.equals("exit")) {
                running = false;
            }

        }

    }

    public static void inform(String fileName, DatagramSocket sock, Hashtable<Integer, String> pool, Server serv) throws Exception {
        //Hashing filename
        int key = fileName.hashCode();
        key = Math.abs((key % 4) + 1);

        //Extracting info for transmission
        String[] info = pool.get(key).split("#", 2); //Stored as IP#Port
        String[] ip = info[0].split("/", 2); //Taking / off IP


        //Sending inform message
        byte[] buffer = new byte[2048];
        buffer = "inform".getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip[1]), Integer.parseInt(info[1]));
        sock.send(packet);

        //Sending filename
        byte[] tempBuf = new byte[2048];
        tempBuf = (fileName + ":" + InetAddress.getLocalHost().toString() + "#" + sock.getLocalPort()).getBytes();
        packet = new DatagramPacket(tempBuf, tempBuf.length, InetAddress.getByName(ip[1]), Integer.parseInt(info[1]));
        sock.send(packet);

        serv.records.put(fileName, key + ip[1]);
    }
}

class TCP extends Thread {
    String data; //Message received from other server
    Socket dest;
    DataOutputStream out;
    FileOutputStream fOut;
    InputStream in;
    byte[] buffer;
    int bytesRead;


    TCP(String filename, String ip) throws Exception{
        String[] connectionInfo = ip.split("#", 2); //Splitting IP int IP and port
        dest = new Socket(InetAddress.getByName(connectionInfo[0]), Integer.parseInt(connectionInfo[1]));
        data = filename;
        out = new DataOutputStream(dest.getOutputStream());
    }

    public void run(){
            try{
                buffer = new byte[10000];

                out.writeBytes(data + '\n');
                out.flush();

                File desktopDir = new File(System.getProperty("user.dir"));

                fOut = new FileOutputStream(desktopDir.getPath() + "/" + data);
                in = dest.getInputStream();


                in.read(buffer,0,buffer.length);
                fOut.write(buffer,0,buffer.length);
                fOut.flush();


                out.close();
                fOut.close();
                in.close();
                dest.close();

                System.out.println("Query sent");
            } catch(Exception e){
                System.out.println("Exception TCP Server");
            }
    }
}

//For acting as server, not used yet
class Server extends Thread {
    boolean running;
    ServerSocket sock;
    String data;

    File directory;
    File pic;
    FileInputStream inStream;
    BufferedInputStream bufStream;
    byte[] fArray;
    OutputStream os = null;


    public Hashtable<String, String> records = new Hashtable<>();

    Server(int port) throws Exception {
        sock = new ServerSocket(port);
        //
        System.out.println("Listening on port " + port);
    }

    public void run() {
        running = true;
        while (running) {
            try {
                Socket connectionSocket = sock.accept();


                BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                String filename = in.readLine();

                System.out.println(filename);
                System.out.println(records.containsKey(filename));

                if(records.containsKey(filename)) {
                    directory = new File(System.getProperty("user.dir"));
                    pic = new File(directory + "/" + filename);
                    inStream = new FileInputStream(pic);
                    os = connectionSocket.getOutputStream();


                    fArray = new byte[10000];
                    inStream.read(fArray,0, fArray.length);

                    os.write(fArray,0,fArray.length);

                    os.flush();

                    os.close();
                    System.out.println("File Sent");
                }else{
                    System.out.println("Doesnt have");
                    in.close();
                }
                in.close();
            } catch (Exception e) {
                //System.out.println(e);
            }
        }
    }
}
