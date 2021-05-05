import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.StringIndexOutOfBoundsException;

public class Program extends Thread {

  private ServerSocket connection;
  private Socket sockets[];
  private String state;
  private Thread receivingThread;
  private int numConnections;

  Socket[] listeners;

  // constructort
  Program(int process, String ips[], int port) {
    state = "running";
    // calling receiving messages thread
    receiveMessages(process, port);
    // calling sending messages thread
    sendMessages(process, ips, port);
  }

  private void receiveMessages(int process, int port) {
    new Thread("input") {
      public void run() {
        numConnections = 0;
        listeners = new Socket[3];
        Thread[] threadListeners = new Thread[3];
        try {
          connection = new ServerSocket();
          // Bind server to ip address and port
          connection.bind(new InetSocketAddress(InetAddress.getLocalHost(), port));

          while (numConnections < 3) {
            listeners[numConnections] = connection.accept();
            numConnections++;
            // Start new thread for each server socket listener
            threadListeners[numConnections - 1] = new Thread(Integer.toString(numConnections - 1)) {
              public void run() {
                try {
                  // Set uo reader for socket
                  BufferedReader msg = new BufferedReader(
                      new InputStreamReader(listeners[Integer.parseInt(this.getName())].getInputStream()));

                  // While server is running
                  while (true) {
                    String message = null;

                    // If message to be read
                    while (msg.ready()) {
                      // Read message
                      message = msg.readLine();
                    }

                    // If no message, go to start of loop again
                    if (message == null) {
                      continue;
                    }
                    // If stop message received
                    else if (message.equals("stop")) {
                      // If state not marked as stop, mark as stopped
                      if (state.equals("running")) {
                        state = "stopped";
                      }
                      // exit while loop
                      System.out.println("stop");
                      break;
                    }
                    System.out.println(message);
                  }
                } catch (IOException e) {
                  System.out.println("Error reading user input");
                }
                if (numConnections == 3) {
                  numConnections--;
                  // Let other thread know that stop message has beeen received
                  if (receivingThread != null && receivingThread.isAlive()) {
                    // Interrupt call
                    receivingThread.interrupt();
                  }
                }
              }
            };
            // Start thread listener for socket
            threadListeners[numConnections - 1].start();
          }
        } catch (IOException e) {
          System.out.print("server failed to connect");
        }
        // wait until thread listeners stopped
        try {
          threadListeners[0].join();
          threadListeners[1].join();
          threadListeners[2].join();
        } catch (Exception e) {
          System.out.print("Thread malfunction");
        }
        // close all connections for server socket
        try {
          connection.close();
          listeners[0].close();
          listeners[1].close();
          listeners[2].close();
        } catch (IOException e) {
          System.out.println("Error closing sockets");
        }
      }
    }.start();
  }

  private void sendMessages(int process, String ips[], int port) {

    receivingThread = new Thread("sender") {
      @Override
      public void run() {
        sockets = new Socket[4];
        // creat sockets
        int ipCounter = 0;
        // connect to all three other machines
        for (int i = 0; i < 4; i++) {
          if (i != process - 1) {
            // keep checking for server to connect to for each socket
            while (sockets[i] == null || !sockets[i].isConnected()) {
              try {
                sockets[i] = new Socket(ips[ipCounter], port);
                ipCounter++;
              } catch (IOException e) {
              }
            }
          }
        }
        // WHile state has not been marked as stopped
        while (state.equals("running")) {
          BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
          try {
            String line = "";
            // If console has command to be read
            if (userInput.ready()) {
              // read command
              line = userInput.readLine();
            } else {
              // if interrupt message from other thread
              if (Thread.interrupted()) {
                // stop current thread
                state = "stopped";
              }
              continue;
            }
            // If stop command received, mark state as stopped
            if (line.substring(0, 4).equals("stop")) {
              state = "stopped";
            }
            // If send message received
            else if (line.substring(0, 5).equals("send ")) {
              line = line.substring(5);
              // If second arg is 0
              if (line.substring(0, 2).equals("0 ")) {
                line = line.substring(2);
                // send message to each socket
                for (int i = 0; i < 4; i++) {
                  if (i != process - 1) {
                    PrintWriter sendMsg = new PrintWriter(sockets[i].getOutputStream(), true);
                    sendMsg.println(line);
                  }
                }
              }
              // else if send to specific process
              else {
                // extract porcess number
                if (!(line.substring(0, 2).equals("1 ") || line.substring(0, 2).equals("2 ")
                    || line.substring(0, 2).equals("3 ") || line.substring(0, 2).equals("4 "))) {
                  System.out.println("invalid message");
                  continue;
                }
                int receivingProcess = Integer.parseInt(line.substring(0, 1)) - 1;
                line = line.substring(2);
                // write to socket
                PrintWriter sendMsg = new PrintWriter(sockets[receivingProcess].getOutputStream(), true);
                sendMsg.println(line);
              }
            }

          }
          // catch any io errors
          catch (IOException e) {
            System.out.println("Error reading user input");
          }
          // catch if message improperly formatted
          catch (NullPointerException e) {
            System.out.println("Invalid Message");
          } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid Receiver number");
          } catch (java.lang.StringIndexOutOfBoundsException e) {
            System.out.println("Message not correct format");
          }
        }
        // send stop message to other processes
        for (int i = 0; i < 4; i++) {
          if (i != process - 1) {
            try {
              PrintWriter sendMsg = new PrintWriter(sockets[i].getOutputStream(), true);
              sendMsg.println("stop");
              sockets[i].close();
            } catch (IOException e) {
              System.out.println("could not send stop message");
            }
          }
        }
      }
    };
    receivingThread.start();
  }

}
