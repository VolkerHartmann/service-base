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

import edu.kit.datamanager.util.json.CustomInstantDeserializer;
import edu.kit.datamanager.util.json.CustomInstantSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;

import java.time.Instant;

import static org.mockito.Mockito.*;

/**
 *
 * @author jejkal
 */
@ExtendWith(MockitoExtension.class)
public class CustomInstantSerializationTest {

    @Mock
    private JsonGenerator gen;

    @Mock
    private JsonParser pars;

    @Test
    public void testSerializeInstant() throws Exception {
        Instant instant = Instant.ofEpochMilli(0);
        new CustomInstantSerializer().serialize(instant, gen, null);
        String expectedOutput = "1970-01-01T00:00:00Z";
        verify(gen, times(1)).writeString(expectedOutput);
    }

    @Test
    public void testDeserializeInstant() throws Exception {
        Instant start = Instant.ofEpochMilli(0);
        when(pars.getText()).thenReturn("1970-01-01T00:00:00Z");
        Instant inst = new CustomInstantDeserializer().deserialize(pars, null);
        Assertions.assertEquals(inst, start);
    }

    @Test
    public void testDeserializeYear() throws Exception {
        Instant start = Instant.ofEpochMilli(0);
        when(pars.getText()).thenReturn("1970");
        Instant inst = new CustomInstantDeserializer().deserialize(pars, null);
        Assertions.assertEquals(inst, start);
    }

    @Test
    public void testDeserializeYearMonth() throws Exception {
        Instant start = Instant.ofEpochMilli(0);
        when(pars.getText()).thenReturn("1970-01");
        Instant inst = new CustomInstantDeserializer().deserialize(pars, null);
        Assertions.assertEquals(inst, start);
    }
    
    @Test
    public void testDeserializeYearMonthDay() throws Exception {
        Instant start = Instant.ofEpochMilli(0);
        when(pars.getText()).thenReturn("1970-01-01");
        Instant inst = new CustomInstantDeserializer().deserialize(pars, null);
        Assertions.assertEquals(inst, start);
    }

    @Test
    public void testNullSerialization() throws Exception {
        new CustomInstantSerializer().serialize(null, gen, null);
        verify(gen, times(1)).writeString("");
    }

    @Test
    public void testNullDeserialization() throws Exception {
        when(pars.getText()).thenReturn(null);
        Instant inst = new CustomInstantDeserializer().deserialize(pars, null);
        Assertions.assertNull(inst);
    }

    @Test
    public void testEmptyDeserialization() throws Exception {
        when(pars.getText()).thenReturn("");
        Instant inst = new CustomInstantDeserializer().deserialize(pars, null);
        Assertions.assertNull(inst);
    }

    @Test
    public void testInvalidDeserializationInput() throws Exception {
      Assertions.assertThrows(JacksonIOException.class, () -> {
        when(pars.getText()).thenReturn("no-instant");
        Instant inst = new CustomInstantDeserializer().deserialize(pars, null);
      });
    }

}
