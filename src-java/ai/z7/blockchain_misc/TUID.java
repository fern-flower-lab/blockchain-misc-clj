package ai.z7.blockchain_misc;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class TUID {
    private static long startEpochTime() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.YEAR, 1582);
        c.set(Calendar.MONTH, 9);
        c.set(Calendar.DATE, 15);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static final long START_EPOCH = startEpochTime();

    private static long makeMSB(long timestamp) {
        long msb = 0L;
        msb |= (4294967295L & timestamp) << 32;
        msb |= (281470681743360L & timestamp) >>> 16;
        msb |= (1152640029630136320L & timestamp) >>> 48;
        msb |= 4096L;
        return msb;
    }

    public static long toTimestamp(UUID uuid) {
        if (uuid == null || uuid.version() != 1) {
            throw new IllegalArgumentException("Can only retrieve the unix timestamp for v1-like UUIDs");
        } else {
            long timestamp = uuid.timestamp();
            return timestamp / 10000L + START_EPOCH;
        }
    }

    static long fromTimestamp(long tstamp) {
        return (tstamp - START_EPOCH) * 10000L;
    }

    private static long millisOf(long timestamp) {
        return timestamp / 10000L;
    }

    private static final AtomicLong lastTimestamp = new AtomicLong(0L);

    private static long getCurrentTimestamp() {
        while (true) {
            long now = fromTimestamp(System.currentTimeMillis());
            long last = lastTimestamp.get();
            if (now > last) {
                if (lastTimestamp.compareAndSet(last, now)) {
                    return now;
                }
            } else {
                long lastMillis = millisOf(last);
                if (millisOf(now) < millisOf(last)) {
                    return lastTimestamp.incrementAndGet();
                }

                long candidate = last + 1L;
                if (millisOf(candidate) == lastMillis && lastTimestamp.compareAndSet(last, candidate)) {
                    return candidate;
                }
            }
        }
    }

    private static Set<String> getAllLocalAddresses() {
        Set<String> allIps = new HashSet<>();

        try {
            InetAddress localhost = InetAddress.getLocalHost();
            allIps.add(localhost.toString());
            allIps.add(localhost.getCanonicalHostName());
            InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getCanonicalHostName());
            if (allMyIps != null) {
                for (InetAddress allMyIp : allMyIps) {
                    allIps.add(allMyIp.toString());
                }
            }
        } catch (UnknownHostException ignored) {
        }

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                Enumeration<InetAddress> enumIpAddr = en.nextElement().getInetAddresses();

                while (enumIpAddr.hasMoreElements()) {
                    allIps.add(enumIpAddr.nextElement().toString());
                }
            }
        } catch (SocketException ignored) {
        }

        return allIps;
    }

    private static void update(MessageDigest digest, String value) {
        if (value != null) {
            digest.update(value.getBytes(StandardCharsets.UTF_8));
        }

    }

    private static String getProcessID() {
        Integer pid = null;

        try {
            String pidJmx = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            pid = Integer.parseInt(pidJmx);
        } catch (Exception ignored) {
        }

        if (pid == null) {
            pid = (new Random()).nextInt();
        }

        ClassLoader loader = TUID.class.getClassLoader();
        int loaderId = loader != null ? System.identityHashCode(loader) : 0;
        return Integer.toHexString(pid) + Integer.toHexString(loaderId);
    }

    private static long makeNode() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            for (String address : getAllLocalAddresses()) {
                update(digest, address);
            }

            Properties props = System.getProperties();
            update(digest, props.getProperty("java.vendor"));
            update(digest, props.getProperty("java.vendor.url"));
            update(digest, props.getProperty("java.version"));
            update(digest, props.getProperty("os.arch"));
            update(digest, props.getProperty("os.name"));
            update(digest, props.getProperty("os.version"));
            update(digest, getProcessID());
            byte[] hash = digest.digest();
            long node = 0L;

            for (int i = 0; i < 6; ++i) {
                node |= (255L & (long) hash[i]) << i * 8;
            }

            return node | 1099511627776L;
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final long NODE_CURRENT = makeNode();

    private static long makeClockSeqAndNode() {
        long clock = System.nanoTime();
        long lsb = 0L;
        lsb |= (clock & 16383L) << 48;
        lsb |= Long.MIN_VALUE;
        lsb |= NODE_CURRENT;
        return lsb;
    }

    public static UUID timeUID() {
        return new UUID(makeMSB(getCurrentTimestamp()), makeClockSeqAndNode());
    }

    public static class TUIDComparator implements Comparator<UUID> {

        @Override
        public int compare(UUID o1, UUID o2) {
            if (o1.version() == 1 && o2.version() == 1) {
                long ts1 = o1.timestamp();
                long ts2 = o2.timestamp();
                if (ts1 == ts2) {
                    long seq1 = o1.getLeastSignificantBits();
                    long seq2 = o2.getLeastSignificantBits();
                    if (seq1 == seq2) return 0;
                    else return seq1 < seq2 ? -1 : 1;
                } else {
                    return ts1 < ts2 ? -1 : 1;
                }
            } else return o1.compareTo(o2);
        }
    }
}
