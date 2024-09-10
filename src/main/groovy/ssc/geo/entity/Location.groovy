package ssc.geo.entity

import javax.persistence.Entity
import javax.persistence.Id

/**
 * Created by schohan on 7/16/2015.
 */

@Entity
class Location {

    @Id
    String name
    float lat
    float lng



    @Override
    public String toString() {
        return "Location{" +
                "name='" + name + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
