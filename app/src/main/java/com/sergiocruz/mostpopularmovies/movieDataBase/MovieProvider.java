package com.sergiocruz.mostpopularmovies.movieDataBase;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.sergiocruz.mostpopularmovies.movieDataBase.MovieContract.MovieTable.MOVIES_TABLE_NAME;

/**
 * Created by Sergio on 23/02/2018.
 */

public class MovieProvider extends ContentProvider {
    // Define final integer constants for the directory of movies and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, 103, ...) for items in that directory.
    public static final int MOVIES = 100;
    public static final int MOVIES_WITH_ID = 101;
    public static final int VIDEOS = 200;
    public static final int VIDEOS_WITH_ID = 201;
    public static final int REVIEWS = 300;
    public static final int REVIEWS_WITH_ID = 301;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper movieDbHelper;

    // Define a static buildUriMatcher method that associates URI's with their int match

    /**
     * Initialize a new matcher object without any matches,
     * then use .addURI(String authority, String path, int match) to add matches
     */
    public static UriMatcher buildUriMatcher() {
        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /* All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the movies directory and a single item by ID. */
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIES_WITH_ID);

        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_VIDEOS, VIDEOS);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_VIDEOS + "/#", VIDEOS_WITH_ID);

        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_REVIEWS, REVIEWS);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_REVIEWS + "/#", REVIEWS_WITH_ID);

        return uriMatcher;
    }

    /**
     * Implement this to initialize your content provider on startup.
     * This method is called for all registered content providers on the
     * application main thread at application launch time.  It must not perform
     * lengthy operations, or application startup will be delayed.
     * <p>
     * <p>You should defer nontrivial initialization (such as opening,
     * upgrading, and scanning databases) until the content provider is used
     * (via {@link #query}, {@link #insert}, etc).  Deferred initialization
     * keeps application startup fast, avoids unnecessary work if the provider
     * turns out not to be needed, and stops database errors (such as a full
     * disk) from halting application launch.
     * <p>
     * <p>If you use SQLite, {@link SQLiteOpenHelper}
     * is a helpful utility class that makes it easy to manage databases,
     * and will automatically defer opening until first use.  If you do use
     * SQLiteOpenHelper, make sure to avoid calling
     * {@link SQLiteOpenHelper#getReadableDatabase} or
     * {@link SQLiteOpenHelper#getWritableDatabase}
     * from this method.  (Instead, override
     * {@link SQLiteOpenHelper#onOpen} to initialize the
     * database when it is first opened.)
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDBHelper(getContext());
        return true;
    }

    /**
     * Implement this to handle query requests from clients.
     * <p>
     * <p>Apps targeting {@link Build.VERSION_CODES#O} or higher should override
     * {@link #query(Uri, String[], String, String[], String)} and provide a stub
     * implementation of this method.
     * <p>
     * <p>This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p>
     * Example client call:<p>
     * <pre>// Request a specific record.
     * Cursor managedCursor = managedQuery(
     * ContentUris.withAppendedId(Contacts.People.MOVIES_CONTENT_URI, 2),
     * projection,    // Which columns to return.
     * null,          // WHERE clause.
     * null,          // WHERE clause value substitution
     * People.NAME + " ASC");   // Sort order.</pre>
     * Example implementation:<p>
     * <pre>// SQLiteQueryBuilder is a helper class that creates the
     * // proper SQL syntax for us.
     * SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
     *
     * // Set the table we're querying.
     * qBuilder.setTables(DATABASE_TABLE_NAME);
     *
     * // If the query ends in a specific record number, we're
     * // being asked for a specific record, so set the
     * // WHERE clause in our query.
     * if((URI_MATCHER.match(uri)) == SPECIFIC_MESSAGE){
     * qBuilder.appendWhere("_id=" + uri.getPathLeafId());
     * }
     *
     * // Make the query.
     * Cursor c = qBuilder.query(mDb,
     * projection,
     * selection,
     * selectionArgs,
     * groupBy,
     * having,
     * sortOrder);
     * c.setNotificationUri(getContext().getContentResolver(), uri);
     * return c;</pre>
     *
     * @param uri           The URI to query. This will be the full URI sent by the client;
     *                      if the client is requesting a specific record, the URI will end in a record number
     *                      that the implementation should parse and add to a WHERE or HAVING clause, specifying
     *                      that _id value.
     * @param projection    The list of columns to put into the cursor. If
     *                      {@code null} all columns are included.
     * @param selection     A selection criteria to apply when filtering rows.
     *                      If {@code null} then all rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the selection.
     *                      The values will be bound as Strings.
     * @param sortOrder     How the rows in the cursor should be sorted.
     *                      If {@code null} then the provider is free to define the sort order.
     * @return a Cursor or {@code null}.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();

        Cursor resultCursor;
        String id;

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                resultCursor = db.query(MovieContract.MovieTable.MOVIES_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case MOVIES_WITH_ID:
                // Get the movie ID from the URI path
                // Use selections/selectionArgs to filter for this ID
                // Check if the movie_id is in the DB
                id = uri.getPathSegments().get(1);
                resultCursor = db.query(MOVIES_TABLE_NAME,
                        new String[]{MovieContract.MovieTable.MOVIE_ID},
                        MovieContract.MovieTable.MOVIE_ID + "=?",
                        new String[]{id},
                        null,
                        null,
                        null);
                break;

            case VIDEOS_WITH_ID:
                id = uri.getPathSegments().get(1);
                resultCursor = db.query(MovieContract.VideosTable.VIDEOS_TABLE_NAME,
                        projection,
                        MovieContract.VideosTable.MOVIE_ID + "=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;

            case REVIEWS_WITH_ID:
                id = uri.getPathSegments().get(1);
                resultCursor = db.query(MovieContract.ReviewsTable.REVIEWS_TABLE_NAME,
                        projection,
                        MovieContract.ReviewsTable.MOVIE_ID + "=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return resultCursor;
    }

    /**
     * Implement this to handle requests for the MIME type of the data at the
     * given URI.  The returned MIME type should start with
     * <code>vnd.android.cursor.item</code> for a single record,
     * or <code>vnd.android.cursor.dir/</code> for multiple items.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p>
     * <p>Note that there are no permissions needed for an application to
     * access this information; if your content provider requires read and/or
     * write permissions, or is not exported, all applications can still call
     * this method regardless of their access permissions.  This allows them
     * to retrieve the MIME type for a URI when dispatching intents.
     *
     * @param uri the URI to query.
     * @return a MIME type string, or {@code null} if there is no type.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        switch (sUriMatcher.match(uri)) {
            case MOVIES_WITH_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE;
            case MOVIES:
                return ContentResolver.CURSOR_DIR_BASE_TYPE;
            case VIDEOS_WITH_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE;
            case VIDEOS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE;
            case REVIEWS_WITH_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE;
            case REVIEWS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE;
            default:
                return null;
        }
    }

    /**
     * Implement this to handle requests to insert a new row.
     * As a courtesy, call {@link ContentResolver#notifyChange(Uri, ContentObserver) notifyChange()}
     * after inserting.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     *
     * @param uri    The content:// URI of the insertion request. This must not be {@code null}.
     * @param values A set of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     * @return The URI for the newly inserted item.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        Uri returnUri;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                long id = db.insert(MOVIES_TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieTable.MOVIES_CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }



    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        int rowsInserted;
        switch (sUriMatcher.match(uri)) {
            case VIDEOS:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    int valuesLength = values.length;
                    for (int i = 0; i < valuesLength; i++) {
                        // Insert new values into the database Inserting values into tasks table
                        long id = db.insert(MovieContract.VideosTable.VIDEOS_TABLE_NAME, null, values[i]);
                        if (id > 0) {
                            rowsInserted++;
                            Log.i("Sergio>", this + "\n" +
                                    "inserted value " + i + " = " + values[i]);
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    // Notify the resolver if the uri has been changed, and return the newly inserted URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            case REVIEWS:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    int valuesLength = values.length;
                    for (int i = 0; i < valuesLength; i++) {
                        // Insert new values into the database Inserting values into tasks table
                        long id = db.insert(MovieContract.ReviewsTable.REVIEWS_TABLE_NAME, null, values[i]);
                        if (id > 0) {
                            rowsInserted++;
                            Log.i("Sergio>", this + "\n" +
                                    "inserted value " + i + " = " + values[i]);
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    // Notify the resolver if the uri has been changed, and return the newly inserted URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }


    }


    /**
     * Implement this to handle requests to delete one or more rows.
     * The implementation should apply the selection clause when performing
     * deletion, allowing the operation to affect multiple rows in a directory.
     * As a courtesy, call {@link ContentResolver#notifyChange(Uri, ContentObserver) notifyChange()}
     * after deleting.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p>
     * <p>The implementation is responsible for parsing out a row ID at the end
     * of the URI, if a specific row is being deleted. That is, the client would
     * pass in <code>content://contacts/people/22</code> and the implementation is
     * responsible for parsing the record number (22) when creating a SQL statement.
     *
     * @param uri           The full URI to query, including a row ID (if a specific record is requested).
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs
     * @return The number of rows affected.
     * @throws SQLException
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted movies
        int moviesDeleted; // starts as 0
        String id = uri.getPathSegments().get(1);

        // Write the code to delete a single row of data
        // [Hint] Use selections to delete an item by its row ID
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case MOVIES_WITH_ID:
                // Get the movie ID from the URI path
                // Use selections/selectionArgs to filter for this ID
                moviesDeleted = db.delete(MOVIES_TABLE_NAME, MovieContract.MovieTable.MOVIE_ID + "=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (moviesDeleted != 0) {
            // A movie was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d("Sergio>", this + " deleted item with id=" + id + "\n" +
                    "uri= " + uri);
        }

        return moviesDeleted;
    }

    /**
     * Implement this to handle requests to update one or more rows.
     * The implementation should update all rows matching the selection
     * to set the columns according to the provided values map.
     * As a courtesy, call {@link ContentResolver#notifyChange(Uri, ContentObserver) notifyChange()}
     * after updating.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     *
     * @param uri           The URI to query. This can potentially have a record ID if this
     *                      is an update request for a specific record.
     * @param values        A set of column_name/value pairs to update in the database.
     *                      This must not be {@code null}.
     * @param selection     An optional filter to match rows to update.
     * @param selectionArgs
     * @return the number of rows affected.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of updated movies
        int moviesUpdated; // starts as 0
        String id = uri.getPathSegments().get(1);

        // Write the code to update a single row of data
        // [Hint] Use selections to update an item by its row ID
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case MOVIES_WITH_ID:
                // Get the movies ID from the URI path
                // Use selections/selectionArgs to filter for this ID
                moviesUpdated = db.update(MOVIES_TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (moviesUpdated > 0) {
            // A task was updated, set notification
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d("Sergio>", this + " deleted item with id=" + id + "\n" +
                    "uri= " + uri);
        }

        return moviesUpdated;
    }
}
