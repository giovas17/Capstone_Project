package com.softwaremobility.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.softwaremobility.data.MoninContract;
import com.softwaremobility.data.MoninDataBase;
import com.softwaremobility.dialogs.SimpleCustomDialog;
import com.softwaremobility.fragments.Home;
//import com.softwaremobility.fragments.MoninRecipes;
import com.softwaremobility.monin.CreateRecipes;
import com.softwaremobility.monin.DetailRecipe;
import com.softwaremobility.monin.R;
import com.softwaremobility.network.NetworkConnection;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.Utils;

import static com.softwaremobility.utilities.Utils.getColor;

public class MoninListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_CREATE = 0;
    private final int VIEW_TYPE_NORMAL = 1;
    private Context context;
    private boolean isGrid, isMoninRecipe, isCommunity, isSlideShow, isUserRecipe;
    private Cursor cursor;

    public MoninListAdapter(Context contextApp, boolean isGridView, boolean isMoninRecipe, boolean isCommunity, boolean isSlideShow){
        context = contextApp;
        isGrid = isGridView;
        this.isMoninRecipe = isMoninRecipe;
        this.isCommunity = isCommunity;
        this.isSlideShow = isSlideShow;
        this.isUserRecipe = false;
    }

    public void setUserRecipe(boolean userRecipe) {
        isUserRecipe = userRecipe;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CREATE && !isMoninRecipe || viewType == VIEW_TYPE_CREATE){
            View view = LayoutInflater.from(context).inflate(isGrid ? R.layout.item_create_grid : R.layout.item_create_list, parent,false);
            HeaderHolder headerHolder = new HeaderHolder(view);
            view.setTag(headerHolder);
            return headerHolder;
        }
        if (viewType == VIEW_TYPE_NORMAL || isMoninRecipe){
            View view = LayoutInflater.from(context).inflate(isGrid ? R.layout.item_grid_recipes : R.layout.item_list_recipes, parent, false);
            ViewHolder viewHolder = new ViewHolder(view, isGrid);
            view.setTag(viewHolder);
            return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            if (isMoninRecipe){
                cursor = getItem(position);
            }else {
                cursor = getItem(position - 1);
            }
            final ViewHolder viewHolder = (ViewHolder) holder;
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.font_path));
            viewHolder.infoRecipe.setText(cursor.getString(cursor.getColumnIndex(MoninContract.MoninEntry.Key_Description)));
            viewHolder.infoRecipe.setTypeface(typeface);
            viewHolder.ratingRecipe.setTypeface(typeface);
            Glide.with(context).load(cursor.getBlob(cursor.getColumnIndex(MoninContract.MoninEntry.Key_ImageFlag)))
                    .override(60, 40)
                    .into(viewHolder.flagImageRecipe);
            Glide.with(context).load(cursor.getBlob(cursor.getColumnIndex(MoninContract.MoninEntry.Key_ImageRecipe)))
                    .into(viewHolder.imageRecipe);
            viewHolder.ratingRecipe.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(MoninContract.MoninEntry.Key_Rating))));
            viewHolder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isUserRecipe){
                        if (MoninPreferences.getBoolean(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NO_LOGIN)){
                            SimpleCustomDialog dialog = new SimpleCustomDialog(context, context.getString(R.string.error_title_no_login), context.getString(R.string.error_no_login), new SimpleCustomDialog.okListener() {
                                @Override
                                public void OnOkSelected() {
                                    //MoninRecipes.shouldFinish = true;
                                    Home.shouldFinish = true;
                                    ((Activity)context).finish();
                                }

                                @Override
                                public void OnCancelSelected() {

                                }
                            });
                            dialog.setOkButtonText(context.getString(R.string.action_login));
                            dialog.show();
                        }else {
                            NetworkConnection.with(context).cancelRequest();
                            Intent intent = new Intent(context, DetailRecipe.class);
                            intent.putExtra(MoninContract.MoninEntry.Key_IsMoninRecipe, (boolean) viewHolder.parent.getTag(R.string.key_monin_recipes));
                            intent.putExtra(MoninContract.MoninEntry.Key_IdRecipe, (String) viewHolder.parent.getTag(R.string.key_id_recipe));
                            intent.putExtra(MoninContract.MoninEntry.Key_Description, (String) viewHolder.parent.getTag(R.string.key_description_recipe));
                            intent.putExtra(MoninContract.MoninEntry.Key_ImageRecipe, (byte[]) viewHolder.parent.getTag(R.string.key_image_recipe));
                            intent.putExtra(MoninContract.MoninEntry.Key_ImageFlag, (byte[]) viewHolder.parent.getTag(R.string.key_flag_recipe));
                            intent.putExtra(MoninContract.MoninEntry.Key_IsCoffee, (boolean) viewHolder.parent.getTag(R.string.key_coffe_recipe));
                            intent.putExtra(MoninContract.MoninEntry.Key_IsAlcoholic, (boolean) viewHolder.parent.getTag(R.string.key_alcohol_recipe));
                            intent.putExtra(MoninContract.MoninEntry.Key_ImageUrl, (String) viewHolder.parent.getTag(R.string.key_image_recipe_url));
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) context,
                                        viewHolder.imageRecipe,context.getString(R.string.transition_tag_image)).toBundle();
                                context.startActivity(intent,bundle);
                            }else {
                                context.startActivity(intent);
                            }
                        }
                    }else {
                        NetworkConnection.with(context).cancelRequest();
                        Intent intent = new Intent(context, DetailRecipe.class);
                        intent.putExtra(MoninContract.MoninEntry.Key_IsMoninRecipe, (boolean) viewHolder.parent.getTag(R.string.key_monin_recipes));
                        intent.putExtra(MoninContract.MoninEntry.Key_IdRecipe, (String) viewHolder.parent.getTag(R.string.key_id_recipe));
                        intent.putExtra(MoninContract.MoninEntry.Key_Description, (String) viewHolder.parent.getTag(R.string.key_description_recipe));
                        intent.putExtra(MoninContract.MoninEntry.Key_ImageRecipe, (byte[]) viewHolder.parent.getTag(R.string.key_image_recipe));
                        intent.putExtra(MoninContract.MoninEntry.Key_ImageFlag, (byte[]) viewHolder.parent.getTag(R.string.key_flag_recipe));
                        intent.putExtra(MoninContract.MoninEntry.Key_IsCoffee, (boolean) viewHolder.parent.getTag(R.string.key_coffe_recipe));
                        intent.putExtra(MoninContract.MoninEntry.Key_IsAlcoholic, (boolean) viewHolder.parent.getTag(R.string.key_alcohol_recipe));
                        intent.putExtra(MoninContract.MoninEntry.Key_ImageUrl, (String) viewHolder.parent.getTag(R.string.key_image_recipe_url));
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) context,
                                    viewHolder.imageRecipe, context.getString(R.string.transition_tag_image)).toBundle();
                            context.startActivity(intent, bundle);
                        } else {
                            context.startActivity(intent);
                        }
                    }
                }
            });
            int isAlcoholValue = cursor.getInt(cursor.getColumnIndex(MoninContract.MoninEntry.Key_IsAlcoholic));
            int isCoffeeValue = cursor.getInt(cursor.getColumnIndex(MoninContract.MoninEntry.Key_IsCoffee));
            int color = getColor(convertIntegerInBoolean(isAlcoholValue), convertIntegerInBoolean(isCoffeeValue));
            if (isGrid) {
                ((CardView) viewHolder.parent).setCardBackgroundColor(context.getResources().getColor(color));
                viewHolder.badgeMoninRecipe.setBackgroundColor(context.getResources().getColor(color));
            } else {
                viewHolder.infoContainerRecipe.setBackgroundColor(context.getResources().getColor(color));
            }
            viewHolder.badgeMoninRecipe.setVisibility(isMoninRecipe ? View.VISIBLE : View.GONE);
            viewHolder.parent.setTag(R.string.key_id_recipe, cursor.getString(cursor.getColumnIndex(MoninContract.MoninEntry.Key_IdRecipe)));
            viewHolder.parent.setTag(R.string.key_description_recipe, cursor.getString(cursor.getColumnIndex(MoninContract.MoninEntry.Key_Description)));
            viewHolder.parent.setTag(R.string.key_image_recipe, cursor.getBlob(cursor.getColumnIndex(MoninContract.MoninEntry.Key_ImageRecipe)));
            viewHolder.parent.setTag(R.string.key_flag_recipe, cursor.getBlob(cursor.getColumnIndex(MoninContract.MoninEntry.Key_ImageFlag)));
            viewHolder.parent.setTag(R.string.key_alcohol_recipe, convertIntegerInBoolean(cursor.getInt(cursor.getColumnIndex(MoninContract.MoninEntry.Key_IsAlcoholic))));
            viewHolder.parent.setTag(R.string.key_coffe_recipe, convertIntegerInBoolean(cursor.getInt(cursor.getColumnIndex(MoninContract.MoninEntry.Key_IsCoffee))));
            viewHolder.parent.setTag(R.string.key_image_recipe_url, cursor.getString(cursor.getColumnIndex(MoninContract.MoninEntry.Key_ImageUrl)));
            viewHolder.parent.setTag(R.string.key_monin_recipes, convertIntegerInBoolean(cursor.getInt(cursor.getColumnIndex(MoninContract.MoninEntry.Key_IsMoninRecipe))));
        }else if (holder instanceof HeaderHolder && (!isMoninRecipe)){
            HeaderHolder headerHolder = (HeaderHolder)holder;
            headerHolder.uniqueView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isNetworkAvailable(context)) {
                        Intent intent = new Intent(context, CreateRecipes.class);
                        intent.putExtra("editRecipe", false);
                        context.startActivity(intent);
                    }else {
                        Utils.showSimpleMessage(context,context.getString(R.string.error),context.getString(R.string.error_no_internet));
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (cursor != null){
            if (isSlideShow){
                return cursor.getCount() + 1;
            }
            if (isMoninRecipe){
                return cursor.getCount();
            }else {
                return cursor.getCount() + 1;
            }
        }else {
            return 0;
        }
    }

    private boolean convertIntegerInBoolean(int value){
        return value == MoninDataBase.BOOLEAN.TRUE.value;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView infoRecipe;
        public final ImageView flagImageRecipe;
        public final ImageView imageRecipe;
        public final TextView ratingRecipe;
        public final ImageView badgeMoninRecipe;
        public final RelativeLayout infoContainerRecipe;
        public final View parent;

        public ViewHolder(View view, boolean isGrid){
            super(view);
            parent = view;
            if (isGrid){
                infoRecipe = (TextView) view.findViewById(R.id.infoRecipeTextItemGrid);
                flagImageRecipe = (ImageView) view.findViewById(R.id.flagItemGridRecipe);
                imageRecipe = (ImageView) view.findViewById(R.id.imageItemGrid);
                ratingRecipe = (TextView) view.findViewById(R.id.ratingStarTextItemGrid);
                badgeMoninRecipe = (ImageView) view.findViewById(R.id.badgeMoninGrid);
                infoContainerRecipe = null;
            }else {
                infoRecipe = (TextView) view.findViewById(R.id.infoRecipeTextItemList);
                flagImageRecipe = (ImageView) view.findViewById(R.id.flagItemListRecipe);
                imageRecipe = (ImageView) view.findViewById(R.id.imageItemList);
                ratingRecipe = (TextView) view.findViewById(R.id.ratingStarTextItemList);
                badgeMoninRecipe = (ImageView) view.findViewById(R.id.badgeMoninList);
                infoContainerRecipe = (RelativeLayout) view.findViewById(R.id.containerInfoList);
            }
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder{

        public final View uniqueView;

        public HeaderHolder(View view){
            super(view);
            uniqueView = view;
        }
    }

    public void swapCursor(Cursor cursor){
        this.cursor = cursor;
        notifyDataSetChanged();
    }


    public Cursor getItem(final int position){
        if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()){
            if (cursor.moveToNext()) {
                cursor.moveToPosition(position);
            }else {
                cursor.moveToFirst();
            }
        }
        return cursor;
    }

    public void setIsGrid(boolean isGrid) {
        this.isGrid = isGrid;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)){
            return VIEW_TYPE_CREATE;
        }else {
            return VIEW_TYPE_NORMAL;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }
}
