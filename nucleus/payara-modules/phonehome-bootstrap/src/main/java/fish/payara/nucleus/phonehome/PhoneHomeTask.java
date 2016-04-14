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

import java.util.logging.Logger;

/**
 *
 * @author David Weaver
 */
public class PhoneHomeTask implements Runnable {
    
    private static final Logger LOGGER = Logger.getLogger(PhoneHomeTask.class.getCanonicalName());
    
    @Override
    public void run() {
        System.out.println("PhoneHomeTask Run()");
    }
}
