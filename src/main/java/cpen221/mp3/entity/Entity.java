package cpen221.mp3.entity;

import cpen221.mp3.event.Event;

public interface Entity {

    /** Get the unique id of the entity.
     *
     * @return the id of the entity
     */
    int getId();

    /** Get the id of the client for which this entity is registered.
     *
     * @return the id of the client for which this entity is registered, or -1 if it is not registered
     */
    int getClientId();

    /** Get the type of the entity.
     *
     * @return the type of the entity
     */
    String getType();

    /** Determine if an entity is an actuator
     *
     * @return true if the entity is an actuator, otherwise false
     */
    boolean isActuator();

    /** Register the entity for the given client by their id
     *
     * @param clientId the id of the client
     * @return true if the entity is successfully registered to the specified client by their
     *         id or is already registered to that client, otherwise false
     *
     */
    boolean registerForClient(int clientId);

    /** Set's the http endpoint of the entity to the given server IP and port.
     *
     * @param serverIP the IP address of the desired endpoint, is not null
     * @param serverPort the port number of the desired endpoint
     */
    void setEndpoint(String serverIP, int serverPort);

    /** Set the frequency at which the entity should generate events
     *
     * @param frequency the desired frequency at which the entity should generate events in Hz, > 0
     */
    void setEventGenerationFrequency(double frequency);


    /** Send an event to the endpoint, if the endpoint has been sent
     *
     * @param event the entity event to be sent to the endpoint, is not null
     */
    void sendEvent(Event event);
}