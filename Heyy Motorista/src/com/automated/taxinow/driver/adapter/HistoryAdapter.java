/**
 * 
 */
package com.automated.taxinow.driver.adapter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.automated.taxinow.driver.R;
import com.automated.taxinow.driver.model.History;
import com.automated.taxinow.driver.widget.MyFontTextView;
import com.automated.taxinow.driver.widget.MyFontTextViewBold;
import com.automated.taxinow.driver.widget.MyFontTextViewTitle;
import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;

/**
 * @author Kishan H Dhamat
 * 
 */
public class HistoryAdapter extends BaseAdapter implements
		PinnedSectionListAdapter {

	private Activity activity;
	private ArrayList<History> list;
	private AQuery aQuery;
	private LayoutInflater inflater;
	private ViewHolder holder;
	private ImageOptions imageOptions;
	public static final int TYPE_ITEM = 0;
	public static final int TYPE_SEPARATOR = 1;
	private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
	private DecimalFormat df;
	TreeSet<Integer> mSeparatorsSet;
	SimpleDateFormat simpleDateFormat;
	private SectionViewHolder sectionHolder;
	private Date currentDate = new Date();
	private Date returnDate = new Date();

	/**
	 * @param historyActivity
	 * @param historyList
	 */
	public HistoryAdapter(Activity activity, ArrayList<History> historyList,
			TreeSet<Integer> mSeparatorsSet) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		this.list = historyList;
		df = new DecimalFormat("00");
		this.mSeparatorsSet = mSeparatorsSet;

		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageOptions = new ImageOptions();
		imageOptions.fileCache = true;
		imageOptions.memCache = true;
		imageOptions.targetWidth = 200;
		imageOptions.fallback = R.drawable.user;
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return list.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		if (convertView == null) {
			switch (type) {
			case TYPE_ITEM:
				convertView = inflater.inflate(R.layout.history_item, parent,
						false);
				holder = new ViewHolder();
				holder.tvName = (MyFontTextViewBold) convertView
						.findViewById(R.id.tvHistoryName);
				// holder.tvBio = (MyFontTextView) convertView
				// .findViewById(R.id.tvHistoryBio);
				holder.tvDate = (MyFontTextViewTitle) convertView
						.findViewById(R.id.tvHistoryDate);
				holder.tvCost = (MyFontTextViewTitle) convertView
						.findViewById(R.id.tvHistoryCost);
				holder.ivIcon = (ImageView) convertView
						.findViewById(R.id.ivHistoryIcon);

				convertView.setTag(holder);
				break;
			case TYPE_SEPARATOR:
				sectionHolder = new SectionViewHolder();
				convertView = inflater.inflate(R.layout.history_date_layout,
						parent, false);

				sectionHolder.tv = (TextView) convertView
						.findViewById(R.id.tvDate);
				convertView.setTag(sectionHolder);
				break;
			}

		} else {
			switch (type) {
			case TYPE_ITEM:
				holder = (ViewHolder) convertView.getTag();
				break;
			case TYPE_SEPARATOR:
				sectionHolder = (SectionViewHolder) convertView.getTag();
				break;

			}
		}

		switch (type) {
		case TYPE_ITEM:
			aQuery = new AQuery(convertView);
			// Collections.sort(list.get(position).getDate(), myComparator);
			History history = list.get(position);

			holder.tvName.setText(history.getFirstName() + " "
					+ history.getLastName());
			// holder.tvBio.setText(history.getBio() + "");
			holder.tvDate.setText(formatTime(history.getDate()));
			holder.tvCost.setText(activity.getString(R.string.payment_unit)+history.getTotal());
			aQuery.id(holder.ivIcon).progress(R.id.pBar)
					.image(history.getPicture(), imageOptions);
			break;
		case TYPE_SEPARATOR:

			Log.i("Return date", "" + list.get(position).getDate());
			SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
			String date = df1.format(currentDate);

			if (list.get(position).getDate().equals(date)) {
				sectionHolder.tv
						.setBackgroundResource(R.drawable.history_header_line_blue);
				sectionHolder.tv.setTextColor(activity.getResources().getColor(
						R.color.white));
				sectionHolder.tv.setText(activity
						.getString(R.string.text_today));
			} else if (list.get(position).getDate()
					.equals(getYesterdayDateString())) {
				sectionHolder.tv
						.setBackgroundResource(R.drawable.history_header_line_white);
				sectionHolder.tv.setTextColor(activity.getResources().getColor(
						R.color.color_blue));
				sectionHolder.tv.setText(activity
						.getString(R.string.text_yesterday));
			}

			else {
				sectionHolder.tv
						.setBackgroundResource(R.drawable.history_header_line_white);
				sectionHolder.tv.setTextColor(activity.getResources().getColor(
						R.color.color_blue));

				try {
					returnDate = df1.parse(list.get(position).getDate());

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SimpleDateFormat pinnedDate = new SimpleDateFormat(
						"dd-MMM-yyyy");
				Log.i("New Date", pinnedDate.format(returnDate));
				sectionHolder.tv.setText(pinnedDate.format(returnDate));

			}

			break;

		}

		return convertView;
	}

	private class ViewHolder {
		MyFontTextView tvBio;
		MyFontTextViewBold tvName;
		MyFontTextViewTitle tvCost, tvDate;
		ImageView ivIcon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hb.views.PinnedSectionListView.PinnedSectionListAdapter#
	 * isItemViewTypePinned(int)
	 */
	@Override
	public boolean isItemViewTypePinned(int viewType) {
		return viewType == TYPE_SEPARATOR;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
	}

	class SectionViewHolder {
		TextView tv;
	}

	private String formatTime(String strDate) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		Date time = new Date();
		try {
			time = sf.parse(strDate);
			Log.i("Return Time", "" + time.getHours() + ":" + time.getMinutes());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time.getTime());
		return sdf.format(cal.getTime());

	}

	private String getYesterdayDateString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return dateFormat.format(cal.getTime());
	}

	Comparator<Date> myComparator = new Comparator<Date>() {
		public int compare(Date currentDate, Date returnDate) {
			return currentDate.compareTo(returnDate);
		}
	};

}
