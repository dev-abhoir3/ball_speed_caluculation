package com.example.ballspeedcalculator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class FrameSelectionActivity extends AppCompatActivity {

    private static final String TAG = "FrameSelectionActivity";

    private RecyclerView recyclerViewFrames;
    private FrameAdapter frameAdapter;
    private EditText inputStartFrame;
    private EditText inputEndFrame;
    private Button buttonCalculateSpeed;
    private List<Bitmap> frameList;
    private Uri videoUri;
    private long duration;
    private int frameRate;
    private long frameInterval;
    private long currentFramePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_selection);

        recyclerViewFrames = findViewById(R.id.recycler_view_frames);
        inputStartFrame = findViewById(R.id.input_start_frame);
        inputEndFrame = findViewById(R.id.input_end_frame);
        buttonCalculateSpeed = findViewById(R.id.button_calculate_speed);

        recyclerViewFrames.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        frameList = new ArrayList<>();
        frameAdapter = new FrameAdapter(frameList);
        recyclerViewFrames.setAdapter(frameAdapter);

        videoUri = getIntent().getParcelableExtra("videoUri");
        initializeMetadata();

        loadFramesBatch();

        recyclerViewFrames.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == frameList.size() - 1) {
                    loadFramesBatch();
                }
            }
        });

        buttonCalculateSpeed.setOnClickListener(v -> calculateSpeed());
    }

    private void initializeMetadata() {
        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
        try {
            retriever.setDataSource(this, videoUri);
            String durationStr = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Long.parseLong(durationStr) * 1000; // Convert to microseconds

            String frameRateStr = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE);
            frameRate = frameRateStr != null ? Integer.parseInt(frameRateStr) : 25; // Default to 25 fps if frame rate is unavailable
            frameInterval = 1000000 / frameRate; // Interval for each frame in microseconds

            Log.d(TAG, "Video duration (microseconds): " + duration);
            Log.d(TAG, "Frame rate: " + frameRate);
            Log.d(TAG, "Frame interval (microseconds): " + frameInterval);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing metadata", e);
        } finally {
            retriever.release();
        }
    }

    private void loadFramesBatch() {
        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, videoUri);
            mmr.setDataSource(this, videoUri);

            long endFramePosition = Math.min(currentFramePosition + (30 * frameInterval), duration);

            for (long i = currentFramePosition; i < endFramePosition; i += frameInterval) {
                Bitmap bitmap = retriever.getFrameAtTime(i, FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (bitmap == null) {
                    bitmap = mmr.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST);
                }
                if (bitmap != null) {
                    Log.d(TAG, "Extracted frame at: " + i + " microseconds");
                    frameList.add(bitmap);
                } else {
                    Log.d(TAG, "No frame extracted at: " + i + " microseconds");
                }
            }
            frameAdapter.notifyDataSetChanged();
            currentFramePosition = endFramePosition;
            Log.d(TAG, "Total frames extracted: " + frameList.size());
        } catch (Exception e) {
            Log.e(TAG, "Error extracting frames", e);
        } finally {
            retriever.release();
            try {
                mmr.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void calculateSpeed() {
        String startFrameStr = inputStartFrame.getText().toString();
        String endFrameStr = inputEndFrame.getText().toString();

        if (!startFrameStr.isEmpty() && !endFrameStr.isEmpty()) {
            int startFrame = Integer.parseInt(startFrameStr);
            int endFrame = Integer.parseInt(endFrameStr);

            Intent intent = new Intent(this, SpeedCalculationActivity.class);
            intent.putExtra("startFrame", startFrame);
            intent.putExtra("endFrame", endFrame);
            intent.putExtra("frameRate", getIntent().getFloatExtra("frameRate", 0));
            startActivity(intent);
        }
    }
}
