/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Benoit Delbosc
 */

/**
 * Application Performance Index
 *
 * The Apdex score converts many measurements into one number on a uniform scale of 0-to-1
 * (0 = no users satisfied, 1 = all users satisfied).
 *
 * Visit http://www.apdex.org/ for more information.
 *
 */
public class Apdex {
    private static final double DEFAULT_T = 1.5;
    final double t;
    long satisfying;
    long tolerable;
    long frustrating;

    enum Rating {
        Unacceptable, Poor, Fair, Good, Excellent
    }

    Apdex() {
        t = DEFAULT_T;
    }

    Apdex(Double t) {
        if (t == null) {
            this.t = DEFAULT_T;
        } else {
            this.t = t;
        }
    }

    public void addMs(long value) {
        add(value / 1000.0);
    }

    public void add(double value) {
        if (value < t) {
            satisfying++;
        } else if (value < 4 * t) {
            tolerable++;
        } else {
            frustrating++;
        }
    }

    public double getScore() {
        long total = satisfying + tolerable + frustrating;
        if (total == 0) {
            return 0;
        }
        return (satisfying + (tolerable / 2.0)) / total;
    }

    public Rating getRating() {
        double score = getScore();
        if (score < 0.5) {
            return Rating.Unacceptable;
        } else if (score < 0.7) {
            return Rating.Poor;
        } else if (score < 0.85) {
            return Rating.Fair;
        } else if (score < 0.94) {
            return Rating.Good;
        } else {
            return Rating.Excellent;
        }
    }
}
