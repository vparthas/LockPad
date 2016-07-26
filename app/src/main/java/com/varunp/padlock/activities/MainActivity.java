package com.varunp.padlock.activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.varunp.padlock.R;
import com.varunp.padlock.adapters.FolderAdapter;
import com.varunp.padlock.adapters.FolderAdapterListener;
import com.varunp.padlock.utils.file.FileManager;
import com.varunp.padlock.utils.file.FileTracker;
import com.varunp.padlock.utils.Globals;
import com.varunp.padlock.utils.file.PLFile;

import net.dealforest.sample.crypt.AES256Cipher;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FolderAdapterListener {

    public static String INTENT_FOLDER = "FOLDER";

    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TextView noItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        byte[] key = getIntent().getByteArrayExtra(DocumentActivity.INTENT_KEY_ENCRYPTION_KEY);
        AES256Cipher.setKey(key == null ? AES256Cipher.getKey() : key);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        initViews();

        initFileTracker();

        initFab();

        initRecyclerView();

        initDrawer();
    }

    private void initViews()
    {
        noItems = (TextView)findViewById(R.id.nothing_view);
    }

    private void initFileTracker()
    {
        FileManager fm = new FileManager(getApplicationContext());
        String[] files = fm.getContentsStr(FileManager.FILE_INTERNAL, Globals.FOLDER_DATA);
        FileTracker.init(getApplicationContext(), files);
    }

    private void initRecyclerView()
    {
        mRecyclerView = (RecyclerView) findViewById(R.id.cardList);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        String folder = this.getIntent().getStringExtra(INTENT_FOLDER);
        if(folder != null)
            resetRecycler(folder);
        else
            resetRecycler(FolderAdapter.QUERY_FOLDERS);
    }

    private void resetRecycler(int query)
    {
        resetRecycler(query + "");
    }

    private void resetRecycler(String query)
    {
        mAdapter = new FolderAdapter(query, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initDrawer()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_folders);
//        toolbar.setTitle(Globals.NAV_HEADER_FOLDERS);
//        setTitle(Globals.NAV_HEADER_FOLDERS);
    }

    private void initFab()
    {
        final FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.right_labels);

        com.getbase.floatingactionbutton.FloatingActionButton addNote =
                (com.getbase.floatingactionbutton.FloatingActionButton)findViewById(R.id.addNote);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent newDocIntent = new Intent(getApplicationContext(), DocumentActivity.class);
                newDocIntent.putExtra(DocumentActivity.INTENT_KEY_IS_NEW_DOC, true);
                newDocIntent.putExtra(DocumentActivity.INTENT_KEY_FOLDER_NAME, ((FolderAdapter)mAdapter).getFocused());
                newDocIntent.putExtra(DocumentActivity.INTENT_KEY_ENCRYPTION_KEY, AES256Cipher.getKey());
                startActivity(newDocIntent);
            }
        });

        com.getbase.floatingactionbutton.FloatingActionButton addImage =
                (com.getbase.floatingactionbutton.FloatingActionButton)findViewById(R.id.addImage);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: do this shit
            }
        });

        com.getbase.floatingactionbutton.FloatingActionButton addFolder =
                (com.getbase.floatingactionbutton.FloatingActionButton)findViewById(R.id.addFolder);
        addFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                floatingActionsMenu.collapse();
                openNewFolderDialog();
            }
        });
    }

    private void openNewFolderDialog()
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_new_folder);
        //dialog.setTitle("Create new folder");
        dialog.setCancelable(true);

        final EditText editText_folder_name = (EditText)dialog.findViewById(R.id.dialog_new_folder_name);

        Button button_ok = (Button)dialog.findViewById(R.id.dialog_new_folder_ok);
        button_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String folderName = editText_folder_name.getText().toString();
                if(!FileTracker.addFolder(getApplicationContext(), folderName))
                    return;

                resetRecycler(FolderAdapter.QUERY_FOLDERS);
                dialog.dismiss();
            }
        });

        Button button_cancel = (Button)dialog.findViewById(R.id.dialog_new_folder_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }

        FolderAdapter adapter = (FolderAdapter)mRecyclerView.getAdapter();
        if(adapter.hasParent())
        {
            adapter.up();
        }
        else
        {
            backTwice();
        }
    }

    private long previous_click = 0;
    private void backTwice()
    {
        long temp = System.currentTimeMillis();
        if(temp - previous_click < 2000)
            super.onBackPressed();
        else
        {
            previous_click = temp;
            Snackbar.make(mRecyclerView, "Press back once more to exit.", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        AES256Cipher.setKey(null);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

//        MenuItem slideshow = menu.findItem(R.id.action_slideshow);
//        Drawable newIcon = (Drawable)slideshow.getIcon();
//        newIcon.mutate().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_IN);
//        slideshow.setIcon(newIcon);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

//        if(id == R.id.action_slideshow)
//        {
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes)
        {
//            toolbar.setTitle(Globals.NAV_HEADER_NOTES);
//            setTitle(Globals.NAV_HEADER_NOTES);

            resetRecycler(FolderAdapter.QUERY_TEXT);
        }
        else if (id == R.id.nav_gallery)
        {
//            toolbar.setTitle(Globals.NAV_HEADER_IMAGES);
//            setTitle(Globals.NAV_HEADER_IMAGES);

            resetRecycler(FolderAdapter.QUERY_IMAGE);
        }
        else if (id == R.id.nav_files)
        {
//            toolbar.setTitle(Globals.NAV_HEADER_FILES);
//            setTitle(Globals.NAV_HEADER_FILES);

            resetRecycler(FolderAdapter.QUERY_FILES);
        }
        else if (id == R.id.nav_folders)
        {
//            toolbar.setTitle(Globals.NAV_HEADER_FOLDERS);
//            setTitle(Globals.NAV_HEADER_FOLDERS);

            resetRecycler(FolderAdapter.QUERY_FOLDERS);
        }
        else if (id == R.id.nav_rate)
        {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getPackageName())));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onQueryChanged(String query, int itemCount)
    {
        noItems.setVisibility(itemCount == 0 ? View.VISIBLE : View.INVISIBLE);

        toolbar.setTitle(query);
        setTitle(query);
    }

    public void open(PLFile file)
    {
        if(file.getSuffix().equals(Globals.FILENAME_TEXT))
        {
            Intent docIntent = new Intent(getApplicationContext(), DocumentActivity.class);
            docIntent.putExtra(DocumentActivity.INTENT_KEY_IS_NEW_DOC, false);
            docIntent.putExtra(DocumentActivity.INTENT_KEY_FOLDER_NAME, file.getFolderName());
            docIntent.putExtra(DocumentActivity.INTENT_KEY_FILE_NAME, file.getFileName());
            docIntent.putExtra(DocumentActivity.INTENT_KEY_ENCRYPTION_KEY, AES256Cipher.getKey());
            startActivity(docIntent);
        }
        else if(file.getSuffix().equals(Globals.FILENAME_IMAGE))
        {
            //TODO
        }
        else
            Log.d("MainActivity", "ERROR: Unrecognized file type: " + file.getRawName());
    }

}
