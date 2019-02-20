package com.example.a96llegend.ar4ece.FSM;

import android.content.DialogInterface;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.a96llegend.ar4ece.R;

public class HelpScreen extends AppCompatActivity implements Tab1.OnFragmentInteractionListener,Tab2.OnFragmentInteractionListener,Tab3.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_screen);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tablayout);
        tabLayout.addTab(tabLayout.newTab().setText("2 States"));
        tabLayout.addTab(tabLayout.newTab().setText("3 States"));
        tabLayout.addTab(tabLayout.newTab().setText("4 States"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(HelpScreen.this);

        builder.setCancelable(true);
        builder.setMessage("For more accurate readings try to place conditions and state names within the zones shown here!" +
                "\n\n For multiple conditions use the concatenation symbol,&" +
                "\n\n Invert conditions by placing the keyword 'not' in front of the conditions.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
