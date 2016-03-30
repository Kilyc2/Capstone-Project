package com.android.myepubs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.android.myepubs.R;
import com.android.myepubs.fragments.BookDetailFragment;
import com.android.myepubs.models.Book;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.plus.PlusShare;

public class BookDetailActivity extends AppCompatActivity {

    private String titleBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_book_detail);
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            Book book = getIntent().getParcelableExtra(BookDetailFragment.ARG_BOOK);
            titleBook = book.getTitle();
            arguments.putParcelable(BookDetailFragment.ARG_BOOK, book);
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.book_detail_container, fragment)
                    .commit();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.share_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new PlusShare.Builder(BookDetailActivity.this)
                        .setType("text/plain")
                        .setText(getString(R.string.share_text).concat(" ").concat(titleBook))
                        .getIntent());
            }
        });

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("2EBE85C1126BA8EC6C46D8C2224F4F9B").build();
        adView.loadAd(adRequest);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, BookListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
