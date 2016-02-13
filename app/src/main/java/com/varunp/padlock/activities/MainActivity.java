package com.varunp.padlock.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.varunp.padlock.R;
import com.varunp.padlock.adapters.FolderAdapter;
import com.varunp.padlock.utils.FileManager;
import com.varunp.padlock.utils.Globals;

import net.dealforest.sample.crypt.AES256Cipher;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        initFab();

        initRecyclerView();

        initDrawer();
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.cardList);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new FolderAdapter(
                new FileManager(this).getInternalPath(Globals.FOLDER_DATA) + "/");
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
        toolbar.setTitle(Globals.NAV_HEADER_FOLDERS);
        setTitle(Globals.NAV_HEADER_FOLDERS);
    }

    private void initFab()
    {
        FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.right_labels);

        com.getbase.floatingactionbutton.FloatingActionButton addNote =
                (com.getbase.floatingactionbutton.FloatingActionButton)findViewById(R.id.addNote);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: do this shit
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
            public void onClick(View v) {
                //TODO: do this shit
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

        MenuItem slideshow = menu.findItem(R.id.action_slideshow);
        Drawable newIcon = (Drawable)slideshow.getIcon();
        newIcon.mutate().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_IN);
        slideshow.setIcon(newIcon);

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

        if(id == R.id.action_slideshow)
        {

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes)
        {
            toolbar.setTitle(Globals.NAV_HEADER_NOTES);
            setTitle(Globals.NAV_HEADER_NOTES);
        }
        else if (id == R.id.nav_gallery)
        {
            toolbar.setTitle(Globals.NAV_HEADER_IMAGES);
            setTitle(Globals.NAV_HEADER_IMAGES);
        }
        else if (id == R.id.nav_files)
        {
            toolbar.setTitle(Globals.NAV_HEADER_FILES);
            setTitle(Globals.NAV_HEADER_FILES);
        }
        else if (id == R.id.nav_folders)
        {
            toolbar.setTitle(Globals.NAV_HEADER_FOLDERS);
            setTitle(Globals.NAV_HEADER_FOLDERS);
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
}
