package nslookup_2.server;

import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

public class DNSResolver {
    // cache để lưu kết quả DNS queries (thread-safe)
    private static final ConcurrentHashMap<String, CacheEntry> dnsCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 300000;

    // statistics
    private static final java.util.concurrent.atomic.AtomicLong cacheHits = new java.util.concurrent.atomic.AtomicLong(0);
    private static final java.util.concurrent.atomic.AtomicLong cacheMisses = new java.util.concurrent.atomic.AtomicLong(0);
    private static final java.util.concurrent.atomic.AtomicLong totalQueries = new java.util.concurrent.atomic.AtomicLong(0);

    /**
     * Resolve a domain or IP into multiple DNS records (A, AAAA, CNAME, MX, NS).
     * Thread-safe with caching support.
     */
    public static String resolve(String input) {
        totalQueries.incrementAndGet();
        if (input == null || input.trim().isEmpty()) {
            return "Invalid query";
        }
        input = input.trim().toLowerCase();

        // check cache first
        CacheEntry cached = dnsCache.get(input);
        if (cached != null && !cached.isExpired()) {
            cacheHits.incrementAndGet();
            return cached.getResult();
        }
        cacheMisses.incrementAndGet();
        String result = performDNSResolution(input);

        // Cache the result
        dnsCache.put(input, new CacheEntry(result, System.currentTimeMillis()));

        return result;

    }
    private static String performDNSResolution(String input) {
        StringBuilder sb = new StringBuilder();

        try {
            // Basic IP resolution
            InetAddress ia = InetAddress.getByName(input);
            sb.append("Canonical: ").append(ia.getCanonicalHostName())
                    .append("/").append(ia.getHostAddress()).append("\n");
        } catch (UnknownHostException e) {
            sb.append("Could not resolve basic address for ").append(input).append("\n");
        }

        try {
            // Setup JNDI DNS context (thread-safe)
            java.util.Hashtable<String, String> env = new java.util.Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns:");
            env.put("com.sun.jndi.dns.timeout.initial", "5000");
            env.put("com.sun.jndi.dns.timeout.retries", "2");

            InitialDirContext iDirC = new InitialDirContext(env);

            // Query multiple record types
            String[] recordTypes = {"A", "AAAA", "CNAME", "MX", "NS", "TXT"};

            for (String type : recordTypes) {
                try {
                    Attributes attrs = iDirC.getAttributes("dns:/" + input, new String[]{type});
                    NamingEnumeration<?> attrEnum = attrs.getAll();
                    boolean hasRecords = false;

                    while (attrEnum.hasMore()) {
                        sb.append(type).append(" : ").append(attrEnum.next()).append("\n");
                        hasRecords = true;
                    }

                    if (!hasRecords) {
                        sb.append(type).append(" : No records found\n");
                    }
                } catch (NamingException e) {
                    sb.append(type).append(" : Query failed - ").append(e.getMessage()).append("\n");
                }
            }

            iDirC.close();

        } catch (Exception e) {
            sb.append("DNS query error: ").append(e.getMessage()).append("\n");
        }

        String result = sb.toString();
        return result.isEmpty() ? "No DNS records found" : result;
    }

    //cache entry class
    private static class CacheEntry {
        private final String result;
        private final long timestamp;
        public CacheEntry(String result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
        public String getResult() {
            return result;
        }
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL;
        }
    }
    //cache management methods
    public static void clearCache() {
        dnsCache.clear();
        System.out.println("DNS cache cleared");
    }
    public static void printCacheStats() {
        System.out.println("=== DNS Resolver Statistics ===");
        System.out.println("Total queries: " + totalQueries.get());
        System.out.println("Cache hits: " + cacheHits.get());
        System.out.println("Cache misses: " + cacheMisses.get());
        System.out.println("Cache hit rate: " +
                (totalQueries.get() > 0 ? (cacheHits.get() * 100.0 / totalQueries.get()) : 0) + "%");
        System.out.println("Cached entries: " + dnsCache.size());
        System.out.println("===============================");
    }
    // cleanup expired cache entries
    // Cleanup expired cache entries
    public static void cleanupExpiredCache() {
        int removed = 0;
        for (String key : dnsCache.keySet()) {
            if (dnsCache.get(key).isExpired()) {
                dnsCache.remove(key);
                removed++;
            }
        }
        if (removed > 0) {
            System.out.println("Cleaned up " + removed + " expired cache entries");
        }
    }

}
