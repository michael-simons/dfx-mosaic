/*
 * Copyright 2014 michael-simons.eu.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

/**
 * Stores buffered images as files or into arbitrary outputstreams.
 *
 * @author Michael J. Simons, 2014-12-18
 */
public class ImageStorage {

    /**
     * Store as a buffered {@code image} into the given file {
     *
     * @target} as jpeg using the quality {@code quality}.
     * @param image Image to store
     * @param target Targetfile
     * @param quality jpeg quality to use
     * @throws IOException Any problems that might happen
     */
    public void storeAsJpeg(final BufferedImage image, final File target, final float quality) throws IOException {
	this.storeAsJpeg(image, new FileOutputStream(target), quality);
    }

    /**
     * Store as a buffered {@code image} into the given stream {
     *
     * @target} as jpeg using the quality {@code quality}.
     * @param image Image to store
     * @param target Targetfile
     * @param quality jpeg quality to use
     * @throws IOException Any problems that might happen
     */
    public void storeAsJpeg(final BufferedImage image, final OutputStream target, final float quality) throws IOException {
	final ImageWriter imageWriter = ImageIO.getImageWritersByMIMEType("image/jpeg").next();
	final JPEGImageWriteParam iwp = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
	iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	iwp.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
	iwp.setCompressionQuality(quality);
	iwp.setOptimizeHuffmanTables(true);

	try (final ImageOutputStream out = ImageIO.createImageOutputStream(target)) {
	    imageWriter.setOutput(out);
	    imageWriter.write(null, new IIOImage(image, null, null), iwp);
	    out.flush();
	} catch (IOException e) {
	    imageWriter.abort();
	    throw e;
	} finally {
	    imageWriter.dispose();
	}
    }
}
