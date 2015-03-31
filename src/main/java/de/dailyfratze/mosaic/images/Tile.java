/*
 * Copyright 2015 michael-simons.eu.
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
package de.dailyfratze.mosaic.images;

import java.time.LocalDate;

/**
 * Representing a tile in a mosaic.
 *
 * @author Michael J. Simons, 2015-03-29
 */
public class Tile {

    /**
     * x-position of tile (number, not pixel coordinates)
     */
    private final int x;

    /**
     * y-position of tile (number, not pixel coordinates)
     */
    private final int y;

    private final int imageId;

    private final String absoluteFilename;

    private final LocalDate takenOn;

    public Tile(int i, int j, int imageId, String absoluteFilename, LocalDate takenOn) {
	this.x = i;
	this.y = j;
	this.imageId = imageId;
	this.absoluteFilename = absoluteFilename;
	this.takenOn = takenOn;
    }

    public int getX() {
	return x;
    }

    public int getY() {
	return y;
    }

    public int getImageId() {
	return imageId;
    }

    public String getAbsoluteFilename() {
	return absoluteFilename;
    }

    public LocalDate getTakenOn() {
	return takenOn;
    }
}
