package cpen221.mp3.server;

import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

enum DoubleOperator {
    EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN_OR_EQUALS
}

enum BooleanOperator {
    EQUALS,
    NOT_EQUALS
}

public class Filter {
    // these two fields represent if the filter is complex
    private boolean complex;
    ComplexFilter complexFilter;
    private boolean actuatorCompatible; // true if filter can sift through actuators
    private boolean sensorCompatible; // true if filter can sift through sensors
    private boolean booleanFilter; // true if filter is a boolean filter
    private boolean booleanValue;
    private BooleanOperator booleanOperator;
    private double doubleValue;
    private DoubleOperator doubleOperator;
    private String field;

    /*
    Abstraction Function:
    Represents a filter which can be used on Events to determine whether or not the fulfill
    certain criteria in their value and timestamp fields.

    Representation Invariant:
    - field is not null
    - complex is true iff complexFilter is not null
     */

    /** Determine the Filter that a string representation of a Filter represents
     *
     * @param serializedFilter the string representation of the filter, is not null
     * @return the Filer that the serializedFilter represents
     */
    public static Filter unserialize(String serializedFilter) {
        String[] serializedFilters = serializedFilter.split(";");
        List<Filter> filters = new ArrayList<>();
        for (String serialized : serializedFilters) {
            if (serialized.isEmpty()) {
                continue;
            }
            String[] fields = serialized.split(":");
            String type = fields[0];
            String op = fields[1];
            String value = fields[2];
            if (type.equals("0")) {
                BooleanOperator operator = BooleanOperator.values()[Integer.parseInt(op)];
                boolean boolVal = Boolean.parseBoolean(value);
                filters.add(new Filter(operator, boolVal));
                continue;
            }
            if (type.equals("1")) {
                DoubleOperator operator = DoubleOperator.values()[Integer.parseInt(op)];
                double doubleVal = Double.parseDouble(value);
                filters.add(new Filter("value", operator, doubleVal));
                continue;
            }
            if (type.equals("2")) {
                DoubleOperator operator = DoubleOperator.values()[Integer.parseInt(op)];
                double doubleVal = Double.parseDouble(value);
                filters.add(new Filter("timestamp", operator, doubleVal));
                continue;
            }
        }
        if (filters.size() == 1) {
            return filters.get(0);
        }
        return new Filter(filters);
    }

    /**
     * Constructs a filter that compares the boolean (actuator) event value
     * to the given boolean value using the given BooleanOperator.
     * (X (BooleanOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A BooleanOperator can be one of the following:
     * 
     * BooleanOperator.EQUALS
     * BooleanOperator.NOT_EQUALS
     *
     * @param operator the BooleanOperator to use to compare the event value with the given value
     * @param value the boolean value to match
     */
    public Filter(BooleanOperator operator, boolean value) {
        this.booleanOperator = operator;
        this.booleanValue = value;
        this.booleanFilter = true;
        this.actuatorCompatible = true;
        this.sensorCompatible = false;
    }

    /**
     * Constructs a filter that compares a double field in events
     * with the given double value using the given DoubleOperator.
     * (X (DoubleOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A DoubleOperator can be one of the following:
     * 
     * DoubleOperator.EQUALS
     * DoubleOperator.GREATER_THAN
     * DoubleOperator.LESS_THAN
     * DoubleOperator.GREATER_THAN_OR_EQUALS
     * DoubleOperator.LESS_THAN_OR_EQUALS
     * 
     * For non-double (boolean) value events, the satisfies method should return false.
     *
     * @param field the field to match (event "value" or event "timestamp")
     * @param operator the DoubleOperator to use to compare the event value with the given value
     * @param value the double value to match
     *
     * @throws IllegalArgumentException if the given field is not "value" or "timestamp"
     */
    public Filter(String field, DoubleOperator operator, double value) {
        if (!field.equals("value") && !field.equals("timestamp")) {
            throw new IllegalArgumentException("The given field is not 'value' or 'timestamp'");
        }
        if (field.equals("timestamp")) {
            this.actuatorCompatible = true;
        }
        this.sensorCompatible = true;
        this.field = field;
        this.doubleOperator = operator;
        this.doubleValue = value;
        this.booleanFilter = false;
    }
    
    /**
     * A filter can be composed of other filters.
     * in this case, the filter should satisfy all the filters in the list.
     * Constructs a complex filter composed of other filters.
     *
     * @param filters the list of filters to use in the composition
     */
    public Filter(List<Filter> filters) {
        this.complexFilter = new ComplexFilter(filters);
        this.complex = true;
    }

