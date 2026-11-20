package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"

    const val ADMOB_APP_ID = "ca-app-pub-3388044983655718~3430672527"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3388044983655718/9804509186"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3388044983655718/9577725583"

    private const val INTERSTITIAL_COOLDOWN_MS = 90_000L

    @Volatile
    private var isInitialized = false

    @Volatile
    private var interstitialAd: InterstitialAd? = null

    @Volatile
    private var isLoadingInterstitial = false

    @Volatile
    private var lastInterstitialShownAt = 0L

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context.applicationContext) {
                isInitialized = true
                preloadInterstitial(context.applicationContext)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "MobileAds initialization skipped: ${t.message}")
        }
    }

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                        Log.d(TAG, "Interstitial failed to load: ${error.message}")
                    }
                }
            )
        } catch (t: Throwable) {
            isLoadingInterstitial = false
            Log.w(TAG, "Interstitial preload skipped: ${t.message}")
        }
    }

    fun showInterstitialIfReady(
        activity: Activity?,
        onCompleted: () -> Unit = {}
    ) {
        val now = System.currentTimeMillis()
        val ad = interstitialAd
        if (activity == null || ad == null || (now - lastInterstitialShownAt) < INTERSTITIAL_COOLDOWN_MS) {
            if (activity != null && ad == null) {
                preloadInterstitial(activity)
            }
            onCompleted()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastInterstitialShownAt = System.currentTimeMillis()
                preloadInterstitial(activity)
                onCompleted()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                preloadInterstitial(activity)
                onCompleted()
            }
        }
        try {
            ad.show(activity)
        } catch (t: Throwable) {
            interstitialAd = null
            onCompleted()
        }
    }
}

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID
) {
    if (LocalInspectionMode.current) return

    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtLeast(320)
    var adLoaded by remember { mutableStateOf(false) }

    val adView = remember(adUnitId, screenWidthDp) {
        runCatching {
            AdView(context).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        context,
                        screenWidthDp
                    )
                )
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        adLoaded = true
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        adLoaded = false
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }.getOrNull()
    }

    DisposableEffect(adView) {
        onDispose {
            runCatching { adView?.destroy() }
        }
    }

    if (adView != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                .padding(vertical = if (adLoaded) 2.dp else 0.dp)
                .testTag("admob_banner_container"),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { adView },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
