package uk.org.mattford.scoutlink.activity;

import uk.org.mattford.scoutlink.R;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

public class MessageListFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.message_list_view, container, false);

        ListView lv = v.findViewById(android.R.id.list);

        lv.setDivider(null);
        lv.setDividerHeight(0);
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        lv.setStackFromBottom(true);
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem+visibleItemCount != totalItemCount) {
                    view.getRootView().findViewById(R.id.not_at_bottom).setVisibility(View.VISIBLE);
                } else {
                    view.getRootView().findViewById(R.id.not_at_bottom).setVisibility(View.GONE);
                }
            }
        });
        v.findViewById(R.id.not_at_bottom).setOnClickListener(v1 -> {
            ListView lv1 = v1.getRootView().findViewById(android.R.id.list);
            lv1.smoothScrollToPosition(lv1.getAdapter().getCount()-1);
        });

        return v;
    }
}
