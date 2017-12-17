package de.queisler.ods_grade_getter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
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
    private View mProgressView;

    /**
     * Keep track of the task to ensure we can cancel it if requested.
     */
    private ParseGradesTask mParseGradesTask = null;

    // DATA
    private Map<Integer, ArrayList<String>> gradesPerSemesterStr = new LinkedHashMap<>();
    private double gradePointAverage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_grades);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ShowGradesActivity.this);
                dlgAlert.setMessage("Dein Notendurchschnitt ist: " + gradePointAverage);
                dlgAlert.setTitle("Notendurchschnitt");
                dlgAlert.setCancelable(true);
                dlgAlert.setPositiveButton("Super...", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
                dlgAlert.create().show();
            }
        });

        mProgressView = findViewById(R.id.parse_progress);
        mViewPager = findViewById(R.id.container);

        showProgress(true);
        mParseGradesTask = new ParseGradesTask(getIntent().getExtras().getString("sidd"));
        mParseGradesTask.execute((Void) null);
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
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
            mViewPager.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener
                    (new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SEMESTER_NUMBER = "semester_number";
        private static final String ARG_SEMESTER_GRADES = "semester_grades";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int position, ArrayList<String>
                subjectsGrades) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putStringArrayList(ARG_SEMESTER_GRADES, subjectsGrades);
            args.putInt(ARG_SEMESTER_NUMBER, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_show_grades, container, false);
            TextView textView = rootView.findViewById(R.id.heading_semester);
            textView.setText((getArguments().getInt(ARG_SEMESTER_NUMBER)) + ". Semester");
            ListView listView = rootView.findViewById(R.id.list_view);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout
                    .simple_list_item_1, getArguments().getStringArrayList(ARG_SEMESTER_GRADES)));
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
            return PlaceholderFragment.newInstance((position + 1), gradesPerSemesterStr.get(position + 1));
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return gradesPerSemesterStr.size();
        }
    }

    public class ParseGradesTask extends AsyncTask<Void, Void, Boolean> {

        private String sidd;
        private String logbuchPage;
        private boolean locked = true;

        ParseGradesTask(String sidd) {
            this.sidd = sidd;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

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
                            webView.loadUrl("javascript:window.HtmlViewer.showHTML" + "" + "" +
                                    "('<html>'+document.getElementsByTagName('html')[0]" + "" +
                                    ".innerHTML+'</html>');");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    parseGrades();
                                    locked = false;
                                }
                            }, 5000);
                        }
                    });
                    final String logbuchURL = "https://ods.fh-dortmund" + "" +
                            ".de/ods?Sicht=ExcS&ExcSicht=DSTL&SIDD=" + sidd;
                    webView.loadUrl(logbuchURL);
                }
            });

            while (locked) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } // Lock

            return !gradesPerSemesterStr.isEmpty();
        }

        private void parseGrades() {
            Map<Integer, Map<String, Double>> gradesPerSemester = new LinkedHashMap<>();
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
                        grades.put(cols.get(0).text(), Double.parseDouble(cols.get(8).text()
                                .replaceAll(",", ".")));
                } else if (!grades.isEmpty()) {
                    gradesPerSemester.put(semester++, grades);
                    grades = new LinkedHashMap<>();
                }
            }
            int numGrades = 0;
            for (Map.Entry<Integer, Map<String, Double>> entry : gradesPerSemester.entrySet()) {
                ArrayList<String> strList = new ArrayList<>();
                for (Map.Entry<String, Double> innerEntry : entry.getValue().entrySet()) {
                    strList.add(innerEntry.getKey() + ": " + innerEntry.getValue());
                    gradePointAverage += innerEntry.getValue();
                    numGrades++;
                }
                gradesPerSemesterStr.put(entry.getKey(), strList);
            }
            gradePointAverage = Math.floor((gradePointAverage / numGrades) * 10d) / 10d;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mParseGradesTask = null;

            if (success) {
                showProgress(false);

                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
                // Set up the ViewPager with the sections adapter.
                mViewPager.setAdapter(mSectionsPagerAdapter);
            } else {
                Intent intent = new Intent(ShowGradesActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
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
