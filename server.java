import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

//import static com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver.length;

public class server {

    public static final int max_clients = 3;
    private static final clientThread[] threads = new clientThread[max_clients];

    private static String[] words;

    public static int num_clients = 0;

    public static void main(String args[]) throws IOException {
	    int port_number = Integer.parseInt(args[0]);

        if (args.length > 1) {
            BufferedReader reader = new BufferedReader(new FileReader(args[1]));
            String line;

            try {
                //parse header line
                int counter = 0;
                line = reader.readLine();
                String[] s = line.split(" ");
                words = new String[Integer.parseInt(s[1])];
                while((line = reader.readLine()) != null) {
                    words[counter] = line;
                    counter++;
                }
            } finally {
                reader.close();
            }
        }
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
                System.out.println();

			    //create new thread
                Thread thread;

                //increment the number of clients
                ++num_clients;
                
                if (words != null) {
                    thread = new clientThread(client_socket, input, output, words);
                } else { 
                    thread = new clientThread(client_socket, input, output);
                }
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

    public String[] words;

    //constructor
    public clientThread(Socket client_socket, DataInputStream dis, DataOutputStream dos) {
        this.client_socket = client_socket;
        this.input = dis;
        this.output = dos;
    }

    public clientThread(Socket client_socket, DataInputStream dis, DataOutputStream dos, String[] dictionary) {
        this.client_socket = client_socket;
        this.input = dis;
        this.output = dos;
        this.words = dictionary;

    }


    @Override
    // game logic
    public void run() {
        String[] dictionary;
        if (words == null) {
            String[] d = {"sparrow", "gopher", "lion", "ant", "aardvark", "zebra", "wolf", "hyena", "bee", "whale", "bass", "corgi", "tiger", "shark", "dog"};
            dictionary = d;
        } else {
            dictionary = words;
        }
        //word to guess
        String word = "";
        //_ guess string
        String guess = "";
        //letter guessed by client
        String letter = "";
        int wrongNum = 0;

        byte[] received = new byte[2];
        boolean end = false;
        boolean badGuess;
        boolean gameStart = false;

        while (!end) {
            try {
                //receive first message from client
                input.read(received);

                if (!gameStart) {
                    if ((char) received[1] == ('n')) {
                        --server.num_clients;
                        System.out.println("A client has chosen not to play.");
                        System.out.println();
                        this.client_socket.close();
                        break;
                    }

                    //if there are already the max number of clients 
                    //send "server busy" message then close the socket
                    if (server.num_clients > server.max_clients) {
                        output.write(packet_translate_message("Server- overloaded"));
                        --server.num_clients;
                        System.out.println("Server- overloaded. Closing client connection.");
                        System.out.println();
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
                        guess = String.join("", Collections.nCopies(word.length(), "_ "));

                        //ask for first guess
                        output.write(packet_translate_guess(word, wrongNum, guess));
                    }

                } else {
                    if (received[0] == (byte) 1) {
                        //read guess
                        char[] c = guess.toCharArray();

                        badGuess = true;
                        letter = Character.toString((char) received[1]);
                        for (int x = 0; x < word.length(); x++) {
                            if (letter.equals(Character.toString(word.charAt(x)))) {
                                c[2 * x] = letter.charAt(0);
                                badGuess = false;
                            }
                        }
                        guess = String.valueOf(c);

                        if (guess.replaceAll(" ", "").equals(word)) {
                            output.write(packet_translate_message("You Win!"));
                            output.write(packet_translate_guess(word, wrongNum, guess));
                            end = true;
                            break;
                        }

                        if (badGuess) {
                            wrongNum++;
                        }

                        if (wrongNum > 5) {
                            output.write(packet_translate_message("Game Over!"));
                            output.write(packet_translate_guess(word, wrongNum, guess));
                            end = true;
                            break;
                        }
                        //ask for next guess
                        output.write(packet_translate_guess(word, wrongNum, guess));

                    } else {
                        System.out.println("Bad packet from client");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //close game after client finishes
        System.out.println("A client has finished their game and closed their connection.");
        try {
            --server.num_clients;
            this.client_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    public byte[] packet_translate_message(String m) {
        byte[] packet = new byte[256];
        packet[0] = (byte) m.length();
        for (int x = 1; x <= m.length(); x++) {
            packet[x] = (byte) (m.charAt(x - 1) & 0x00FF);
        }
        return packet;
    }
}
