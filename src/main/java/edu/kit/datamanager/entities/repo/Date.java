/*
 * Copyright 2017 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.entities.repo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import edu.kit.datamanager.entities.BaseEnum;
import edu.kit.datamanager.util.json.CustomInstantDeserializer;
import edu.kit.datamanager.util.json.CustomInstantSerializer;
import java.time.Instant;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Data;

/**
 * Entity for date (DataCite)
 *
 * @author jejkal
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("UnnecessarilyFullyQualified")
public class Date{

  //Date types
  public enum DATE_TYPE implements BaseEnum{
    ACCEPTED("Accepted"),
    AVAILABLE("Available"),
    COLLECTED("Collected"),
    COPYRIGHTED("Copyrighted"),
    CREATED("Created"),
    ISSUED("Issued"),
    SUBMITTED("Submitted"),
    UPDATED("Updated"),
    VALID("Valid"),
    REVOKED("Revoked");

    private final String value;

    DATE_TYPE(String value){
      this.value = value;
    }

    @Override
    public String getValue(){
      return value;
    }
  }
  
  private long id;
  //ISO format
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  @JsonSerialize(using = CustomInstantSerializer.class)
  Instant value;

  DATE_TYPE type;

}
