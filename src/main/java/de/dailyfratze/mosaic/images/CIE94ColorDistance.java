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

/**
 * Computes the
 * <a href="http://en.wikipedia.org/wiki/Color_difference#CIE94">CIE94 color
 * distance</a> of two rgb values.
 *
 * @author Michael J. Simons, 2015-03-29
 */
public final class CIE94ColorDistance {

    /**
     * Standard D65 daylight color coefficients
     */
    private static final double[] D65 = {95.047, 100.0, 108.883};
    // CIE94 coefficients for graphic arts
    private static final double kL = 1;
    private static final double K1 = 0.045;
    private static final double K2 = 0.015;
    // Weighting factors
    private static final double sl = 1.0;
    private static final double kc = 1.0;
    private static final double kh = 1.0;

    /**
     * Converts the given color from sRGB color space to CIEL*a*b*<br>
     * Mathematics from
     * <a href="http://www.easyrgb.com/index.php?X=MATH">EasyRGB</a> and
     * <a href="http://www.brucelindbloom.com">Bruce Lindbloom</a>.
     *
     * @param color Color to convert
     * @return 3-item double array containing L*, a*, b*
     */
    static double[] toLab(final int rgb) {
	double r = ((rgb >> 16) & 0xFF) / 255.0;
	double g = ((rgb >> 8) & 0xFF) / 255.0;
	double b = (rgb & 0xFF) / 255.0;

	r = (r > 0.04045 ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92f) * 100.0;
	g = (g > 0.04045 ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92f) * 100.0;
	b = (b > 0.04045 ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92f) * 100.0;

	// Observer 2°, Standard Daylight D65
	double X, Y, Z;
	X = r * 0.4124 + g * 0.3576 + b * 0.1805;
	Y = r * 0.2126 + g * 0.7152 + b * 0.0722;
	Z = r * 0.0193 + g * 0.1192 + b * 0.9505;

	X /= D65[0];
	Y /= D65[1];
	Z /= D65[2];

	X = X > 0.008856 ? Math.pow(X, 1.0 / 3.0) : (7.787 * X) + (16.0 / 116.0);
	Y = Y > 0.008856 ? Math.pow(Y, 1.0 / 3.0) : (7.787 * Y) + (16.0 / 116.0);
	Z = Z > 0.008856 ? Math.pow(Z, 1.0 / 3.0) : (7.787 * Z) + (16.0 / 116.0);

	return new double[]{(116 * Y) - 16, 500 * (X - Y), 200 * (Y - Z)};
    }

    /**
     * Computes the CIE94 distance between two colors.
     *
     * @param rgb1
     * @param rgb2
     * @return
     */
    public static double compute(final int rgb1, final int rgb2) {
	// Convert to L*a*b* color space
	final double[] lab1 = toLab(rgb1);
	final double[] lab2 = toLab(rgb2);

	// Make it more readable
	final double L1 = lab1[0];
	final double a1 = lab1[1];
	final double b1 = lab1[2];
	final double L2 = lab2[0];
	final double a2 = lab2[1];
	final double b2 = lab2[2];

	// See http://en.wikipedia.org/wiki/Color_difference#CIE94	
	double c1 = Math.sqrt(a1 * a1 + b1 * b1);
	double deltaC = c1 - Math.sqrt(a2 * a2 + b2 * b2);
	double deltaA = a1 - a2;
	double deltaB = b1 - b2;
	double deltaH = Math.sqrt(Math.max(0.0, deltaA * deltaA + deltaB * deltaB - deltaC * deltaC));

	return Math.sqrt(Math.max(0.0, Math.pow((L1 - L2) / (kL * sl), 2) + Math.pow(deltaC / (kc * (1 + K1 * c1)), 2) + Math.pow(deltaH / (kh * (1 + K2 * c1)), 2.0)));
    }

    private CIE94ColorDistance() {
    }
}
