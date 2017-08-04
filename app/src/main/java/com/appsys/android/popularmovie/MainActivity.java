package com.appsys.android.popularmovie;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appsys.android.popularmovie.classes.Movie;
import com.appsys.android.popularmovie.classes.MovieDbException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    RecyclerView mRecyclerView;
    MovieAdapter mMovieAdapter;
    TextView mErrorTextView;
    ProgressBar mProgressBar;
    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mErrorTextView = (TextView) findViewById(R.id.error_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_view);

        GridLayoutManager glm;
        int o = getResources().getConfiguration().orientation;
        if (o == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            glm = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        } else {
            glm = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        }

        mRecyclerView.setLayoutManager(glm);

        mMovieAdapter = new MovieAdapter(this);
        mMovieAdapter.setMoviesData(new ArrayList<Movie>());
        mRecyclerView.setAdapter(mMovieAdapter);
        if (savedInstanceState == null || !savedInstanceState.containsKey("Movie")) {
            new FetchMovieTask(true).execute(1);
        } else {
            mMovieAdapter.setMoviesData(savedInstanceState.<Movie>getParcelableArrayList("Movie"));
        }
    }

    @Override
    public void onClick(Movie m) {
//        Toast.makeText(this, m.getTitle(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MovieDetail.class);
        i.putExtra(Intent.EXTRA_TEXT, m);
        startActivity(i);
    }

    public void showMessage(String message) {
        if (mToast != null)
            mToast.cancel();

        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id
        switch (item.getItemId()) {
            case R.id.action_sort_popular:
                new FetchMovieTask(true).execute(1);
                return true;
            case R.id.action_sort_top_rated:
                new FetchMovieTask(false).execute(1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUI(ViewEnum e) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);

        switch (e) {
            case Data:
                mRecyclerView.setVisibility(View.VISIBLE);
                break;
            case Progress:
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            default:
                mErrorTextView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("Movie", mMovieAdapter.getMoviesData());
        super.onSaveInstanceState(outState);
    }

    public class FetchMovieTask extends AsyncTask<Integer, Void, ArrayList<Movie>> {

        boolean mPopular = true;
        String mErrorMessage = "";
        public FetchMovieTask(boolean popular) {
            mPopular = popular;
        }

        @Override
        protected void onPreExecute() {
            setUI(ViewEnum.Progress);
        }

        @Override
        protected ArrayList<Movie> doInBackground(Integer... integers) {
            if (integers.length == 0)
                return null;

            try {
                JSONObject json;
                if (mPopular)
                    json = NetworkUtils.getPopularMovies(1);
                else
                    json = NetworkUtils.getTopRatedMovies(1);

                if (json.has("results")) {
                    JSONArray ja = json.getJSONArray("results");
                    return Movie.getArrayByJSON(ja);
                }
            } catch (MovieDbException e) {
                mErrorMessage = e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                mErrorMessage = "Please check your Internet connection";
                e.printStackTrace();
            } catch (Exception e) {
                mErrorMessage = "Main: " + e.getMessage();
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            if (movies == null) {
                setUI(ViewEnum.Error);
                showMessage(mErrorMessage);
            } else {
                setUI(ViewEnum.Data);
                mMovieAdapter.setMoviesData(movies);
            }
        }
    }
}
