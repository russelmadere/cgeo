package cgeo.geocaching.utils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Single;
import rx.Single.OnSubscribe;
import rx.SingleSubscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class RxOkHttpUtils {

    private RxOkHttpUtils() {
        // Do not instantiate
    }

    /**
     * Create a Single for running a cancellable request.
     *
     * @param client the client to use for this request
     * @param request the request
     * @return a Single containing the response or an IOException
     */
    public static Single<Response> request(final OkHttpClient client, final Request request) {
        return Single.create(new OnSubscribe<Response>() {
            @Override
            public void call(final SingleSubscriber<? super Response> singleSubscriber) {
                final Call call = client.newCall(request);
                final AtomicBoolean completed = new AtomicBoolean(false);
                singleSubscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        if (!completed.get()) {
                            call.cancel();
                        }
                    }
                }));
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, final IOException e) {
                        completed.set(true);
                        singleSubscriber.onError(e);
                    }

                    @Override
                    public void onResponse(final Call call, final Response response) throws IOException {
                        completed.set(true);
                        singleSubscriber.onSuccess(response);
                    }
                });
            }
        });
    }

}