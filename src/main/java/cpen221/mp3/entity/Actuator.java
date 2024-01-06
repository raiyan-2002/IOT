package cpen221.mp3.entity;

import cpen221.mp3.client.Request;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.server.SeverCommandToActuator;

import java.io.*;
import java.net.*;
import java.net.Socket;
import java.util.Random;
import java.util.regex.*;

public class Actuator implements Entity {
    private final int id;
    private int clientId;
    private final String type;
    private boolean state;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)

    // the following 2 fields specify the http endpoint that the actuator should send events to
    private String serverIP = null;
    private int serverPort = 0;

    // the following  2 fields are the http endpoint that the actuator should be able to receive commands on from server
    private String host = null;
    private int port = 0;
    private Socket socket;
    private Socket inSocket;
    private ServerSocket serverSocket;
    private BufferedReader in;
    private PrintWriter out;
    private int numFailedInARow;
    private boolean startedGeneratingEvents;

    /*
    Abstraction Function:
    Represents an actuator entity with a unique id and a type. The client that the sensor is registered to is represented
    by the clientId. If the sensor is not registered to a client, then clientId == -1. The sensor has a Socket and
    PrintWriter that are used to send events to the server. The serverIP and serverPort represent the IP address and
    http endpoint that the sensor sends events to. No events are sent if the http endpoint has not been set. Events
    are sent at a frequency specified by eventGenerationFrequency.

    Representation Invariant:
    - the id is unique for each Actuator, and is never changed after the object is constructed
    - once an actuator is registered to a client, it cannot be registered to another client
      (if clientId != -1, it will not be changed again)
    - type is not null
    - eventGenerationFrequency > 0
    - 0 <= numFailedInARow <= 5
    - socket, inSocket, serverSocket, in, and out are null if http endpoints are not set
    - serverIP and serverPort are null and 0 if http endpoints are not set, otherwise they are a valid IP and port number
    - host and port are null and 0 if http endpoints are not set, otherwise they are a valid IP and port number
     */

    /** Create a new unregistered actuator object with the given id, type, and initial state
     *
     * @param id the unique id of the actuator
     * @param type the type of the actuator, is not null
     * @param init_state the initial state of the actuator
     */
    public Actuator(int id, String type, boolean init_state) {
        this.id = id;
        this.clientId = -1;
        this.type = type;
        this.state = init_state;
        this.startedGeneratingEvents = false;
    }

    /** Create a new actuator object with the given id, type, and initial state, registered for the given client
     *  based off of their client id
     *
     * @param id the unique id of the actuator
     * @param clientId the id of the client for which the actuator is to be registered under
     * @param type the type of the actuator, is not null
     * @param init_state the initial state of the actuator
     */
    public Actuator(int id, int clientId, String type, boolean init_state) {
        this.id = id;
        this.clientId = clientId;
        this.type = type;
        this.state = init_state;
        this.startedGeneratingEvents = false;
    }

    /** Create a new unregistered actuator object with the given id, type, initial state, and http endpoint
     *
     * @param id the unique id of the actuator
     * @param type the type of the actuator, is not null
     * @param init_state the initial state of the actuator
     * @param serverIP the IP address of the endpoint, is not null
     * @param serverPort the port number of the endpoint
     */
    public Actuator(int id, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.startedGeneratingEvents = false;
        makeSocket();
    }

    /** Create a new actuator object with the given id, type, initial state, http endpoint, and
     *  registered for the given client based off of their client id
     *
     * @param id the unique id of the actuator
     * @param clientId the id of the client for which the actuator is to be registered under
     * @param type the type of the actuator, is not null
     * @param init_state the initial state of the actuator
     * @param serverIP the IP address of the endpoint, is not null
     * @param serverPort the port number of the endpoint
     */
    public Actuator(int id, int clientId, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.startedGeneratingEvents = false;
        makeSocket();
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public String getType() {
        return type;
    }

    public boolean isActuator() {
        return true;
    }

    /** Get the current state of the actuator
     *
     * @return the current state of the actuator
     */
    public boolean getState() {
        return state;
    }

    /** Get the IP address of the actuator
     *
     * @return the IP address of the actuator
     */
    public String getIP() {
        return host;
    }

    /** Get the port number of the actuator
     *
     * @return the port number of the actuator
     */
    public int getPort() {
        return port;
    }

    /** Update the state of the actuator
     *
     * @param new_state the desired new state of the actuator
     */
    public void updateState(boolean new_state) {
        this.state = new_state;
    }

    public boolean registerForClient(int clientId) {
        if (this.clientId == -1) {
            this.clientId = clientId;
            return true;
        }
        return this.clientId == clientId;
    }

    public void setEndpoint(String serverIP, int serverPort){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        makeSocket();
    }

    public void setEventGenerationFrequency(double frequency){
        this.eventGenerationFrequency = frequency;
    }

    public void sendEvent(Event event) {
        if (this.serverIP== null) {
            System.out.println("Actuator "+id+" has not set its http endpoint yet, cannot send event");
            return;
        }
        if (this.socket.isClosed()) {
            makeSocket();
        }
        try {
            if (this.host == null) {
                makeServerSocket();
            }
            out.println("ACTUATOR" + "<()>" + this.clientId + "<()>" +event.toString() +"<()>"+this.host + "/"+this.port);
            out.flush();
        } catch (Exception e) {
            numFailedInARow++;
            e.printStackTrace();
        }
    }

    /** Update the state of the actuator depending on the request sent from the server
     *
     * @param command the request sent from the server, is not null
     */
    public void processServerMessage(Request command) {
        System.out.println(command);
        if (command.getCommandToActuator().equals(SeverCommandToActuator.SET_STATE)) {
            this.state = true;
        } else if (command.getCommandToActuator().equals(SeverCommandToActuator.TOGGLE_STATE)) {
            // IMPORTANT: command.getRequestData() represents the state to toggle TO, which should NOT be inverted
            this.state = Boolean.parseBoolean(command.getRequestData());
        }
        System.out.println("Actuator " + id + " for client " + clientId + " has been updated to " + state);
    }

    /** Takes the request from the server in the form of a string and processes it
     *
     * @param line the String to be processed, is not null
     */
    public void processInput(String line) {
        System.out.println("Actuator received something: line = " + line);
        Pattern pattern = Pattern.compile("commandToActuator=[\\w]*");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String commandToActuator = matcher.group(0);
            StringBuilder sb = new StringBuilder(commandToActuator);
            commandToActuator = sb.substring(18, sb.length());

            pattern = Pattern.compile("requestData=[\\w]*");
            matcher = pattern.matcher(line);
            if (!matcher.find()) {
                System.out.println("Something went wrong with matcher inside Actuator.processInput");
                return;
            }
            boolean bool = Boolean.parseBoolean(matcher.group(0).substring(12));

            if (commandToActuator.equals("SET_STATE")) {
                Request req = new Request(SeverCommandToActuator.SET_STATE, true);
                processServerMessage(req);
            } else if (commandToActuator.equals("TOGGLE_STATE")) {
                Request req = new Request(SeverCommandToActuator.TOGGLE_STATE, bool);
                processServerMessage(req);
            }
        }
    }

