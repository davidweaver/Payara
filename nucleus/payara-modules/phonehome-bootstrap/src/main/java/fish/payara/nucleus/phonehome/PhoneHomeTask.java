/*
 * 
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 *  Copyright (c) 2016 C2B2 Consulting Limited and/or its affiliates.
 *  All rights reserved.
 * 
 *  The contents of this file are subject to the terms of the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License.  You can
 *  obtain a copy of the License at
 *  https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 *  or packager/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 * 
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at packager/legal/LICENSE.txt.
 * 
 */
package fish.payara.nucleus.phonehome;

import com.sun.appserv.server.util.Version;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import org.glassfish.api.admin.ServerEnvironment;

/**
 *
 * @author David Weaver
 */
public class PhoneHomeTask implements Runnable {
    
    //private static final String PHONE_HOME_URL = "https://phonehome.payara.fish/test";
    private static final String PHONE_HOME_URL = "https://localhost:8181/echo/echo";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final int CONN_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;
    
    private static final Logger LOGGER = Logger.getLogger(PhoneHomeTask.class.getCanonicalName());
    
    @Inject
    ServerEnvironment env;
    
    @Override
    public void run() {
        LOGGER.info("Phone Home");
        
        String version = getVersion();
        String javaVersion = getJavaVersion();
        String uptime = getUptime();
        
        Map<String,String> params = new HashMap<>();
        params.put("ver", version);
        params.put("jvm", javaVersion);
        params.put("uptime", uptime);
        
        String targetURL = PHONE_HOME_URL + encodeParams(params);
        send(targetURL);
    }
    
    private String getVersion() {
        return Version.getFullVersion();
    }
    
    private String getJavaVersion() {
        return System.getProperty("java.version");
    }
    
    private String getUptime() {
        
        RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
        long totalTime_ms = -1;

        if (mxbean != null)
            totalTime_ms = mxbean.getUptime();

        if (totalTime_ms <= 0) {
            long start = env.getStartupContext().getCreationTime();
            totalTime_ms = System.currentTimeMillis() - start;
        }
        return Long.toString(totalTime_ms);
    }
    
    private String encodeParams(Map<String,String> params) {
        
        StringBuilder sb = new StringBuilder();
        char seperator;
        seperator = '?';
        for (Map.Entry<String,String> param : params.entrySet()) {
            
            try {
                sb.append(String.format("%c%s=%s", seperator,
                    URLEncoder.encode(param.getKey(), "UTF-8"),
                    URLEncoder.encode(param.getValue(), "UTF-8")
                ));
                seperator='&';
            } catch (UnsupportedEncodingException uee) {}                     
        }
        return sb.toString();
    }
    
    private void send(String target) {
        
        System.out.println("PhoneHomeTask send() target = " + target);
        
        try { 
            URL url = new URL(target);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(CONN_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.getResponseCode();
        }
        catch (IOException ioe) {}
    }
}
