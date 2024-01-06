package cpen221.mp3.event;

public class SensorEvent implements Event {

    private double timeStamp;
    private int clientId;
    private int entityId;
    private String entityType;
    private double value;


    public SensorEvent(double TimeStamp,
                       int ClientId,
                       int EntityId,
                       String EntityType,
                       double Value) {
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

    public double getValueDouble() {
        return this.value;
    }

    // Sensor events do not have a boolean value
    // no need to implement this method
    public boolean getValueBoolean() {
        return false;
    }

    @Override
    public String toString() {
        return "SensorEvent: {" +
                "timeStamp=" + timeStamp +
                ", clientId=" + clientId +
                ", entityId=" + entityId +
                ", entityType=" + entityType +
                ", value=" + value +
                '}';
    }
}