//    public static void main(String[] args) {
//        Actuator a1 = new Actuator(1, "switch", true);
//        a1.processInput((new Request(SeverCommandToActuator.TOGGLE_STATE, true)).toString());
//        String line = "Request: {" +
//                "timeStamp=" + 1.5 +
//                ", commandToActuator=" + "SET_STATE" +
//                '}';
//        Actuator actuator = new Actuator(1, "Light", false);
//        System.out.println(actuator.getState());
//        actuator.processInput(line);
//        System.out.println(actuator.getState());
//        String line2 = "Request: {" +
//                "timeStamp=" + 1.5 +
//                ", commandToActuator=" + "TOGGLE_STATE" +
//                '}';
//        actuator.processInput(line2);
//        System.out.println(actuator.getState());
//    }

    /** Get a string representation of the actuator
     *
     * @return a string representation of the actuator
     */
    @Override
    public String toString() {
        return "Actuator{" +
                "getId=" + getId() +
                ",ClientId=" + getClientId() +
                ",EntityType=" + getType() +
                ",IP=" + getIP() +
                ",Port=" + getPort() +
                '}';
    }

    /** Creates a socket based off the actuator's specified http endpoint and begins sending events to the server
     *
     */
    private void makeSocket() {
        try {
            this.socket = new Socket(serverIP, serverPort);
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            generateEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Creates a serverSocket on the client side that listens for events from the server
     *
     */
    private void makeServerSocket() {
        int port = 20488;
        for(;;) {
            try {
                serverSocket = new ServerSocket(port);
                this.port = port;
                this.host = InetAddress.getLocalHost().getHostAddress();
                listenEvents();
                System.out.println("actuator with id=" + id + " started its server on IP=" + host+ " and port="+port);
                break;
            } catch (Exception e) {

                if (e instanceof BindException) {
                    System.out.println("Actuator with id="+id+" failed to start on port "+port+" as it is already in use, trying the next one");
                } else {
                    e.printStackTrace();
                }
                port++;
                if (port == 65536) port = 1;
            }
        }
    }

    /** Makes a new random actuator event
     *
     * @return an ActuatorEvent, whose value has a 50% chance of being either true or false
     */
    public ActuatorEvent makeEvent() {

        Random random = new Random();
        double timestamp = System.currentTimeMillis();
        boolean value = random.nextBoolean();
        this.updateState(value);

        return new ActuatorEvent(timestamp, this.clientId, this.id, this.type, value);
    }

    /** Begin sending new random ActuatorEvents to the server at the frequency specified by eventGenerationFrequency
     *  and if 5 events in a row fail to send, wait 10 seconds before trying again.
     *
     */
    public void generateEvents() {
        if (startedGeneratingEvents) {
            return;
        }
        startedGeneratingEvents = true;
        (new Thread(() -> {
            numFailedInARow = 0;

            while (true) {
                ActuatorEvent event = makeEvent();
                sendEvent(event);

                int sleeptime = (int) (1000/eventGenerationFrequency);
                if (numFailedInARow == 5) {
                    numFailedInARow = 0;
                    sleeptime = 10000;
                }
                try {
                    Thread.sleep(sleeptime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        })).start();
    }

    /** Begin listening for events from the server from the actuator's serverSocket
     *
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
                while (!inSocket.isClosed()) {
                    try {
                        String line = in.readLine();
                        processInput(line);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();
    }
}
