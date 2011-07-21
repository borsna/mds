/*******************************************************************************
 * Copyright (c) 2010 DataCite
 *
 * All rights reserved. This program and the accompanying 
 * materials are made available under the terms of the 
 * Apache License, Version 2.0 which accompanies 
 * this distribution, and is available at 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package org.datacite.mds.service.impl;

import org.datacite.mds.util.Utils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * This class is responsible for retrieving master user credentials. The
 * crendentials for a given username are built from the role and username of a
 * datacentre and the password of the assigned allocator.
 */
public class MasterUserDetailsServiceImpl extends BaseMasterUserDetailsService {
 
    @Override
    public String getMasterUsername(String username) {
        String mastername = Utils.getAllocatorFromDatacentreSymbol(username);
        if (mastername == null) {
            throw new UsernameNotFoundException("cannot parse allocator symbol");
        }
        return mastername;
    }

}
