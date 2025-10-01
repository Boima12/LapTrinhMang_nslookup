package nslookup_2.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import com.google.gson.Gson;
import nslookup_2.shared.DNSResult;

public class DNSResolver {

	private static final Gson gson = new Gson();
	
    /**
     * Resolve a domain or IP into multiple DNS records (A, AAAA, CNAME, MX, NS).
     */
    public static String resolve(String input) {
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

        // dnsjava resolver instead of JDNI
        try {
            Resolver resolver = new SimpleResolver();

            // Record types we want to fetch
            int[] recordTypes = {Type.A, Type.AAAA, Type.CNAME, Type.MX, Type.NS};
            for (int type : recordTypes) {
                try {
                    Lookup lookup = new Lookup(input, type);
                    lookup.setResolver(resolver);
                    Record[] records = lookup.run();

                    if (records != null) {
                        for (Record record : records) {
                            sb.append(record.toString()).append("\n");
                        }
                    }
                } catch (Exception e) {
                    // ignore missing types, continue
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
}
