package cpen221.mp3.handler;

import cpen221.mp3.client.Request;
import cpen221.mp3.event.Event;

public class MessageHandlerEvent {
    double expirationTimestamp;
    String[] returnSocketInfo;
    boolean isEvent;
    Event event;
    Request request;
    int clientID;
    double timestamp;

    /*
    Abstraction Function:
    Represents an event or request that the MessageHandler has received from an Entity or Client.

    Representation Invariant:
    - expirationTimestamp > timestamp > 0
     */

    /** Create a MessageHandlerEvent which corresponds to an Event from an Entity to the server.
     *
     * @param event the Event from the Entity to the Server, is not null
     * @param returnSocketInfo the socket information of the Entity, is not null
     * @param expirationTimestamp the expiration timestamp of this MessageHandlerEvent
     * @param clientID the ID of the Client
     * @param timestamp the timestamp of this MessageHandlerEvent
     */
    MessageHandlerEvent(Event event, String[] returnSocketInfo, double expirationTimestamp, int clientID, double timestamp) {
        this.event = event;
        this.request = null;
        this.expirationTimestamp = expirationTimestamp;
        this.returnSocketInfo = returnSocketInfo;
        this.clientID = clientID;
        this.timestamp = timestamp;
        this.isEvent = true;
    }

    /** Create a MessageHandlerEvent which corresponds to a Request from a Client to the Server.
     *
     * @param request the Request from the Client to the Server, is not null
     * @param returnSocketInfo the socket information of the Client, is not null
     * @param expirationTimestamp the expiration timestamp of this MessageHandlerEvent
     * @param clientID the ID of the Client
     * @param timestamp the timestamp of this MessageHandlerEvent
     */
    MessageHandlerEvent(Request request, String[] returnSocketInfo, double expirationTimestamp, int clientID, double timestamp) {
        this.request = request;
        this.event = null;
        this.expirationTimestamp = expirationTimestamp;
        this.returnSocketInfo = returnSocketInfo;
        this.clientID = clientID;
        this.timestamp = timestamp;
        this.isEvent = false;
    }

    /** Get the expiration timestamp of this MessageHandlerEvent.
     *
     * @return the expiration timestamp of this MessageHandlerEvent
     */
    public double getExpirationTimestamp() {
        return expirationTimestamp;
    }

}
