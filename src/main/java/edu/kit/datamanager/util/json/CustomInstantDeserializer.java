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
package edu.kit.datamanager.util.json;

import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ValueDeserializer;

/**
 * Deserializer for Instant attributes.
 *
 * @author jejkal
 */
public class CustomInstantDeserializer extends ValueDeserializer<Instant> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomInstantDeserializer.class);

    private final DateTimeFormatter isoFormat = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC);
    //additional date patterns according to DataCite JSON spec ordered by probability of use
    private final DateTimeFormatter[] additionalFormats = new DateTimeFormatter[]{
        new DateTimeFormatterBuilder()
        .appendPattern("yyyy")
        .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
        .toFormatter(),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM")
        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
        .toFormatter()
    };

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String text = p.getText();
        if (text == null || text.length() == 0) {
            return null;
        }

        try {
            LOGGER.trace("Trying to parse date {} using ISO_DATE_TIME.", text);
            return Instant.from(isoFormat.parse(text)).truncatedTo(ChronoUnit.MILLIS);
        } catch (DateTimeParseException ex) {
            //no iso format...continue with other formats
            LOGGER.trace("Date {} is no ISO_DATE_TIME, trying alternatives.", text);
        }
        for (DateTimeFormatter formatter : additionalFormats) {
            try {
                LOGGER.trace("Parsing date {} using formatter {}.", text, formatter);
                LocalDate date = LocalDate.from(formatter.parse(text));
                return Instant.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            } catch (DateTimeException ex) {
                //no valid date according to the current format, continue if possible
                LOGGER.trace("Date {} cannot be parsed using formatter {}.", text, formatter);
            }
        }
        throw JacksonIOException.construct(new IOException("Invalid date string. Supported format patterns are: yyyy-MM-dd'T'HH:mm:ss'Z', yyyy, yyyy-MM-dd and yyyy-MM but is '" +text + "'."));
    }
}
