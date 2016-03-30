package com.android.myepubs.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.myepubs.R;
import com.android.myepubs.adapters.BookRecyclerViewAdapter;
import com.android.myepubs.data.BooksTable;
import com.android.myepubs.helpers.CursorHelper;
import com.android.myepubs.models.Book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String KEY_STATE_BOOKS = "kStateBooks";
    private final static int LOADER_ID = 0;
    protected boolean isTwoPane;
    private RecyclerView recyclerView;
    private List<Book> booksInLibrary = new ArrayList<>();
    private ProgressBar spinner;
    private String sortOrder;
    private String displayMode;
    private BookRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        spinner = (ProgressBar) findViewById(R.id.progress_bar_library);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        isTwoPane = findViewById(R.id.book_detail_container) != null;

        recyclerView = (RecyclerView)findViewById(R.id.book_list);
        assert recyclerView != null;
        setReciclerView();
        adapter = new BookRecyclerViewAdapter(this, this.booksInLibrary, this.isTwoPane,
                getSupportFragmentManager());
        recyclerView.setAdapter(adapter);

        if(savedInstanceState == null || !savedInstanceState.containsKey(KEY_STATE_BOOKS)) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        } else {
            List<Book> books = savedInstanceState.getParcelableArrayList(KEY_STATE_BOOKS);
            setLibrary(books);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(
                this,
                BooksTable.getContentUri(),
                null, null, null, sortOrder
        );
        this.spinner.setVisibility(View.VISIBLE);
        return loader;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<Book> books = new ArrayList<>();
        if(CursorHelper.isValidCursor(cursor)) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(BooksTable._ID));
                String title = cursor.getString(
                        cursor.getColumnIndex(BooksTable.COLUMN_TITLE));
                String authors = cursor.getString(
                        cursor.getColumnIndex(BooksTable.COLUMN_AUTHORS));
                String coverPath = cursor.getString(
                        cursor.getColumnIndex(BooksTable.COLUMN_COVER_PATH));
                String date = cursor.getString(
                        cursor.getColumnIndex(BooksTable.COLUMN_DATE));
                Book book = new Book(id, title, authors, coverPath, date);
                books.add(book);
            } while (cursor.moveToNext());
        }
        setLibrary(books);
        this.spinner.setVisibility(View.INVISIBLE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLibrary(List<Book> books) {
        this.booksInLibrary.clear();
        for (Book book : books) {
            this.booksInLibrary.add(book);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_STATE_BOOKS,
                (ArrayList<? extends Parcelable>) this.booksInLibrary);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (setReciclerView()) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private boolean setReciclerView() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSortOrderChanged = changeSortOrder(sharedPref.getString(
                SettingsActivity.PREFERENCE_SORT_ORDER,
                BooksTable.COLUMN_TITLE));
        boolean isDisplayModeChanged = changeDisplayMode(sharedPref.getString(
                SettingsActivity.PREFERENCE_DISPLAY,
                getString(R.string.display_list)));
        return isSortOrderChanged || isDisplayModeChanged;
    }

    private boolean changeSortOrder(String sortOrder) {
        if (sortOrder.equals(this.sortOrder)) {
            return false;
        }
        this.sortOrder = sortOrder;
        boolean isOrderByTitle = sortOrder.equals(getString(R.string.sort_order_title));
        if (isOrderByTitle) {
            Collections.sort(booksInLibrary, new Comparator<Book>() {
                @Override
                public int compare(Book lhs, Book rhs) {
                    return lhs.getTitle().compareTo(rhs.getTitle());
                }
            });
        } else {
            Collections.sort(booksInLibrary, new Comparator<Book>() {
                @Override
                public int compare(Book lhs, Book rhs) {
                    return lhs.getDate().compareTo(rhs.getDate());
                }
            });
        }
        return true;
    }

    private boolean changeDisplayMode(String displayMode) {
        if (displayMode.equals(this.displayMode)) {
            return false;
        }
        this.displayMode = displayMode;
        boolean isLinearLayout = displayMode.equals(getString(R.string.display_list));
        if (isLinearLayout) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
        recyclerView.setTag(isLinearLayout);
        return true;
    }
}
