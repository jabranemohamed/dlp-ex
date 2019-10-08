package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import com.typesafe.sslconfig.ssl.SSLConfigFactory;
import model.Image;
import play.Logger;
import play.api.libs.ws.WSClientConfig;
import play.api.libs.ws.ahc.AhcConfigBuilder;
import play.api.libs.ws.ahc.AhcWSClientConfig;
import play.cache.SyncCacheApi;
import play.libs.ws.WSResponse;
import play.libs.ws.ahc.AhcWSClientConfigFactory;
import play.shaded.ahc.org.asynchttpclient.*;
import play.shaded.ahc.org.asynchttpclient.uri.Uri;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static play.mvc.Http.HeaderNames.ACCEPT;
import static play.mvc.Http.MimeTypes.HTML;

/**
 * This service has for role to build WS Client and get the result back to the controller
 */
public class ContentAPIService {
    /**
     * URL Where we will read our images
     */
    public static final String IMAGE_URL = ConfigFactory.load().getString("images.url");

    public static final String MULTIPLE_IMAGE_URL = ConfigFactory.load().getString("multi_images.url");

    /**
     * Time counter cache for the same URL
     */
    public static final int IMAGE_CACHE_EXPIRATION = ConfigFactory.load().getInt("images.cache");

    public static final String IMAGE_KEY = "image";

    public AsyncHttpClient httpClient;


    @Inject
    private  SyncCacheApi cacheApi;


    /**
     * Modifie l'Url précendente
     */
    public String getURIfromcache(String uri) {
        String imageFromCache = (String) cacheApi.get(IMAGE_KEY);
        if(imageFromCache == null) {//either there are no image yet or the 5 seconds are done
            cacheApi.set(IMAGE_KEY, uri, IMAGE_CACHE_EXPIRATION);
            return uri;
        }
        else
            return imageFromCache;
    }


    public CompletionStage<Response> getRandomImageResponse() {
        RequestBuilder rb = new RequestBuilder("GET")
                .setUrl(IMAGE_URL)
                .addHeader(ACCEPT, HTML);
        this.httpClient = asyncHttpClient();
        return httpClient.executeRequest(rb.build(), new AsyncCompletionHandlerBase() {
            @Override
            public void onThrowable(Throwable t) {
            }

            @Override
            public Response onCompleted(Response response) {
                return response;
            }
        }).toCompletableFuture().thenApply((Response r) -> {
            // Closing the Client
            try {
                httpClient.close();
            } catch (Exception e) {
                Logger.warn("An error {} has been occurred when trying to close the AsyncHttpClient ", e.getClass().getSimpleName(), e);
            }
            return r;
        });
    }

    public CompletionStage<Response> getMultipleImageResponse() {
        RequestBuilder rb = new RequestBuilder("GET")
                .setUrl(MULTIPLE_IMAGE_URL)
                .addHeader(ACCEPT, HTML);
        this.httpClient = asyncHttpClient();
        return httpClient.executeRequest(rb.build(), new AsyncCompletionHandlerBase() {
            @Override
            public void onThrowable(Throwable t) {
            }

            @Override
            public Response onCompleted(Response response) {
                return response;
            }
        }).toCompletableFuture().thenApply((Response r) -> {
            // Closing the Client
            try {
                httpClient.close();
            } catch (Exception e) {
                Logger.warn("An error {} has been occurred when trying to close the AsyncHttpClient ", e.getClass().getSimpleName(), e);
            }
            return r;
        });
    }

    public List getRandomImage() {
        try {
            return getRandomImageResponse()
                    .thenApply((Response r) -> {
                        try {
                            Uri uri = r.getUri();
                            Image image = new Image();
                            image.setId(1L);
                            image.setDownload_url(getURIfromcache(uri.toUrl()));
                            List<Image> myObjects = Arrays.asList(image);
                            return myObjects;
                        } catch (Exception e) {
                            return null;
                        }
                    }).exceptionally(exception -> {
                        return null;
                    }).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException exception) {
            return null;
        }
    }


    public List getMultipleImage() {
        try {
            return getMultipleImageResponse()
                    .thenApply((Response r) -> {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            Logger.debug("Extracting response body from a {} object as a {} object", WSResponse.class.getName(), JsonNode.class.getName());
                            List<Image> myObjects = mapper.readValue(r.getResponseBody(), new TypeReference<List<Image>>() {
                            });
                            return myObjects;
                        } catch (Exception e) {
                            return null;
                        }
                    }).exceptionally(exception -> {
                        return null;
                    }).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException exception) {
            return null;
        }
    }


    public static AsyncHttpClient asyncHttpClient() {
        // Set up the client config (you can also use a parser here):
        scala.Option<String> noneString = scala.None$.empty();
        WSClientConfig wsClientConfig = new WSClientConfig(
                scala.concurrent.duration.Duration.apply(120, TimeUnit.SECONDS), // connectionTimeout
                scala.concurrent.duration.Duration.apply(120, TimeUnit.SECONDS), // idleTimeout
                scala.concurrent.duration.Duration.apply(120, TimeUnit.SECONDS), // requestTimeout
                true, // followRedirects
                true, // useProxyProperties
                noneString, // userAgent
                true, // compressionEnabled / enforced
                SSLConfigFactory.defaultConfig());

        AhcWSClientConfig clientConfig = AhcWSClientConfigFactory.forClientConfig(wsClientConfig);

        // Add underlying asynchttpclient options to WSClient
        AhcConfigBuilder builder = new AhcConfigBuilder(clientConfig);
        DefaultAsyncHttpClientConfig.Builder ahcBuilder = builder.configure();
        return new DefaultAsyncHttpClient(ahcBuilder.build());
    }


}
