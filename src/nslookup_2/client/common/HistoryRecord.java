package nslookup_2.client.common;

public class HistoryRecord {
    private final String time;
    private final String query;

    public HistoryRecord(String time, String query) {
        this.time = time;
        this.query = query;
    }

    public String getTime() {
        return time;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "[" + time + "] " + query;  // for display in JList
    }
}
