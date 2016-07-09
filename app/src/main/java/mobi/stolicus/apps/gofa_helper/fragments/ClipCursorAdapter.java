package mobi.stolicus.apps.gofa_helper.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mobi.stolicus.app.gofa_helper.R;
import mobi.stolicus.apps.gofa_helper.db.Clip;
import mobi.stolicus.apps.gofa_helper.db.ClipWrapper;
import mobi.stolicus.apps.gofa_helper.support.DateHelper;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Adapter for showing single row of clip data
 * Created by shtolik on 26.08.2015.
 */


public class ClipCursorAdapter extends CursorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClipCursorAdapter.class);
    private final ClipListListener mListener;
    private final LayoutInflater mInflater;

    private final Context mContext;

    private boolean mSelectingMode = false;

    public ClipCursorAdapter(Context context, Cursor items, int flags, ClipListListener listener) {
        super(context, items, flags);
        mInflater = LayoutInflater.from(context);
        this.mContext = context.getApplicationContext();
        this.mListener = listener;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View viewToUse = mInflater.inflate(R.layout.row_clip, parent, false);

        final ViewHolder holder = new ViewHolder();
        holder.parent = parent;
        holder.img1left = (ImageView) viewToUse.findViewById(R.id.img1left);
        holder.txt1title = (TextView) viewToUse.findViewById(R.id.txt1title);
        holder.txt2desc = (TextView) viewToUse.findViewById(R.id.txt2desc);
        holder.img2center = (ImageView) viewToUse.findViewById(R.id.img2center);
        holder.img3right = (ImageView) viewToUse.findViewById(R.id.img3right);
        viewToUse.setTag(holder);
        return viewToUse;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        Clip clip = cupboard().withCursor(cursor).get(Clip.class);
        fillHolder(holder, clip);
    }

    private void confirmDeletion(final Clip clip) {
        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        adb.setTitle(R.string.confirm_deletion);
        adb.setCancelable(true);
        adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClipWrapper cw = ClipWrapper.getInstance(mContext);
                cw.dbDeleteItem(clip);
                dialog.dismiss();
            }
        });
        adb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        adb.show();
    }


    public boolean isSelectingMode() {
        return mSelectingMode;
    }

    public void setSelectingMode(boolean selectingMode) {
        this.mSelectingMode = selectingMode;
    }

    private void fillHolder(ViewHolder holder, final Clip clip) {

        if (!isSelectingMode()) {

            int id = Clip.getResIconFromClipText(clip.getText());
            holder.img1left.setBackgroundResource(id);
        } else {
            long[] checked = ((AbsListView) holder.parent).getCheckedItemIds();
            boolean selected = false;
            for (long aChecked : checked) {
                if (aChecked == clip.get_id()) {
                    selected = true;
                    break;
                }
            }
            if (selected) {
                holder.img1left.setBackgroundResource(R.drawable.ic_check_box_white_24dp);
                holder.txt1title.setSelected(true);
                holder.txt2desc.setSelected(true);

            } else {
                holder.img1left.setBackgroundResource(R.drawable.ic_check_box_outline_blank_white_24dp);
                holder.txt1title.setSelected(false);
                holder.txt2desc.setSelected(false);
            }
        }

        String url;
        try {
            url = java.net.URLDecoder.decode(clip.getText(), "UTF-8");
        } catch (Throwable e) {
            logger.warn("/fillHolder/" + e.getMessage());
            url = clip.getText();
        }
        holder.txt1title.setText(url);

        holder.txt2desc.setText(DateHelper.FORMAT_HUMAN_READABLE.format(clip.getTimestamp()));

        View.OnClickListener voclDelete = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeletion(clip);
            }
        };
        holder.img3right.setOnClickListener(voclDelete);

        View.OnClickListener voclRun = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClipCopyClicked(clip);
            }
        };
        holder.img2center.setOnClickListener(voclRun);

    }

    /**
     * Holder for the list items.
     */
    private class ViewHolder {
        ImageView img1left;
        TextView txt1title;
        TextView txt2desc;
        ImageView img2center;
        ImageView img3right;
        ViewGroup parent;
    }
}
