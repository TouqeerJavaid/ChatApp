package com.example.whats_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView image;
    private String imageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        image =findViewById(R.id.image_viewr);

        imageUrl = getIntent().getStringExtra("url");

        Picasso.get().load(imageUrl).into(image);
    }
}
