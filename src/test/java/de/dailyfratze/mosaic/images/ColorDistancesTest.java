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
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2015-03-29
 */
public class ColorDistancesTest {

    @Test
    public void toLabShouldWork() {
	double[] lab;

	lab = CIE94ColorDistance.toLab(Color.black.getRGB());
	Assert.assertEquals(0.0f, lab[0], 0);
	Assert.assertEquals(0.0f, lab[1], 0);
	Assert.assertEquals(0.0f, lab[2], 0);

	lab = CIE94ColorDistance.toLab(Color.yellow.getRGB());
	Assert.assertEquals(97.138, lab[0], 0.001);
	Assert.assertEquals(-21.556, lab[1], 0.001);
	Assert.assertEquals(94.482, lab[2], 0.001);

	lab = CIE94ColorDistance.toLab(Color.pink.getRGB());
	Assert.assertEquals(79.05, lab[0], 0.001);
	Assert.assertEquals(29.249, lab[1], 0.001);
	Assert.assertEquals(11.89, lab[2], 0.001);
    }

    @Test
    public void CIE94DistanceShouldWork() {
	Assert.assertEquals(0.0, CIE94ColorDistance.compute(Color.black.getRGB(), Color.black.getRGB()), 0);
	Assert.assertEquals(56.2996, CIE94ColorDistance.compute(Color.red.getRGB(), Color.black.getRGB()), 0.0001);
	Assert.assertEquals(89.717, CIE94ColorDistance.compute(Color.green.getRGB(), Color.black.getRGB()), 0.0001);
    }

}
