package kr.ac.cu.moai.dcumusicplayer;

import static androidx.core.util.TimeUtils.formatDuration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.io.IOException;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private TextView seekBarTime;
    private Handler handler = new Handler();
    private Button playAndStopbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


        seekBar = findViewById(R.id.seekBar);
        seekBarTime = findViewById(R.id.seekBarTime);
        playAndStopbtn = findViewById(R.id.playAndStopBtn);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String mp3file = intent.getStringExtra("mp3");

        if (mp3file != null) {
            initializePlayer(mp3file);
        }

        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            ImageView ivCover = findViewById(R.id.ivCover);
            retriever.setDataSource(mp3file);
            byte[] b = retriever.getEmbeddedPicture();
            if (b != null) {
                Bitmap cover = BitmapFactory.decodeByteArray(b, 0, b.length);
                ivCover.setImageBitmap(cover);
            }

            TextView tvTitle = findViewById(R.id.tvTitle);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            tvTitle.setText(title);

            TextView tvDuration = findViewById(R.id.tvDuration);
            tvDuration.setText(ListViewMP3Adapter.getDuration(retriever));

            TextView tvArtist = findViewById(R.id.tvArtist);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            tvArtist.setText(artist);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        playAndStopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        playAndStopbtn.setText("▶");
                    } else {
                        mediaPlayer.start();
                        playAndStopbtn.setText("| |");
                        updateSeekBarAndTimer();
                    }
                }
            }
        });
    }

    private void initializePlayer(String mp3file) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mp3file);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(mp -> {
            seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.start();
            updateSeekBarAndTimer();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
            seekBar.setProgress(0);
            updateSeekBarAndTimer();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Thread updateSeekBar = new Thread() {
            @Override
            public void run() {
                int currentPosition = 0;
                int total = mediaPlayer.getDuration();
                while (currentPosition < total) {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        updateSeekBar.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    private void updateSeekBarAndTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    seekBarTime.setText(formatDuration(currentPosition));
                }
                handler.postDelayed(this, 100);
            }
        }, 0);
    }

    private String formatDuration(int duration) {
        int seconds = duration / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
