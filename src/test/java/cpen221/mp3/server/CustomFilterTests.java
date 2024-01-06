package cpen221.mp3.server;

import cpen221.mp3.CSVEventReader;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import static cpen221.mp3.server.BooleanOperator.EQUALS;
import static cpen221.mp3.server.BooleanOperator.NOT_EQUALS;
import static cpen221.mp3.server.DoubleOperator.GREATER_THAN_OR_EQUALS;
import static cpen221.mp3.server.DoubleOperator.LESS_THAN;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CustomFilterTests{
    @Test
    public void testCoverage() {
        Event event1 = new SensorEvent(0.00011, 0,
                1,"TempSensor", 1.0);
        Event event2 = new ActuatorEvent(0.33080, 0,
                97,"Switch", false);
        Filter timeStampFilter = new Filter("timestamp", LESS_THAN, 0.0);
        assertNull(timeStampFilter.sift(event1));
        assertNull(timeStampFilter.sift(event2));
    }

    @Test
    public void testSerialization() {
        Filter filter1 = new Filter(BooleanOperator.EQUALS, true);
        Filter filter2 = new Filter(BooleanOperator.NOT_EQUALS, true);
        Filter filter3 = new Filter("timestamp",DoubleOperator.LESS_THAN, 6.9);
        Filter filter4 = new Filter("value",DoubleOperator.LESS_THAN, 1.9);
        Filter filter5 = new Filter("value",DoubleOperator.EQUALS, 5.9);
        Filter filter6 = new Filter("value",DoubleOperator.LESS_THAN_OR_EQUALS, 8.9);
        Filter filter7 = new Filter("value",DoubleOperator.GREATER_THAN, 2.9);
        Filter filter8 = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 9.9);
        Filter filter9 = new Filter(List.of(filter1, filter2, filter3, filter4, filter5, filter6, filter7, filter8));
        Filter filter10 = new Filter(BooleanOperator.EQUALS, false);
        Filter filter11 = new Filter("timestamp",DoubleOperator.LESS_THAN, 6.69);
        Filter filter12 = new Filter("timestamp",DoubleOperator.EQUALS, 6.69);
        Filter filter13 = new Filter("timestamp",DoubleOperator.LESS_THAN_OR_EQUALS, 6.69);
        Filter filter14 = new Filter("timestamp",DoubleOperator.GREATER_THAN, 6.69);
        Filter filter15 = new Filter("timestamp", DoubleOperator.GREATER_THAN_OR_EQUALS, 6.69);

        (new HashMap<>()).put(filter9, filter1);
        (new HashMap<>()).put(filter1, filter9);
        assertNotEquals(filter1, null);
        assertNotEquals(filter1, new HashMap<>());
        assertEquals(filter1, filter1);
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter2, filter3);
        assertNotEquals(filter3, filter11);
        assertNotEquals(filter1, filter10);
        assertNotEquals(filter3, filter4);
        assertNotEquals(filter4, filter5);
        assertEquals(filter1, Filter.unserialize(filter1.toString()));
        assertEquals(filter2, Filter.unserialize(filter2.toString()));
        assertEquals(filter3, Filter.unserialize(filter3.toString()));
        assertEquals(filter4, Filter.unserialize(filter4.toString()));
        assertEquals(filter5, Filter.unserialize(filter5.toString()));
        assertEquals(filter6, Filter.unserialize(filter6.toString()));
        assertEquals(filter7, Filter.unserialize(filter7.toString()));
        assertEquals(filter8, Filter.unserialize(filter8.toString()));
        assertEquals(filter9, Filter.unserialize(filter9.toString()));
        //assertEquals(filter9.complexFilter.getFilters(), List.of(filter1, filter2, filter3, filter4, filter5, filter6, filter7, filter8));
        assertEquals(filter9.complexFilter, filter9.complexFilter);
        assertNotEquals(filter9.complexFilter, null);
        assertNotEquals(filter9.complexFilter, new ArrayList<>());
        filter1.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter2.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter3.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter4.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter5.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter6.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter7.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter8.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter9.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter10.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter11.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter12.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter13.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter14.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));
        filter15.satisfies(new SensorEvent(0, 0, 0, "Temperature", 6.9));

    }

    @Test
    public void testFilterWrongArgument() {
        try {
            Filter filter10 = new Filter("among us", GREATER_THAN_OR_EQUALS, 5.0);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }


}

