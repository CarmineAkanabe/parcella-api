package com.carmineabk.parcella.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public final class Geo {

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private Geo() {}

    public static Point point(double lat, double lng) {
        return GF.createPoint(new Coordinate(lng, lat));   // x = lng, y = lat
    }
}