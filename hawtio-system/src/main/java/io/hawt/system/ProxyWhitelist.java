package io.hawt.system;

import io.hawt.util.Hosts;
import io.hawt.util.Strings;
import io.hawt.web.ProxyDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Whitelist manager for hawtio proxy.
 *
 * TODO: For now this implementation is heavily relying on Fabric v1, and should be rewritten to a more general form.
 */
public class ProxyWhitelist {

    private static final transient Logger LOG = LoggerFactory.getLogger(ProxyWhitelist.class);

    private static final String FABRIC_MBEAN = "io.fabric8:type=Fabric";

    protected CopyOnWriteArraySet<String> whitelist;
    protected MBeanServer mBeanServer;
    protected ObjectName fabricMBean;

    public ProxyWhitelist(String whitelistStr) {
        if (Strings.isBlank(whitelistStr)) {
            whitelist = new CopyOnWriteArraySet<>();
        } else {
            whitelist = new CopyOnWriteArraySet<>(Strings.split(whitelistStr, ","));
        }
        initialiseWhitelist();
        LOG.info("Initial proxy whitelist: {}", whitelist);

        mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            fabricMBean = new ObjectName(FABRIC_MBEAN);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    protected void initialiseWhitelist() {
        Map<String, Set<InetAddress>> localAddresses = Hosts.getNetworkInterfaceAddresses(true);
        for (Set<InetAddress> addresses : localAddresses.values()) {
            for (InetAddress address : addresses) {
                whitelist.add(address.getHostAddress());
                whitelist.add(address.getHostName());
                whitelist.add(address.getCanonicalHostName());
            }
        }
    }

    public boolean isAllowed(ProxyDetails details) {
        if (details.isAllowed(whitelist)) {
            return true;
        }

        // Update whitelist and check again
        LOG.debug("Updating proxy whitelist: {}, {}", whitelist, details);
        if (update()) {
            return details.isAllowed(whitelist);
        } else {
            return false;
        }
    }

    public boolean update() {
        if (!mBeanServer.isRegistered(fabricMBean)) {
            LOG.debug("Whitelist MBean not available");
            return false;
        }

        Set<String> newWhitelist = invokeMBean();
        int previousSize = whitelist.size();
        whitelist.addAll(newWhitelist);
        if (whitelist.size() == previousSize) {
            LOG.debug("No new proxy whitelist to update");
            return false;
        } else {
            LOG.info("Updated proxy whitelist: {}", whitelist);
            return true;
        }
    }

    protected Set<String> invokeMBean() {
        Set<String> list = new HashSet<>();
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> containers = (List<Map<String, Object>>) mBeanServer.invoke(fabricMBean,
                "containers",
                new Object[] { Arrays.asList("localHostname", "localIp", "manualIp", "publicHostname", "publicIp") },
                new String[] { List.class.getName() });
            LOG.debug("Returned containers from MBean: {}", containers);
            for (Map<String, Object> container : containers) {
                for (Object value : container.values()) {
                    if (value != null && Strings.isNotBlank(value.toString())) {
                        list.add(value.toString());
                    }
                }
            }
            LOG.debug("Extracted whitelist: {}", list);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
            LOG.error("Invocation to whitelist MBean failed: " + e.getMessage(), e);
        }
        return list;
    }
}
