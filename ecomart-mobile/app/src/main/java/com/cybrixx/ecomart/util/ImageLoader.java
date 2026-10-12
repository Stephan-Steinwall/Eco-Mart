package com.cybrixx.ecomart.util;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.cybrixx.ecomart.network.NetworkErrors;

/**
 * Single entry point for loading product images (grid, cart, detail), so caching behaviour
 * (see {@link EcoMartGlideModule}) is identical everywhere.
 */
public final class ImageLoader {

    private ImageLoader() {}

    public static void load(ImageView view, String url) {
        RequestBuilder<Drawable> request = Glide.with(view).load(url);
        // Offline: serve from the cache only. Cached images appear instantly instead of waiting
        // on a network timeout, and uncached ones fail fast to the error drawable.
        if (!NetworkErrors.isOnline(view.getContext())) {
            request = request.onlyRetrieveFromCache(true);
        }
        request.into(view);
    }
}
