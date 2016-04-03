package com.android.myepubs.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.myepubs.R;
import com.android.myepubs.data.BooksTable;
import com.android.myepubs.helpers.CursorHelper;

import java.util.ArrayList;
import java.util.List;


public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final int COLUMN_TITLE = 1;
    public static final int COLUMN_COVER_PATH = 3;

    private List<String> books = new ArrayList<>();
    private Context context;

    public WidgetFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        this.books.clear();
        long identityToken = Binder.clearCallingIdentity();
        try {
            ContentResolver contentResolver = this.context.getContentResolver();
            Cursor cursor =
                    contentResolver.query(BooksTable.getContentUri(),
                            null, null, null, null);
            if (CursorHelper.isValidCursor(cursor)) {
                do {
                    String book = cursor.getString(COLUMN_TITLE) + ";" +
                            cursor.getString(COLUMN_COVER_PATH);
                    this.books.add(book);
                } while (cursor.moveToNext());
            } else {
                this.books.add("");
            }
            Log.v("WIDGET", "Books: " + this.books.size());
            CursorHelper.closeCursor(cursor);
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        String book = this.books.get(position);
        if (book.isEmpty()) {
            return new RemoteViews(context.getPackageName(),
                    R.layout.no_books_item);
        } else {
            RemoteViews mView = new RemoteViews(context.getPackageName(),
                    R.layout.widget_item);
            String[] bookValues = book.split(";");
            Bitmap cover = getCover(bookValues[1]);
            mView.setImageViewBitmap(R.id.cover_widget, cover);
            mView.setTextViewText(R.id.title_widget, bookValues[0]);
            mView.setTextColor(R.id.title_widget, Color.BLACK);
            return mView;
        }
    }

    private Bitmap getCover(String pathCover) {
        Bitmap cover = BitmapFactory.decodeFile(pathCover, new BitmapFactory.Options());
        return Bitmap.createScaledBitmap(cover, 100, 150, true);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
