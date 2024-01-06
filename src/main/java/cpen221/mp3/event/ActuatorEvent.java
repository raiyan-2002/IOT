package cpen221.mp3.event;

public class ActuatorEvent implements Event {

    private double timeStamp;
    private int clientId;
    private int entityId;
    private String entityType;
    private boolean value;

    public ActuatorEvent(double TimeStamp,
                         int ClientId,
                         int EntityId,
                         String EntityType,
                         boolean Value) {
        this.timeStamp = TimeStamp;
        this.clientId = ClientId;
        this.entityId = EntityId;
        this.entityType = EntityType;
        this.value = Value;
    }

    public double getTimeStamp() {
        return this.timeStamp;
    }

    public int getClientId() {
        return this.clientId;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getEntityType() {
        return this.entityType;
    }

    public boolean getValueBoolean() {
        return this.value;
    }

    // Actuator events do not have a double value
    // no need to implement this method
    public double getValueDouble() {
        return -1;
    }

    @Override
    public String toString() {
        return "ActuatorEvent: {" +
                "timeStamp=" + timeStamp +
                ", clientId=" + clientId +
                ", entityId=" + entityId +
                ", entityType=" + entityType +
                ", value=" + value +
                '}';
    }
}