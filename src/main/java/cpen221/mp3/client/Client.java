package cpen221.mp3.client;

import cpen221.mp3.entity.Entity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private final int clientId;
    private String email;
    private String serverIP;
    private int serverPort;
    private List<Entity> entities;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String host;
    private int port;
    private Socket inSocket;
    private ServerSocket serverSocket;

    /*
    Abstraction Function:
    Represents a client which can send requests to the server, and receive data from the server after requesting it.

    Representation Invariant:
    - email is not null
    - serverIP is not null
    - serverPort is not null
    - id is positive and constant
    - entities is not null
     */



    /** Make a Client with an http endpoint to a server
     *
     * @param clientId the ID of the client
     * @param email the email of the client, is not null
     * @param serverIP the IP of the server, is not null
     * @param serverPort the port of the server
     */
    public Client(int clientId, String email, String serverIP, int serverPort) {
        this.clientId = clientId;
        this.email = email;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.entities = new ArrayList<>();

    }

    /** Get the ID of the client
     *
     * @return the ID of the client
     */
    public int getClientId() {
        return clientId;
    }

    /** Registers an entity for the client
     *
     * @param entity the entity to register, is not null
     * @return true if the entity is new and gets successfully registered, false if the Entity is already registered
     *         for this Client, or another Client
     */
    public boolean addEntity(Entity entity) {
        if (this.entities.contains(entity)) {
            return false;
        }
        if (entity.registerForClient(this.clientId)) {
            this.entities.add(entity);
            return true;
        }
        return false;
    }


    /** Sends a request to the server
     *
     * @param request the request to send to the server, is not null. The requestData field of the request is as follows:
     *
     *                - if the request is RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, then the requestData is the new
     *                  update time in seconds (decimals are allowed), as a string
     *
     *                - if the request is RequestCommand.CONTROL_SET_ACTUATOR_STATE, then the requestData is the id of the
     *                  actuator whose state should be set, as a string, followed by the '<>' symbol, followed by the
     *                  serialized Filter (Filter.toString defines exactly how a Filter is serialized) which should
     *                  determine the condition that the latest event must pass to set the actuator's state
     *
     *                - if the request is RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, then the requestData is the id of the
     *                  actuator whose state should be toggled, as a string, followed by the '<>' symbol, followed by
     *                  the serialized Filter which should determine the condition that the latest event must pass to
     *                  toggle the actuator's state
     *
     *                - if the request is RequestCommand.CONTROL_NOTIFY_IF, then the requestData is either:
     *                - - a serialized Filter, which should determine the condition that incoming events must pass to be
     *                    logged for the next time the client wants to see logs, or
     *                - - empty, which means the client wants to see the current logs
     *
     *                - if the request is RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW, then the requestData is the
     *                  serialized TimeWindow which determines the time window, within which, the client wants to see
     *                  the events that arrived during.
     *
     *                - if the request is RequestCommand.ANALYSIS_GET_ALL_ENTITIES, then the requestData is empty
     *
     *                - if the request is RequestCommand.ANALYSIS_GET_LATEST_EVENTS, then the requestData is the number
     *                  of events that the client wants to see, as a string
     *
     *                - if the request is RequestCommand.ANALYSIS_GET_MOST_ACTIVE_ENTITY, then the requestData is empty
     *
     *                - if the request is RequestCommand.PREDICT_NEXT_N_TIMESTAMPS, then the requestData is id of the
     *                  entity whose next timestamps to predict, as a string, followed by the '<>' symbol, followed by
     *                  the number of timestamps to predict, as a string
     *
     *                - if the request is RequestCommand.PREDICT_NEXT_N_VALUES, then the requestData is the id of the
     *                  entity whose next values to predict, as a string, followed by the '<>' symbol, followed by the
     *                  number of values to predict, as a string
     */
    public void sendRequest(Request request) {
        if (this.socket == null || this.socket.isClosed()) {
            connectServer();
            if (this.out == null) {
                System.out.println("Client failed to connect to server, is MessageHandler started correctly?");
                return;
            }
        }
        if (this.host == null) {
            makeServerSocket();
        }
//        while (true) {
//            if (this.out != null) break;
//            //System.out.println("this.out = " + this.out);
//        }
        out.println("CLIENT" + "<()>" + this.clientId + "<()>" + request.toString() + "<()>" +this.host + "/"+this.port);
        out.flush();
    }

    /**
     * Connects client to the server specified by the http endpoint described by the serverIP and serverPort
     */
    public void connectServer() {
        //(new Thread(() -> {
            try {
                this.socket = new Socket(serverIP, serverPort);
                this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch(Exception e) {
                e.printStackTrace();
            }
        //})).start();
    }

    /**
     * Makes a server socket for the client to listen for data received from the http endpoint specified by
     * the serverIP and serverPort
     */
    private void makeServerSocket() {
        int port = 10000;
        for(;;) {
            try {
                serverSocket = new ServerSocket(port);
                this.port = port;
                this.host = InetAddress.getLocalHost().getHostAddress();
                listenEvents();
                break;
            } catch (Exception e) {
                if (e instanceof BindException) {
                    System.out.println("Client with id="+clientId+" failed to start on port "+port+" as it is already in use, trying the next one");
                } else {
                    e.printStackTrace();
                }
                port++;
                if (port == 65536) port = 1;
            }
        }
    }

    /**
     * Listens for any incoming data from the server
     */
    public void listenEvents() {
        (new Thread(() -> {
            while(true) {
                try {
                    this.inSocket = serverSocket.accept();
                    this.in = new BufferedReader(new InputStreamReader(inSocket.getInputStream()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //boolean last_fail = false;
                while (!inSocket.isClosed()) {
                    try {
                        String line = in.readLine();
                        System.out.println("Client with id=" + clientId + " received something: line=" + line);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();
    }

}