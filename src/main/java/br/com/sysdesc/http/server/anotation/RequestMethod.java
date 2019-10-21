package br.com.sysdesc.http.server.anotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.sysdesc.http.server.enumeradores.HttpMethod;

@Target({ METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMethod {

    public HttpMethod method();

    public String path() default "";
}
