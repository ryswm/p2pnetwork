import java.io.*;
import java.net.*;
import java.util.*;

public class directoryServ2 {


    public static void main(String argv[]) throws Exception {
        String clientSentence;
        int servID = 2;

        Hashtable<String, String> table = new Hashtable<String, String>();

        //TCP Connection

        ServerSocket welcomeSocket = new ServerSocket(20270);
        while(true){
            Socket connectionSocket = welcomeSocket.accept();

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            clientSentence = inFromClient.readLine();


            outToClient.writeBytes(clientSentence);
        }
    }

}
