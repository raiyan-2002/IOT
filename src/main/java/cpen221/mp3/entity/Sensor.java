package cpen221.mp3.entity;

import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class Sensor implements Entity {
    private final int id;
    private int clientId;
    private final String type;
    private String serverIP = null;
    private int serverPort = 0;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)
    private Socket socket;
    public PrintWriter out;
    private int numFailedInARow;
    private boolean startedGeneratingEvents;

    /*
    Abstraction Function:
    Represents a sensor entity with a unique id and a type. The client that the sensor is registered to is represented
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
    - socket, inSocket, serverSocket, in, and out are null if http endpoints are not set
    - serverIP and serverPort are null and 0 if http endpoints are not set, otherwise they are a valid IP and port number
    */

    /** Create a new unregistered sensor with the given id and type
     *
     * @param id the unique id of the sensor
     * @param type the type of the sensor, is not null
     */
    public Sensor(int id, String type) {
        this.id = id;
        this.clientId = -1;
        this.type = type;
        this.startedGeneratingEvents = false;
    }

    /** Create a new sensor with the given id, and type, registered for the given client by their id
     *
     * @param id the unique id of the sensor
     * @param clientId the id of the client for which this sensor will be registered
     * @param type the type of the sensor, is not null
     */
    public Sensor(int id, int clientId, String type) {
        this.id = id;
        this.clientId = clientId;
        this.type = type;
        this.startedGeneratingEvents = false;
    }

    /** Create a new unregistered sensor with the given id, type, and http endpoint
     *
     * @param id the unique id of the sensor
     * @param type the type of the sensor, is not null
     * @param serverIP the IP address of the desired endpoint, is not null
     * @param serverPort the port number of the desired endpoint
     */
    public Sensor(int id, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.startedGeneratingEvents = false;
        makeSocket();
    }

    /** Create a new sensor with the given id, type, http endpoint, and registered for the given client by their id
     *
     * @param id the unique id of the sensor
     * @param clientId the id of the client for which this sensor will be registered
     * @param type the type of the sensor, is not null
     * @param serverIP the IP address of the desired endpoint, is not null
     * @param serverPort the port number of the desired endpoint
     */
    public Sensor(int id, int clientId, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
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
        return false;
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

    /** Creates a socket and begins sending events to the server
     *
     */
    private void makeSocket() {
        try {
            this.socket = new Socket(this.serverIP, this.serverPort);
            this.out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            generateEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEventGenerationFrequency(double frequency){
        this.eventGenerationFrequency = frequency;
    }

    /**
     * Makes a new random sensor event
     * @return a random SensorEvent, not null, whose value is:
     *         between 20 and 24 if the type is TempSensor
     *         between 1020 and 1024 if the type is PressureSensor
     *         between 400 and 450 if the type is CO2Sensor
     */
    public SensorEvent makeEvent() {

        Random random = new Random();
        double timestamp = System.currentTimeMillis();
        double value;

        if (this.type.equals("TempSensor")){
            value = 20 + random.nextDouble() * 4;
            return new SensorEvent(timestamp, this.clientId, this.id, this.type, value);
        } else if (this.type.equals("PressureSensor")) {
            value = 1020 + random.nextDouble() * 4;
            return new SensorEvent(timestamp, this.clientId, this.id, this.type, value);
        } else {
            value = 400 + random.nextDouble() * 50;
            return new SensorEvent(timestamp, this.clientId, this.id, this.type, value);
        }

    }

    public void sendEvent(Event event) {
        if (this.serverIP== null) {
            System.out.println("Sensor "+id+" has not set its http endpoint yet, cannot send event");
            return;
        }
        if (this.out == null || this.socket.isClosed()) {
            makeSocket();
        }
        try {
            out.println("SENSOR" + "<()>" + this.clientId + "<()>" +event.toString());
            out.flush();
        } catch (Exception e) {
            numFailedInARow++;
            e.printStackTrace();
        }
    }

    /** Begin sending new random ActuatorEvents to the server at the frequency specified by eventGenerationFrequency
     *  and if 5 events in a row fail to send, wait 10 seconds before trying again.
     */
    public void generateEvents() {
        if (startedGeneratingEvents) {
            return;
        }
        startedGeneratingEvents = true;
        (new Thread(() -> {
            numFailedInARow = 0;

            while (true) {
                SensorEvent event = makeEvent();
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
}