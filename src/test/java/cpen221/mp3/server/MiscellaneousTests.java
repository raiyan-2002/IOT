package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.handler.MessageHandler;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class MiscellaneousTests {
    @Test
    public void testsForCoverage(){
        MessageHandler.main(null);
        Client client1 = new Client(1, "client1@gmail.com","1.1.1.1", 10);
        Actuator actuator2 = new Actuator(2, "Switch", true);
        Actuator actuator3 = new Actuator(3, 1, "Switch", true);
        Sensor sensor4 = new Sensor(4,1,"CO2");
        Sensor sensor5 = new Sensor(5,"CO2");
        assertTrue(actuator2.registerForClient(1));
        assertFalse(actuator2.registerForClient(2));
        assertTrue(sensor5.registerForClient(1));
        assertFalse(sensor5.registerForClient(2));
        assertFalse(actuator3.toString().isEmpty());
        assertFalse(sensor5.toString().isEmpty());
        assertTrue(client1.addEntity(actuator2));
        assertFalse(client1.addEntity(actuator2));
        actuator2.sendEvent(null);
        sensor4.sendEvent(null);
        client1.sendRequest(null);
        assertFalse((new SensorEvent(0, 1, 1, "Temperature", 6.9)).getValueBoolean());
    }
}
