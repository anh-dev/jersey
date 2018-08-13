package com.pluralsight;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.jersey.server.ManagedAsync;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.util.Collection;

@Path("/books")
public class BookResource {

    @Context
    private BookDao dao;
    @Context
    private Request request;

    @GET
    @Produces({"application/json;qs=1", "application/xml;qs=0.5"})
    @ManagedAsync
    public void getBooks(@Suspended final AsyncResponse response) {
       response.resume(dao.getBooks());
       ListenableFuture<Collection<Book>> future = dao.getBooksAsync();
       Futures.addCallback(future, new FutureCallback<Collection<Book>>() {
           @Override
           public void onSuccess(Collection<Book> books) {
               response.resume(books);
           }

           @Override
           public void onFailure(Throwable throwable) {
                response.resume(throwable);
           }
       });
    }

    @Path("/{id}")
    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void updateBook(@PathParam("id") final String id, final  Book book, @Suspended final AsyncResponse response) {

        ListenableFuture<Book> getBookFuture = dao.getBookAsync(id);
        Futures.addCallback(getBookFuture, new FutureCallback<Book>() {
            @Override
            public void onSuccess(Book originalBook) {
                Response.ResponseBuilder rb = request.evaluatePreconditions(generateEntityTag(originalBook));
                if (rb != null){
                    response.resume(rb.build());
                }
                else {
                    ListenableFuture<Book> future = dao.updateBookAsync(id, book);
                    Futures.addCallback(future, new FutureCallback<Book>() {
                        @Override
                        public void onSuccess(Book updatedBook) {
                            response.resume(updatedBook);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            response.resume(throwable);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });


    }


    @PoweredBy("Pluralsight")
    @Path("/{id}")
    @GET
    @Produces({"application/json;qs=1", "application/xml;qs=0.5"})
    @ManagedAsync
    public void getBook(@PathParam("id") String id, @Suspended final AsyncResponse response) {


        final ListenableFuture<Book> bookFuture = dao.getBookAsync(id);
        Futures.addCallback(bookFuture, new FutureCallback<Book>() {
            @Override
            public void onSuccess(Book book) {
                EntityTag entityTag = generateEntityTag(book);
                Response.ResponseBuilder rb = request.evaluatePreconditions(entityTag);
                if (rb != null) {
                    response.resume(rb.build());
                } else {
                    response.resume(Response.ok().tag(entityTag).entity(book).build());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void addBook(@Valid @NotNull Book book, @Suspended final AsyncResponse response){

        ListenableFuture<Book> bookFuture = dao.addBookAsync(book);
        Futures.addCallback(bookFuture, new FutureCallback<Book>() {

            @Override
            public void onSuccess(Book addedBook) {
                response.resume(addedBook);
            }

            @Override
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    EntityTag generateEntityTag(Book book) {
        return  new EntityTag(DigestUtils.md5Hex(book.getAuthor() + book.getTitle() + book.getPublished() + book.getExtras()));
    }
}
