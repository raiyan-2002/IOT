package cpen221.mp3.server;

import cpen221.mp3.entity.Actuator;
import cpen221.mp3.event.Event;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ServerEntity {
    public final int id;
    public final List<Event> eventList;
    public final boolean isActuator;
    public PrintWriter pWriter;
    public Socket actuatorSocket;
    public String actuatorSocketIP;
    public int actuatorSocketPort;
    public double lastEventTimeStamp;
    public Filter filter;
    public Actuator localActuator;
    public boolean isToggle;

    /*
    Abstraction Function:
    Represents an Entity on the server side. This class stores information about the events that the
    Entity has previously sent to the server, and allows for the server to send messages back to the
    Entity, if the Entity is an Actuator.

    Representation Invariant:
    - eventList is not null
    - if isActuator is true, then actuatorSocket is not null and pWriter is not null and actuatorSocketIP is not null
    - if isActuator is false, then actuatorSocket is null and pWriter is null and actuatorSocketIP is null

     */

    /** Construct a new ServerEntity object representing an Entity on the server
     *
     * @param id the id of the Entity that this ServerEntity represents
     * @param isActuator whether the Entity is an Actuator or not
     */
    ServerEntity(int id, boolean isActuator) {
        this.id = id;
        this.isActuator = isActuator;
        this.eventList = new ArrayList<>();
        this.pWriter = null;
        this.lastEventTimeStamp = 0;
        this.filter = null;
    }

    /** Construct a new ServerEntity object representing an Entity on the server, with an http
     *  connection back to the Entity
     *
     * @param id the id of the Entity that this ServerEntity represents
     * @param isActuator whether the Entity is an Actuator or not
     * @param IP the IP address of the Entity
     * @param port the port of the Entity
     * @param actuatorSocket the socket of the Entity, is not null
     * @param pWriter the PrintWriter of the Entity, is not null
     */
    ServerEntity(int id, boolean isActuator, String IP, int port, Socket actuatorSocket, PrintWriter pWriter) {
        this.id = id;
        this.isActuator = isActuator;
        this.eventList = new ArrayList<>();
        this.actuatorSocketIP = IP;
        this.actuatorSocketPort = port;
        this.actuatorSocket = actuatorSocket;
        this.pWriter = pWriter;
        this.lastEventTimeStamp = 0;
        this.filter = null;
    }

    /** Get the most recent event that has been sent to the server by the Entity which this ServerEntity
     *  represents. The most recent event is the event that has the largest timestamp.
     *
     * @return the event with the largest timestamp that the Entity represented by
     *         this ServerEntity has sent to the server, null if the Entity has not sent any events to the server
     */
    public Event getLatestEvent() {
        if (this.eventList.isEmpty()) {
            return null;
        }
        this.eventList.sort((a, b) -> (int) (a.getTimeStamp() - b.getTimeStamp()));
        return this.eventList.get(this.eventList.size()-1);
    }
}
