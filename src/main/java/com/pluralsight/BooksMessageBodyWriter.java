package com.pluralsight;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

@Provider
@Produces(MediaType.APPLICATION_XML)
public class BooksMessageBodyWriter implements MessageBodyWriter<Collection<Book>> {

    @JacksonXmlRootElement(localName = "books")
    public class BooksWrapper {

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "book")
        public Collection<Book> books;

        public BooksWrapper(Collection<Book> books) {
            this.books = books;
        }
    }

    @Context
    Providers providers;

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return Collection.class.isAssignableFrom(aClass);
    }

    @Override
    public long getSize(Collection<Book> books, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Collection<Book> books, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        providers.getMessageBodyWriter(BooksWrapper.class, type,annotations,mediaType)
                .writeTo(new BooksWrapper(books), aClass, type, annotations, mediaType,multivaluedMap,outputStream);
    }
}
