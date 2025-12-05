/*
 * Copyright 2018 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.test;

import edu.kit.datamanager.util.NetworkUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jejkal
 */
public class NetworkUtilsTest{

  private final static String HOSTNAME_KIT = "www.kit.edu";
  private final static String IPV4_KIT = "129.13.40.10";
  private final static String IPV6_KIT = "2a00:1398:9:fd10::810d:280a";

  private static final String IPV4_LOCALHOST = "127.0.0.1";
  private static final String IPV6_LOCALHOST = "::1";
  private final static String HOSTNAME_LOCALHOST = "localhost";
  private static final String IPV4_192_168_0_1 = "192.168.0.1";
  private static final String IPV6_192_168_0_1 = "::ffff:c0a8:1";

  @Test
  public void testIpV4Check(){
    Assertions.assertTrue(NetworkUtils.isIpV4(IPV4_LOCALHOST));
    Assertions.assertTrue(NetworkUtils.isIpV4(IPV4_192_168_0_1));
    Assertions.assertFalse(NetworkUtils.isIpV4(HOSTNAME_LOCALHOST));

    Assertions.assertTrue(NetworkUtils.isIpV4(IPV4_KIT));
    Assertions.assertFalse(NetworkUtils.isIpV4(IPV6_KIT));
    Assertions.assertFalse(NetworkUtils.isIpV4(HOSTNAME_KIT));

    Assertions.assertFalse(NetworkUtils.isIpV4("1.300.1.1"));
  }

  @Test
  public void testIpV6Check(){
    Assertions.assertTrue(NetworkUtils.isIpV6(IPV6_LOCALHOST));
    Assertions.assertTrue(NetworkUtils.isIpV6(IPV6_192_168_0_1));
    Assertions.assertFalse(NetworkUtils.isIpV6(HOSTNAME_LOCALHOST));

    Assertions.assertFalse(NetworkUtils.isIpV6(IPV4_KIT));
    Assertions.assertTrue(NetworkUtils.isIpV6(IPV6_KIT));
    Assertions.assertFalse(NetworkUtils.isIpV6(HOSTNAME_KIT));

    Assertions.assertFalse(NetworkUtils.isIpV6("::G"));
  }

  @Test
  public void testIsIp(){
    Assertions.assertTrue(NetworkUtils.isIp(IPV4_LOCALHOST));
    Assertions.assertTrue(NetworkUtils.isIp(IPV6_LOCALHOST));
    Assertions.assertTrue(NetworkUtils.isIp(IPV6_192_168_0_1));
    Assertions.assertTrue(NetworkUtils.isIp(IPV4_192_168_0_1));
    Assertions.assertFalse(NetworkUtils.isIp(HOSTNAME_LOCALHOST));

    Assertions.assertTrue(NetworkUtils.isIp(IPV4_KIT));
    Assertions.assertTrue(NetworkUtils.isIp(IPV6_KIT));
    Assertions.assertFalse(NetworkUtils.isIp(HOSTNAME_KIT));

    Assertions.assertFalse(NetworkUtils.isIp("1.300.1.1"));
    Assertions.assertFalse(NetworkUtils.isIp("::G"));
  }

  @Test
  public void testIsHostname(){
    Assertions.assertFalse(NetworkUtils.isHostname(IPV4_LOCALHOST));
    Assertions.assertFalse(NetworkUtils.isHostname(IPV6_LOCALHOST));
    Assertions.assertFalse(NetworkUtils.isHostname(IPV6_192_168_0_1));
    Assertions.assertFalse(NetworkUtils.isHostname(IPV4_192_168_0_1));
    Assertions.assertTrue(NetworkUtils.isHostname(HOSTNAME_LOCALHOST));

    Assertions.assertFalse(NetworkUtils.isHostname(IPV4_KIT));
    Assertions.assertFalse(NetworkUtils.isHostname(IPV6_KIT));
    Assertions.assertTrue(NetworkUtils.isHostname(HOSTNAME_KIT));

    Assertions.assertFalse(NetworkUtils.isHostname("1.300.1.1"));
    Assertions.assertFalse(NetworkUtils.isHostname("::G"));

    Assertions.assertFalse(NetworkUtils.isHostname("invalidHostname.uu"));
  }

  @Test
  @Disabled
  public void testMatches(){
//skip matching test as this may fail often due to DynDNS issues or resolution issues
//    Assertions.assertTrue(NetworkUtils.matches(HOSTNAME_KIT, IPV4_KIT));
//    Assertions.assertTrue(NetworkUtils.matches(HOSTNAME_KIT, IPV6_KIT));
//    Assertions.assertTrue(NetworkUtils.matches(HOSTNAME_KIT, HOSTNAME_KIT));
//    Assertions.assertTrue(NetworkUtils.matches(IPV4_KIT, IPV4_KIT));
//    Assertions.assertTrue(NetworkUtils.matches(IPV6_KIT, IPV4_KIT));

    Assertions.assertTrue(NetworkUtils.matches(IPV4_KIT, IPV4_KIT));

    Assertions.assertTrue(NetworkUtils.matches(IPV4_KIT, HOSTNAME_KIT));
//    Assertions.assertTrue(NetworkUtils.matches(IPV6_KIT, HOSTNAME_KIT));
    Assertions.assertTrue(NetworkUtils.matches(HOSTNAME_KIT, HOSTNAME_KIT));

    Assertions.assertFalse(NetworkUtils.matches(HOSTNAME_KIT, IPV4_LOCALHOST));
    Assertions.assertFalse(NetworkUtils.matches(IPV4_LOCALHOST, IPV4_KIT));
    Assertions.assertFalse(NetworkUtils.matches(IPV6_KIT, IPV6_192_168_0_1));
    Assertions.assertFalse(NetworkUtils.matches(HOSTNAME_KIT, HOSTNAME_LOCALHOST));

    Assertions.assertFalse(NetworkUtils.matches(HOSTNAME_KIT, "invalidHostnameButNotCheckedHere.uu"));

  }

}
