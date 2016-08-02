package com.softwaremobility.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
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
import com.softwaremobility.monin.DetailRecipe;
import com.softwaremobility.monin.R;
import com.softwaremobility.objects.Recipe;

import java.util.ArrayList;

import static com.softwaremobility.utilities.Utils.getColor;

/**
 * Created by darkgeat on 6/1/16.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder>{

    private boolean isGrid = false;
    private Context context;
    private ArrayList<Recipe> recipes;

    public RecipeAdapter(Context context, boolean isGrid, ArrayList<Recipe> data){
        this.context = context;
        this.recipes = data;
        this.isGrid = isGrid;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(isGrid ? R.layout.item_grid_recipes : R.layout.item_list_recipes, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, isGrid);
        view.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.font_path));
        holder.infoRecipe.setText(recipe.getDescription());
        holder.infoRecipe.setTypeface(typeface);
        holder.ratingRecipe.setTypeface(typeface);
        Glide.with(context).load(recipe.getFlagURL())
                .override(60, 40)
                .into(holder.flagImageRecipe);
        Glide.with(context).load(recipe.getImageURL())
                .into(holder.imageRecipe);
        holder.ratingRecipe.setText(String.valueOf(recipe.getRating()));
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailRecipe.class);
                intent.putExtra(MoninContract.MoninEntry.Key_IsMoninRecipe, (boolean) holder.parent.getTag(R.string.key_monin_recipes));
                intent.putExtra(MoninContract.MoninEntry.Key_IdRecipe, (String) holder.parent.getTag(R.string.key_id_recipe));
                intent.putExtra(MoninContract.MoninEntry.Key_Description, (String) holder.parent.getTag(R.string.key_description_recipe));
                intent.putExtra(MoninContract.MoninEntry.Key_ImageRecipe, (byte[]) holder.parent.getTag(R.string.key_image_recipe));
                intent.putExtra(MoninContract.MoninEntry.Key_ImageFlag, (byte[]) holder.parent.getTag(R.string.key_flag_recipe));
                intent.putExtra(MoninContract.MoninEntry.Key_IsCoffee, (boolean) holder.parent.getTag(R.string.key_coffe_recipe));
                intent.putExtra(MoninContract.MoninEntry.Key_IsAlcoholic, (boolean) holder.parent.getTag(R.string.key_alcohol_recipe));
                intent.putExtra(MoninContract.MoninEntry.Key_ImageUrl, (String) holder.parent.getTag(R.string.key_image_recipe_url));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) context,
                            holder.imageRecipe,context.getString(R.string.transition_tag_image)).toBundle();
                    context.startActivity(intent,bundle);
                }else {
                    context.startActivity(intent);
                }
            }
        });
        int color = getColor(recipe.isAlcohol(), recipe.isCofee());
        if (isGrid) {
            ((CardView) holder.parent).setCardBackgroundColor(context.getResources().getColor(color));
            holder.badgeMoninRecipe.setBackgroundColor(context.getResources().getColor(color));
        } else {
            holder.infoContainerRecipe.setBackgroundColor(context.getResources().getColor(color));
        }
        holder.badgeMoninRecipe.setVisibility(View.VISIBLE);
        holder.parent.setTag(R.string.key_id_recipe, recipe.getId());
        holder.parent.setTag(R.string.key_description_recipe, recipe.getDescription());
        holder.parent.setTag(R.string.key_image_recipe, new byte[]{});
        holder.parent.setTag(R.string.key_flag_recipe, new byte[]{});
        holder.parent.setTag(R.string.key_alcohol_recipe, recipe.isAlcohol());
        holder.parent.setTag(R.string.key_coffe_recipe, recipe.isCofee());
        holder.parent.setTag(R.string.key_image_recipe_url, recipe.getImageURL());
        holder.parent.setTag(R.string.key_monin_recipes, true);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void setIsGrid(boolean isGrid) {
        this.isGrid = isGrid;
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
}
