package com.offsetnull.btfree;

import com.offsetnull.bt.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FreeLauncher extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_launcher_layout);
        
        Intent launch = new Intent(this,com.offsetnull.bt.launcher.Launcher.class);
        launch.putExtra("LAUNCH_MODE","com.happygoatstudios.bt");
        copySharedLibraries();
        this.startActivity(launch);
        this.finish();
    }

    /**
     * In more recent versions of android, shared libraries are not copied over to
     * /data/user/0/<package_name>/lib, which is a problem with LuaJIT that I didn't manage to solve
     *
     * This function copies the files from the /data/app/<random suffix>-<app_name>/lib in an
     * attempt to work around that. Please fix me
     *
     * @return
     */
    protected void copySharedLibraries() {
        Log.e("debug-rl", "copySharedLibraries: start");
        // Get the source directory of the APK
        File sourceDir = new File(getApplicationInfo().nativeLibraryDir);

        // Get the native library directory inside the APK
        File destDir = new File(getApplicationInfo().dataDir + "/lib");

        Log.i("debug-rl", "copySharedLibraries: from " + sourceDir.toString() + " to " + destDir.toString() );

        // Ensure destination directory exists
        if (!destDir.exists()) {
            destDir.mkdirs();
            Log.i("debug-rl", "Creating " + destDir.toString());
        }
        else {
            Log.i("debug-rl", destDir.toString() + " exists");
        }

        try {
            // Copy each .so file from source to destination
            File[] files = sourceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".so")) {
                        File destFile = new File(destDir, file.getName());
                        Log.e("debug-rl", "Trying to copy " + file.toString() + " to " + destFile.toString());
                        copyFile(file, destFile);
                        Log.e("debug-rl", "Copied " + file.toString() + " to " + destFile.toString());
                    }
                }
            }
        } catch (Exception e) {
            Log.i("BT_Free", "Error copying .so files", e);
        }
    }

    private void copyFile(File src, File dst) throws Exception {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}