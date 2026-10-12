package com.cybrixx.ecomart.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.cybrixx.ecomart.R;

/**
 * App-wide image caching setup. Every Glide.with(...).load(url) in the app inherits these defaults.
 *
 * - Memory cache: recently shown images are reused instantly while scrolling.
 * - Disk cache (250 MB, LRU): images survive app restarts and still show when offline.
 * - DiskCacheStrategy.ALL: caches both the original download and the resized version, so the
 *   product grid thumbnail and the larger detail image never re-download each other.
 * - Consistent placeholder / error / fallback drawables and a short cross-fade.
 */
@GlideModule
public final class EcoMartGlideModule extends AppGlideModule {

    private static final long DISK_CACHE_BYTES = 250L * 1024 * 1024;

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(3) // default is 2 screens' worth of bitmaps
                .build();

        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize()))
                .setDiskCache(new InternalCacheDiskCacheFactory(context, "image_cache", DISK_CACHE_BYTES))
                .setDefaultRequestOptions(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_error)
                        .fallback(R.drawable.bg_image_placeholder)) // product with no image URL
                .setDefaultTransitionOptions(Drawable.class, DrawableTransitionOptions.withCrossFade(150));
    }

    // We configure everything in code; skip the slow, deprecated AndroidManifest meta-data scan.
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
