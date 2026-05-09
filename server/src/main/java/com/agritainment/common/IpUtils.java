package com.agritainment.common;

import jakarta.servlet.http.HttpServletRequest;

public final class IpUtils {

    private static final int MAX_IP_LENGTH = 45;

    private IpUtils() {}

    public static String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            String ip = xff.split(",")[0].trim();
            if (isValidIp(ip)) return ip;
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && isValidIp(realIp)) return realIp;
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || ip.length() > MAX_IP_LENGTH) return false;
        if (!ip.matches("[0-9a-fA-F.:]+")) return false;
        if (ip.contains(":")) return isValidIpv6(ip);
        return isValidIpv4(ip);
    }

    private static boolean isValidIpv4(String ip) {
        String[] parts = ip.split("\\.", -1);
        if (parts.length != 4) return false;
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3) return false;
            try {
                int val = Integer.parseInt(part);
                if (val < 0 || val > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidIpv6(String ip) {
        String simplified = ip;
        if (simplified.startsWith("[") && simplified.endsWith("]")) {
            simplified = simplified.substring(1, simplified.length() - 1);
        }
        if (simplified.length() < 2 || simplified.length() > 39) return false;
        String[] parts = simplified.split(":", -1);
        if (parts.length < 3 || parts.length > 8) return false;
        boolean hasDoubleColon = simplified.contains("::");
        if (hasDoubleColon && countOccurrences(simplified, "::") > 1) return false;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (part.length() > 4) return false;
            try {
                Integer.parseInt(part, 16);
            } catch (NumberFormatException e) {
                if (!isValidIpv4(part)) return false;
            }
        }
        return true;
    }

    private static int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
