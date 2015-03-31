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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Creates an auto closable image reader. If the image reader is based on
 * {@link ImageInputStream}s that stream must be closed cleanly as well.
 *
 * @author Michael J. Simons, 2014-12-10
 */
public class AutoCloseableImageReader implements AutoCloseable {

    private final ImageReader imageReader;

    public static AutoCloseableImageReader create(final File file) {
	try {
	    return new AutoCloseableImageReader(new FileInputStream(file));
	} catch(IOException e) {
	    throw new RuntimeException(e);
	}
    }
    
    AutoCloseableImageReader(final InputStream inputStream) throws IOException {
	final ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
	final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
	if (!imageReaders.hasNext()) {
	    throw new IOException("Invalid image format!");
	}
	this.imageReader = imageReaders.next();
	this.imageReader.setInput(imageInputStream);
    }

    @Override
    public void close() {
	final Object input = this.imageReader.getInput();
	if (input != null && input instanceof ImageInputStream) {
	    try {
		((ImageInputStream) input).close();
	    } catch (IOException ex) {
		throw new RuntimeException(ex);
	    }
	}
	this.imageReader.dispose();
    }

    /**
     * Reads the image at index 0.
     *
     * @return The image at index 0
     */
    public BufferedImage read() {
	try {
	    return this.imageReader.read(0);
	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	}
    }    
}
