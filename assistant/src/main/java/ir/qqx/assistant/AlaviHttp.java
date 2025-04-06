package ir.qqx.assistant;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 4/12/2018.
 */

public class AlaviHttp {
    public enum AlaviHttpMethod {
        GET, POST;
    }

    public static class GetStringRequest extends AsyncTask<String, Integer, String> {
        public Context context;
        public String subname = null, response = "", method = "";
        public boolean success;
        public HttpURLConnection connection = null;
        public int connectTimeout = 0;
        public int readTimeout = 0;
        boolean wait;

        public GetStringRequest(Context context, String subname, AlaviHttpMethod method) {
            this.context = context;
            this.subname = subname;
            this.method = method.toString();


        }

        public GetStringRequest(AlaviHttpMethod method) {
            wait = true;
            this.method = method.toString();
            subname = "";
        }

        @Override
        protected String doInBackground(String... urls) {
            success = false;
            response = "";

            InputStream is = null;
            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);

                if (connectTimeout > 0)
                    connection.setConnectTimeout(connectTimeout);
                if (readTimeout > 0)
                    connection.setReadTimeout(readTimeout);

                if (method == AlaviHttpMethod.GET.toString()) {
                    connection.connect();
                } else {
                    connection.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes("query=" + urls[1]);
                    wr.flush();
                    wr.close();
                }
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = connection.getInputStream();// is is inputstream
                    success = true;
                } else {
                    is = connection.getErrorStream();
                }

                ByteArrayOutputStream oas = new ByteArrayOutputStream();
                StringBuilder sb = new StringBuilder();
                copyStream(is, oas);
                String t = oas.toString();


                is.close();
                connection.disconnect();

