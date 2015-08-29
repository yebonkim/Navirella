package com.withcamp.soma6.navirella;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

/**
 * Created by jiyoungpark on 15. 8. 29..
 */
public interface NetworkService {


    /**
     * 아래보이는건 API_KEY이며 현재는 app에 넣어두었지만 보안상 대체로 서버를 통해서 인증키를 받아갑니다
     *
     * Local Device -> Server(Get API_EKY) -> Request 이런식입니다.
     */

    //TODO : API_KEY 넣기

    public static final String API_KEY = "";


    /**
     * GET 방식으로 요청을 하며 동기화 방식으로 자료를 요청합니다.
     *
     * @param parameters : Reference를 참고하면 나오지만 Query가 여러개이기 때문에 QueryMap 형식으로 받아서 요청!
     * @return : 가져온 객체(응답)를 반환합니다.
     */

    @GET("/shopping/search")
    Object getDataSync(@QueryMap HashMap<String, String> parameters);


    /**
     * GET 방식으로 요청을 하며 비동기 방식으로 자료를 요청합니다.
     *
     * 안드로이드에서는 내부적으로 비동기화 방식으로 하도록 권장하며 설계되었다고 합니다.
     *
     * @param parameters : Reference를 참고하면 나오지만 Query가 여러개이기 때문에 QueryMap 형식으로 받아서 요청!
     * @param callback : Object에 응답결과가 오며 그 결과를 가지고 Callback으로 처리를 합니다.
     */

    @GET("/shopping/search")
    void getDataAsync(@QueryMap HashMap<String, String> parameters, Callback<Object> callback);


}