/*
 * Copyright 2019 Karlsruhe Institute of Technology.
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

import edu.kit.datamanager.security.filter.NoopAuthenticationEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 *
 * @author jejkal
 */
public class NoopAuthenticationProviderTest{

  @Test
  public void testPublishAuthenticationSuccess(){
    new NoopAuthenticationEventPublisher().publishAuthenticationSuccess(null);
    Assertions.assertTrue(true, "Success event successfully published");
  }

  @Test
  public void testPublishAuthenticationFailure(){
    new NoopAuthenticationEventPublisher().publishAuthenticationFailure(new UsernameNotFoundException("Username not found."), null);
    Assertions.assertTrue(true, "Failure event successfully published");
  }
}
