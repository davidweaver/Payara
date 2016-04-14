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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author David Weaver
 */
@Service(name = "phonehome-core")
@RunLevel(StartupRunLevel.VAL)
public class PhoneHomeCore implements EventListener {
    
    private static final Logger LOGGER = Logger.getLogger(PhoneHomeCore.class.getCanonicalName());
    private static final String THREAD_NAME = "PhoneHomeThread";
    
    private static PhoneHomeCore theCore;
    private boolean enabled;
    
    private ScheduledExecutorService executor;
    
    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    PhoneHomeRuntimeConfiguration configuration;
    
    @Inject
    private Events events;
    
    /**
     *
     * @param event
     */
    @Override
    public void event(Event event) {
        if (event.is(EventTypes.SERVER_STARTUP)) {
            bootstrapPhoneHome();
            System.out.println("PhoneHomeCore SERVER_STARTUP");
        } else if (event.is(EventTypes.SERVER_SHUTDOWN)) {
            executor.shutdownNow();
            System.out.println("PhoneHome SERVER_SHUTDOWN");
        }
    }
    
    @PostConstruct
    public void postConstruct() {
        System.out.println("PhoneHomeCore PostConstruct");
        theCore = this;
        
        if (configuration == null) {
            enabled = true;
        } else {
            enabled = Boolean.valueOf(configuration.getEnabled());
        }
    }
    
    @PreDestroy
    public void preDestroy() {
        System.out.println("PhoneHomeCore PreDestroy");
    }
    
  
    private void bootstrapPhoneHome() {
        executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, THREAD_NAME);
            }
        });
        events.register(this);
        
        if (enabled) {
            executeTask();
        }
        
        System.out.println("PhoneHomeCore bootstrapPhoneHome()");
    }
    
    private void shutdownPhoneHome() {
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    private void executeTask() {
        //executor.scheduleAtFixedRate(new PhoneHomeTask(), 0, 1, TimeUnit.DAYS);
        executor.scheduleAtFixedRate(new PhoneHomeTask(), 0, 2, TimeUnit.MINUTES);
    }
        
    public void enable() {
        System.out.println("PhoneHomeCore enable()");
        setEnabled(true);
    }
    public void disable() {
        System.out.println("PhoneHomeCore disable()");
        setEnabled(false);
    }
    public void setEnabled(Boolean enabled) {
        if (this.enabled && !enabled) {
            this.enabled = false;
            shutdownPhoneHome();
        } else if (!this.enabled && enabled) {
            this.enabled = true;
            bootstrapPhoneHome();
        } else if (this.enabled && enabled) {
            shutdownPhoneHome();
            bootstrapPhoneHome();
        }
    }
}
