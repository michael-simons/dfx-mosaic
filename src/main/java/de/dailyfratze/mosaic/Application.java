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
package de.dailyfratze.mosaic;

import de.dailyfratze.mosaic.images.AutoCloseableImageReader;
import de.dailyfratze.mosaic.images.ImageLibrary;
import de.dailyfratze.mosaic.images.ImageStorage;
import de.dailyfratze.mosaic.images.Mosaic;
import de.dailyfratze.mosaic.images.Tile;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Sample application for generating image libraries and mosaics.
 * 
 * @author Michael J. Simons, 2015-03-30
 */
public class Application {

    public static void main(String... args) throws IOException {
	if("createMosaic".equalsIgnoreCase(args[0])) {
	    createMosaic(args[1], args[2], args[3]);
	} else if("createDatabase".equalsIgnoreCase(args[0])) {
	    createDatabase(args[1], args[2]);
	}
    }
    
    /**
     * Creates a new image library
     * 
     * @param baseDir
     * @param databaseFile
     * @throws IOException 
     */
    static void createDatabase(String baseDir, String databaseFile) throws IOException {
	ImageLibrary createNewDatabaseCmd = new ImageLibrary(
		baseDir,
		databaseFile,
		"\\d{4}-\\d{2}-\\d{2}(_small)?\\.jpg",
		"yyyy-MM-dd['_small']'.jpg'"
	);	
	createNewDatabaseCmd.create();
    }

    /**
     * Creates a new mosaic. Source image is blend over the tiles for some additional color correction.
     * 
     * @param databaseFile
     * @param sourceImageFile
     * @param targetFile
     * @throws IOException 
     */
    static void createMosaic(final String databaseFile, final String sourceImageFile, final String targetFile) throws IOException {
	Mosaic mosaic = new Mosaic(
		databaseFile,
		sourceImageFile
	);
	final List<Tile> tiles = mosaic.create(null);
	int numTilesHorizontal = tiles.stream().mapToInt(Tile::getX).max().orElse(0);
	int numTilesVertical = tiles.stream().mapToInt(Tile::getY).max().orElse(0);

	final BufferedImage target = new BufferedImage(numTilesHorizontal * 150, numTilesVertical * 113, BufferedImage.TYPE_INT_RGB);

	final Graphics2D g2 = target.createGraphics();
	// As much quality as it gets
	g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
	g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	g2.drawImage(mosaic.getSourceImage(), 0, 0, target.getWidth(), target.getHeight(), null);

	final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
	g2.setComposite(ac);
	tiles.forEach(tile -> {
	    try (AutoCloseableImageReader imageReader = AutoCloseableImageReader.create(new File(tile.getAbsoluteFilename()))) {
		BufferedImage image = imageReader.read();
		g2.drawImage(image, null, tile.getX() * image.getWidth(), tile.getY() * image.getHeight());
	    }
	});

	new ImageStorage().storeAsJpeg(target, new File(targetFile), 0.95f);
    }
}
