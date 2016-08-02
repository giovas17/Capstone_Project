package com.softwaremobility.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.softwaremobility.json.JSONUtils;
import com.softwaremobility.monin.R;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;
import com.softwaremobility.objects.Ingredient;
import com.softwaremobility.preferences.MoninPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cristian Cardoso on 8/04/16.
 */

public class AutoCompleteAdapter extends ArrayAdapter<Ingredient> implements Filterable {

    private Context mContext;
    private List<Ingredient> ingredients;


    public AutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mContext = context;
        ingredients = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return ingredients.size();
    }

    @Override
    public Ingredient getItem(int index) {
        return ingredients.get(index);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent){
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_autocomplete_text, parent, false);
        TextView text = (TextView) rootView.findViewById(android.R.id.text1);
        Ingredient ingredient = getItem(position);
        text.setText(ingredient.getIngredient());
        return rootView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                final FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    if(constraint.toString().length() >= 3){
                        NetworkConnection.with(mContext).cancelRequest();
                        Ingredient ingredient = new Ingredient();
                        ingredient.setIngredient(mContext.getString(R.string.loading));
                        ingredients.add(ingredient);
                        filterResults.values = ingredients;
                        Map<String, String> params = new HashMap<>();
                        params.put(mContext.getString(R.string.key_page), "1");
                        params.put(mContext.getString(R.string.key_search_term), constraint.toString());
                        Map<String, String> headers = new HashMap<>();
                        headers.put(mContext.getString(R.string.header_authorization), mContext.getString(R.string.header_token,
                                MoninPreferences.getString(mContext, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN)));
                        NetworkConnection.with(mContext).withListener(new NetworkConnection.ResponseListener() {
                            @Override
                            public void onSuccessfullyResponse(String response) {
                                ingredients.clear();
                                ingredients = JSONUtils.with(mContext).getMoninIngredients(response);
                                filterResults.values = ingredients;
                                filterResults.count = ingredients.size();
                                publishResults(constraint, filterResults);

                            }
                            @Override
                            public void onErrorResponse(String error, String message, int code) {
                                ingredients.clear();
                                filterResults.values = ingredients;
                                publishResults(constraint, filterResults);
                            }
                        }).doRequest(Connection.REQUEST.GET, Uri.parse(mContext.getString(R.string.api_user_monin_ingredients)), params, headers, null);
                    }
                }else{
                    filterResults.values = null;
                    ingredients.clear();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                clear();
                if(results != null && results.count > 0) {
                    ingredients = (ArrayList<Ingredient>) results.values;
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }

        };
    }
}
