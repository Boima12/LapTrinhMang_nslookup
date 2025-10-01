package nslookup_2.server;

import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import com.google.gson.Gson;
import nslookup_2.server.common.*;
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

        try {
            // setup JNDI DNS context
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            InitialDirContext iDirC = new InitialDirContext(env);

            // query multiple record types
            String[] recordTypes = {"A", "AAAA", "CNAME", "MX", "NS"};
            for (String type : recordTypes) {
                try {
                    Attributes attrs = iDirC.getAttributes("dns:/" + input, new String[]{type});
                    NamingEnumeration<?> attrEnum = attrs.getAll();
                    while (attrEnum.hasMore()) {
                        sb.append(type).append(" : ").append(attrEnum.next()).append("\n");
                    }
                } catch (NamingException e) {
                    // ignore if that record type doesn't exist
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
