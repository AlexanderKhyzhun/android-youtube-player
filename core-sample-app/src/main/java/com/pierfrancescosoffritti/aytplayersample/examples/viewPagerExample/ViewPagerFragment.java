package com.pierfrancescosoffritti.aytplayersample.examples.viewPagerExample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.aytplayersample.R;
import com.pierfrancescosoffritti.aytplayersample.utils.VideoIdsProvider;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ViewPagerFragment extends Fragment {

    private String videoId = VideoIdsProvider.getNextVideoId();
    private YouTubePlayer initializedYouTubePlayer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_view_pager, container, false);

        YouTubePlayerView youTubePlayerView = view.findViewById(R.id.youtube_player_view);

        getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.getPlayerUIController().showFullscreenButton(false);

        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.cueVideo(videoId, 0);
                initializedYouTubePlayer = youTubePlayer;
            }
        }, true);

        return view;
    }

    // pause when fragment goes offscreen
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (!visible && initializedYouTubePlayer != null)
            initializedYouTubePlayer.pause();
    }
}