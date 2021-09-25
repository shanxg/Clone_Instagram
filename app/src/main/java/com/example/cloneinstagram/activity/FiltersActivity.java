package com.example.cloneinstagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cloneinstagram.R;
import com.example.cloneinstagram.adapter.AdapterImageFilters;
import com.example.cloneinstagram.helper.BitmapHelper;
import com.example.cloneinstagram.helper.RecyclerItemClickListener;

import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FiltersActivity extends AppCompatActivity {

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private List<ThumbnailItem> filtersList;

    private Bitmap image, atFilterImage;

    private ImageView filterImageView;
    private RecyclerView recyclerImageFilter;
    private AdapterImageFilters adapterFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filters);

        filtersList = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        toolbar.setTitle("Choose a filter");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        filterImageView = findViewById(R.id.filterImageView);
        recyclerImageFilter = findViewById(R.id.filterRecycler);


        image = BitmapHelper.getInstance().getBitmap();
        if (image != null) {
            atFilterImage = image.copy(image.getConfig(), true);
            filterImageView.setImageBitmap(atFilterImage);


            adapterFilters = new AdapterImageFilters(filtersList, this);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
            recyclerImageFilter.setLayoutManager(layoutManager);
            recyclerImageFilter.setAdapter(adapterFilters);

            getFiltersData();

            recyclerImageFilter.addOnItemTouchListener(
                    new RecyclerItemClickListener(
                            this, recyclerImageFilter,
                            new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    Filter filter = filtersList.get(position).filter;

                                    atFilterImage.recycle();
                                    atFilterImage = image.copy(image.getConfig(), true);
                                    filterImageView.setImageBitmap(filter.processFilter(atFilterImage));
                                }

                                @Override
                                public void onLongItemClick(View view, int position) {
                                }

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                }
                            }));
        }
    }

    private void getFiltersData() {

        ThumbnailsManager.clearThumbs();
        filtersList.clear();

        List<Filter> filters = FilterPack.getFilterPack(this);

        for (Filter filter : filters) {

            ThumbnailItem item = new ThumbnailItem();
            item.image = image;
            item.filter = filter;
            item.filterName = filter.getName();
            ThumbnailsManager.addThumb(item);

        }

        filtersList.addAll(ThumbnailsManager.processThumbs(getApplicationContext()));
        adapterFilters.notifyDataSetChanged();
    }

    /** ##############################     OPEN ACTIVITIES     ################################# **/

    private void goPostActivity(){

        if(atFilterImage!=null) {
            BitmapHelper.getInstance().setBitmap(atFilterImage);
        }
        else BitmapHelper.getInstance().setBitmap(image);

        Intent intentPost = new Intent(this, PostActivity.class);

        if(BitmapHelper.getInstance().getBitmap() !=null) {
            startActivity(intentPost);
        }else throwToast("Null Bitmap", true);
    }

    /** ##############################    ACTIVITY PROCESS    ################################# **/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_post, menu);

        menu.findItem(R.id.menuGoForward).setIcon(R.drawable.ic_arrow_forward_black_24dp);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.menuGoForward){
            goPostActivity();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        Intent i  = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
        return true;
    }

    /** ##############################        UTILITIES        ################################# **/

    private void throwToast(String msgText, boolean longToast) {

        if (longToast) {
            Toast.makeText(this,
                    msgText,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    msgText,
                    Toast.LENGTH_SHORT).show();
        }
    }
}