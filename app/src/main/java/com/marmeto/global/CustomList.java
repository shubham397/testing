package com.marmeto.global;

import java.util.ArrayList;

import com.marmeto.sproxiltnt.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String> {

	private final Activity context;
	private final ArrayList<String> web;
	private final ArrayList<Integer>imageId;

	public CustomList(Activity context, ArrayList<String> web, ArrayList<Integer> imageId) {
		super(context, R.layout.customlist, web);
		this.context = context;
		this.web = web;
		this.imageId = imageId;

	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.customlist, parent, false);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
		txtTitle.setText(web.get(position));

		imageView.setImageResource(imageId.get(position));
		return rowView;
	}
}