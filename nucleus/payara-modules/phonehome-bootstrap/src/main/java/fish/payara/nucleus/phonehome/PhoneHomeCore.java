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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
//import org.glassfish.hk2.api.PostConstruct;
//import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author David Weaver
 */
@Service(name = "phonehome-core")
@RunLevel(StartupRunLevel.VAL)
public class PhoneHomeCore implements EventListener {
//public class PhoneHomeCore implements EventListener, PostConstruct, PreDestroy {
    
    //private static final Logger LOGGER = Logger.getLogger(PhoneHomeCore.class.getCanonicalName());
    //private static final String PREFIX = "phonehome-core-";
    
    @Inject
    private Events events;
    
    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    
    //private ScheduledExecutorService executor;
    
    /**
     *
     * @param event
     */
    @Override
    public void event(Event event) {
        if (event.is(EventTypes.SERVER_STARTUP)) {
            //System.out.println("PhoneHomeCore SERVER_STARTUP");
        } else if (event.is(EventTypes.SERVER_SHUTDOWN)) {
            //System.out.println("PhoneHome SERVER_SHUTDOWN");
        }
    }
    
    //@Override
    @PostConstruct
    public void postConstruct() {
        //bootstrapPhoneHome();
        //events.register(this);
        //System.out.println("PhoneHomeCore PostConstruct");
    }
    
    //@Override
    @PreDestroy
    public void preDestroy() {
        //System.out.println("PhoneHomeCore PreDestroy");
    }
    
    public void bootstrapPhoneHome() {
        //System.out.println("PhoneHomeCore bootstrapPhoneHome()");
        events.register(this);
    }
}
