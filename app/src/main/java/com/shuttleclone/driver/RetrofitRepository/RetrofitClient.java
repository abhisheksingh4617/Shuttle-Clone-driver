package com.shuttleclone.driver.RetrofitRepository;

import android.util.Log;
import com.shuttleclone.driver.Util.AppConstants;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String TAG = "RetrofitClient";

    public static ApiCalls getClient() {

        if (retrofit == null) {

            HttpLoggingInterceptor httpLoggingInterceptor=new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
            
            // Error sanitization interceptor
            httpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    try {
                        return chain.proceed(request);
                    } catch (UnknownHostException e) {
                        // Network not available
                        Log.e(TAG, "Network error: No internet connection");
                        throw new IOException("No internet connection available");
                    } catch (SocketTimeoutException e) {
                        // Request timeout
                        Log.e(TAG, "Network error: Request timeout");
                        throw new IOException("Request timeout. Please try again");
                    } catch (IOException e) {
                        // Other IO errors - sanitize message
                        String message = e.getMessage();
                        if (message != null && (message.contains("51.21.185.70") || 
                            message.contains("Failed to connect to"))) {
                            Log.e(TAG, "Network error: Connection failed");
                            throw new IOException("Connection failed. Please check your internet");
                        }
                        throw e;
                    }
                }
            });
            
            // Header interceptor
            httpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request.Builder requestBuilder = chain.request().newBuilder();
                    requestBuilder.header("Content-Type", "application/json");
                    requestBuilder.header("Accept", "application/json");
                    return chain.proceed(requestBuilder.build());
                }
            });
            
            httpClientBuilder.addInterceptor(httpLoggingInterceptor);


            OkHttpClient httpClient = httpClientBuilder
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(AppConstants.BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();
        }
        return retrofit.create(ApiCalls.class);
    }
}
