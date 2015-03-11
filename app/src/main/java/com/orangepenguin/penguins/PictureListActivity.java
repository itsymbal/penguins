package com.orangepenguin.penguins;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.mopub.common.MoPub;
import com.mopub.mobileads.MoPubView;
import com.orangepenguin.penguins.service.PxPhoto;
import com.orangepenguin.penguins.service.PxService;
import com.orangepenguin.penguins.service.SearchResults;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.fabric.sdk.android.Fabric;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PictureListActivity extends ActionBarActivity implements Callback<SearchResults> {
    private static final int NUM_COLUMNS = 2;

    @InjectView(R.id.px_list)// ButterKnife
    private RecyclerView mPxListView;

    // TODO: Replace this test id with your personal ad unit id
    private static final String MOPUB_BANNER_AD_UNIT_ID = "d4a0aba637d64a9f9a05a575fa757ac2";
    @InjectView(R.id.mopub_sample_ad) // ButterKnife
    private MoPubView moPubView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set up Fabric with Crashlytics and MoPub
        Fabric.with(this, new Crashlytics(), new MoPub());

        setContentView(R.layout.activity_px_list);

        ButterKnife.inject(this);

        // set up MoPub banner view
        moPubView.setAdUnitId(MOPUB_BANNER_AD_UNIT_ID);
        moPubView.loadAd();

        // set up Retrofit
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL: RestAdapter.LogLevel.NONE)
                .setEndpoint(PxService.API_URL)
                .build();
        final PxService service = restAdapter.create(PxService.class);

        mPxListView.setClickable(true);
        mPxListView.setHasFixedSize(true);
//        mPxListView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS,
//                GridLayoutManager.VERTICAL, false));
        mPxListView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        service.searchPhotos("penguin", this);

    }

    @Override
    public void success(SearchResults searchResults, Response response) {
        mPxListView.setAdapter(new PxListAdapter(this, searchResults.photos));
    }

    @Override
    public void failure(RetrofitError error) {
        Toast.makeText(this, "Failed to load photo list: " + error.getMessage(),
                Toast.LENGTH_LONG).show();
    }

    private static class PxListAdapter extends RecyclerView.Adapter<AppViewHolder> {
        private final LayoutInflater mInflater;
        private final Picasso mPicasso;
        private final List<PxPhoto> mPhotos;

        public PxListAdapter(Context context, List<PxPhoto> photos) {
            mInflater = LayoutInflater.from(context);
            mPicasso = Picasso.with(context);
            if (BuildConfig.DEBUG) {
                mPicasso.setIndicatorsEnabled(true);
            }
            mPhotos = photos;
        }

        @Override
        public int getItemCount() {
            return mPhotos.size();
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            final View view = mInflater.inflate(R.layout.px_frame, parent, false);
            return new AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AppViewHolder holder, int position) {
            mPicasso.load(mPhotos.get(position).image_url)
//                    .placeholder(R.drawable.placeholder)
                    .into(holder.image);
        }
    }

    private static class AppViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image;

        public AppViewHolder(View view) {
            super(view);
            this.image = (ImageView) view.findViewById(R.id.image);
        }
    }
}
