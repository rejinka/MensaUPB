package de.ironjan.mensaupb.stw;

import org.androidannotations.annotations.rest.*;
import org.androidannotations.api.rest.*;
import org.springframework.http.converter.json.*;

import de.ironjan.mensaupb.*;

/**
 * Do not use this class directly, use StwRestWrapper instead!
 * Rest-API of the Studentenwerk Paderborn.
 * BuildConfig.STW_URL is a secret URL that must not be shared because of "quality assurance"
 */
@Rest(converters = MappingJackson2HttpMessageConverter.class, rootUrl = BuildConfig.STW_URL)
interface StwRest {
    @Get("&restaurant={restaurant}&date={date}")
    @Accept(MediaType.APPLICATION_JSON)
    RawMenu[] getMenus(String restaurant, String date);
}