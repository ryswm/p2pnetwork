import java.io.*;
import java.net.*;

public class TCPClient {

    public static void main(String argv[]) throws Exception {
        String sentence;
        String modSentence;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket("hostname", 6789);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServ = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        sentence = inFromUser.readLine();
        outToServer.writeBytes(sentence +'\n');

        modSentence = inFromServ.readLine();

        System.out.println("FROM SERVER: " + modSentence);
        clientSocket.close();


    }
}
