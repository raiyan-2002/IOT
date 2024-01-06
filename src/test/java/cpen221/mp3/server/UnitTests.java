package cpen221.mp3.server;

import cpen221.mp3.CSVEventReader;
import cpen221.mp3.client.Client;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class UnitTests{

    String csvFilePath = "data/tests/single_client_1000_events_out-of-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();
    Client client = new Client(0, "test@test.com", "1.1.1.1", 1);
    Actuator actuator1 = new Actuator(97, 0, "Switch", true);

    @Test
    public void filteringTimeStamps() {
        Event event1 = new SensorEvent(0.00011, 0,
                1,"TempSensor", 1.0);
        Event event2 = new ActuatorEvent(0.33080, 0,
                97,"Switch", false);
        Filter lessTOEFilter = new Filter("timestamp", DoubleOperator.LESS_THAN_OR_EQUALS, 0.33080);
        assertTrue(lessTOEFilter.satisfies(event1));
        assertTrue(lessTOEFilter.satisfies(event2));

        Filter lessFilter = new Filter("timestamp", DoubleOperator.LESS_THAN, 0.33080);
        assertTrue(lessFilter.satisfies(event1));
        assertFalse(lessFilter.satisfies(event2));

        Filter eqFilter = new Filter("timestamp", DoubleOperator.EQUALS, 0.33080);
        assertFalse(eqFilter.satisfies(event1));
        assertTrue(eqFilter.satisfies(event2));

        Filter greaterFilter = new Filter("timestamp", DoubleOperator.GREATER_THAN, 0.2);
        assertFalse(greaterFilter.satisfies(event1));
        assertTrue(greaterFilter.satisfies(event2));

        Filter greaterTOEFilter = new Filter("timestamp", DoubleOperator.GREATER_THAN_OR_EQUALS, 0.33080);
        assertFalse(greaterTOEFilter.satisfies(event1));
        assertTrue(greaterTOEFilter.satisfies(event2));
    }

    @Test
    public void filterValues() {
        Event event1 = new ActuatorEvent(0.15, 0, 19, "Switch", false);
        Event event2 = new SensorEvent(0.20, 0, 1, "TempSensor", 15.5);
        Event event3 = new SensorEvent(0.20, 0, 1, "CO2Sensor", 30);

        Filter lessTOEFilter = new Filter("value", DoubleOperator.LESS_THAN_OR_EQUALS, 15.5);
        assertFalse(lessTOEFilter.satisfies(event1));
        assertTrue(lessTOEFilter.satisfies(event2));
        assertFalse(lessTOEFilter.satisfies(event3));

        Filter lessFilter = new Filter("value", DoubleOperator.LESS_THAN, 15.6);
        assertFalse(lessFilter.satisfies(event1));
        assertTrue(lessFilter.satisfies(event2));
        assertFalse(lessFilter.satisfies(event3));

        Filter eqFilter = new Filter("value", DoubleOperator.EQUALS, 15.5);
        assertFalse(eqFilter.satisfies(event1));
        assertTrue(eqFilter.satisfies(event2));
        assertFalse(eqFilter.satisfies(event3));

        Filter greaterFilter = new Filter("value", DoubleOperator.GREATER_THAN, 15.5);
        assertFalse(greaterFilter.satisfies(event1));
        assertFalse(greaterFilter.satisfies(event2));
        assertTrue(greaterFilter.satisfies(event3));

        Filter greaterTOEFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 30);
        assertFalse(greaterTOEFilter.satisfies(event1));
        assertFalse(greaterTOEFilter.satisfies(event2));
        assertTrue(greaterTOEFilter.satisfies(event3));
    }

    @Test
    public void testBooleans() {
        Event event1 = new ActuatorEvent(0.15, 0, 19, "Switch", false);
        Event event2 = new ActuatorEvent(0.18, 0, 25, "Switch", true);
        Event event3 = new SensorEvent(0.20, 0, 1, "TempSensor", 15.5);

        Filter eqFilterFalse = new Filter(BooleanOperator.EQUALS, false);
        assertTrue(eqFilterFalse.satisfies(event1));
        assertFalse(eqFilterFalse.satisfies(event2));
        assertFalse(eqFilterFalse.satisfies(event3));

        Filter eqFilterTrue = new Filter(BooleanOperator.EQUALS, true);
        assertFalse(eqFilterTrue.satisfies(event1));
        assertTrue(eqFilterTrue.satisfies(event2));
        assertFalse(eqFilterTrue.satisfies(event3));

        Filter neqFilterFalse = new Filter(BooleanOperator.NOT_EQUALS, false);
        assertFalse(neqFilterFalse.satisfies(event1));
        assertTrue(neqFilterFalse.satisfies(event2));
        assertFalse(neqFilterFalse.satisfies(event3));

        Filter neqFilterTrue = new Filter(BooleanOperator.NOT_EQUALS, true);
        assertTrue(neqFilterTrue.satisfies(event1));
        assertFalse(neqFilterTrue.satisfies(event2));
        assertFalse(neqFilterTrue.satisfies(event3));

    }

    @Test
    public void complexFilterTests() {
        Event event1 = new ActuatorEvent(0.15, 0, 19, "Switch", false);
        Event event2 = new ActuatorEvent(0.18, 0, 25, "Switch", true);
        Event event3 = new SensorEvent(0.20, 0, 10, "TempSensor", 15.5);
        Event event4 = new SensorEvent(0.25, 0, 15, "CO2Sensor", 30);

        Filter lessTOEFilter = new Filter("timestamp", DoubleOperator.LESS_THAN_OR_EQUALS, 0.18);
        Filter eqFilterFalse = new Filter(BooleanOperator.EQUALS, false);
        List<Filter> filterList1 = new ArrayList<>();
        filterList1.add(lessTOEFilter);
        filterList1.add(eqFilterFalse);
        Filter complexFilter1 = new Filter(filterList1);
        assertTrue(complexFilter1.satisfies(event1));
        assertFalse(complexFilter1.satisfies(event2));
        assertFalse(complexFilter1.satisfies(event3));
        assertFalse(complexFilter1.satisfies(event4));

        Filter greaterFilter = new Filter("timestamp", DoubleOperator.GREATER_THAN, 0.18);
        Filter eqFilterTrue = new Filter(BooleanOperator.EQUALS, true);
        List<Filter> filterList2 = new ArrayList<>();
        filterList2.add(greaterFilter);
        filterList2.add(eqFilterTrue);
        Filter complexFilter2 = new Filter(filterList2);
        assertFalse(complexFilter2.satisfies(event1));
        assertFalse(complexFilter2.satisfies(event2));
        assertFalse(complexFilter2.satisfies(event3));
        assertFalse(complexFilter2.satisfies(event4));

        Filter greaterTOEFilter = new Filter("timestamp", DoubleOperator.GREATER_THAN_OR_EQUALS, 0.18);
        List<Filter> filterList3 = new ArrayList<>();
        filterList3.add(greaterTOEFilter);
        filterList3.add(eqFilterTrue);
        Filter complexFilter3 = new Filter(filterList3);
        assertFalse(complexFilter3.satisfies(event1));
        assertTrue(complexFilter3.satisfies(event2));
        assertFalse(complexFilter3.satisfies(event3));
        assertFalse(complexFilter3.satisfies(event4));

        Filter greaterTOEFilterValue = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 15.5);
        Filter lessTOEFilterTS = new Filter("timestamp", DoubleOperator.LESS_THAN_OR_EQUALS, 0.25);
        List<Filter> filterList4 = new ArrayList<>();
        filterList4.add(greaterTOEFilterValue);
        filterList4.add(lessTOEFilterTS);
        Filter complexFilter4 = new Filter(filterList4);
        assertFalse(complexFilter4.satisfies(event1));
        assertFalse(complexFilter4.satisfies(event2));
        assertTrue(complexFilter4.satisfies(event3));
        assertTrue(complexFilter4.satisfies(event4));
    }

    @Test
    public void siftTests() {
        Event event1 = new SensorEvent(0.1, 0, 1,"TempSensor", 24);
        Event event2 = new ActuatorEvent(0.25, 0, 97,"Switch", false);
        Event event3 = new SensorEvent(0.2, 0, 1,"TempSensor", 20);
        Event event4 = new ActuatorEvent(0.35, 0, 97,"Switch", true);
        List<Event> allEvents = new ArrayList<>();
        allEvents.add(event1);
        allEvents.add(event2);
        allEvents.add(event3);
        allEvents.add(event4);

        Filter timeStampFilter = new Filter("timestamp", DoubleOperator.GREATER_THAN, 0.15);
        assertNull(timeStampFilter.sift(event1));
        assertEquals(event2, timeStampFilter.sift(event2));
        assertEquals(event3, timeStampFilter.sift(event3));
        assertEquals(event4, timeStampFilter.sift(event4));
        List<Event> list1 = new ArrayList<>();
        list1.add(event2);
        list1.add(event3);
        list1.add(event4);
        assertEquals(list1, timeStampFilter.sift(allEvents));


        Filter valueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 24);
        assertEquals(event1, valueFilter.sift(event1));
        assertNull(valueFilter.sift(event2));
        assertNull(valueFilter.sift(event3));
        assertNull(valueFilter.sift(event4));
        List<Event> list2 = new ArrayList<>();
        list2.add(event1);
        assertEquals(list2, valueFilter.sift(allEvents));

        Filter booleanFilter1 = new Filter(BooleanOperator.EQUALS, false);
        assertNull(booleanFilter1.sift(event1));
        assertEquals(event2, booleanFilter1.sift(event2));
        assertNull(booleanFilter1.sift(event3));
        assertNull(booleanFilter1.sift(event4));
        List<Event> list3 = new ArrayList<>();
        list3.add(event2);
        assertEquals(list3, booleanFilter1.sift(allEvents));

        Filter noPass = new Filter("timestamp", DoubleOperator.GREATER_THAN, 0.35);
        assertNull(noPass.sift(event1));
        assertNull(noPass.sift(event2));
        assertNull(noPass.sift(event3));
        assertNull(noPass.sift(event4));
        assertEquals(new ArrayList<>(), noPass.sift(allEvents));
    }

    @Test
    public void testLog() {
        Server server = new Server(client);
        Filter filter1 = new Filter("timestamp", DoubleOperator.GREATER_THAN, 0.15);
        Filter filter2 = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 1000);
        Filter complex = new Filter(new ArrayList<>(Arrays.asList(filter1, filter2)));
        server.logIf(complex);
        for (int i = 0; i < 11; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        List<Integer> expected = new ArrayList<>();
        // because we expect processing in chronological order
        expected.add(eventList.get(4).getEntityId());
        expected.add(eventList.get(7).getEntityId());
        expected.add(eventList.get(10).getEntityId());
        expected.add(eventList.get(9).getEntityId());
        assertEquals(expected, server.readLogs());
        assertEquals(new ArrayList<>(), server.readLogs());
    }

    @Test
    public void testMostActiveEntity1() {
        Server server = new Server(client);
        for (int i = 0; i < 9; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        assertEquals(164, server.mostActiveEntity());
    }

    @Test
    public void testMostActiveEntity2() {
        Server server = new Server(client);
        Event e1 = new SensorEvent(0.1, 0, 1,"TempSensor", 24);
        Event e2 = new ActuatorEvent(0.25, 0, 97,"Switch", false);
        Event e3 = new SensorEvent(0.2, 0, 1,"TempSensor", 20);
        Event e4 = new ActuatorEvent(0.3, 0, 97,"Switch", false);
        Event e5 = new ActuatorEvent(0.35, 0, 97,"Switch", false);
        Event e6 = new ActuatorEvent(0.4, 0, 97,"Switch", false);

        server.processIncomingEvent(e1);
        server.processIncomingEvent(e2);
        server.processIncomingEvent(e3);
        server.processIncomingEvent(e4);
        server.processIncomingEvent(e5);
        server.processIncomingEvent(e6);
        assertEquals(97, server.mostActiveEntity());
    }

    @Test
    public void testAllEntities() {
        Server server = new Server(client);
        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
            expected.add(eventList.get(i).getEntityId());
        }
        Set<Integer> actual = new HashSet<>(server.getAllEntities());
        assertEquals(new HashSet<>(expected), actual);
    }

    @Test
    public void testWindow() {
        Server server = new Server(client);
        TimeWindow tw = new TimeWindow(0.22787690162658691, 0.8107128143310547);
        Set<Event> expected = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
            if (eventList.get(i).getTimeStamp() >= tw.getStartTime() && eventList.get(i).getTimeStamp() <= tw.getEndTime()) {
                expected.add(eventList.get(i));
            }
        }
        Set<Event> actual = new HashSet<>(server.eventsInTimeWindow(tw));
        assertEquals(expected, actual);
    }

    @Test
    public void recentEvents() {
        Server server = new Server(client);
        List<Integer> expected = new ArrayList<>(List.of(9, 11));
        Set<Event> expectedEvents = new HashSet<>();

        for (int i = 0; i < 12; i++) {
            if (expected.contains(i)) {
                expectedEvents.add(eventList.get(i));
            }
            server.processIncomingEvent(eventList.get(i));
        }
        assertEquals(expectedEvents, new HashSet<>(server.lastNEvents(2)));
    }

    @Test
    public void testSetStateIf() {
        Server server = new Server(client);
        for (int i = 0; i < 11; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        Filter filter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 1000);
        server.setActuatorStateIf(filter, actuator1);
        assertTrue(actuator1.getState());
    }

    @Test
    public void testToggleStateIf() {
        Server server = new Server(client);
        for (int i = 0; i < 11; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        Filter filter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 1000);
        server.toggleActuatorStateIf(filter, actuator1);
        assertTrue(actuator1.getState());
    }


}

