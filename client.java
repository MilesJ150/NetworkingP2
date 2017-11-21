import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {

    public static void main(String args[]) throws UnknownHostException, IOException {
        try {
            Scanner scanner = new Scanner(System.in);

            //get serverIP and serverPort from input args
            String server_IP = args[0];
            int server_port = Integer.parseInt(args[1]);

            //establish connecction
            Socket client_socket = new Socket(server_IP, server_port);

	        //obtain input and output streams
            DataInputStream input = new DataInputStream(client_socket.getInputStream());
            DataOutputStream output = new DataOutputStream(client_socket.getOutputStream());

            String from_user;
            String from_server;
	        //loop for exchanging messages
            while (true) {
                //ask user if they want to play
                System.out.println("Ready to start game? [y/n]");
                from_user = scanner.nextLine();
                
                //if the user types n close the connection
                if (from_user.equals("n")) {
                    output.writeUTF("Exit");
                    System.out.println("Closing this connection.");
                    client_socket.close();
                    break;
                }
                    
                //send the start message as specified in document
                output.writeUTF("Play");

                //show the message from server
                from_server = input.readUTF();
                System.out.println(from_server);
            }

            scanner.close();
            input.close();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
