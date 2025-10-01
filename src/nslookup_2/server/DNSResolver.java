package nslookup_2.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import com.google.gson.Gson;
import nslookup_2.shared.DNSResult;

public class DNSResolver {

	private static final Gson gson = new Gson();
	
    /**
     * Resolve a domain or IP into multiple DNS records (A, AAAA, CNAME, MX, NS).
     */
    public static String resolve(String input, ArrayList<String> inputRecordTypes) {
        StringBuilder sb = new StringBuilder();
        boolean success = false;
        String errorMessage = null;

        if (input == null || input.trim().isEmpty()) {
            return gson.toJson(new DNSResult(false, "", "Invalid query"));
        }
        input = input.trim();

        try {
            InetAddress ia = InetAddress.getByName(input);
            sb.append("Canonical: ")
            	.append(ia.getCanonicalHostName())
            	.append("/")
            	.append(ia.getHostAddress())
            	.append("\n");
            success = true;
        } catch (UnknownHostException e) {
        	errorMessage = "Could not resolve basic address for " + input;
        }

     // dnsjava resolver
        try {
            Resolver resolver = new SimpleResolver();

            for (String typeStr : inputRecordTypes) {
                int type;
                switch (typeStr.toUpperCase()) {
                    case "A":
                        type = Type.A;
                        break;
                    case "AAAA":
                        type = Type.AAAA;
                        break;
                    case "CNAME":
                        type = Type.CNAME;
                        break;
                    case "MX":
                        type = Type.MX;
                        break;
                    case "NS":
                        type = Type.NS;
                        break;
                    default:
                        continue; // skip unsupported types
                }

                try {
                    Lookup lookup = new Lookup(input, type);
                    lookup.setResolver(resolver);
                    Record[] records = lookup.run();

                    if (records != null && records.length > 0) {
                        sb.append(Type.string(type)).append(" Records:\n");
                        for (Record record : records) {
                            sb.append("  ").append(formatRecord(record)).append("\n");
                        }
                        sb.append("\n");
                    }
                } catch (Exception e) {
                    // ignore errors for this type, continue with next
                }
            }

        } catch (Exception e) {
            errorMessage = "DNS query error: " + e.getMessage();
        }


        if (!success) {
            return gson.toJson(new DNSResult(false, sb.toString(), errorMessage));
        }
        
        return gson.toJson(new DNSResult(true, sb.toString(), null));
    }
    
    private static String formatRecord(Record record) {
        switch (record.getType()) {
            case Type.A:
                return "IPv4: " + ((ARecord) record).getAddress().getHostAddress();

            case Type.AAAA:
                return "IPv6: " + ((AAAARecord) record).getAddress().getHostAddress();

            case Type.CNAME:
                return "Alias to: " + ((CNAMERecord) record).getTarget();

            case Type.MX:
                MXRecord mx = (MXRecord) record;
                return "Preference " + mx.getPriority() + " -> " + mx.getTarget();

            case Type.NS:
                return "NS: " + ((NSRecord) record).getTarget();

            default:
                return record.toString(); // fallback for unsupported types
        }
    }
}
