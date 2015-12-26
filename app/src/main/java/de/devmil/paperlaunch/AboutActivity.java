package de.devmil.paperlaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import de.devmil.common.licensing.LicenseManager;
import de.devmil.common.licensing.PackageInfo;

public class AboutActivity extends Activity {

    private static final String TAG = AboutActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private LicenseManager mLicenseManager;
    private ListView mLicenseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mToolbar = (Toolbar)findViewById(R.id.activity_about_toolbar);
        mLicenseList = (ListView)findViewById(R.id.activity_about_info_listView);

        setActionBar(mToolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mLicenseManager = new LicenseManager(this, R.raw.licenseinfo);

        LicenseEntryAdapter adapter = new LicenseEntryAdapter(this, mLicenseManager.getLicenseInfo().getPackages().toArray(new PackageInfo[0]));

        mLicenseList.setAdapter(adapter);
    }

    class LicenseEntryAdapter extends ArrayAdapter<PackageInfo>
    {
        public LicenseEntryAdapter(Context context, PackageInfo[] items) {
            super(context, 0, items);
        }

        class ViewHolder
        {
            ImageView image;
            TextView name;
            TextView copyright;
            TextView url;
            Button licenseButton;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_about_info_entry, parent, false);
                ViewHolder vh = new ViewHolder();
                vh.image = (ImageView)convertView.findViewById(R.id.activity_license_info_entry_image);
                vh.name = (TextView)convertView.findViewById(R.id.activity_license_info_entry_name);
                vh.copyright = (TextView)convertView.findViewById(R.id.activity_license_info_entry_copyright);
                vh.url = (TextView)convertView.findViewById(R.id.activity_license_info_entry_url);
                vh.licenseButton = (Button)convertView.findViewById(R.id.activity_license_info_entry_licensebutton);
                convertView.setTag(vh);
            }

            ViewHolder holder = (ViewHolder)convertView.getTag();

            PackageInfo item = getItem(position);

            int drawableId = getContext().getResources().getIdentifier(item.getIconName(), "drawable", "de.devmil.paperlaunch");

            holder.image.setImageResource(drawableId);
            holder.name.setText(item.getName());
            holder.copyright.setText(item.getCopyright());
            holder.url.setText(Html.fromHtml("<a href=\"" + item.getUrl() + "\">" + item.getUrl() + "</a>"));
            holder.url.setAutoLinkMask(Linkify.WEB_URLS);
            holder.url.setTag(item);
            holder.url.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        PackageInfo pi = (PackageInfo) v.getTag();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(pi.getUrl()));

                        startActivity(intent);
                    }
                    catch(Exception e)
                    {
                        Log.w(TAG, "Error parsing package url", e);
                    }
                }
            });
            holder.licenseButton.setText(item.getLicense().getName());
            holder.licenseButton.setTag(item);
            holder.licenseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PackageInfo pi = (PackageInfo) v.getTag();
                    ScrollView scrollView = new ScrollView(getContext());
                    TextView tvMessage = new TextView(getContext());
                    tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 6);
                    tvMessage.setTypeface(Typeface.MONOSPACE);
                    tvMessage.setText(pi.getLicense().getContent());
                    scrollView.addView(tvMessage);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    AlertDialog dlg = builder.setTitle(pi.getLicense().getName())
                            .setView(scrollView)
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    dlg.show();

                }
            });

            return convertView;
        }
    }
}
