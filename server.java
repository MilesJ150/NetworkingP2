import java.io.*;
import java.net.*;
import java.util.Random;
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
    // game logic
    public void run() {
        //String received;
        String toreturn;

        String[] dictionary = {"sparrow", "gopher", "lion", "ant", "aardvark", "zebra", "wolf", "hyena", "bee",
                "whale", "bass", "corgi", "tiger", "shark", "dog"};

        //word to guess
        String word = "";
        String guess = "";
        int wrongNum = 0;

        byte[] received = new byte[2];
        boolean end = false;
        boolean gameStart = false;

        while (!end) {
            try {
                //receive first message from client
                input.read(received);

                if (!gameStart) {
                    if ((char) received[1] == ('n')) {
                        System.out.println("A client has closed their connection.");
                        this.client_socket.close();
                        break;
                    }

                    if (received[0] == (byte) 0) {
                        //set game start
                        gameStart = true;

                        //generate random word to guess from dictionary
                        Random r = new Random();
                        int rand = r.nextInt(15);
                        word = dictionary[rand];

                        //set guess
                        guess = new String(new char[word.length()]).replace("\0", "_ ");

                        //ask for first guess
                        output.write(packet_translate_guess(word, wrongNum, guess));
                    }
                } else {
                    // begin game
                    System.out.println("Game Start!");


                    end = true;
                }

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

    public byte[] packet_translate_guess(String s, int wrongNum, String guess) {
        byte[] packet = new byte[256];
        packet[0] = (byte) 0;
        packet[1] = (byte) s.length();
        packet[2] = (byte) wrongNum;
        for (int x = 3; x <= guess.length() + 2; x++) {
            packet[x] = (byte) (guess.charAt(x - 3) & 0x00FF);
        }
        return packet;
    }
}
