// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class PastebinPoster {
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    
    public static void paste(String code, PasteCallback callback) {
        PasteProcessor processor = new PasteProcessor(code, callback);
        Thread thread = new Thread(processor);
        thread.start();
    }
    
    public static interface PasteCallback {
        public void handleSuccess(String url);
        public void handleError(String err);
    }
    
    private static class PasteProcessor implements Runnable {
        private String code;
        private PasteCallback callback;
        
        public PasteProcessor(String code, PasteCallback callback) {
            this.code = code;
            this.callback = callback;
        }
        
        public void run() {
            HttpURLConnection conn = null;
            OutputStream out = null; 
            InputStream in = null;
            
            try {
                URL url = new URL("http://pastebin.com/api/api_post.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.addRequestProperty("Content-type",
                        "application/x-www-form-urlencoded");
                conn.setInstanceFollowRedirects(false);
                conn.setDoOutput(true);
                out = conn.getOutputStream();
                
                out.write(("api_option=paste"
                        + "&api_dev_key=" + URLEncoder.encode("4867eae74c6990dbdef07c543cf8f805", "utf-8")
                        + "&api_paste_code=" + URLEncoder.encode(code, "utf-8")
                        + "&api_paste_private=" + URLEncoder.encode("0", "utf-8")
                        + "&api_paste_name=" + URLEncoder.encode("", "utf-8")
                        + "&api_paste_expire_date=" + URLEncoder.encode("1D", "utf-8")
                        + "&api_paste_format=" + URLEncoder.encode("text", "utf-8")
                        + "&api_user_key=" + URLEncoder.encode("", "utf-8")).getBytes());
                out.flush();
                out.close();
                
                if (conn.getResponseCode() == 200) {//Get Response        
                    in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                        response.append("\r\n");
                    }
                    reader.close();
                    
                    String result = response.toString().trim();
                    
                    if (result.matches("^https?://.*")) {
                        callback.handleSuccess(result.trim());
                    } else {
                        String err =result.trim();
                        if (err.length() > 100) {
                            err = err.substring(0, 100);
                        }
                        callback.handleError(err);
                    }
                } else {
                    callback.handleError("didn't get a 200 response code!");
                }
            } catch (IOException e) {
                callback.handleError(e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        
    }
    
}
