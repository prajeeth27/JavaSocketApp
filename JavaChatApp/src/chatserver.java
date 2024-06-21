import java.io.*;
import java.net.*;
import java.util.*;

public class chatserver {
    private static Set<String> userNames = new HashSet<>();
    private static Set<PrintWriter> printWriters = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Chat server started...");
        ServerSocket serverSocket = new ServerSocket(9806);

        while (true) {
            new Handler(serverSocket.accept()).start();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("NAMEREQUIRED");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (userNames) {
                        if (!userNames.contains(name)) {
                            userNames.add(name);
                            break;
                        }
                    }
                    out.println("NAMEALREADYEXISTS");
                }

                out.println("NAMEACCEPTED " + name);
                printWriters.add(out);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    for (PrintWriter writer : printWriters) {
                        writer.println(name + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    userNames.remove(name);
                }
                if (out != null) {
                    printWriters.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
