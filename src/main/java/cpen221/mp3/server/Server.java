package cpen221.mp3.server;

import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.client.Client;
import cpen221.mp3.event.Event;
import cpen221.mp3.client.Request;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.handler.MessageHandler;
import cpen221.mp3.entity.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private Client client;
    private double maxWaitTime = 2; // in seconds
    public Map<Integer, ServerEntity> entities;
    private Filter logIfFilter = null;
    private List<Event> logList;
    private List<Event> allEvents;
    private String clientIP = null;
    private int clientPort = -1;
    private Socket clientSocket = null;
    private PrintWriter clientPrintWriter;

    /*
    Abstraction Function:
    Represents a server that handles events that are sent from entities which are registered under the
    Server's Client, and requests that are sent from the Server's Client.

    Representation Invariant:
    - maxWaitTime > 0
    -
     */

    /** Create a new Server object for the given Client.
     *
     * @param client the client for which this server will process events and requests, is not null
     */
    public Server(Client client) {
        System.out.println("constructing server");
        this.client = client;
        this.entities = new HashMap<>();
        this.allEvents = new ArrayList<>();
        this.logList = new ArrayList<>();
    }

    /**
     * Update the max wait time for the client.
     * The max wait time is the maximum amount of time
     * that the server can wait for before starting to process each event of the client:
     * It is the difference between the time the message was received on the server
     * (not the event timeStamp from above) and the time it started to be processed.
     *
     * @param maxWaitTime the new max wait time
     */
    public void updateMaxWaitTime(double maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    /** Get the max wait time for the client
     *
     * @return the max wait time for the client
     */
    public double getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * Set the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event 
     * that was received by the server the latest.
     *
     * If the actuator is not registered for the client, then this method should do nothing.
     * If the server has not received any events so far, this method does nothing.
     * 
     * @param filter the filter to check, is not null
     * @param actuator the actuator to set the state of as true, is not null
     */
    public void setActuatorStateIf(Filter filter, Actuator actuator) {
        if (actuator.getClientId() != this.client.getClientId()) {
            return;
        }

        if (allEvents.isEmpty()) {
            return;
        }

        ServerEntity actuatorEntity = this.entities.get(actuator.getId());
        if (actuatorEntity == null) {
            entities.put(actuator.getId(), new ServerEntity(actuator.getId(), true));
            actuatorEntity = this.entities.get(actuator.getId());
        }
        actuatorEntity.filter = filter;
        actuatorEntity.localActuator = actuator;
        actuatorEntity.isToggle = false;

//        Event latestEvent = lastNEvents(1).get(0);
//
//        if (filter.satisfies(latestEvent)) {
//            actuator.updateState(true);
//            sendCommandToActuator(SeverCommandToActuator.SET_STATE, true, actuator.getId());
//        }
    }
    
    /**
     * Toggle the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event 
     * that was received by the server the latest.
     * 
     * If the actuator has never sent an event to the server, then this method should do nothing.
     * If the actuator is not registered for the client, then this method should do nothing.
     * If the server has received no events so far, this method does nothing.
     *
     * @param filter the filter to check, is not null
     * @param actuator the actuator to toggle the state of (true -> false, false -> true), is not null
     */
    public void toggleActuatorStateIf(Filter filter, Actuator actuator) {
        if (actuator.getClientId() != this.client.getClientId()) {
            return;
        }

        ServerEntity actuatorEntity = this.entities.get(actuator.getId());
        if (actuatorEntity == null) {
            return;
        }

        if (allEvents.isEmpty()) {
            return;
        }
        actuatorEntity.filter = filter;
        actuatorEntity.localActuator = actuator;
        actuatorEntity.isToggle = true;
//        Event latestEvent = lastNEvents(1).get(0);

//        if (filter.satisfies(latestEvent)) {
//            boolean actuatorLatestBoolean = actuatorEntity.getLatestEvent().getValueBoolean();
//            actuator.updateState(!actuatorLatestBoolean);
//            sendCommandToActuator(SeverCommandToActuator.TOGGLE_STATE, !actuatorLatestBoolean, actuator.getId());
//        }
    }

    /**
     * Log the event ID for which a given filter was satisfied.
     * This method is checked for every event received by the server.
     *
     * @param filter the filter to check, is not null
     */
    public void logIf(Filter filter) {
        this.logIfFilter = filter;
    }

    /**
     * Return all the logs made by the "logIf" method so far.
     * If no logs have been made, then this method returns an empty list.
     * The list is sorted in the order of event timestamps.
     * After the logs are read, they are cleared from the server.
     *
     * @return list of event IDs 
     */
    public List<Integer> readLogs() {
        this.logList.sort((a, b) -> {
            double diff = a.getTimeStamp() - b.getTimeStamp();
            if (diff == 0) {
                return 0;
            } else if (diff < 0) {
                return -1;
            } else {
                return 1;
            }
        });
        List<Integer> logIDs = this.logList.stream().map(Event::getEntityId).toList();
        this.logList = new ArrayList<>();
        return logIDs;
    }

    /**
     * List all the events of the client that occurred in the given time window.
     * Here the timestamp of an event is the time at which the event occurred, not 
     * the time at which the event was received by the server.
     * If no events occurred in the given time window, then this method returns an empty list.
     *
     * @param timeWindow the time window of events, inclusive of the start and end times, is not null
     * @return list of the events for the client in the given time window
     */
    public List<Event> eventsInTimeWindow(TimeWindow timeWindow) {
        List<Event> validEvents = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getTimeStamp() >= timeWindow.startTime && event.getTimeStamp() <= timeWindow.endTime) {
                validEvents.add(event);
            }
        }
        validEvents.sort((a, b) -> {
            double diff = a.getTimeStamp() - b.getTimeStamp();
            if (diff == 0) {
                return 0;
            } else if (diff < 0) {
                return -1;
            } else {
                return 1;
            }
        });
        return validEvents;
    }

     /**
     * Returns a set of IDs for all the entities of the client for which 
     * we have received events so far.
     * Returns an empty list if no events have been received for the client.
     * 
     * @return list of all the entities of the client for which we have received events so far
     */
    public List<Integer> getAllEntities() {
        return new ArrayList<>(entities.keySet());
    }

    /**
     * List the latest n events of the client.
     * Here the order is based on the original timestamp of the events, not the time at which the events were received by the server.
     * If the client has fewer than n events, then this method returns all the events of the client.
     * If no events exist for the client, then this method should return an empty list.
     * If there are multiple events with the same timestamp in the boundary,
     * the ones with largest EntityId should be included in the list.
     *
     * @param n the max number of events to list, > 0
     * @return list of the latest n events of the client
     */
    public List<Event> lastNEvents(int n) {
        List<Event> lastEvents = new ArrayList<>();
        this.allEvents.sort((a, b) -> {
            double diff = b.getTimeStamp() - a.getTimeStamp();
            if (diff == 0) {
                return b.getEntityId() - a.getEntityId();
            } else if (diff < 0) {
                return -1;
            } else {
                return 1;
            }
        });
        for (int i = 0; i < this.allEvents.size() && i < n ; i++) {
            lastEvents.add(this.allEvents.get(i));
        }
        Collections.reverse(lastEvents);
        return lastEvents;
    }

    /**
     * Get the ID corresponding to the most active entity of the client
     * in terms of the number of events it has generated. If there was a tie, then this method returns
     * the largest ID.
     * 
     * @return the most active entity ID of the client
     */
    public int mostActiveEntity() {
        int max = 0;

        for (Integer id : entities.keySet()) {
            if (entities.get(id).eventList.size() > max) {
                max = entities.get(id).eventList.size();
            }
        }

        List<Integer> allEntitiesWithMax = new ArrayList<>();

        for (Integer id : entities.keySet()) {
            if (entities.get(id).eventList.size() == max) {
                allEntitiesWithMax.add(id);
            }
        }

        int maxId = 0;

        for (Integer id : allEntitiesWithMax) {
            if (id > maxId) {
                maxId = id;
            }
        }

        return maxId;
    }

    /**
     * Checks if the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client
     * @param entityId the ID of the entity being checked
     * @return true if server has received events for an entity with that ID,
     * and if that Entity is registered for the client, false otherwise
     */
    private boolean entityEventExists (int entityId){
        boolean hasEvents = false;
        boolean clientHasEntity = false;

        for(Event event: this.allEvents){
            if(event.getEntityId()==entityId){
                hasEvents = true;
                break;
            }
        }
        for(Integer id : this.getAllEntities()){
            if(id==entityId){
                clientHasEntity = true;
                break;
            }
        }
        return hasEvents&&clientHasEntity;
    }

    /** Get the predictions of the next n timestamps for the next n events of the given entity
     * identified by its ID. If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method returns an empty list.
     * 
     * @param entityId the ID of the entity
     * @param n the number of timestamps to predict, > 0
     * @return list of the predicted timestamps
     */
    public List<Double> predictNextNTimeStamps(int entityId, int n) {
        List<Double> prevTimeStamps = new ArrayList<>();

        if(!this.entityEventExists(entityId)){
            return new ArrayList<>();
        }

        for(Event event : this.allEvents){
            if(event.getEntityId()==entityId){
                prevTimeStamps.add(event.getTimeStamp());
            }
        }

        Collections.sort(prevTimeStamps);

        Predictor predictor = new Predictor(new ArrayList<>(prevTimeStamps), true, n);

        return predictor.predictDouble();
    }

    /**
     * Get the predictions of the next n values of the timestamps for the next n events of the
     * given entity of the client, identified by its ID.
     * The values correspond to Event.getValueDouble() or Event.getValueBoolean() 
     * based on the type of the entity. If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method returns an empty list.
     * 
     * @param entityId the ID of the entity
     * @param n the number of double value to predict, > 0
     * @return list of the predicted timestamps
     */
    public List<Object> predictNextNValues(int entityId, int n) {
        List<Object> prevValues = new ArrayList<>();
        List<Event> prevEvents = new ArrayList<>();
        boolean isDouble = true;

        if(!this.entityEventExists(entityId)){
            return new ArrayList<>();
        }

        for (Integer ID : this.getAllEntities()){
            if(ID == entityId){
                if(this.entities.get(ID).isActuator){
                    isDouble=false;
                }
                break;
            }
        }

        for(Event event : this.allEvents){
            if(event.getEntityId()==entityId){
                prevEvents.add(event);
            }
        }

        prevEvents.sort((a, b) -> {
            double diff = a.getTimeStamp() - b.getTimeStamp();
            if (diff == 0) {
                return 0;
            } else if (diff < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        if(isDouble){
            for(Event event : prevEvents){
                prevValues.add(event.getValueDouble());
            }
            Predictor predictor = new Predictor(new ArrayList<>(prevValues), isDouble, n);
            return new ArrayList<>(predictor.predictDouble());
        }
        else{
            for(Event event : prevEvents){
                prevValues.add(event.getValueBoolean());
            }
            Predictor predictor = new Predictor(new ArrayList<>(prevValues), isDouble, n);
            return new ArrayList<>(predictor.predictBool());
        }
    }

    /** Process an incoming event from an Entity to the Server.
     *
     * @param event the incoming event to be processed, is not null
     */
    void processIncomingEvent(Event event) {
        System.out.println("Server for client="+client.getClientId()+" received a new event: " + event.toString());
        if (event.getClientId() != this.client.getClientId()) {
            return;
        }
        boolean isActuator = event instanceof ActuatorEvent;
        int entityId = event.getEntityId();
        if (!entities.containsKey(entityId)) {
            entities.put(entityId, new ServerEntity(entityId, isActuator));
        }

        entities.get(entityId).eventList.add(event);
        allEvents.add(event);
        if (logIfFilter != null) {
            if (logIfFilter.satisfies(event)) {
                logList.add(event);
            }
        }

        //actuator update things
        for (ServerEntity entity : entities.values()) {
            if (entity.isActuator && entity.filter != null) {
                double eventTimeStamp = event.getTimeStamp();
                if (eventTimeStamp > entity.lastEventTimeStamp && entity.filter != null) {
                    entity.lastEventTimeStamp = eventTimeStamp;
                    Event latestEvent = lastNEvents(1).get(0);

                    if (entity.isToggle) {
                        //toggleActuatorStateIf(entity.filter, entity.localActuator);
                        if (entity.filter.satisfies(latestEvent)) {
                            boolean actuatorLatestBoolean = entity.getLatestEvent().getValueBoolean();
                            entity.localActuator.updateState(!actuatorLatestBoolean);
                            sendCommandToActuator(SeverCommandToActuator.TOGGLE_STATE, !actuatorLatestBoolean, entity.id);
                        }
                    } else {
//                        setActuatorStateIf(entity.filter, entity.localActuator);
                        if (entity.filter.satisfies(latestEvent)) {
                            entity.localActuator.updateState(true);
                            sendCommandToActuator(SeverCommandToActuator.SET_STATE, true, entity.id);
                        }
                    }
                }
            }
        }
    }

    /** Process an incoming request from a Client to the Server.
     *
     * @param request the incoming request to be processed, is not null
     */
    void processIncomingRequest(Request request) {
        System.out.println("Server for client="+client.getClientId()+" received a new request: " + request.toString());
        RequestType requestType = request.getRequestType();
        RequestCommand requestCommand = request.getRequestCommand();
        String requestData = request.getRequestData();
        if (requestType.equals( RequestType.CONFIG)) {
            if (requestCommand.equals(RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME)) {
                updateMaxWaitTime(Double.parseDouble(requestData));
            }
        }
        if (requestType.equals( RequestType.CONTROL)) {
            if (requestCommand.equals(RequestCommand.CONTROL_NOTIFY_IF)) {
                if (requestData.isEmpty()) {
                    List<Integer> output = readLogs();
                    sendToClient(output.toString(), requestCommand);
                }
                logIf(Filter.unserialize(requestData));

            }
            if (requestCommand.equals(RequestCommand.CONTROL_SET_ACTUATOR_STATE)) {
                int actuatorID = Integer.parseInt(requestData.split("<>")[0]);
                Filter unserializedFilter = Filter.unserialize(requestData.split("<>")[1]);
                setActuatorStateIf(unserializedFilter, new Actuator(actuatorID, client.getClientId(), "switch", true));
            }
            if (requestCommand.equals(RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE)) {
                int actuatorID = Integer.parseInt(requestData.split("<>")[0]);
                Filter unserializedFilter = Filter.unserialize(requestData.split("<>")[1]);
                toggleActuatorStateIf(unserializedFilter, new Actuator(actuatorID, client.getClientId(), "switch", true));
            }
        }
        if (requestType.equals( RequestType.ANALYSIS)) {
            if (requestCommand.equals(RequestCommand.ANALYSIS_GET_ALL_ENTITIES)) {
                List<Integer> output = getAllEntities();
                sendToClient(output.toString(), requestCommand);
            }
            if (requestCommand.equals(RequestCommand.ANALYSIS_GET_LATEST_EVENTS)) {
                List<Event> output = lastNEvents(Integer.parseInt(requestData));
                sendToClient(output.toString(), requestCommand);
            }
            if (requestCommand.equals(RequestCommand.ANALYSIS_GET_MOST_ACTIVE_ENTITY)) {
                int entityId = mostActiveEntity();
                sendToClient(String.valueOf(entityId), requestCommand);
            }
            if (requestCommand.equals(RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW)) {
                double startTime = Double.parseDouble(requestData.split("<>")[0]);
                double endTime = Double.parseDouble(requestData.split("<>")[1]);
                List<Event> output = eventsInTimeWindow(new TimeWindow(startTime, endTime));
                sendToClient(output.toString(), requestCommand);
            }

        }
        if (requestType.equals( RequestType.PREDICT)) {
            if (requestCommand.equals(RequestCommand.PREDICT_NEXT_N_TIMESTAMPS)) {
                int entityId = Integer.parseInt(requestData.split("<>")[0]);
                int n = Integer.parseInt(requestData.split("<>")[1]);
                List<Double> output = predictNextNTimeStamps(entityId, n);
                sendToClient(output.toString(), requestCommand);

                //predictNextNTimeStamps("<>")
            }
            if (requestCommand.equals(RequestCommand.PREDICT_NEXT_N_VALUES)) {
                int entityId = Integer.parseInt(requestData.split("<>")[0]);
                int n = Integer.parseInt(requestData.split("<>")[1]);
                List<Object> output = predictNextNValues(entityId, n);
                sendToClient(output.toString(), requestCommand);
                //predictNextNValues("<>")
            }
        }
        // implement this method
    }

    /** Get the Client that this Server object serves
     *
     * @return the Client that this Server object serves
     */
    public Client getClient() {
        return this.client;
    }

    /** Process an incoming event or request from an Entity to the Server or from a Client to the Server.
     *
     * @param event the incoming event to be processed, is not null
     * @param request the incoming request to be processed, is not null
     * @param actuatorSocketInfo the IP address and port number of the actuator that sent the event, is not null
     *                           - actuatorSocketInfo[0] is the IP address of the actuator
     *                           - actuatorSocketInfo[1] is the port number of the actuator
     */
    public void processInput(Event event, Request request, String[] actuatorSocketInfo) {
        if (event instanceof ActuatorEvent) {

            int entityId = event.getEntityId();
            if (entities.get(entityId) == null) {
                entities.put(entityId, new ServerEntity(entityId, true));
            }

            ServerEntity entity = entities.get(entityId);

            if (entity.actuatorSocket == null) {
                String actuatorHost = actuatorSocketInfo[0];
                int actuatorPort = Integer.parseInt(actuatorSocketInfo[1]);

                try {
                    Socket actuatorSocket = new Socket(actuatorHost, actuatorPort);
                    PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(actuatorSocket.getOutputStream()));
                    entity.actuatorSocket = actuatorSocket;
                    entity.pWriter = pWriter;
                    entity.actuatorSocketIP = actuatorHost;
                    entity.actuatorSocketPort = actuatorPort;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            processIncomingEvent(event);
        }
        if (event instanceof SensorEvent) {
            processIncomingEvent(event);
        }
        if (request != null) {

            if (this.clientPort == -1) {
                this.clientIP = actuatorSocketInfo[0];
                this.clientPort = Integer.parseInt(actuatorSocketInfo[1]);
            }
            if (clientSocket == null || clientSocket.isClosed()) {
                try {
                    clientSocket = new Socket(clientIP, clientPort);
                    clientPrintWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            processIncomingRequest(request);
        }
    }

    /** Send a command to an actuator to set its state to the given boolean.
     *
     * @param command the command to be sent to the actuator, is not null
     * @param bool the state that the actuator should be set to
     * @param actuatorID the ID of the actuator to send the command to
     */
    public void sendCommandToActuator(SeverCommandToActuator command, boolean bool, int actuatorID) {
        ServerEntity actuatorEntity = entities.get(actuatorID);
        if (actuatorEntity == null) {
            System.out.println("No actuator with the ID is found, please try again later");
            return;
        }
        if (actuatorEntity.actuatorSocket == null) {
            return;
        }
        Request req = new Request(command, bool);
        if(actuatorEntity.actuatorSocket.isClosed()) {
            try {
                actuatorEntity.actuatorSocket = new Socket(actuatorEntity.actuatorSocketIP, actuatorEntity.actuatorSocketPort);
                actuatorEntity.pWriter = new PrintWriter(new OutputStreamWriter(actuatorEntity.actuatorSocket.getOutputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        actuatorEntity.pWriter.println(req);
        actuatorEntity.pWriter.flush();
    }

    /** Send requested information by the Client, to the Client in the form of a String
     *
     * @param line the line of information to be sent to the Client, is not null
     * @param command the command that the Client requested, is not null
     */
    public void sendToClient(String line, RequestCommand command) {
        if (clientPrintWriter == null) {
            System.out.println("Server for client " + client.getClientId() + " unable to send to client");
            return;
        }
        String message = "You requested " + command.toString() + ", here is your result: " + line;
        clientPrintWriter.println(message);
        clientPrintWriter.flush();
        System.out.println("Successfully sent the following message to client " + client.getClientId() + " at timestamp " + System.currentTimeMillis());
        System.out.println(message);
    }
}

