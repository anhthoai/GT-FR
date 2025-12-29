package vn.com.goldtek.facenotify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import com.bumptech.glide.Glide;
import vn.com.goldtek.facenotify.R;

public class notification extends Fragment {

    android.widget.LinearLayout parentLayout;
    LinearLayout layoutDisplayNotification;
    TextView tvNoRecordsFound;

    Bundle bundle = new Bundle();

    SQLiteHelper sQLiteHelper;

    public Button settingBtn;
    public Button deleteBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        parentLayout = (LinearLayout) rootView.findViewById(R.id.parentLayout);
        layoutDisplayNotification = (LinearLayout) rootView.findViewById(R.id.layoutDisplayNotification);

        tvNoRecordsFound = (TextView) rootView.findViewById(R.id.tvNoRecordsFound);

        sQLiteHelper = new SQLiteHelper(getActivity());

        settingBtn = rootView.findViewById(R.id.SettingBtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), SettingActivity.class);
                startActivity(i);
            }
        });

        deleteBtn = rootView.findViewById(R.id.DeleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sQLiteHelper.deleteAllRecords();
                parentLayout.removeAllViews();
            }
        });

        bundle = getArguments();
        if (bundle != null) {
            //TextIntent = bundle.getString("link");
            String name = bundle.getString("name");
            String time = bundle.getString("time");
            String group = bundle.getString("group");
            String image_url = bundle.getString("image_url");

            NotificationModel notificationModel = new NotificationModel(name, time, group, image_url);
            sQLiteHelper.insertRecord(notificationModel);
        }

        ArrayList<NotificationModel> notificationModels = sQLiteHelper.getAllRecords();
        parentLayout.removeAllViews();
        if (notificationModels.size() > 0)
        {
            tvNoRecordsFound.setVisibility(View.GONE);
            for (int v = notificationModels.size() - 1; v >= 0 ; v--)
            {
                final Holder holder = new Holder();
                final View view = LayoutInflater.from(getActivity()).inflate(R.layout.list_item, null);
                holder.ImageFace = (ImageView) view.findViewById(R.id.faceView);
                holder.NameView = (TextView) view.findViewById(R.id.nameResultView);
                holder.TimeView = (TextView) view.findViewById(R.id.timeView);
                holder.GroupView = (TextView) view.findViewById(R.id.groupView);

                view.setTag(notificationModels.get(v).getID());
                holder.NameView.setText(notificationModels.get(v).getName());
                holder.TimeView.setText(notificationModels.get(v).getTime());
                holder.GroupView.setText(notificationModels.get(v).getGroup());
                Glide.with(getActivity()).load(notificationModels.get(v).getImageUrl()).into(holder.ImageFace);

                parentLayout.addView(view);
            }
        }
        else
        {
            tvNoRecordsFound.setVisibility(View.VISIBLE);
        }

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class Holder {
        TextView NameView;
        TextView TimeView;
        TextView GroupView;
        ImageView ImageFace;
    }
}