                return t;

            } catch (Exception e) {
                Log.d("alavi_log", "doInBackground: " + e.getMessage());

            }


            return null;
        }

        private void copyStream(InputStream is, OutputStream os) {
            final int buffer_size = 1024;
            try {
                byte[] bytes = new byte[buffer_size];
                for (; ; ) {
                    int count = is.read(bytes, 0, buffer_size);
                    if (count == -1)
                        break;
                    os.write(bytes, 0, count);
                }
            } catch (Exception ex) {
            }
        }

        public void startwait() {
            wait = true;
            AlaviUtill.waitstart();
        }

        public void stopwait() {
            wait = false;
            AlaviUtill.waitstop();
        }

        public boolean isWait() {
            return wait;
        }

        public void cancel() {
            this.cancel(true);
            connection.disconnect();
            // TODO try close input stream
        }

        @Override
        protected void onPostExecute(String string) {
            response = string;
            if (wait) {
                stopwait();
            } else {

                if (subname != "") {
                    AlaviUtill.callsub(context, subname, 0, string);
                }
            }

        }


    }

    public static class SendJsonRaw extends AsyncTask<String, Integer, String> {
        public Context context;
        public boolean upload_progress = false;
        public String subname = null, response = "";
        public boolean success;
        public String authorization;
        public HttpURLConnection connection = null;
        public int connectTimeout = 0;
        public int readTimeout = 0;
        boolean wait;
        private ProgressDialog progressDialog;

        public SendJsonRaw(Context context, String subname) {
            this.context = context;
            this.subname = subname;


        }

        public SendJsonRaw() {
            wait = true;
            subname = "";
        }

        public SendJsonRaw(ProgressDialog progressDialog, boolean upload_progress) {
            wait = true;
            subname = "";
            this.progressDialog = progressDialog;
            this.upload_progress = upload_progress;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (progressDialog != null) {
                progressDialog.setProgress(values[0]);
            }

        }

        @Override
        protected String doInBackground(String... urls) {
            success = false;
            response = "";

            InputStream is = null;
            try {
                URL url = new URL(urls[0]);
                if (upload_progress && progressDialog != null) {
                    progressDialog.setProgress(0);
                }
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");   //Way of submitting data(e.g. GET, POST)
                if (authorization != null && authorization.trim().length() > 1)
                    connection.setRequestProperty("Authorization", authorization);
                connection.setRequestProperty("Content-Type", "application/json"); //Setting content type-  JSON

//Setting length of the request body
                connection.setRequestProperty("Content-Length", Integer.toString(urls[1].getBytes().length));
                connection.setDoInput(true);
                connection.setDoOutput(true);

                byte bufInput[] = urls[1].getBytes("UTF-8");
                int bytesRead = 0, totalSize = bufInput.length, readLength = 1024;


                connection.setFixedLengthStreamingMode(totalSize);
                if (connectTimeout > 0)
                    connection.setConnectTimeout(connectTimeout);
                if (readTimeout > 0)
                    connection.setReadTimeout(readTimeout);

                connection.connect();
                DataOutputStream dataoutput = new DataOutputStream(connection.getOutputStream());


                while (bytesRead < totalSize) {
                    // write output
                    if (totalSize - bytesRead < readLength) readLength = totalSize - bytesRead;
                    dataoutput.write(bufInput, bytesRead, readLength);
                    dataoutput.flush();
                    bytesRead = bytesRead + readLength;
                    if (upload_progress && progressDialog != null) {
                        publishProgress((bytesRead * 100) / (totalSize));
                        Thread.sleep(25);
                    }

                }

                dataoutput.close();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = connection.getInputStream();// is is inputstream
                    success = true;
                } else {
                    is = connection.getErrorStream();
                }


                ByteArrayOutputStream oas = new ByteArrayOutputStream();
                StringBuilder sb = new StringBuilder();
                copyStream(is, oas);
                String t = oas.toString();


                is.close();
                connection.disconnect();

                return t;

            } catch (Exception e) {
                Log.d("alavi_log", "doInBackground: " + e.getMessage());

            }


            return null;
        }

        private void copyStream(InputStream is, OutputStream os) {
            final int buffer_size = 1024;
            try {
                byte[] bytes = new byte[buffer_size];
                for (; ; ) {
                    int count = is.read(bytes, 0, buffer_size);
                    if (count == -1)
                        break;
                    os.write(bytes, 0, count);
                }
            } catch (Exception ex) {
            }
        }

        public void startwait() {
            wait = true;
            AlaviUtill.waitstart();
        }

        public void stopwait() {
            wait = false;
            AlaviUtill.waitstop();
        }

        public boolean isWait() {
            return wait;
        }

        public void cancel() {
            this.cancel(true);
            connection.disconnect();
            // TODO try close input stream
        }

        @Override
        protected void onPostExecute(String string) {
            response = string;
            if (wait) {
                stopwait();
            } else {

                if (subname != "") {
                    AlaviUtill.callsub(context, subname, 0, string);
                }
            }

        }


    }

    public static class DownloadFileWithResume extends AsyncTask<String, Integer, String> {
        public Context context;
        public String subname = null, response = "";
        public String filename = "";
        public String dir = "";
        public boolean success;
        public HttpURLConnection connection = null;
        public int connectTimeout = 0;
        public int readTimeout = 0;
        boolean wait;
        int downloaded = 0;
        int fileLength = 0;
        int lentgh=0;
        public DownloadFileWithResume(Context context, String subname, String dir, String filename) {
            this.context = context;
            this.subname = subname;
            this.filename = filename;
            this.dir = dir;
        }

        public void startwait() {
            wait = true;
            AlaviUtill.waitstart();
        }

        public void cancel() {
            this.cancel(true);
            connection.disconnect();
            // TODO try close input stream
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            int rem = (fileLength - downloaded) / 100;

            if(rem!=0)
                lentgh=downloaded + (progress[0] * rem);
            else lentgh=fileLength;

            AlaviUtill.callsub(context,subname+"_onProgressUpdate",0,lentgh,fileLength);



        }

        @Override
        protected void onPostExecute(String string) {
            response = string;
            AlaviUtill.callsub(context,subname+"_onProgressUpdate",0,fileLength,fileLength);
            if (wait) {
                AlaviUtill.waitstop();
            } else {
                if (subname != "") {
                    AlaviUtill.callsub(context, subname, 0, DownloadFileWithResume.this);
                }
            }
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... urls) {
            response = "";
            InputStream input = null;
            OutputStream output = null;
            downloaded = 0;
            fileLength = 0;
            success = false;
            File file = new File(dir + "/" + filename);

            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("HEAD");
                if (connectTimeout > 0)
                    connection.setConnectTimeout(connectTimeout);
                if (readTimeout > 0)
                    connection.setReadTimeout(readTimeout);
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    fileLength = connection.getContentLength();
                } else {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                connection.disconnect();


                if (file.exists()) {
                    downloaded = (int) file.length();
                }

                if (fileLength == downloaded) {
                    success = true;
                    return null;
                }

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
                connection.setRequestMethod("GET");
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if ((connection.getResponseCode() != HttpURLConnection.HTTP_OK) && (connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL)) {

                    int rc = connection.getResponseCode();
                    String rm = connection.getResponseMessage();

                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                input = connection.getInputStream();
                output = new FileOutputStream(dir + "/" + filename, downloaded == 0 ? false : true);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);

                }
                success = true;
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();

                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }

}