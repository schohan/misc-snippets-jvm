package ssc.geo.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import ssc.geo.entity.Location
import ssc.geo.service.LocationService

/**
 * Created by schohan on 7/15/2015.
 */
@RestController
@RequestMapping("/geo")
@ConfigurationProperties(prefix="geo")
class GeoController {

    @Autowired
    private LocationService service

    @Autowired
    Environment env

    @Autowired
    ApplicationContext context



    @RequestMapping("/ping")
    def ping() {
        return "Geo OK at " + new Date()
    }

    /* Save a location */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public void addLocation(@RequestBody Location data) {
        println data
    }




}
