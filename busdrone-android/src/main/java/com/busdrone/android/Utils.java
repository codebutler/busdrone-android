package com.busdrone.android;

import android.app.ActivityManager;
import android.content.Context;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;

// From Picasso

public class Utils {
    private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_MEM_CACHE_SIZE = 20 * 1024 * 1024; // 20MB

    public static int calculateMemoryCacheSize(Context context) {
      ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
      boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
      int memoryClass = am.getLargeMemoryClass();
      // Target 15% of the available RAM.
      int size = 1024 * 1024 * memoryClass / 7;
      // Bound to max size for mem cache.
      return Math.min(size, MAX_MEM_CACHE_SIZE);
    }
}
