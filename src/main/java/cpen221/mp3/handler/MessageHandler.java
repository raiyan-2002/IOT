package cpen221.mp3.handler;

import cpen221.mp3.client.Request;
import cpen221.mp3.event.Event;
import cpen221.mp3.server.*;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class MessageHandler {
    private ServerSocket serverSocket;
    private int port;
    public ConcurrentHashMap<Integer, Server> serverMap;
    public PriorityBlockingQueue<MessageHandlerEvent> messagePriorityQueue;
    public ConcurrentHashMap<Integer, Double> waitTime;

    /*
    Abstraction Function:
    Represents the network side interface that is the first place that incoming Requests/Events are received.
    The MessageHandler starts MessageHandlerThreads which concurrently handle incoming Requests/Events.

    Representation Invariant:
    - waitTime is not null
    - messagePriorityQueue is not null
    - serverMap is not null

    Thread Safety Arguments:
    - Everything that is shared between threads is thread safe:
    - - waitTime is thread safe because it is a ConcurrentHashMap
    - - messagePriorityQueue is thread safe because it is a PriorityBlockingQueue. Even though items get removed from
        it inside MessageHandler, that does not interfere with other threads adding to it, since other threads do not
        care if anything gets removed. Between all the threads, it is guaranteed that every item that has to be added
        to the queue is always added. It is also guaranteed that every item that has to be removed from the queue, to
        either be processed, or to be modified and inserted back into the queue, can be removed without issue.
    - - serverMap is thread safe because it is a ConcurrentHashMap
     */

    /** Create a new MessageHandler that will handle incoming Requests/Events from the given port.
     *
     * @param port the port number that the MessageHandler will listen to for incoming Requests/Events
     */
    public MessageHandler(int port) {
        this.port = port;
        this.serverMap = new ConcurrentHashMap<>();
        this.messagePriorityQueue = new PriorityBlockingQueue<>(1000000,Comparator.comparing(MessageHandlerEvent::getExpirationTimestamp));
        this.waitTime = new ConcurrentHashMap<>();
    }


    /**
     * Starts the MessageHandler, which acts as a server.
     * Please use the IP and port printed to the console for connecting Clients and Entities.
     */
    public void start() {
        for(;;) {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server started on port " + serverSocket.getLocalPort() + " from IP "+ InetAddress.getLocalHost().getHostAddress());
                port = serverSocket.getLocalPort();
                break;
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                port++;
            }
        }

        (new Thread(() -> {
            for (;;) {
                try {
                    Socket incomingSocket = serverSocket.accept();
                    System.out.println("Client/Entity connected: " + incomingSocket.getInetAddress().getHostAddress());
                    // create a new thread to handle the client request or entity event
                    Thread handlerThread = new Thread(new MessageHandlerThread(incomingSocket, this));
                    handlerThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })).start();
        (new Thread(() -> {
            for(;;){
                try {
                    double currentTime = System.currentTimeMillis();
                    MessageHandlerEvent nextMessage = messagePriorityQueue.take();
                    int clientID = nextMessage.clientID;

                    Server nextServer = serverMap.get(clientID);
                    double waitTime = nextServer.getMaxWaitTime();
                    double previousWaitTime = this.waitTime.get(clientID);

                    //if next event is due soon
                    if (nextMessage.expirationTimestamp - currentTime < previousWaitTime * 500) {
                        if (!nextMessage.isEvent) System.out.println("Time is " + System.currentTimeMillis() + " processing server=" +nextMessage.clientID + " with command " + nextMessage.request.getRequestCommand());

                        if (nextMessage.isEvent) {
                            double eventTimestamp = nextMessage.event.getTimeStamp();
                            List<MessageHandlerEvent> earlierEvents = messagePriorityQueue.stream().filter(handlerMessage -> {
                                return handlerMessage.clientID == clientID && handlerMessage.isEvent && handlerMessage.event.getTimeStamp() <= eventTimestamp;
                            }).toList();

                            //process them
                            if (!earlierEvents.isEmpty()) {
                                for (MessageHandlerEvent handlerMessage : earlierEvents) {
                                    messagePriorityQueue.remove(handlerMessage);
                                    nextServer.processInput(handlerMessage.event, handlerMessage.request, handlerMessage.returnSocketInfo);
                                }
                            }
                        }
                        nextServer.processInput(nextMessage.event, nextMessage.request, nextMessage.returnSocketInfo);


                        if (previousWaitTime != waitTime) {
                            this.waitTime.put(clientID, waitTime);
                            List<MessageHandlerEvent> toModify = messagePriorityQueue.stream().filter(handlerMessage -> {
                                return handlerMessage.clientID == clientID;
                            }).toList();

                            for (MessageHandlerEvent handlerMessage : toModify) {
                                messagePriorityQueue.remove(handlerMessage);
                                handlerMessage.expirationTimestamp = handlerMessage.timestamp + waitTime*1000;
                                messagePriorityQueue.add(handlerMessage);
                            }

                        }
                    } else {
                        messagePriorityQueue.add(nextMessage);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        })).start();

    }

    /** Get the port number of the MessageHandler
     *
     * @return the port number of the MessageHandler
     */
    public int getPort() {
        return this.port;
    }

    public static void main(String[] args) {
        int port = 16777;
        MessageHandler messageHandler = new MessageHandler(port);
        messageHandler.start();
        // you would need to initialize the RequestHandler with the port number
        // and then start it here
    }
}

