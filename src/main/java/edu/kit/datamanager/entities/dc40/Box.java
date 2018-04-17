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
package edu.kit.datamanager.entities.dc40;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author jejkal
 */
@ApiModel(description = "Geo location information as box.")
public class Box {

    @ApiModelProperty(value = "-67.302", notes = "-180 <= westLongitude <= 180", required = true)
    private float westLongitude;
    @ApiModelProperty(value = "-67.302", notes = "-180 <= eastLongitude <= 180", required = true)
    private float eastLongitude;
    @ApiModelProperty(value = "31.233", notes = "-90 <= southLatitude <= 90", required = true)
    private float southLatitude;
    @ApiModelProperty(value = "31.233", notes = "-90 <= northLatitude <= 90", required = true)
    private float northLatitude;

    public Box() {
    }

    public float getWestLongitude() {
        return westLongitude;
    }

    public void setWestLongitude(float westLongitude) {
        this.westLongitude = westLongitude;
    }

    public float getEastLongitude() {
        return eastLongitude;
    }

    public void setEastLongitude(float eastLongitude) {
        this.eastLongitude = eastLongitude;
    }

    public float getSouthLatitude() {
        return southLatitude;
    }

    public void setSouthLatitude(float southLatitude) {
        this.southLatitude = southLatitude;
    }

    public float getNorthLatitude() {
        return northLatitude;
    }

    public void setNorthLatitude(float northLatitude) {
        this.northLatitude = northLatitude;
    }

}
