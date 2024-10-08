package ssc.temp

/**
 * Created by schohan on 11/13/2015.
 */
class GeoDistanceCalculator {

    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }


    /* Test geo distance */
    public static void main(String[] args) {
        float a1 = 38.961276;
        float a2 = -77.361637;
        float d1 = 38.959441;
        float d2 = -77.360393;

        print("Distance is " + GeoDistanceCalculator.distFrom(a1,a2,d1,d2))

    }

}
