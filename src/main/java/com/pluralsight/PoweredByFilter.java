package com.pluralsight;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;

@PoweredBy
@Provider
public class PoweredByFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {

        for (Annotation a : containerResponseContext.getEntityAnnotations()){
            if (a.annotationType() == PoweredBy.class) {
                String value = ((PoweredBy) a).value();
                containerResponseContext.getHeaders().add("X-Powered-By", value);
            }
        }


    }
}
