package cpen221.mp3.server;

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

public class Task1Tests {
    public static void main (String[] args) throws UnknownHostException, InterruptedException {
        //IMPORTANT start MessageHandler.java first
        String IP = String.valueOf(InetAddress.getLocalHost().getHostAddress());
        //IP = "localhost";
        int sPort = 16777; //IMPORTANT copy the correct port from console after starting MessageHandler.java
        Client testClient = new Client(1, "oof@gmail.com", IP,sPort);
        //Server testServer = new Server(testClient);

        testClient.sendRequest(new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "10"));

        Actuator actuator2 = new Actuator(2, 1,"Switch", true, IP, sPort);
        //Actuator actuator3 = new Actuator(3, 1,"Switch", true, IP, sPort);
        //Actuator actuator4 = new Actuator(4, 1,"Switch", true, IP, sPort);
        //Actuator actuator5 = new Actuator(5, 1,"Switch", true, IP, sPort);
        System.out.println("constructed actuator, sending some events to the server");
        actuator2.sendEvent(new ActuatorEvent(0, 1, 2, "Switch", false));
        //actuator3.sendEvent(new ActuatorEvent(0, 1, 3, "Switch", false));
        //actuator4.sendEvent(new ActuatorEvent(0, 1, 4, "Switch", false));
        //actuator5.sendEvent(new ActuatorEvent(0, 1, 5, "Switch", false));
        System.out.println("sent actuator event to server, waiting for server command to actuator");
        Thread.sleep(1000);

        Filter filter = new Filter("timestamp", DoubleOperator.GREATER_THAN, 0.0);

        testClient.sendRequest(new Request(RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, filter.toString()));

        Thread.sleep(10000);

        testClient.sendRequest(new Request(RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, ""));


        //testServer.sendCommandToActuator(SeverCommandToActuator.SET_STATE, 2);
//        System.out.println("about to construct actuator");
//        Entity se = new Actuator(1, c.getClientId(),"Motor", true, "1.1.1.1",6777);
//        System.out.println("actuator about to send");
//        se.sendEvent(new SensorEvent(0.1, 1, 1, "Temperature", 6.9));
        //assertTrue(true);
    }
    @Test
    public void testActuatorSend() {
        Client c = new Client(1, "oof@gmail.com", "1.1.1.1",6777);
        Server s = new Server(c);
        Entity se = new Sensor(1, c.getClientId(), "Temperature", "1.1.1.1",6777);
//        se.sendEvent(new SensorEvent(0.1, 1, 1, "Temperature", 6.9));
        assertTrue(true);
    }
}

