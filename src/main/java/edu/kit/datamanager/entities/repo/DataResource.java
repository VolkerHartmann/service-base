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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import tools.jackson.databind.annotation.JsonDeserialize;
//import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import edu.kit.datamanager.entities.BaseEnum;
import edu.kit.datamanager.util.json.CustomInstantDeserializer;
import edu.kit.datamanager.util.json.CustomInstantSerializer;
import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity for data resource.
 *
 * @author jejkal
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "resource", namespace = "http://datacite.org/schema/kernel-4")
@SuppressWarnings("UnnecessarilyFullyQualified")
public class DataResource implements Serializable{

  public enum State implements BaseEnum{
    VOLATILE,
    FIXED,
    REVOKED,
    GONE;

    @Override
    public String getValue(){
      return toString();
    }
  }

  //The internal resource identifier assigned once during creation
  //mandatory
  private PrimaryIdentifier identifier;

  @XmlTransient
  private String id = null;
  //vocab
  @XmlElementWrapper(name = "creators")
  @XmlElement(name = "creator")
  private Set<Agent> creators = new HashSet<>();
  @XmlElementWrapper(name = "titles")
  @XmlElement(name = "title")
  private Set<Title> titles = new HashSet<>();
  private String publisher;

  //format: YYYY
  private String publicationYear;
  @XmlElement(name = "resourceType")
  private ResourceType resourceType;

  //recommended
  @XmlElementWrapper(name = "subjects")
  @XmlElement(name = "subject")
  private Set<Subject> subjects = new HashSet<>();
  @XmlElementWrapper(name = "contributors")
  @XmlElement(name = "contributor")
  private Set<Contributor> contributors = new HashSet<>();
  @XmlElementWrapper(name = "dates")
  @XmlElement(name = "date")
  private Set<Date> dates = new HashSet<>();
  @XmlElementWrapper(name = "relatedIdentifiers")
  @XmlElement(name = "relatedIdentifier")
  private Set<RelatedIdentifier> relatedIdentifiers = new HashSet<>();
  @XmlElementWrapper(name = "descriptions")
  @XmlElement(name = "description")
  private Set<Description> descriptions = new HashSet<>();
  private State state;
  @XmlElementWrapper(name = "geoLocations")
  @XmlElement(name = "geoLocation")
  private Set<GeoLocation> geoLocations = new HashSet<>();
  private String language;
  @XmlElementWrapper(name = "alternateIdentifiers")
  @XmlElement(name = "alternateIdentifier")
  private Set<Identifier> alternateIdentifiers = new HashSet<>();
  @XmlElementWrapper(name = "sizes")
  @XmlElement(name = "size")
  private Set<String> sizes = new HashSet<>();
  @XmlElementWrapper(name = "formats")
  @XmlElement(name = "format")
  private Set<String> formats = new HashSet<>();
  private String version;
  @XmlElementWrapper(name = "rightsList")
  @XmlElement(name = "rights")
  private Set<Scheme> rights = new HashSet<>();
  private Set<FundingReference> fundingReferences = new HashSet<>();

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  @JsonSerialize(using = CustomInstantSerializer.class)
  private Instant lastUpdate;

  @XmlElementWrapper(name = "aclEntries")
  @XmlElement(name = "aclEntry")
  private Set<AclEntry> acls = new HashSet<>();

  @XmlTransient
  @JsonIgnore
  private List<ContentInformation> associatedContentInformation;

}
