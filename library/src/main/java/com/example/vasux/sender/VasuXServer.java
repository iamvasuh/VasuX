package com.example.vasux.sender;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.example.vasux.R;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;



class VasuXServer extends NanoHTTPD {

    private static final String TAG = "ShareServer";

    private static final String MIME_JSON = "application/json";
    private static final String MIME_FORCE_DOWNLOAD = "application/force-download";
    private static final String MIME_PNG = "image/png";

    private String[] m_filesTobeHosted;
    private FileTransferStatusListener m_clientsFileTransferListener;
    private Context m_context;

    public VasuXServer(String host_name, int port) {
        super(host_name, port);
    }

    public VasuXServer(Context context, FileTransferStatusListener statusListener, String[] filesToBeHosted) {
        this(null, 0);
        m_context = context;
        m_clientsFileTransferListener = statusListener;
        m_filesTobeHosted = filesToBeHosted;
    }

    public VasuXServer(Context context, FileTransferStatusListener statusListener, String[] filesToBeHosted, int port) {
        this(null, port);
        m_context = context;
        m_clientsFileTransferListener = statusListener;
        m_filesTobeHosted = filesToBeHosted;
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Response res = null;
        try {
            String url = session.getUri();
            Log.d(TAG, "request uri: " + url);
            if (TextUtils.isEmpty(url) || url.equals("/") || url.contains("/open"))
                res = createHtmlResponse();
            else if (url.equals("/status"))
                res = new NanoHTTPD.Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Available");
            else if (url.equals("/apk"))
                res = createApkResponse(session.getHeaders().get("http-client-ip"));
            else if (url.equals("/logo") || url.equals("/favicon.ico"))
                res = createLogoResponse();
            else if (url.equals("/files"))
                res = createFilePathsResponse();
            else if (url.contains("/file/")) {
                int index = Integer.parseInt(url.replace("/file/", ""));
                if (index != -1)
                    res = createFileResponse(m_filesTobeHosted[index], session.getHeaders().get("http-client-ip"));
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            res = createErrorResponse(Response.Status.FORBIDDEN, ioe.getMessage());
        } finally {
            if (null == res)
                res = createForbiddenResponse();
        }
        res.addHeader("Accept-Ranges", "bytes");
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        return res;
    }

    private Response createErrorResponse(Response.Status status, String message) {
        Log.e(TAG, "error while creating response: " + message);
        return new Response(status, NanoHTTPD.MIME_PLAINTEXT, message);
    }

    private Response createForbiddenResponse() {
        return createErrorResponse(Response.Status.FORBIDDEN,
                "FORBIDDEN: Reading file failed.");
    }

    /**
     * Creates a success {@link Response} with Shared Files URLS data in @{@link com.google.gson.JsonArray} format
     *
     * @return {@link Response}
     */
    private Response createFilePathsResponse() {
        return new NanoHTTPD.Response(Response.Status.OK, MIME_JSON, new JSONArray(Arrays.asList(m_filesTobeHosted)).toString());
    }

    /**
     * Creates a success <code>Response</code> with binary data of file in {@link VasuXServer#m_filesTobeHosted} with provided index.
     *
     * @param clientIp Receiver IP to which Response is intended for
     * @param fileUrl  url of file among Shared files array
     * @return {@link Response}
     * @throws IOException
     */
    private Response createFileResponse(String fileUrl, String clientIp) throws IOException {
        final File file = new File(fileUrl);
        Log.d(TAG, "resolve info found, file location: " + file.getAbsolutePath() + ", file length: " + file.length() + ", file name: " + file.getName());
        Response res = new Response(Response.Status.OK, MIME_FORCE_DOWNLOAD, clientIp, file, m_clientsFileTransferListener);
        res.addHeader("Content-Length", "" + file.length());
        res.addHeader("Content-Disposition", "attachment; filename='" + file.getName() + "'");
        return res;
    }

    private Response createHtmlResponse() {
        String answer = "";
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(m_context.getAssets().open("iamvasuh.html")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                answer += line;
            }
        } catch (IOException ioe) {
            Log.e("NanoHTTPD", ioe.toString());
        }
        return new NanoHTTPD.Response(answer);
    }

    private Response createLogoResponse() {
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(m_context.getResources(), R.mipmap.ic_launcher);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
            Response res = new Response(Response.Status.OK, MIME_PNG, bs);
            res.addHeader("Accept-Ranges", "bytes");
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createForbiddenResponse();
    }

    private Response createApkResponse(String ip) throws IOException {
        Response res = null;
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(m_context.getPackageName());
        ResolveInfo info = m_context.getPackageManager().resolveActivity(mainIntent, 0);
        if (null != info) {
            res = createFileResponse(info.activityInfo.applicationInfo.publicSourceDir, ip);
        }
        return res;
    }

}
