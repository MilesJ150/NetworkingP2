import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {

    public static void main(String args[]) throws UnknownHostException, IOException {
        try {
            boolean server_busy = false;
            Scanner scanner = new Scanner(System.in);

            //get serverIP and serverPort from input args
            String server_IP = args[0];
            int server_port = Integer.parseInt(args[1]);

            //establish connection
            Socket client_socket = new Socket(server_IP, server_port);

	        //obtain input and output streams
            DataInputStream input = new DataInputStream(client_socket.getInputStream());
            DataOutputStream output = new DataOutputStream(client_socket.getOutputStream());

            String from_user = "";
            //Guess string from server
            String guess;
            //Message from server
            String message;
            //previous iteration of guess for incorrect guess check
            String guessString = "";
            //array of client guesses
            String[] guessed = new String[27];
            int guessNum = 0;


            byte[] received = new byte[256];
            //string of incorrect guesses
            String incorrectGuess = "";

            boolean gameStart = false;
            boolean end = false;

	        //loop for exchanging messages
            while (!end) {
                if (!gameStart) {
                    //ask user if they want to play
                    System.out.println("Ready to start game? [y/n]");
                    from_user = scanner.nextLine();

                    //if the user types n close the connection
                    if (from_user.toLowerCase().equals("n")) {
                        //format output
                        output.write(packet_translate('n'));

                        System.out.println("Closing this connection.");
                        client_socket.close();
                        break;
                    }

                    //send the start message as specified in document
                    if (from_user.toLowerCase().equals("y")) {
                        output.write(packet_translate('0'));

                        //game start
                        gameStart = true;
                    } else {
                        System.out.println("Bad input!");
                    }
                }

                input.read(received);

                //got a game control packet from server
                if (received[0] == (byte) 0) {
                    guess = "";
                    for (int x = 0; x < (int) received[1] * 2; x++) {
                        guess += (char) received[x + 3];
                    }

                    //incorrect guess occurred from last picked letter
                    if (guessString.equals(guess)) {
                        incorrectGuess += from_user + " ";
                    }

                    //set guessString to updated guess
                    guessString = guess;

                    System.out.printf("\n%s\nIncorrect guesses: %s\n", guess, incorrectGuess);
                    System.out.println("Guess a letter: ");

                    from_user = "";
                    while (from_user.equals("")) {
                        from_user = scanner.nextLine();

                        if (from_user.length() != 1 || !Character.isLetter(from_user.charAt(0))) {
                            System.out.println("Bad entry, guess a letter: ");
                            from_user = "";
                        }

                        for (int x = 0; x < guessNum; x++) {
                            if (from_user.toLowerCase().equals(guessed[x])) {
                                System.out.println("Duplicate guess, guess another letter: ");
                                from_user = "";
                            }
                        }
                    }
                    guessed[guessNum] = from_user;
                    guessNum++;

                    output.write(packet_translate(from_user.charAt(0)));

                //got a message from server                
                } else {
                    //extract message from packet
                    message = "";
                    for (int x = 1; x < (int) (received[0]) + 1; x++) {
                        message += Character.toString((char) received[x]);
                    }
    
                    //check if server response is "server busy"
                    if (message.equals("Server- overloaded")) {
                        System.out.println(message);
                        client_socket.close();
                        break;
                    }

                    input.read(received);
                    if (received[0] == (byte) 0) {
                        guess = "";
                        for (int x = 0; x < (int) received[1] * 2; x++) {
                            guess += (char) received[x + 3];
                        }

                        if (guessString.equals(guess)) {
                            incorrectGuess += from_user + " ";
                        }
                        guessString = guess;

                        System.out.printf("\n%s\nIncorrect guesses: %s\n", guess, incorrectGuess);
                    }
                    System.out.println(message);
                    end = true;
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
