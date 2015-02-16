package com.herokuapp.ezhao.codepathimagesearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.herokuapp.ezhao.codepathimagesearch.adapters.ImageResultsAdapter;
import com.herokuapp.ezhao.codepathimagesearch.fragments.SearchOptionsDialog;
import com.herokuapp.ezhao.codepathimagesearch.interfaces.EndlessScrollListener;
import com.herokuapp.ezhao.codepathimagesearch.models.ImageResult;
import com.herokuapp.ezhao.codepathimagesearch.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SearchActivity extends ActionBarActivity implements SearchOptionsDialog.SearchOptionsListener {
    public static final String SEARCH_OPTIONS_TAG = "search_options";
    private ArrayList<ImageResult> imageResults;
    private ImageResultsAdapter aImageResults;
    private String query;
    private String optionSize;
    private String optionColor;
    private String optionType;
    private String optionSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        imageResults = new ArrayList<>();
        aImageResults = new ImageResultsAdapter(this, imageResults);
        setupViews();

        optionSize = "any";
        optionColor = "any";
        optionType = "any";
        optionSite = "";

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "The internet is NOT working", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupViews() {
        StaggeredGridView gvResults = (StaggeredGridView) findViewById(R.id.gvResults);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(SearchActivity.this, ImageDetailActivity.class);
                ImageResult result = imageResults.get(position);
                i.putExtra("result", result);
                startActivity(i);
            }
        });
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                fetchResults(totalItemsCount, false);
            }
        });
        gvResults.setAdapter(aImageResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.miSearch);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                query = s;
                if (!query.isEmpty()) {
                    fetchResults(0, true);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.miSearchSettings) {
            SearchOptionsDialog searchOptionsDialog = SearchOptionsDialog.newInstance(optionSize, optionColor, optionType, optionSite);
            searchOptionsDialog.show(getFragmentManager(), SEARCH_OPTIONS_TAG);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchResults(int start, final boolean clearOnFetch) {
        if (query == null || query.isEmpty()) {
            Log.i("EMILY", "fetchResults called without a query");
            return; // Can't do anything without a query
        }
        if (start >= 64) {
            return; // Can't do anything past the 8th page
        }

        // Apply settings
        String searchUrl = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8";
        searchUrl = searchUrl + "&q=" + query;
        if (start > 0) {
            searchUrl = searchUrl + "&start=" + start;
        }
        if (!optionSize.isEmpty() && !optionSize.equals("any")) {
            searchUrl = searchUrl + "&imgsz=" + optionSize;
        }
        if (!optionColor.isEmpty() && !optionColor.equals("any")) {
            searchUrl = searchUrl + "&imgcolor=" + optionColor;
        }
        if (!optionType.isEmpty() && !optionType.equals("any")) {
            searchUrl = searchUrl + "&imgtype=" + optionType;
        }
        if (!optionSite.isEmpty()) {
            searchUrl = searchUrl + "&as_sitesearch=" + optionSite;
        }
        Log.i("EMILY", "Logging final searchUrl:" + searchUrl);

        // Search and populate grid
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(searchUrl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray imageResultsJson;
                try {
                    imageResultsJson = response.getJSONObject("responseData").getJSONArray("results");
                    if(clearOnFetch) {
                        aImageResults.clear();
                    }
                    aImageResults.addAll(ImageResult.fromJSONArray(imageResultsJson));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onSearchOptionsEdited(String optionSize, String optionColor, String optionType, String optionSite) {
        this.optionSize = optionSize;
        this.optionColor = optionColor;
        this.optionType = optionType;
        this.optionSite = optionSite;
        if (query != null && !query.isEmpty()) {
            fetchResults(0, true);
        }
    }
}
