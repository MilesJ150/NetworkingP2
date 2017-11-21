import java.io.*;
import java.net.*;
import java.util.Scanner;

public class server {

    private static final int max_clients = 3;
    private static final clientThread[] threads = new clientThread[max_clients];

    //ServerSocket server_socket;
    //Socket client_socket;

    public static void main(String args[]) throws IOException {
	    int port_number = Integer.parseInt(args[0]);
        

        //open a server socket on port_number
        ServerSocket server_socket = new ServerSocket(port_number);

        //run infinite loop for getting client requests
        while (true) {
            Socket client_socket = null;
		    try {
			    client_socket = server_socket.accept();

			    System.out.println("A new client is connected : " + client_socket);
			
			    //obtain input and output streams
			    DataInputStream input = new DataInputStream(client_socket.getInputStream());
			    DataOutputStream output = new DataOutputStream(client_socket.getOutputStream());

			    System.out.println("Assigning new thread for this client");

			    //create new thread
			    Thread thread = new clientThread(client_socket, input, output);
			    thread.start();
		    } catch (Exception e) {
			    client_socket.close();
			    e.printStackTrace();
		    }
        }
    }
}

class clientThread extends Thread {
    final DataInputStream input;
    final DataOutputStream output;
    final Socket client_socket;

    private final clientThread[] threads;
    private int max_clients;

    //constructor
    public clientThread(Socket client_socket, DataInputStream dis, DataOutputStream dos) {
        this.client_socket = client_socket;
        this.input = dis;
        this.output = dos;
        this.threads = null;
        this.max_clients = 0;
        //this.threads = threads;
        //max_clients = threads.length;
    }

    @Override
    public void run() {
        String received;
        String toreturn;
        while (true) {
            try {
                //receive first message from client
                received = input.readUTF();

                if (received.equals("Exit")) {
                    System.out.println("A client has closed their connection.");
                    this.client_socket.close();
                    break;
                }
                toreturn = "You may play.";
                output.writeUTF(toreturn);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            // closing resources
            this.input.close();
            this.output.close();
             
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
