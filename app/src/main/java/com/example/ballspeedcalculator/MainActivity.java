package com.example.ballspeedcalculator;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import wseemann.media.FFmpegMediaMetadataRetriever;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_VIDEO = 1;
    private Button buttonSelectVideo;
    private ImageView imageThumbnail;
    private TextView textFrameRate;
    private Button buttonProceed;
    private String videoPath;
    private float frameRate;
    private Uri selectedVideoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSelectVideo = findViewById(R.id.button_select_video);
        imageThumbnail = findViewById(R.id.image_thumbnail);
        textFrameRate = findViewById(R.id.text_frame_rate);
        buttonProceed = findViewById(R.id.button_proceed);

        buttonSelectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVideo();
            }
        });

        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToFrameSelection(selectedVideoUri);
            }
        });
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                selectedVideoUri = videoUri;
                displayVideoThumbnail(videoUri);
                try {
                    extractAndDisplayMetadata(videoUri);
                    buttonProceed.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    private void displayVideoThumbnail(Uri videoUri) {
        Glide.with(this)
                .load(videoUri)
                .into(imageThumbnail);
    }

    private void extractAndDisplayMetadata(Uri videoUri) throws IOException {
        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
        try {
            retriever.setDataSource(this, videoUri);
            String frameRateStr = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE);
            frameRate = frameRateStr != null ? Float.parseFloat(frameRateStr) : 0;
            textFrameRate.setText(String.format("Frame Rate: %s fps", frameRateStr));
        } finally {
            retriever.release();
        }
    }

    private void navigateToFrameSelection(Uri videoUri) {
        Intent intent = new Intent(this, FrameSelectionActivity.class);
        intent.putExtra("videoUri", videoUri); // Pass URI directly
        intent.putExtra("frameRate", frameRate);
        startActivity(intent);
    }
}
