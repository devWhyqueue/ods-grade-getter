package de.queisler.ods_grade_getter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShowGradesActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    private ArrayAdapter<String> adapter;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private ParseGradesTask mParseGradesTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_grades);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            }
        });

        mParseGradesTask = new ParseGradesTask(getIntent().getExtras().getString("sidd"));
        mParseGradesTask.execute((Void) null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_grades, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_show_grades, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    public class ParseGradesTask extends AsyncTask<Void, Void, Boolean> {

        Map<Integer, Map<String, Double>> gradesPerSemester = new HashMap<>();
        private String sidd;
        private String logbuchPage;

        ParseGradesTask(String sidd) {
            this.sidd = sidd;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            final WebView webView = findViewById(R.id.web_view_grades);
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.addJavascriptInterface(new MyJavaScriptInterface(ShowGradesActivity
                            .this), "HtmlViewer");
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            webView.loadUrl("javascript:window.HtmlViewer.showHTML" + "" +
                                    "('<html>'+document.getElementsByTagName('html')[0]" +
                                    ".innerHTML+'</html>');");
                        }
                    });
                    final String logbuchURL = "https://ods.fh-dortmund" +
                            ".de/ods?Sicht=ExcS&ExcSicht=DSTL&SIDD=" + sidd;
                    webView.loadUrl(logbuchURL);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Document doc = Jsoup.parse(logbuchPage);
                            Element table = doc.select("table").get(0); //select the first table.
                            Elements rows = table.select("tr");

                            int semester = 1;
                            Map<String, Double> grades = new LinkedHashMap<>();
                            for (int i = 2; i < rows.size(); i++) { //first row is the col names
                                // so skip it.
                                Element row = rows.get(i);
                                Elements cols = row.select("td");

                                if (cols.size() > 7) {
                                    if (!cols.get(8).text().isEmpty())
                                        grades.put(cols.get(0).text(), Double.parseDouble(cols
                                                .get(8).text().replaceAll(",", ".")));
                                } else if (!grades.isEmpty()) {
                                    gradesPerSemester.put(semester++, grades);
                                    grades = new LinkedHashMap<>();
                                }

                            }
                        }
                    }, 5000);
                }
            });

            try {
                Thread.sleep(5500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return !gradesPerSemester.isEmpty();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mParseGradesTask = null;

            if (success) {
                adapter = new ArrayAdapter<String>(ShowGradesActivity.this, android.R.layout
                        .simple_list_item_1, ((String[]) gradesPerSemester.entrySet().toArray()));
                setListAdapter(adapter);
                System.out.println(Arrays.toString(gradesPerSemester.entrySet().toArray()));
            } else {
                Intent intent = new Intent(ShowGradesActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mParseGradesTask = null;
        }

        class MyJavaScriptInterface {

            private Context ctx;

            MyJavaScriptInterface(Context ctx) {
                this.ctx = ctx;
            }

            @JavascriptInterface
            public void showHTML(String html) {
                ParseGradesTask.this.logbuchPage = html;
            }

        }
    }
}
