package com.nivesh.library.annotation;

import com.nivesh.library.configuration.cache.NiveshCacheAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(NiveshCacheAutoConfiguration.class)
public @interface EnableNiveshCache {
}
