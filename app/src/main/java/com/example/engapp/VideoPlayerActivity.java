package com.example.engapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.ui.PlayerView;

@OptIn(markerClass = UnstableApi.class)
public class VideoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayerActivity";
    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar progressBar;
    private TextView tvTitle, tvDescription;
    private ImageView btnBack;
    private String videoId;
    private String videoTitle;
    private String platform;
    private String streamUrl;
    private android.media.AudioFocusRequest focusRequest; // Store for proper cleanup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Get video data from intent
        videoId = getIntent().getStringExtra("videoId");
        videoTitle = getIntent().getStringExtra("videoTitle");
        platform = getIntent().getStringExtra("platform");
        streamUrl = getIntent().getStringExtra("streamUrl");

        Log.d(TAG, "Platform: " + platform);
        Log.d(TAG, "Video ID: " + videoId);
        Log.d(TAG, "Stream URL: " + streamUrl);
        Log.d(TAG, "Video Title: " + videoTitle);

        initViews();
        loadVideo();
    }

    private void initViews() {
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        btnBack = findViewById(R.id.btnBack);

        tvTitle.setText(videoTitle != null ? videoTitle : "Video");
        tvDescription.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> finish());

        // Initialize Media3 ExoPlayer with premium audio settings
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                .setEnableDecoderFallback(true);
        
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();
        
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    3000,   // Min buffer 3s
                    15000,  // Max buffer 15s
                    1500,   // Playback buffer 1.5s
                    3000    // Rebuffer 3s
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .setBackBuffer(5000, false) // 5s back buffer
                .build();
        
        player = new ExoPlayer.Builder(this)
                .setRenderersFactory(renderersFactory)
                .setAudioAttributes(audioAttributes, true)
                .setLoadControl(loadControl)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .setHandleAudioBecomingNoisy(true) // Auto pause when headphone unplugged
                .setSkipSilenceEnabled(false) // Keep all audio
                .build();
        
        // Set audio session ID for better audio processing
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            int audioSessionId = audioManager.generateAudioSessionId();
            player.setAudioSessionId(audioSessionId);
        }
        
        playerView.setPlayer(player);
        playerView.setControllerAutoShow(true);
        playerView.setUseController(true);
        playerView.setControllerShowTimeoutMs(3000);
        playerView.setKeepScreenOn(true);
        
        // Enable high quality audio offload
        player.setVolume(1.0f);
        
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (playbackState == Player.STATE_READY) {
                    Log.d(TAG, "Video ready to play");
                }
            }
            
            @Override
            public void onPlayerError(androidx.media3.common.PlaybackException error) {
                Log.e(TAG, "Player error: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(VideoPlayerActivity.this, "Video playback error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVideo() {
        String videoUrl = null;
        
        // Check if it's a direct video (MP4 from GitHub)
        if ("direct".equals(platform) && streamUrl != null && !streamUrl.isEmpty()) {
            videoUrl = streamUrl;
            Log.d(TAG, "Loading direct video: " + videoUrl);
        } 
        // Check if it's a YouTube video
        else if (videoId != null && !videoId.isEmpty()) {
            // For YouTube, open in YouTube app (ExoPlayer can't play YouTube directly)
            openInYouTube();
            return;
        } else {
            Log.e(TAG, "No valid video source");
            Toast.makeText(this, "No valid video source", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (videoUrl != null) {
            try {
                // Build MediaItem with adaptive streaming
                MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(Uri.parse(videoUrl))
                    .build();
                
                player.setMediaItem(mediaItem);
                player.prepare();
                player.setPlayWhenReady(true); // Autoplay for faster experience
                
                Log.d(TAG, "Video loaded successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error loading video: " + e.getMessage());
                Toast.makeText(this, "Error loading video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openInYouTube() {
        if (videoId == null || videoId.isEmpty()) {
            Toast.makeText(this, "Video ID not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
        
        try {
            // Try to open in YouTube app first
            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
            youtubeIntent.setPackage("com.google.android.youtube");
            startActivity(youtubeIntent);
            Log.d(TAG, "Opened in YouTube app: " + youtubeUrl);
            finish();
        } catch (Exception e) {
            // If YouTube app not installed, open in browser
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
                startActivity(browserIntent);
                Log.d(TAG, "Opened in browser: " + youtubeUrl);
                finish();
            } catch (Exception ex) {
                Toast.makeText(this, "Cannot open video: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error opening video: " + ex.getMessage());
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && !isFinishing()) {
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            // Resume if was playing
            player.setPlayWhenReady(true);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Request audio focus for uninterrupted playback
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.media.AudioManager audioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                focusRequest = new android.media.AudioFocusRequest.Builder(
                        android.media.AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(
                            new android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MOVIE)
                                .build()
                        )
                        .setOnAudioFocusChangeListener(focusChange -> {
                            if (player != null) {
                                switch (focusChange) {
                                    case android.media.AudioManager.AUDIOFOCUS_GAIN:
                                        player.setVolume(1.0f);
                                        if (!player.isPlaying()) {
                                            player.play();
                                        }
                                        break;
                                    case android.media.AudioManager.AUDIOFOCUS_LOSS:
                                        player.pause();
                                        break;
                                    case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                        player.pause();
                                        break;
                                    case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                        player.setVolume(0.3f);
                                        break;
                                }
                            }
                        })
                        .build();
                audioManager.requestAudioFocus(focusRequest);
            }
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Release audio focus when stopping
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.media.AudioManager audioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null && focusRequest != null) {
                audioManager.abandonAudioFocusRequest(focusRequest);
                focusRequest = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
        }
    }
}
