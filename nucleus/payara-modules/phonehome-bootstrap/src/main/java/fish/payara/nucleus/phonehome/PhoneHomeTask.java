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
import com.sun.enterprise.config.serverbeans.Domain;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.glassfish.api.admin.ServerEnvironment;

/**
 *
 * @author David Weaver
 */
public class PhoneHomeTask implements Runnable {
    
    //private static final String PHONE_HOME_URL = "https://localhost:8181/echo/echo";
    private static final String PHONE_HOME_URL = "https://www.payara.fish/phonehome";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final int CONN_TIMEOUT = 5000;    // 5 seconds
    private static final int READ_TIMEOUT = 5000;    // 5 seconds
    
    private static final Logger LOGGER = Logger.getLogger(PhoneHomeTask.class.getCanonicalName());
    
    
    ServerEnvironment env;
    Domain domain;
    
    PhoneHomeTask(Domain domain, ServerEnvironment env) {
        this.env = env;
        this.domain = domain;
    }

    @Override
    public void run() {
        
        LOGGER.info("Phone Home");
        
        Map<String,String> params = new HashMap<>();
        params.put("ver", getVersion());
        params.put("jvm", getJavaVersion());
        params.put("uptime", getUptime());
        params.put("nodes", getNodeCount());
        params.put("servers", getServerCount());
        
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
    
    private String getNodeCount(){
        return Integer.toString(domain.getNodes().getNode().size());
    }
    
    private String getServerCount(){
        return Integer.toString(domain.getServers().getServer().size());
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
        
        try {
            URL url = new URL(target);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            
            SSLSocketFactory sslsf = getCertValidationDisabledSSLSocketFactory();
            if (sslsf != null ) {
                conn.setSSLSocketFactory(sslsf);
            }            
            System.out.println("PhoneHome UTLConnection = " + conn);
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            
            int resCode = conn.getResponseCode();
            System.out.println("PhoneHome Response Code = " + resCode);
        }
        catch (IOException ioe) {}
    }
    
    private SSLSocketFactory getCertValidationDisabledSSLSocketFactory() {
        
        SSLSocketFactory sslsf = null;
        
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts;
        trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() { return null; }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }};
        
        try {
            SSLContext sc = SSLContext.getInstance("TLS");  // Transport Layer Security protocol
            sc.init(null, trustAllCerts, new SecureRandom());
            sslsf = sc.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {}
        
        return sslsf;
    }
    
}
