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
            //String from_server;
            byte[] received = new byte[256];

            boolean gameStart = false;
            boolean end = false;

	        //loop for exchanging messages
            while (!end) {
                if (!gameStart) {
                    //ask user if they want to play
                    System.out.println("Ready to start game? [y/n]");
                    from_user = scanner.nextLine();

                    //if the user types n close the connection
                    if (from_user.equals("n")) {
                        //format output
                        output.write(packet_translate('n'));

                        System.out.println("Closing this connection.");
                        client_socket.close();
                        break;
                    }

                    //send the start message as specified in document
                    output.write(packet_translate('0'));

                    //game start
                    gameStart = true;
                }

                input.read(received);

                if (received[0] == (byte) 0) {
                    String guess = "";
                    for (int x = 0; x < (int) received[1]; x++) {
                        guess += (char) received[x + 3];
                    }

                    //TODO: Incorrect guesses
                    System.out.printf("\n%s\nIncorrect guesses: \n", guess);
                    System.out.println("Guess a letter: ");

                    from_user = "";
                    if (from_user.equals("")) {
                        from_user = scanner.nextLine();

                        if (from_user.length() != 1 || !Character.isLetter(from_user.charAt(0))) {
                            System.out.println("Bad entry, guess a letter: ");
                            from_user = "";
                        }
                    }

                //TODO OR MESSAGE FROM SERVER
                } else {
                    System.out.println("Error in game flow");
                }
            }

            scanner.close();
            input.close();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] packet_translate(char c) {
        if (c == '0') {
            return new byte[] {(byte) 0, (byte) (c & 0x00FF)};
        } else {
            return new byte[] {(byte) 1, (byte) (c & 0x00FF)};
        }
    }
}
