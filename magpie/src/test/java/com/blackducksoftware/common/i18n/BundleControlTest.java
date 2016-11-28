/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.common.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.junit.Test;

/**
 * Tests for {@link BundleControl}.
 *
 * @author jgustie
 */
public class BundleControlTest {

    @Test(expected = MissingResourceException.class)
    public void nonExistantBundle() {
        ResourceBundle.getBundle("does.not.exist", BundleControl.create());
    }

}
