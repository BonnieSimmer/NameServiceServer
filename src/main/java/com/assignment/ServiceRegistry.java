package com.assignment;

import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {
    private static final ServiceRegistry instance = new ServiceRegistry();
    private final ConcurrentHashMap<String, String> nameToIpMap;
    private final ConcurrentHashMap<String, String> activeIpAddresses;

    private ServiceRegistry() {
        nameToIpMap = new ConcurrentHashMap<>();
        activeIpAddresses = new ConcurrentHashMap<>();
    }

    // To get the singleton instance of ServiceRegistry
    public static ServiceRegistry getInstance() {
        return instance;
    }

    public synchronized void register(String name, String ipAddress) {
        if (!IPValidator.isValid(ipAddress)) {
            throw new IllegalArgumentException("Invalid IP address format: " + ipAddress);
        }
        if (activeIpAddresses.containsKey(ipAddress)) {
            if (!activeIpAddresses.get(ipAddress).equals(name)) {
                throw new IllegalStateException("IP address already registered with a different name: " + ipAddress);
            }
        }
        nameToIpMap.put(name, ipAddress);
        activeIpAddresses.put(ipAddress, name);
    }

    public String resolve(String name) {
        String ip = nameToIpMap.get(name);
        if (ip == null) {
            throw new IllegalStateException("Name not found: " + name);
        }
        return ip;
    }

    public synchronized void deregister(String name) {
        String ipAddress = nameToIpMap.remove(name);
        if (ipAddress != null) {
            activeIpAddresses.remove(ipAddress);
        } else {
            throw new IllegalStateException("Name not found: " + name);
        }
    }
}
