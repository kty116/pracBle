package com.deltaworks.pracble.model;

import okhttp3.RequestBody;

/**
 * Created by Administrator on 2018-04-24.
 */

public class FileRequestBody {
    private RequestBody requestBody;
    private String fileName;

    public FileRequestBody(RequestBody requestBody, String fileName) {
        this.requestBody = requestBody;
        this.fileName = fileName;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
