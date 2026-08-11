package com.simple_shop.gateway.dto.formatter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import tools.jackson.databind.annotation.JsonSerialize;

// TODO Move to common-utils lib
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = ScientificBigDecimalSerializer.class)
public @interface JsonScientificBigDecimalFormatter {

}