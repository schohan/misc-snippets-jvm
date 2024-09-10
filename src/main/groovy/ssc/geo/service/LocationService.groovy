package ssc.geo.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.data.geo.Point
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ssc.Application
import ssc.geo.entity.Location
import ssc.geo.entity.LocationRepository

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Created by schohan on 7/16/2015.
 */
@Service
@Transactional
class LocationService {

    @Autowired
    private LocationRepository locationRepository

     /* Given a point, return */
    def findNearByLocations(Point point, int radius, int max) {

    }


    /* Save a location */
    def save(Location location) {
        if (location) {
            locationRepository.save(location)
        } else {
           throw new RuntimeException("No location specified as input")
        }
    }

    /* Find location by id */
    public Location get(String name) {
        locationRepository.findOne(name)
    }


}
