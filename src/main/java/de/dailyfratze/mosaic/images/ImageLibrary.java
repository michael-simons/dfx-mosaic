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

import de.dailyfratze.mosaic.images.db.tables.records.ImagesRecord;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import static java.util.Arrays.stream;

/**
 * Creates a new database of images (an image library) used to create mosaics.
 *
 * @author Michael J. Simons, 2015-03-29
 */
public class ImageLibrary {

    /**
     * UTC Zone
     */
    private static final ZoneId ZONE_ID_UTC = ZoneId.of("UTC");

    /**
     * DataSource for storing image records.
     */
    private final DataSource dataSource;

    /**
     * jOOQ context for "stringless" database access.
     */
    private final DSLContext create;

    /**
     * Base dir to scan.
     */
    private final File baseDir;

    /**
     * Filename pattern. Files must match this pattern to be included.
     */
    private final Pattern filenamePattern;

    /**
     * A date time format. I have the idea to arrange the tiles on the z axis by
     * year, so i store the date.
     */
    private final DateTimeFormatter dateTimeFormatter;

    public ImageLibrary(String baseDir, String databaseFile, String filenamePattern, String dateFormat) {

	// TODO check for invalid paths and stuff
	final String databaseUrl = String.format("jdbc:h2:file:%s;FILE_LOCK=FS", databaseFile);

	final JdbcDataSource jdbcDataSource = new JdbcDataSource();
	jdbcDataSource.setUrl(databaseUrl);
	this.dataSource = jdbcDataSource;
	this.create = DSL.using(dataSource, SQLDialect.H2);

	this.baseDir = new File(baseDir);
	this.filenamePattern = Pattern.compile(filenamePattern);
	this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat, Locale.ENGLISH);
    }

    /**
     * Opens connection and creates the schema via Flyway api
     */
    void createDatabase() {
	final Flyway flyway = new Flyway();
	flyway.setDataSource(this.dataSource);
	flyway.clean();
	flyway.migrate();
	// Could be done in a migration, but not through
	// maven because CIE94ColorDistance doesn't exist yet
	create.execute("create alias if not exists f_CIE94_color_distance deterministic for \"de.dailyfratze.mosaic.images.CIE94ColorDistance.compute\"");
    }

    /**
     * Scans a path for files matching the given pattern. Assumes all files are
     * readable images and converts them to {@link ImageRecord ImageRecords}.
     *
     * @return
     * @throws IOException
     */
    List<ImagesRecord> scanTree() throws IOException {
	final List<Path> files = new ArrayList<>();
	// Get a list of all files matching our... Nice addition to standard API
	Files.walkFileTree(baseDir.toPath(), new SimpleFileVisitor<Path>() {
	    @Override
	    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (attrs.isRegularFile() && filenamePattern.matcher(file.getFileName().toString()).matches()) {
		    files.add(file);
		}
		return FileVisitResult.CONTINUE;
	    }
	});

	// Create a list of image records
	// Insert could be done in single steps, but i want to use batches
	return files
		.stream()
		.parallel()
		.map(Path::toFile)
		.map(file -> {
		    final ImagesRecord record = new ImagesRecord();
		    record.setAbsoluteFileName(file.getAbsolutePath());
		    record.setTakenOn(new Date(dateTimeFormatter.parse(file.getName(), LocalDate::from).atStartOfDay(ZONE_ID_UTC).toInstant().toEpochMilli()));
		    try (AutoCloseableImageReader reader = AutoCloseableImageReader.create(file)) {
			final BufferedImage image = reader.read();
			final int width = image.getWidth();
			final int height = image.getHeight();
			// This one is cool... Grab the rgb value of all pixels as array, stream it 
			// and reduce it with the RGBAverage
			record.setAverageColor(
				stream(image.getRGB(0, 0, width, height, null, 0, width))
					.parallel()
					.collect(RGBAverage::new, RGBAverage::accept, RGBAverage::combine)
					.value()
			);
		    }
		    return record;
		}).collect(Collectors.toList());	
    }
    
    /**
     * Uses jOOQ batch API for fast storage
     * @param records 
     */
    void storeImageRecords(final List<ImagesRecord> records) {
	// Using pretty sweet jOOQ batch api
	create.batchInsert(records).execute();
	Logger.getLogger(ImageLibrary.class.getName()).log(Level.INFO, "Created database containing {0} images", new Object[]{records.size()});
    }

    /**
     * Public api for creating new image databases.
     * @throws IOException 
     */
    public void create() throws IOException {
	createDatabase();
	storeImageRecords(scanTree());	
    }    
}
