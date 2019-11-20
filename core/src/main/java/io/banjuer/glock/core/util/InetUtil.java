package io.banjuer.glock.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author guochengsen
 */
public class InetUtil {

    public static String getHostName() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getHostIp() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(getHostName() + "/" + getHostIp());
    }

}
