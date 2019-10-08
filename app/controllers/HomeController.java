package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import model.Image;
import play.Logger;
import play.cache.SyncCacheApi;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import services.ContentAPIService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final Config config;
    private final SyncCacheApi cache;
    private final WSClient wsClient;

    @Inject
    private ContentAPIService contentAPIService;

    @Inject
    public HomeController(Config config, SyncCacheApi cache, WSClient wsClient) {
        this.config = config;
        this.cache = cache;
        this.wsClient = wsClient;
    }

    public CompletionStage<Result> testMultiple() throws ExecutionException, InterruptedException {
        List<Image> multipleImage = contentAPIService.getMultipleImage();
        List<Image> result = multipleImage.stream().filter(image -> image.getId() % 2 == 0).collect(Collectors.toList());
        return CompletableFuture.completedFuture(
                ok(views.html.images.render(result)));
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    /**
     * This is where the test will be coded
     *
     * @return Result
     */
    public CompletionStage<Result> test() throws ExecutionException, InterruptedException {
        try {
            List<Image> result = contentAPIService.getRandomImage();
            return CompletableFuture.completedFuture(
                    ok(views.html.images.render(result)));
        } catch (Exception exception) {
            Logger.error("An exception of type {} occurred when trying to call YPolice with params: agent={}");
            return null;
        }
    }
}