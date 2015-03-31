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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import static de.dailyfratze.mosaic.images.db.tables.Images.IMAGES;
import static org.jooq.impl.DSL.not;
import static org.jooq.impl.DSL.val;

/**
 *
 * @author Michael J. Simons, 2015-03-29
 */
public class Mosaic {

    /**
     * jOOQ context for "stringless" database access.
     */
    private final DSLContext create;
    
    /**
     * Tile width
     */
    private final int tileWidth = 40;

    /**
     * Tile height
     */
    private final int tileHeight = 30;

    private final BufferedImage sourceImage;

    public Mosaic(final String databaseFile, final String sourceImageFile) {
	final String databaseUrl = String.format("jdbc:h2:file:%s;FILE_LOCK=FS", databaseFile);

	final JdbcDataSource jdbcDataSource = new JdbcDataSource();
	jdbcDataSource.setUrl(databaseUrl);
	this.create = DSL.using(jdbcDataSource, SQLDialect.H2);
	try (AutoCloseableImageReader imageReader = AutoCloseableImageReader.create(new File(sourceImageFile))) {
	    this.sourceImage = imageReader.read();
	}
    }

    public int getTileWidth() {
	return tileWidth;
    }

    public int getTileHeight() {
	return tileHeight;
    }

    public BufferedImage getSourceImage() {
	return sourceImage;
    }

    /**
     * Public api for creating mosaics. The basic idea is to cut the source
     * image into tiles and compute the average color in this tiles and
     * selecting the library image with the minimal CIE94 to this color which
     * hasn't been used in a 10x10 box.
     *
     * @param target The list of tiles. Not a finished image.
     * @return
     */
    public List<Tile> create(final List<Tile> target) {
	final List<Tile> tiles = target == null ? new ArrayList<>() : target;

	final int sourceWidth = sourceImage.getWidth();
	final int sourceHeight = sourceImage.getHeight();

	int ty = 0;

	int j = 0;
	while (ty < sourceHeight) {	    
	    final int height = Math.min(tileHeight, sourceHeight - ty);	    
	    int tx = 0;
	    int i = 0;
	    while (tx < sourceWidth) {
		final int width = Math.min(tileWidth, sourceWidth - tx);
		final int tileAvgColor = Arrays
			.stream(sourceImage.getRGB(tx, ty, width, height, null, 0, width))
			.parallel()
			.collect(RGBAverage::new, RGBAverage::accept, RGBAverage::combine)
			.value();
		// That is a reference to a stored procedure, one parameter filled from
		// a column, the other one from a single, constant value computed above.
		final Field<Double> cie94ColorDistance = DSL.function("f_CIE94_color_distance", Double.class, IMAGES.AVERAGE_COLOR, val(tileAvgColor));
		final int tileX = i, tileY = j;
		final List<Integer> exclude = tiles.stream()
			.filter(t -> Math.abs(t.getX() - tileX) <= 10)
			.filter(t -> Math.abs(t.getY() - tileY) <= 10)
			.map(Tile::getImageId)
			.collect(Collectors.toList());
		tiles.addAll(create
			.select(
				IMAGES.ID,
				IMAGES.ABSOLUTE_FILE_NAME,
				IMAGES.TAKEN_ON
			)
			.from(IMAGES)
			.where(not(IMAGES.ID.in(exclude)))
			.orderBy(cie94ColorDistance.asc())
			.limit(1)
			.fetch()
			.map(r -> new Tile(tileX, tileY, r.getValue(IMAGES.ID), r.getValue(IMAGES.ABSOLUTE_FILE_NAME), r.getValue(IMAGES.TAKEN_ON).toLocalDate()))
		);
		++i;
		tx += tileWidth;
	    }
	    ++j;
	    ty += tileHeight;
	}

	return tiles;
    }
}
