package cpen221.mp3.handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.server.*;

class MessageHandlerThread implements Runnable {
    private final Socket incomingSocket;
    private final MessageHandler messageHandler;

    /*
    Abstraction Function:
    Represents a thread that the messageHandler has created to handle and process incoming Requests/Events from a socket.

    Representation Invariant:
    - incomingSocket is not null
    - messageHandler is not null
     */

    /** Create a new MessageHandlerThread that will handle incoming Requests/Events from the given socket.
     *
     * @param incomingSocket the socket for which this thread will handle incoming Requests/Events, is not null
     * @param messageHandler the MessageHandler that will handle the incoming socket object which
     *                       created this thread, is not null
     */
    public MessageHandlerThread(Socket incomingSocket, MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        this.incomingSocket = incomingSocket;
    }

    @Override
    public void run() {
        System.out.println("handling socket");
        try {
            //PrintWriter out = new PrintWriter(new OutputStreamWriter(incomingSocket.getOutputStream()));
            BufferedReader in  = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));
            while (!incomingSocket.isClosed()) {
                String nextLine = in.readLine();
                processMessage(nextLine, System.currentTimeMillis());
            }

            // implement the Server constructor
        } catch (Exception e) {
            e.printStackTrace();
        }
        // handle the client request or entity event here
        // and deal with exceptions if needed
    }

    /** Takes a line of input from the message of the socket in the form of a string as well as when
     *  the message was received by the MessageHandler, and  processes it
     *
     * @param line the line of input from the socket, is not null
     * @param receiveTime the time at which the message was received by the MessageHandler, is not null
     * @throws UnknownHostException if the local host name could not be resolved into an address
     */
    private void processMessage(String line, double receiveTime) throws UnknownHostException {

        String[] parts = line.split("<\\(\\)>");
        String name = parts[0];
        int clientID = Integer.parseInt(parts[1]);
        String dataline = parts[2];

        if (!messageHandler.serverMap.containsKey(clientID)) {
            messageHandler.serverMap.putIfAbsent(clientID, new Server(new Client(clientID, clientID + "client@gmail.com", InetAddress.getLocalHost().getHostAddress(), messageHandler.getPort())));
            messageHandler.waitTime.putIfAbsent(clientID, 2.0);
        }

        Server server = messageHandler.serverMap.get(clientID);
        String[] returnSocketInfo = null;
        if (parts.length > 3) {
            returnSocketInfo = parts[3].split("/");
        }

        String[] split = line.split("[{}]");
        String[] fields = split[1].split(",");
        String[] values = new String[5];
        for (int i = 0; i < fields.length; i++) {
            try {
                values[i] = fields[i].trim().split("=")[1].trim();
            } catch (Exception e) {
                values[i] = "";
            }
        }

        MessageHandlerEvent nextHandlerMessage = null;
        double waitTimeMS = messageHandler.waitTime.get(clientID)*1000;
        if (Objects.equals(name, "CLIENT")) {
            RequestType rt;
            RequestCommand rc;

            rt = switch (values[1]) {
                case "CONFIG" -> RequestType.CONFIG;
                case "CONTROL" -> RequestType.CONTROL;
                case "ANALYSIS" -> RequestType.ANALYSIS;
                case "PREDICT" -> RequestType.PREDICT;
                default -> throw new RuntimeException(values[0] + " is not a correct RequestType");
            };
            rc = switch (values[2]) {
                case "CONFIG_UPDATE_MAX_WAIT_TIME" -> RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME;
                case "CONTROL_SET_ACTUATOR_STATE" -> RequestCommand.CONTROL_SET_ACTUATOR_STATE;
                case "CONTROL_TOGGLE_ACTUATOR_STATE" -> RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE;
                case "CONTROL_NOTIFY_IF" -> RequestCommand.CONTROL_NOTIFY_IF;
                case "ANALYSIS_GET_EVENTS_IN_WINDOW" -> RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW;
                case "ANALYSIS_GET_ALL_ENTITIES" -> RequestCommand.ANALYSIS_GET_ALL_ENTITIES;
                case "ANALYSIS_GET_LATEST_EVENTS" -> RequestCommand.ANALYSIS_GET_LATEST_EVENTS;
                case "ANALYSIS_GET_MOST_ACTIVE_ENTITY" -> RequestCommand.ANALYSIS_GET_MOST_ACTIVE_ENTITY;
                case "PREDICT_NEXT_N_TIMESTAMPS" -> RequestCommand.PREDICT_NEXT_N_TIMESTAMPS;
                case "PREDICT_NEXT_N_VALUES" -> RequestCommand.PREDICT_NEXT_N_VALUES;
                default -> throw new RuntimeException(values[1] + " is not a correct RequestCommand");
            };
            nextHandlerMessage = new MessageHandlerEvent(new Request(rt, rc, values[3]), returnSocketInfo, receiveTime + waitTimeMS, clientID, receiveTime);
        } else if (Objects.equals(name, "SENSOR")){
            Event nextEvent = new SensorEvent(
                    Double.parseDouble(values[0]),
                    Integer.parseInt(values[1]),
                    Integer.parseInt(values[2]),
                    values[3],
                    Double.parseDouble(values[4])
            );
            nextHandlerMessage = new MessageHandlerEvent(nextEvent, returnSocketInfo, receiveTime + waitTimeMS, clientID, receiveTime);
        } else if (Objects.equals(name, "ACTUATOR")) {
            Event nextEvent = new ActuatorEvent(
                    Double.parseDouble(values[0]),
                    Integer.parseInt(values[1]),
                    Integer.parseInt(values[2]),
                    values[3],
                    Boolean.parseBoolean(values[4])
            );
            nextHandlerMessage = new MessageHandlerEvent(nextEvent, returnSocketInfo, receiveTime + waitTimeMS, clientID, receiveTime);
        } else {
            System.out.println("something went really wrong in MessageHandlerThread, it received a new message that is from neither an entity or a server");
            return;
        }
        messageHandler.messagePriorityQueue.add(nextHandlerMessage);

    }
}