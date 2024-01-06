package cpen221.mp3.client;

import cpen221.mp3.server.SeverCommandToActuator;

public class Request {
    private final double timeStamp;
    private final RequestType requestType;
    private final RequestCommand requestCommand;
    private final String requestData;
    private final boolean isActuatorCommand;
    private final SeverCommandToActuator commandToActuator;

    /*
    Abstraction Function:
    Represents a request from a Client to the server, or a request from the server to an Actuator. If the request is from
    a Client to the server, then the requestType, requestCommand, and requestData will be non-null, and the
    commandToActuator will be null, and isActuatorCommand will be false. If the request is from the server to an Actuator,
    then the commandToActuator will be non-null, and the requestType, requestCommand, and requestData will be null, and
    isActuatorCommand will be true. The timeStamp of the request is the time when the request was sent.

    Representation Invariant:
    - timeStamp > 0
    - isActuatorCommand == true iff commandToActuator != null
    - isActuatorCommand == false iff commandToActuator == null
     */

    /** Make a Request from the client to the server
     *
     * @param requestType the type of request, must be one of the following:
     *                    - RequestType.CONFIG
     *                    - RequestType.CONTROL
     *                    - RequestType.ANALYSIS
     *                    - RequestType.PREDICT
     * @param requestCommand the command of the request, must be one of the following:
     *                       - RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME
     *                       - RequestCommand.CONTROL_SET_ACTUATOR_STATE
     *                       - RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE
     *                       - RequestCommand.CONTROL_NOTIFY_IF
     *                       - RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW
     *                       - RequestCommand.ANALYSIS_GET_ALL_ENTITIES
     *                       - RequestCommand.ANALYSIS_GET_LATEST_EVENTS
     *                       - RequestCommand.ANALYSIS_GET_MOST_ACTIVE_ENTITY
     *                       - RequestCommand.PREDICT_NEXT_N_TIMESTAMPS
     *                       - RequestCommand.PREDICT_NEXT_N_VALUES
     * @param requestData the data of the request, is not null
     */
    public Request(RequestType requestType, RequestCommand requestCommand, String requestData) {
        this.timeStamp = System.currentTimeMillis();
        this.requestType = requestType;
        this.requestCommand = requestCommand;
        this.requestData = requestData;
        isActuatorCommand = false;
        this.commandToActuator = null;
    }

    /** Make a Request from the server to an actuator
     *
     * @param commandToActuator the command to the actuator, must be one of the following:
     *                          - SeverCommandToActuator.SET_ACTUATOR_STATE
     *                          - SeverCommandToActuator.TOGGLE_ACTUATOR_STATE
     * @param bool the state which the actuator should be set to
     */
    public Request(SeverCommandToActuator commandToActuator, boolean bool) {
        this.timeStamp = System.currentTimeMillis();
        this.requestType = null;
        this.requestCommand = null;
        this.isActuatorCommand = true;
        this.commandToActuator = commandToActuator;
        this.requestData = String.valueOf(bool);
    }

    /** Get the time when the request was sent
     *
     * @return the timestamp, > 0
     */
    public double getTimeStamp() {
        return timeStamp;
    }

    /** Get the command type sent from the server to the actuator
     *
     * @return the command type
     */
    public SeverCommandToActuator getCommandToActuator() {
        return commandToActuator;
    }

    /** Get the request type sent to the server
     *
     * @return the request type
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /** Get the RequestCommand type sent to the server
     *
     * @return the RequestCommand type
     */
    public RequestCommand getRequestCommand() {
        return requestCommand;
    }

    /** Get the requestdata sent to the server
     *
     * @return the requestdata
     */
    public String getRequestData() {
        return requestData;
    }

    /** Get the string representation of the request
     *
     * @return the string representation of the request
     */
    @Override
    public String toString() {
        if (isActuatorCommand) {
            return "Request: {" +
                    "timeStamp=" + timeStamp +
                    ", commandToActuator=" + commandToActuator +
                    ", requestData=" + requestData +
                    '}';
        }
        return "Request: {" +
                "timeStamp=" + timeStamp +
                ", requestType=" + requestType +
                ", requestCommand=" + requestCommand +
                ", requestData=" + requestData +
                '}';
    }
}