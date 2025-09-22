package nslookup_2.client;

import java.io.*;
import java.net.Socket;

public class DNSLookupClient {
    private String serverHost;
    private int serverPort;

    public DNSLookupClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public String lookup(String domain) {
    	return "";
    }
}