    /**
     * Returns true if the given event satisfies the filter criteria.
     *
     * @param event the event to check
     * @return true if the event satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(Event event) {
        if (this.complex) {
            return this.complexFilter.satisfies(event);
        }
        // if the event is an actuator event and the filter is not actuator compatible
        if (event instanceof ActuatorEvent && !this.actuatorCompatible) {
            return false;
        }
        // if the event is a sensor event and the filter is not sensor compatible
        if (event instanceof SensorEvent && !this.sensorCompatible) {
            return false;
        }

        if (this.booleanFilter) {
            if (this.booleanOperator.equals(BooleanOperator.EQUALS)) {
                return event.getValueBoolean() == this.booleanValue;
            } else if (this.booleanOperator.equals(BooleanOperator.NOT_EQUALS)) {
                return event.getValueBoolean() != this.booleanValue;
            }
        }
        else if (this.field.equals("value")) {
            if (this.doubleOperator.equals(DoubleOperator.EQUALS)) {
                return event.getValueDouble() == this.doubleValue;
            } else if (this.doubleOperator.equals(DoubleOperator.GREATER_THAN)) {
                return event.getValueDouble() > this.doubleValue;
            } else if (this.doubleOperator.equals(DoubleOperator.LESS_THAN)) {
                return event.getValueDouble() < this.doubleValue;
            } else if (this.doubleOperator.equals(DoubleOperator.GREATER_THAN_OR_EQUALS)) {
                return event.getValueDouble() >= this.doubleValue;
            } else if (this.doubleOperator.equals(DoubleOperator.LESS_THAN_OR_EQUALS)) {
                return event.getValueDouble() <= this.doubleValue;
            }
        } else if (this.field.equals("timestamp")) {
            if (this.doubleOperator.equals(DoubleOperator.EQUALS)) {
                return event.getTimeStamp() == this.doubleValue;
            } else if (this.doubleOperator.equals(DoubleOperator.GREATER_THAN)) {
                return event.getTimeStamp() > this.doubleValue;
            } else if (this.doubleOperator.equals(DoubleOperator.LESS_THAN)) {
                return event.getTimeStamp() < this.doubleValue;
            } else if (this.doubleOperator.equals(DoubleOperator.GREATER_THAN_OR_EQUALS)) {
                return event.getTimeStamp() >= this.doubleValue;
            } else if (this.doubleOperator.equals(DoubleOperator.LESS_THAN_OR_EQUALS)) {
                return event.getTimeStamp() <= this.doubleValue;
            }
        }

        return false;
    }

    /**
     * Returns true if the given list of events satisfies the filter criteria.
     *
     * @param events the list of events to check
     * @return true if every event in the list satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(List<Event> events) {
        for (Event event : events) {
            if (!this.satisfies(event)) {
                return false;
            }
        }
        // TODO: implement this method
        return true;
    }

    /**
     * Returns a new event if it satisfies the filter criteria.
     * If the given event does not satisfy the filter criteria, then this method should return null.
     *
     * @param event the event to sift
     * @return a new event if it satisfies the filter criteria, null otherwise
     */
    public Event sift(Event event) {
        if (this.satisfies(event)) {
            return event;
        }
        return null;
    }

    /**
     * Returns a list of events that contains only the events in the given list that satisfy the filter criteria.
     * If no events in the given list satisfy the filter criteria, then this method should return an empty list.
     *
     * @param events the list of events to sift
     * @return a list of events that contains only the events in the given list that satisfy the filter criteria
     *        or an empty list if no events in the given list satisfy the filter criteria
     */
    public List<Event> sift(List<Event> events) {
        List<Event> validEvents = new ArrayList<>();
        for (Event event : events) {
            if (this.satisfies(event)) {
                validEvents.add(event);
            }
        }
        return validEvents;
    }

    /**
     * field1
     * 0-boolean
     * 1-value
     * 2-timestamp
     *
     * field2
     * operator's ordinal
     *
     * field3
     * value (either double or boolean)
     * @return serialized
     */
    @Override
    public String toString() {
        if (this.complex) {
            StringBuilder builder = new StringBuilder();
            for (Filter f : complexFilter.getFilters()) {
                builder.append(f.toString());
                builder.append(";");
            }
            return builder.toString();
        }
        String field1 = booleanFilter ? "0" : Objects.equals(field, "value") ? "1" : "2";
        String field2;
        String field3;
        if (booleanOperator == null) {
            field2 = String.valueOf(doubleOperator.ordinal());
            field3 = String.valueOf(doubleValue);
        } else {
            field2 = String.valueOf(booleanOperator.ordinal());
            field3 = String.valueOf(booleanValue);

        }
        return field1 + ":" + field2 + ":" + field3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filter filter = (Filter) o;
        return complex == filter.complex && actuatorCompatible == filter.actuatorCompatible && sensorCompatible == filter.sensorCompatible && booleanFilter == filter.booleanFilter && booleanValue == filter.booleanValue && Double.compare(doubleValue, filter.doubleValue) == 0 && Objects.equals(complexFilter, filter.complexFilter) && booleanOperator == filter.booleanOperator && doubleOperator == filter.doubleOperator && Objects.equals(field, filter.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(complex, complexFilter, actuatorCompatible, sensorCompatible, booleanFilter, booleanValue, booleanOperator, doubleValue, doubleOperator, field);
    }
}
class ComplexFilter {
    private List<Filter> filters;
    /*
    Abstraction Function:
    Represents a Filter which is composed of one or more Filters.

    Representation Invariant:
    - filters is not null
     */

    /** Make a ComplexFilter with a list of filters
     *
     * @param filters the list of filters this ComplexFilter contains, is not null
     */
    public ComplexFilter(List<Filter> filters) {
        this.filters = filters;
    }

    /** Get the list of filters this ComplexFilter contains
     *
     * @return the list of filters this ComplexFilter contains
     */
    public List<Filter> getFilters() {
        return new ArrayList<>(this.filters);
    }

    /** Determine if an event satisfies all the filters in this ComplexFilter
     *
     * @param event the event to be checked, is not null
     * @return true if all the filters in this ComplexFilter are satisfied by the event, false otherwise
     */
    public boolean satisfies(Event event) {
        for (Filter filter : filters) {
            if (!filter.satisfies(event)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexFilter that = (ComplexFilter) o;
        return Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filters);
    }
}
