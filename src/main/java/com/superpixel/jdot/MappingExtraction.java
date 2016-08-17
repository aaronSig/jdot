package com.superpixel.jdot;

/**
 * Created by montywest on 17/08/2016.
 */
public interface MappingExtraction<T> {

    T apply(String json);

}
