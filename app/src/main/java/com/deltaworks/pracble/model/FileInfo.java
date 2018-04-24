package com.deltaworks.pracble.model;


import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.http.Part;

/**
 * Created by Administrator on 2018-03-14.
 */

public class FileInfo {
    public ArrayList<String> fileName;

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileName=" + fileName +
                '}';
    }
}
