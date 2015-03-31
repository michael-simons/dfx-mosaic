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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Michael J. Simons, 2015-03-29
 */
public class RGBAverageTest {

    @Test
    public void testSomeMethod() throws Exception {
	try (AutoCloseableImageReader imageReader = new AutoCloseableImageReader(RGBAverageTest.class.getResourceAsStream("/de/dailyfratze/mosaic/images/black.jpg"))) {
	    Assert.assertEquals(Color.black, getAverageColor(imageReader));
	}

	try (AutoCloseableImageReader imageReader = new AutoCloseableImageReader(RGBAverageTest.class.getResourceAsStream("/de/dailyfratze/mosaic/images/createTheFuture.jpg"))) {
	    Assert.assertEquals(new Color(128, 126, 126), getAverageColor(imageReader));
	}

	try (AutoCloseableImageReader imageReader = new AutoCloseableImageReader(RGBAverageTest.class.getResourceAsStream("/de/dailyfratze/mosaic/images/IPTC-PhotometadataRef01.jpg"))) {
	    Assert.assertEquals(new Color(170, 197, 212), getAverageColor(imageReader));
	}
    }

    Color getAverageColor(final AutoCloseableImageReader imageReader) {
	final BufferedImage image = imageReader.read();
	final int width = image.getWidth();
	final int height = image.getHeight();
	return new Color(Arrays.stream(image.getRGB(0, 0, width, height, null, 0, width)).parallel().collect(RGBAverage::new, RGBAverage::accept, RGBAverage::combine).value());
    }
}
