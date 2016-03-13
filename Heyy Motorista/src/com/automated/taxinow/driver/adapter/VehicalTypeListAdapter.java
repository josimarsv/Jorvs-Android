/**
 * 
 */
package com.automated.taxinow.driver.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.automated.taxinow.driver.R;
import com.automated.taxinow.driver.fragment.RegisterFragment;
import com.automated.taxinow.driver.model.VehicalType;

/**
 * @author Hardik A Bhalodi
 * 
 */
public class VehicalTypeListAdapter extends BaseAdapter {

	private ArrayList<VehicalType> listVehicalType;
	private LayoutInflater inflater;
	private ViewHolder holder;
	private AQuery aQuery;
	public static int seletedPosition = 0;
	RegisterFragment regFrag;

	public VehicalTypeListAdapter(Context context,
			ArrayList<VehicalType> listVehicalType, RegisterFragment mapfrag) {
		this.listVehicalType = listVehicalType;
		this.regFrag = mapfrag;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return listVehicalType.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_type, parent,
					false);
			holder = new ViewHolder();
			holder.tvType = (TextView) convertView.findViewById(R.id.tvType);
			holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
			holder.ivSelectService = (ImageView) convertView
					.findViewById(R.id.ivSelectService);
			// holder.viewSeprater = (View) convertView
			// .findViewById(R.id.seprateView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		aQuery = new AQuery(convertView);
		holder.tvType.setText(listVehicalType.get(position).getName() + "");
		holder.ivIcon.setTag(position);
		aQuery.id(holder.ivIcon).image(listVehicalType.get(position).getIcon(),
				true, true);
		if (listVehicalType.get(position).isSelected) {
			holder.ivSelectService.setVisibility(View.VISIBLE);
			holder.ivSelectService
					.setImageResource(R.drawable.selected_service);
		} else {
			// holder.ivSelectService.setBackgroundColor(Color.TRANSPARENT);
			holder.ivSelectService.setVisibility(View.INVISIBLE);

		}

		// if (position == listVehicalType.size() - 1) {
		// holder.viewSeprater.setVisibility(View.GONE);
		// } else {
		// holder.viewSeprater.setVisibility(View.VISIBLE);
		// }

		return convertView;
	}

	private class ViewHolder {
		TextView tvType;
		ImageView ivIcon, ivSelectService;
		// View viewSeprater;
	}

}
