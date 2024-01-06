package cpen221.mp3.server;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.net.*;
import java.io.*;
import cpen221.mp3.client.*;
import cpen221.mp3.entity.*;
import cpen221.mp3.event.*;
import cpen221.mp3.handler.*;
import cpen221.mp3.server.*;

import static org.junit.jupiter.api.Assertions.*;
public class DeploymentTests {
    int port = 16777;
    static MessageHandler testServer;
    @BeforeAll
    public static void startServer() throws UnknownHostException {
        int port = -1;
        testServer = new MessageHandler(port);
        testServer.start();
    }

    @Test
    public void testEventsOutOfOrder() throws UnknownHostException, InterruptedException {
        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));

        Actuator actuator2 = new Actuator(2, 1,"Switch", true);
        Actuator actuator3 = new Actuator(3, 2,"Switch", true);
        actuator2.setEventGenerationFrequency(0.000000001);
        actuator2.setEndpoint(IP, port);
        actuator3.setEventGenerationFrequency(0.000000001);
        actuator3.setEndpoint(IP, port);
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(0.0, 1, 2, "Switch", false));
        actuator2.sendEvent(new ActuatorEvent(0.2, 1, 2, "Switch", false));
        Thread.sleep(500);
        testClient.sendRequest(new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_LATEST_EVENTS, "10"));
        actuator2.sendEvent(new ActuatorEvent(0.1, 1, 2, "Switch", true));
        actuator3.sendEvent(new ActuatorEvent(0.1, 10, 3, "Switch", true));
        Thread.sleep(1000);
    }
    @Test
    public void testChangeMaxWaitTime() throws UnknownHostException, InterruptedException {

        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        Actuator actuator2 = new Actuator(2, 1,"Switch", true);
        Actuator actuator3 = new Actuator(3, 2,"Switch", true);
        actuator2.setEventGenerationFrequency(0.000000001);
        actuator2.setEndpoint(IP, port);
        actuator3.setEventGenerationFrequency(0.000000001);
        actuator3.setEndpoint(IP, port);
        Thread.sleep(1000);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", false));
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", false));
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", false));
        actuator3.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 2, 3, "Switch", false));
        Thread.sleep(1000);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "1"));
        Thread.sleep(1000);
    }
    @Test
    public void testChangeLogIfAndReadLogs() throws UnknownHostException, InterruptedException {
        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));

        Actuator actuator2 = new Actuator(2, 1,"Switch", true);
        actuator2.setEventGenerationFrequency(0.000000001);
        actuator2.setEndpoint(IP, port);
        Thread.sleep(1000);
        testClient.sendRequest(new Request(RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, (new Filter("timestamp", DoubleOperator.GREATER_THAN, 0.0)).toString()));
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", false));
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", false));
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", false));
        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, ""));

        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, (new Filter("timestamp", DoubleOperator.LESS_THAN, 0.0)).toString()));
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", true));
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", true));
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", true));

        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, ""));
        Thread.sleep(1000);
    }
    @Test
    public void testToggleActuatorState() throws UnknownHostException, InterruptedException {

        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));

        Actuator actuator2 = new Actuator(2, 1,"Switch", true);
        actuator2.setEventGenerationFrequency(0.000000001);
        actuator2.setEndpoint(IP, port);
        Thread.sleep(1000);
        testClient.sendRequest(new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, "2<>"+(new Filter(BooleanOperator.EQUALS, true)).toString()));
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", true));
        //actuator2 should be updated to false
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", false));
        //actuator2 should not change
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", true));
        //actuator2 should be updated to false
        Thread.sleep(100);
    }
    @Test
    public void testSetActuatorState() throws UnknownHostException, InterruptedException {

        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));

        Actuator actuator2 = new Actuator(2, 1,"Switch", false);
        actuator2.setEventGenerationFrequency(0.000000001);
        actuator2.setEndpoint(IP, port);
        Thread.sleep(1000);
        testClient.sendRequest(new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, "2<>"+(new Filter(BooleanOperator.EQUALS, true)).toString()));
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", false));
        //actuator2 should be not change
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", false));
        //actuator2 should be not change
        Thread.sleep(100);
        actuator2.sendEvent(new ActuatorEvent(System.currentTimeMillis(), 1, 2, "Switch", true));
        //actuator2 should be updated to true
        Thread.sleep(100);
    }
    @Test
    public void testGetAllEvents() throws UnknownHostException, InterruptedException {

        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));

        Sensor sensor2 = new Sensor(2, 1,"Temperature");
        sensor2.setEventGenerationFrequency(0.000000001);
        sensor2.setEndpoint(IP, port);
        Thread.sleep(1000);
        sensor2.sendEvent(new SensorEvent(System.currentTimeMillis(), 1, 2, "Temperature", 1000.0));
        sensor2.sendEvent(new SensorEvent(System.currentTimeMillis(), 1, 2, "Temperature", 10000.0));
        sensor2.sendEvent(new SensorEvent(System.currentTimeMillis(), 1, 2, "Temperature", 10000000.0));
        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_LATEST_EVENTS, "1000"));
        Thread.sleep(1000);
        //client should be receiving the result, which is displayed in console

    }
    @Test
    public void testGetLastNEvents() throws UnknownHostException, InterruptedException {

        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));

        Sensor sensor2 = new Sensor(2, 1,"Temperature");
        Sensor sensor3 = new Sensor(3, 1,"Temperature");
        sensor2.setEventGenerationFrequency(0.000000001);
        sensor2.setEndpoint(IP, port);
        sensor3.setEventGenerationFrequency(0.000000001);
        sensor3.setEndpoint(IP, port);
        Thread.sleep(1000);
        sensor2.sendEvent(new SensorEvent(0, 1, 2, "Temperature", 1000.0));
        sensor2.sendEvent(new SensorEvent(10, 1, 2, "Temperature", 10000.0));
        sensor2.sendEvent(new SensorEvent(100, 1, 2, "Temperature", 10000000.0));
        sensor3.sendEvent(new SensorEvent(10, 1, 3, "Temperature", 99.999));
        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_LATEST_EVENTS, "4"));
        Thread.sleep(1000);
        //client should be receiving the result, which is displayed in console

    }
    @Test
    public void testPredictTimeStamps() throws UnknownHostException, InterruptedException {
        Client testClient = new Client(1, "amongus@among.us", InetAddress.getLocalHost().getHostAddress(), testServer.getPort());
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));
        Thread.sleep(1000);
        Server server = testServer.serverMap.get(1);
        server.processIncomingEvent(new SensorEvent(0, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(1, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(2, 1, 2, "Temperature", 1000.0));
        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_TIMESTAMPS, "2<>5"));
        Thread.sleep(1000);
    }
    @Test
    public void testPredictSensorValConst() throws UnknownHostException, InterruptedException {
        Client testClient = new Client(1, "amongus@among.us", InetAddress.getLocalHost().getHostAddress(), testServer.getPort());
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));
        Thread.sleep(1000);
        Server server = testServer.serverMap.get(1);
        server.processIncomingEvent(new SensorEvent(0, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(1, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(2, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(3, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(4, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(5, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(6, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(7, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(8, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(9, 1, 2, "Temperature", 1000.0));
        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_VALUES, "2<>5"));
        Thread.sleep(1000);
    }
    @Test
    public void testPredictSensorValAlternate() throws UnknownHostException, InterruptedException {
        Client testClient = new Client(1, "amongus@among.us", InetAddress.getLocalHost().getHostAddress(), testServer.getPort());
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));
        Thread.sleep(1000);
        Server server = testServer.serverMap.get(1);
        server.processIncomingEvent(new SensorEvent(0, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(1, 1, 2, "Temperature", 2000.0));
        server.processIncomingEvent(new SensorEvent(2, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(3, 1, 2, "Temperature", 2000.0));
        server.processIncomingEvent(new SensorEvent(4, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(5, 1, 2, "Temperature", 2000.0));
        server.processIncomingEvent(new SensorEvent(6, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(7, 1, 2, "Temperature", 2000.0));
        server.processIncomingEvent(new SensorEvent(8, 1, 2, "Temperature", 1000.0));
        server.processIncomingEvent(new SensorEvent(9, 1, 2, "Temperature", 2000.0));

        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_VALUES, "2<>5"));
        Thread.sleep(1000);
    }
    @Test
    public void testPredictActuatorValConst() throws UnknownHostException, InterruptedException {
        Client testClient = new Client(1, "amongus@among.us", InetAddress.getLocalHost().getHostAddress(), testServer.getPort());
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));
        Thread.sleep(1000);
        Server server = testServer.serverMap.get(1);
        server.processIncomingEvent(new ActuatorEvent(0, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(1, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(2, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(3, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(4, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(5, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(6, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(7, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(8, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(9, 1, 2, "Switch", false));
        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_VALUES, "2<>5"));
        Thread.sleep(1000);
    }
    @Test
    public void testPredictActuatorValAlternate() throws UnknownHostException, InterruptedException {
        Client testClient = new Client(1, "amongus@among.us", InetAddress.getLocalHost().getHostAddress(), testServer.getPort());
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));
        Thread.sleep(1000);
        Server server = testServer.serverMap.get(1);
        server.processIncomingEvent(new ActuatorEvent(0, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(1, 1, 2, "Switch", true));
        server.processIncomingEvent(new ActuatorEvent(2, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(3, 1, 2, "Switch", true));
        server.processIncomingEvent(new ActuatorEvent(4, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(5, 1, 2, "Switch", true));
        server.processIncomingEvent(new ActuatorEvent(6, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(7, 1, 2, "Switch", true));
        server.processIncomingEvent(new ActuatorEvent(8, 1, 2, "Switch", false));
        server.processIncomingEvent(new ActuatorEvent(9, 1, 2, "Switch", true));
        Thread.sleep(100);
        testClient.sendRequest(new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_VALUES, "2<>5"));
        Thread.sleep(1000);
    }
    @Test
    public void testGetAllEntities() throws UnknownHostException, InterruptedException {

        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));

        Sensor sensor2 = new Sensor(2, 1,"Temperature");
        Actuator actuator3 = new Actuator(3, 1,"Switch",true);
        Actuator actuator4 = new Actuator(4, 1,"Switch",true);
        Sensor sensor5 = new Sensor(5, 1,"Temperature");
        sensor2.setEventGenerationFrequency(0.000000001);
        sensor2.setEndpoint(IP, port);
        actuator3.setEventGenerationFrequency(0.000000001);
        actuator3.setEndpoint(IP, port);
        actuator4.setEventGenerationFrequency(0.000000001);
        actuator4.setEndpoint(IP, port);
        sensor5.setEventGenerationFrequency(0.000000001);
        sensor5.setEndpoint(IP, port);

        Thread.sleep(1000);
        testClient.sendRequest(new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, ""));
        Thread.sleep(1000);
        //client should be receiving the result, which is displayed in console

    }
    @Test
    public void testGetMostActive() throws UnknownHostException, InterruptedException {

        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));

        Sensor sensor2 = new Sensor(2, 1,"Temperature");
        Actuator actuator3 = new Actuator(3, 1,"Switch",true);
        Actuator actuator4 = new Actuator(4, 1,"Switch",true);
        Sensor sensor5 = new Sensor(5, 1,"Temperature");
        sensor2.setEventGenerationFrequency(0.000000001);
        sensor2.setEndpoint(IP, port);
        actuator3.setEventGenerationFrequency(2.0);
        actuator3.setEndpoint(IP, port);
        actuator4.setEventGenerationFrequency(0.000000001);
        actuator4.setEndpoint(IP, port);
        sensor5.setEventGenerationFrequency(0.000000001);
        sensor5.setEndpoint(IP, port);

        Thread.sleep(1000);

        testClient.sendRequest(new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_MOST_ACTIVE_ENTITY, ""));
        Thread.sleep(1000);
        //client should be receiving the result, which is displayed in console

    }
    @Test
    public void testGetEventsInTimeWindow() throws UnknownHostException, InterruptedException {

        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Client testClient = new Client(1, "amongus@among.us", IP, port);
        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0"));

        Sensor sensor2 = new Sensor(2, 1,"Temperature");
        Actuator actuator3 = new Actuator(3, 1,"Switch",true);
        Actuator actuator4 = new Actuator(4, 1,"Switch",true);
        Sensor sensor5 = new Sensor(5, 1,"Temperature");
        sensor2.setEventGenerationFrequency(0.000000001);
        sensor2.setEndpoint(IP, port);
        actuator3.setEventGenerationFrequency(0.000000001);
        actuator3.setEndpoint(IP, port);
        actuator4.setEventGenerationFrequency(0.000000001);
        actuator4.setEndpoint(IP, port);
        sensor5.setEventGenerationFrequency(0.000000001);
        sensor5.setEndpoint(IP, port);
        Thread.sleep(1000);

        sensor2.sendEvent(new SensorEvent(9, 1, 2, "Temperature", 10));
        sensor2.sendEvent(new SensorEvent(10, 1, 2, "Temperature", 20));
        sensor5.sendEvent(new SensorEvent(21, 1, 2, "Temperature", 40));
        sensor5.sendEvent(new SensorEvent(20, 1, 2, "Temperature", 37));
        actuator3.sendEvent(new ActuatorEvent(1, 1, 2, "Switch", false));
        actuator3.sendEvent(new ActuatorEvent(11, 1, 2, "Switch", true));
        actuator4.sendEvent(new ActuatorEvent(16, 1, 2, "Switch", true));
        actuator4.sendEvent(new ActuatorEvent(27, 1, 2, "Switch", false));

        Thread.sleep(500);
        testClient.sendRequest(new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW, (new TimeWindow(10, 20)).toString()));
        Thread.sleep(1000);
        //client should be receiving the result, which is displayed in console

    }

    @Test
    public void testCoverage() throws UnknownHostException, InterruptedException {
        String IP = InetAddress.getLocalHost().getHostAddress();
        int port = testServer.getPort(); //please set the port to the one printed by MessageHandler
        Actuator actuator2 = new Actuator(2,1,"Switch", true, IP, port);
        Actuator actuator3 = new Actuator(3,"Switch", true, IP, port);
        Sensor sensor4 = new Sensor(4,1,"TempSensor", IP, port);
        Sensor sensor5 = new Sensor(5,"PressureSensor", IP, port);
        actuator2.generateEvents();
        sensor4.generateEvents();
    }
}
