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
        byte[] tempBuf = new byte[2048];
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

        //Starting server functions of client
        Server serv = new Server(port);
        serv.start();

        while (running) {
            sock = new DatagramSocket(port, InetAddress.getLocalHost());

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
            } else if (clientRequest.equals("inform")) {
                System.out.println("Please enter the file for which to upload: ");
                fileName = in.nextLine();

                //Hashing filename
                key = fileName.hashCode();
                key = Math.abs((key % 4) + 1);

                //Extracting info for transmission
                info = pool.get(key).split("#", 2); //Stored as IP#Port
                ip = info[0].split("/", 2); //Taking / off IP


                //Sending inform message
                buffer = "inform".getBytes();
                packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip[1]), Integer.parseInt(info[1]));
                sock.send(packet);

                //Sending filename
                tempBuf = (fileName + ":" + InetAddress.getLocalHost().toString() + "#" + port).getBytes();
                packet = new DatagramPacket(tempBuf, tempBuf.length, InetAddress.getByName(ip[1]), Integer.parseInt(info[1]));
                sock.send(packet);

                serv.records.put(fileName, key + ip[1]);

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

                //Sending filename that is being requested
                tempBuf = fileName.getBytes();
                packet = new DatagramPacket(tempBuf, tempBuf.length, InetAddress.getByName(ip[1]), Integer.parseInt(info[1]));
                sock.send(packet);

                //Waiting for response
                packet = new DatagramPacket(responseBuf, responseBuf.length);
                sock.receive(packet);
                response = new String(packet.getData(), 0, packet.getLength());
                sock.close(); //Closing socket

                if (!response.equals("404")) {
                    System.out.println("Please type the ip of peer to download from: (Please include the # and port number)");
                    System.out.println(response);
                    downloadAdd = in.nextLine();

                    download(fileName, downloadAdd);
                } else {
                    System.out.println("File not found");
                }

            } else if (clientRequest.equals("exit")) {
                running = false;
            }

        }

    }

    public static void download(String fileName, String ip) throws Exception {
        byte[] newPic = new byte[10000];
        String[] connectionInfo = ip.split("#", 2); //Splitting IP int IP and port

        System.out.println(connectionInfo[0] + ":" + Integer.parseInt(connectionInfo[1]));
        //Making socket out stream and sending filename request
        Socket socket = new Socket(InetAddress.getByName(connectionInfo[0]), Integer.parseInt(connectionInfo[1]));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeBytes(fileName);
        out.flush();

        FileOutputStream fos = new FileOutputStream("/Users/ryan/Desktop/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream is = socket.getInputStream();

        int bytesRead = 0;

        while((bytesRead=is.read(newPic))!=-1)
            bos.write(newPic, 0, bytesRead);

        bos.flush();
        socket.close();

        System.out.println("File saved successfully!");
    }

}

//For acting as server, not used yet
class Server extends Thread {
    boolean running;
    ServerSocket sock;
    String data;

    File pic;
    FileInputStream inStream;
    BufferedInputStream bufStream;
    byte[] fArray;
    OutputStream os = null;

    public Hashtable<String, String> records = new Hashtable<>();

    Server(int port) throws Exception {
        sock = new ServerSocket(port);
        System.out.println("Listening on port " + port);
    }

    public void run() {
        running = true;
        while (running) {
            try {
                Socket connectionSocket = sock.accept();


                BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                System.out.println("Connection Accepted with " + connectionSocket.getRemoteSocketAddress());
                System.out.println(in.ready());
                System.out.println(in.readLine() != null);
                String filename = in.readLine();

                if (records.containsKey(filename)) {
                    System.out.println("Uploading file");

                    pic = new File(data);
                    inStream = new FileInputStream(pic);
                    bufStream = new BufferedInputStream(inStream);
                    os = connectionSocket.getOutputStream();

                    byte[] contents;
                    long fileLength = pic.length();
                    long current = 0;

                    long start = System.nanoTime();
                    while(current!=fileLength){
                        int size = 10000;
                        if(fileLength - current >= size)
                            current += size;
                        else{
                            size = (int)(fileLength - current);
                            current = fileLength;
                        }
                        contents = new byte[size];
                        bufStream.read(contents, 0, size);
                        os.write(contents);
                        System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
                    }

                    in.close();
                    os.flush();
                    connectionSocket.close();

                    System.out.println("File Uploaded");

                }else{
                    System.out.println("Doesnt have");
                    in.close();
                }

            } catch (Exception e) {
                //System.out.println(e);
            }
        }
    }
}
