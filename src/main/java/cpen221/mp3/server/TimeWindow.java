package cpen221.mp3.server;

public class TimeWindow {
    public final double startTime;
    public final double endTime;

    /*
     Abstraction Function:
     Represents a time interval specified by a startTime and endTime.

     Representation Invariant:
     - startTime, endTime > 0
     - endTime > startTime
     */

    /** Make a TimeWindow with a start time and end time
     *
     * @param startTime the start time of the window, > 0
     * @param endTime the end time of the window, > startTime
     */
    public TimeWindow(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** Get the start time of the window
     *
     * @return the start time of the window
     */
    public double getStartTime() {
        return startTime;
    }

    /** Get the end time of the window
     *
     * @return the end time of the window
     */
    public double getEndTime() {
        return endTime;
    }

    /** Get the string representation of the TimeWindow
     *
     * @return the string representation of the TimeWindow
     */
    @Override
    public String toString() {
        return getStartTime() + "<>" + getEndTime();
//        return "TimeWindow{" +
//               "StartTime=" + getStartTime() +
//               ",EndTime=" + getEndTime() +
//               '}';
    }
}
