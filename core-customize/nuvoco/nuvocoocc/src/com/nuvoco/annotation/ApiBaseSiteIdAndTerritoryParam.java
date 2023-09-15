package com.nuvoco.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target(value =
{ ElementType.TYPE, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@Parameters({@Parameter(
   name = "baseSiteId",
   required = true
), @Parameter(name = "territory", description = "Sub area pk", required = false, schema = @Schema(type = "string"), in = ParameterIn.PATH)})
public @interface ApiBaseSiteIdAndTerritoryParam {
}
