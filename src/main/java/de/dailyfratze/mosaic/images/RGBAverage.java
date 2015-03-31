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

/**
 * Computes an average RGB color
 * @author Michael J. Simons, 2015-03-29
 */
public class RGBAverage {

    private int r = 0, g = 0, b = 0;
    private int cnt;

    public void accept(int rgb) {
	r += (rgb >> 16) & 0xFF;
	g += (rgb >> 8) & 0xFF;
	b += (rgb) & 0xFF;
	++cnt;
    }

    public void combine(RGBAverage rhs) {
	r += rhs.r;
	g += rhs.g;
	b += rhs.b;
	cnt += rhs.cnt;
    }

    public int value() {
	return new Color(r / cnt, g / cnt, b / cnt).getRGB();
    }
}
