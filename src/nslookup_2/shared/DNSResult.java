package nslookup_2.shared;

public class DNSResult {
    private boolean success;
    private String output;
    private String error;

    public DNSResult(boolean success, String output, String error) {
        this.success = success;
        this.output = output;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public String getOutput() { return output; }
    public String getError() { return error; }
}
