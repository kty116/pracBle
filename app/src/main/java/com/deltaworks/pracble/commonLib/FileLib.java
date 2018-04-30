package com.deltaworks.pracble.commonLib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 파일 관련 메소드
 */

public class FileLib {

    public static final String TAG = FileLib.class.getSimpleName();

    private File folderPath;
    private Context context;

    public FileLib(Context context, File folderPath) {
        this.folderPath = folderPath;
        this.context = context;
    }

    /**
     * 파일을 압축하기
     * 압축파일로 만들려고 하는 파일
     * 파일 제대로 만들어졌으면 true 아님 false
     *
     * @return zipFile
     */
    public boolean makeZipFile(String textFileName) {

        String textFilePath = folderPath + "/" + textFileName + ".txt";
        String zipFilePath = folderPath + "/" + textFileName + ".zip";

        File zipFile = new File(zipFilePath);
        byte[] buffer = new byte[1024];

        try {
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            FileInputStream fis = new FileInputStream(textFilePath);
            zos.setLevel(9);  //최대 압축률 9, 디폴트값 8
            zos.putNextEntry(new ZipEntry(textFileName + ".txt"));


            int length;

            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.close();
            fis.close();
            fos.close();

        } catch (IOException e) {
        }
//        Log.d("dd", "makeZipFile: 끝");
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + zipFilePath)));
//        Log.d(TAG, "makeZipFile: " + zipFile.length());
        if (zipFile.length() > 0) {  //값 있음
            return true;
        } else {
            return false;
        }
    }

//    /**
//     * 파일 재 정렬
//     */
//
//    private void reArrangeFiles(){
//        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                Uri.parse("file://" + zipFilePath)));
//    }

    /**
     * 파일 만들기
     *
     * @param fileContent
     */
    public void createFile(String textFileName, String fileContent) {

        String textFilePath = folderPath + "/" + textFileName + ".txt";

        if (!folderPath.exists()) {
            folderPath.mkdirs();
        }

        File textFile = new File(textFilePath);

        try {
            FileOutputStream out = new FileOutputStream(textFile);
            out.write(fileContent.getBytes());
//            Log.d("----------", "getAlbumStorageDir: " + textFilePath);
            out.flush();
            out.close();


        } catch (IOException e) {
        }
    }

    /**
     * 파일 삭제하기
     * 폴더 PATH는 있음
     */
    public boolean deleteFile(String deleteFileName) {

        String textFilePath = folderPath + "/" + deleteFileName;

        File textFile = new File(textFilePath);
        if (textFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 파일 중 50MB 이하까지만 리스트 만들어서 리스트 반환
     * @return
     */
    public ArrayList<File> fileListBelow50MBCapacity(){
        ArrayList<File> AllFiles = new ArrayList<>();
        ArrayList<File> files = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            AllFiles.add(new File(folderPath + "/포트폴리오-김경애.pptx"));
        }

        long fFileSize = 0;
        int allFileSize = AllFiles.size();

        for (int i = 0; i < allFileSize; i++) {
            long size = AllFiles.get(i).length();
            fFileSize += size;
            if (fFileSize <= 50000000) { //50 MB 이하
//                26226 한시간 26KB  하루 629KB 열흘 6.29MB 한달 18.87MB

                files.add(AllFiles.get(i));
            } else {
                allFileSize = 0;
            }
        }

        for (int i = 0; i < files.size(); i++) {
            Log.d(TAG, "onCreate: " + files.get(i).getName());
            Log.d(TAG, "onCreate: " + files.get(i).length());
        }
        return files;
    }
}
