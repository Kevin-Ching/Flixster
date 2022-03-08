package com.example.flixster

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RatingBar
import android.widget.TextView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import okhttp3.Headers

private const val TAG = "DetailActivity"
private const val TRAILERS_URL = "https://api.themoviedb.org/3/movie/%d/videos?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed"

class DetailActivity : YouTubeBaseActivity() {

    private lateinit var youtubeApiKey: String
    private lateinit var tvTitle: TextView
    private lateinit var tvOverview: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var ytPlayerView: YouTubePlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            tvTitle = findViewById(R.id.tvTitle)
            tvOverview = findViewById(R.id.tvOverview)
            ratingBar = findViewById(R.id.rbVoteAverage)
        }
        ytPlayerView = findViewById(R.id.player)
        youtubeApiKey = getString(R.string.youtube_api_key)

        val movie = intent.getParcelableExtra<Movie>(MOVIE_EXTRA) as Movie
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            tvTitle.text = movie.title
            tvOverview.text = movie.overview
            ratingBar.rating = movie.voteAverage.toFloat()
        }

        val client = AsyncHttpClient()
        client.get(TRAILERS_URL.format(movie.movieId), object: JsonHttpResponseHandler() {
            override fun onFailure(
                statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?
            ) {
                Log.e(TAG, "onFailure $statusCode")
            }

            override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                Log.i(TAG, "onSuccess: JSON data $json")
                val results = json.jsonObject.getJSONArray("results")
                if (results.length() == 0) {
                    Log.w(TAG, "No movie trailers found")
                    return
                }
                val movieTrailerJson = results.getJSONObject(0)
                val youtubeKey = movieTrailerJson.getString("key")
                initializeYoutube(youtubeKey, movie.voteAverage > 5)
            }

        })
    }

    private fun initializeYoutube(youtubeKey: String, popular: Boolean) {
        ytPlayerView.initialize(youtubeApiKey, object: YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider?,
                player: YouTubePlayer?,
                p2: Boolean
            ) {
                Log.i(TAG, "onInitializationSuccess")
                if (popular) {
                    player?.loadVideo(youtubeKey)
                } else {
                    player?.cueVideo(youtubeKey)
                }
            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider?,
                p1: YouTubeInitializationResult?
            ) {
                Log.i(TAG, "onInitializationFailure")
            }
        })
    }
}