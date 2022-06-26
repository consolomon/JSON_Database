package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    public static final int PORT = 12345;

    public void start(Args commands) {

        try (
                Socket socket = new Socket(SERVER_ADDRESS, PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                ) {

            System.out.println("Client started!");

            String msgToSend = commands.getCommands();
            output.writeUTF(msgToSend);
            System.out.println("Sent: " + msgToSend);

            String msgToRecieved = input.readUTF();
            System.out.println("Received: " + msgToRecieved);

        } catch (IOException e) {

                System.out.println("Server is unavailable: try to connect later");
        }
    }
}
