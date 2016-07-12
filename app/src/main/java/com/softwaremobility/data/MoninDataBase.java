package com.softwaremobility.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.softwaremobility.objects.Detail_Recipe;
import com.softwaremobility.objects.Direction;
import com.softwaremobility.objects.Ingredient;
import com.softwaremobility.objects.Recipe;
import com.softwaremobility.utilities.PictureTools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

public class MoninDataBase {

    private static final String TAG = MoninDataBase.class.getSimpleName();

    /** --------------------------------- DataBase name -------------------------------------**/
    private static final String DataBaseName = "MoninDataBase";
    /** --------------------------------- Data Base Version ---------------------------------**/
    private static final int version = 10;
    /** --------------------------------- Table Statements ----------------------------------**/
    private static final String TMoninInfo = "CREATE TABLE " + MoninContract.MoninEntry.TABLE_NAME + " (" +
            MoninContract.MoninEntry.Key_IdRecipe + " TEXT PRIMARY KEY NOT NULL, " +
            MoninContract.MoninEntry.Key_Description + " TEXT NOT NULL, " +
            MoninContract.MoninEntry.Key_Rating + " INTEGER, " +
            MoninContract.MoninEntry.Key_IsCoffee + " TINYINT DEFAULT 0, " +
            MoninContract.MoninEntry.Key_IsAlcoholic + " TINYINT DEFAULT 0, " +
            MoninContract.MoninEntry.Key_IsMoninRecipe + " TINYINT DEFAULT 0, " +
            MoninContract.MoninEntry.Key_Region + " TINYINT DEFAULT 0, " +
            MoninContract.MoninEntry.Key_IsCommunity + " INTEGER, " +
            MoninContract.MoninEntry.Key_Date + " INTEGER, " +
            MoninContract.MoninEntry.Key_Location + " TEXT, " +
            MoninContract.MoninEntry.Key_ImageRecipe + " BLOB, " +
            MoninContract.MoninEntry.Key_ImageFlag + " BLOB, " +
            MoninContract.MoninEntry.Key_ImageUrl + " TEXT, " +
            "UNIQUE (" + MoninContract.MoninEntry.Key_IdRecipe + ") ON CONFLICT REPLACE);";

    private static final String TDetailRecipes = "CREATE TABLE " + MoninContract.DetailRecipeEntry.TABLE_NAME + " (" +
            MoninContract.DetailRecipeEntry.Key_Id + " TEXT PRIMARY KEY NOT NULL, " +
            MoninContract.DetailRecipeEntry.Key_FinalPortion + " TEXT, " +
            MoninContract.DetailRecipeEntry.Key_Level + " INTEGER, " +
            MoninContract.DetailRecipeEntry.Key_Directions + " TEXT, " +
            MoninContract.DetailRecipeEntry.Key_Glass + " TEXT, " +
            MoninContract.DetailRecipeEntry.Key_ServingSize + " INTEGER, " +
            MoninContract.DetailRecipeEntry.Key_IsFavorite + " INTEGER, " +
            MoninContract.DetailRecipeEntry.Key_Ingredients + " TEXT," +
            MoninContract.DetailRecipeEntry.Key_Instructions + " TEXT, " +
            "UNIQUE (" + MoninContract.DetailRecipeEntry.Key_Id + ") ON CONFLICT REPLACE);";

    private static final String TPeopleCommunity = "CREATE TABLE " + MoninContract.PeopleEntry.TABLE_NAME + " (" +
            MoninContract.PeopleEntry.Key_Id + " TEXT PRIMARY KEY NOT NULL, " +
            MoninContract.PeopleEntry.Key_Name + " TEXT, " +
            MoninContract.PeopleEntry.Key_RecipesCount + " INTEGER, " +
            MoninContract.PeopleEntry.Key_FavoritesCount + " INTEGER, " +
            MoninContract.PeopleEntry.Key_FollowersCount + " INTEGER, " +
            MoninContract.PeopleEntry.Key_FollowingCount + " INTEGER, " +
            MoninContract.PeopleEntry.Key_PersonalStatement + " TEXT, " +
            MoninContract.PeopleEntry.Key_FollowByMe + " INTEGER, " +
            MoninContract.PeopleEntry.Key_Photo + " BLOB, " +
            "UNIQUE (" + MoninContract.PeopleEntry.Key_Id + ") ON CONFLICT REPLACE);";

    private static final String TSlideShows = "CREATE TABLE " + MoninContract.GalleryEntry.TABLE_NAME + " (" +
            MoninContract.GalleryEntry.Key_Id + " TEXT PRIMARY KEY NOT NULL, " +
            MoninContract.GalleryEntry.Key_Title + " TEXT, " +
            MoninContract.GalleryEntry.Key_Description + " TEXT, " +
            MoninContract.GalleryEntry.Key_CreatedTime + " TEXT, " +
            MoninContract.GalleryEntry.Key_TemplateId + " INTEGER, " +
            MoninContract.GalleryEntry.Key_Active + " INTEGER, " +
            MoninContract.GalleryEntry.Key_Photo + " BLOB, " +
            "UNIQUE (" + MoninContract.GalleryEntry.Key_Id + ") ON CONFLICT REPLACE);";
    /** ---------------------------------- SQLite Helper ------------------------------------**/
    private MyHelper myDB;
    private SQLiteDatabase myDataBase;
    private final Context mContext;

    public MoninDataBase(Context c){ mContext = c; }

    private static class MyHelper extends SQLiteOpenHelper {

        public MyHelper(Context context) {
            super(context, DataBaseName, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TMoninInfo);
            db.execSQL(TDetailRecipes);
            db.execSQL(TPeopleCommunity);
            db.execSQL(TSlideShows);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MoninContract.MoninEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MoninContract.DetailRecipeEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MoninContract.PeopleEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MoninContract.GalleryEntry.TABLE_NAME);
            onCreate(db);
            db.setVersion(newVersion);
        }
    }
    /**  -----------------------------------------------------------------------------------**/

    /**
     * Open the DataBase
     * @return The database opened with write permissions
     */
    public MoninDataBase open(){
        myDB = new MyHelper(mContext);
        myDataBase = myDB.getWritableDatabase();
        return this;
    }

    /**
     * Open the DataBase
     * @return The database opened with read permissions
     */
    public MoninDataBase open_read(){
        myDB = new MyHelper(mContext);
        myDataBase = myDB.getReadableDatabase();
        return this;
    }

    /**
     * Close the DataBase
     */
    public void close(){
        myDB.close();
        myDataBase.close();
    }

    /**
     *
     * @param sql
     * @param selectionArgs
     * @return
     */
    public Cursor querySQL(String sql, String[] selectionArgs){
        Cursor regreso = null;
        open();
        regreso = myDataBase.rawQuery(sql, selectionArgs);
        return regreso;
    }

    public Cursor getRecipe(String id){
        Cursor cursor = querySQL("SELECT * FROM " + MoninContract.DetailRecipeEntry.TABLE_NAME +
                " WHERE " + MoninContract.DetailRecipeEntry.Key_Id + " = ? ", new String[]{id});
        return cursor;
    }

    public void deleteDetailRecipe(String id){
        open();
        myDataBase.delete(MoninContract.DetailRecipeEntry.TABLE_NAME,MoninContract.DetailRecipeEntry.Key_Id + "=?",new String[]{id});
        close();
    }

    public void deleteRecipe(String id){
        open();
        myDataBase.delete(MoninContract.MoninEntry.TABLE_NAME,MoninContract.DetailRecipeEntry.Key_Id + "=?",new String[]{id});
        close();
    }

    public void updateDetailRecipe(Detail_Recipe recipe, String id) {
        open();
        ContentValues values = new ContentValues();
        values.put(MoninContract.DetailRecipeEntry.Key_Id,id);
        values.put(MoninContract.DetailRecipeEntry.Key_ServingSize,recipe.getServingsSize());
        values.put(MoninContract.DetailRecipeEntry.Key_FinalPortion,recipe.getFinalPortion());
        values.put(MoninContract.DetailRecipeEntry.Key_Instructions,recipe.getInstructions());
        values.put(MoninContract.DetailRecipeEntry.Key_Glass,recipe.getGlass());
        values.put(MoninContract.DetailRecipeEntry.Key_Level,recipe.getLevel());
        values.put(MoninContract.DetailRecipeEntry.Key_Ingredients, convertIngredientsToString(recipe.getIngredients()));
        values.put(MoninContract.DetailRecipeEntry.Key_Directions, convertDirectionsToString(recipe.getDirections()));
        myDataBase.update(MoninContract.DetailRecipeEntry.TABLE_NAME,values,MoninContract.DetailRecipeEntry.Key_Id + "=?",new String[]{id});
        close();
    }

    public void updateRecipe(Recipe recipe, boolean isMoninRecipe) {
        open();
        ContentValues registro = new ContentValues();
        registro.put(MoninContract.MoninEntry.Key_IdRecipe, recipe.getId());
        registro.put(MoninContract.MoninEntry.Key_Description, recipe.getDescription());
        registro.put(MoninContract.MoninEntry.Key_Rating, recipe.getRating());
        registro.put(MoninContract.MoninEntry.Key_IsAlcoholic, recipe.isAlcohol() ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_IsCoffee, recipe.isCofee() ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_IsMoninRecipe, isMoninRecipe ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_Location, recipe.getLocation());
        registro.put(MoninContract.MoninEntry.Key_Date, System.currentTimeMillis());
        registro.put(MoninContract.MoninEntry.Key_Region, MoninDataBase.BOOLEAN.TRUE.value);
        registro.put(MoninContract.MoninEntry.Key_ImageRecipe, getImageInBytes(recipe.getImageURL(),recipe.isImageFromBitmap()));
        registro.put(MoninContract.MoninEntry.Key_ImageFlag, (byte[]) null);
        registro.put(MoninContract.MoninEntry.Key_ImageUrl, recipe.getImageURL());
        myDataBase.update(MoninContract.MoninEntry.TABLE_NAME,registro,MoninContract.DetailRecipeEntry.Key_Id + "=?",new String[]{recipe.getId()});
        close();
    }

    public void newEntryDetailRecipe(Detail_Recipe recipe, String id){
        open();
        ContentValues values = new ContentValues();
        values.put(MoninContract.DetailRecipeEntry.Key_Id,id);
        values.put(MoninContract.DetailRecipeEntry.Key_ServingSize,recipe.getServingsSize());
        values.put(MoninContract.DetailRecipeEntry.Key_FinalPortion,recipe.getFinalPortion());
        values.put(MoninContract.DetailRecipeEntry.Key_Instructions,recipe.getInstructions());
        values.put(MoninContract.DetailRecipeEntry.Key_Glass,recipe.getGlass());
        values.put(MoninContract.DetailRecipeEntry.Key_Level,recipe.getLevel());
        values.put(MoninContract.DetailRecipeEntry.Key_IsFavorite, recipe.isFavorite() ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        values.put(MoninContract.DetailRecipeEntry.Key_Ingredients, convertIngredientsToString(recipe.getIngredients()));
        values.put(MoninContract.DetailRecipeEntry.Key_Directions, convertDirectionsToString(recipe.getDirections()));
        myDataBase.insert(MoninContract.DetailRecipeEntry.TABLE_NAME,null,values);
        close();
    }

    public void newEntryRecipe(Recipe recipe, boolean isMoninRecipe){
        open();
        ContentValues registro = new ContentValues();
        registro.put(MoninContract.MoninEntry.Key_IdRecipe, recipe.getId());
        registro.put(MoninContract.MoninEntry.Key_Description, recipe.getDescription());
        registro.put(MoninContract.MoninEntry.Key_Rating, recipe.getRating());
        registro.put(MoninContract.MoninEntry.Key_IsAlcoholic, recipe.isAlcohol() ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_IsCoffee, recipe.isCofee() ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_IsMoninRecipe, isMoninRecipe ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_Location, recipe.getLocation());
        registro.put(MoninContract.MoninEntry.Key_Date, System.currentTimeMillis());
        registro.put(MoninContract.MoninEntry.Key_Region, MoninDataBase.BOOLEAN.TRUE.value);
        registro.put(MoninContract.MoninEntry.Key_ImageRecipe, getImageInBytes(recipe.getImageURL(),recipe.isImageFromBitmap()));
        registro.put(MoninContract.MoninEntry.Key_ImageFlag, (byte[]) null);
        registro.put(MoninContract.MoninEntry.Key_IsCommunity,0);
        registro.put(MoninContract.MoninEntry.Key_ImageUrl, recipe.getImageURL());
        myDataBase.insert(MoninContract.MoninEntry.TABLE_NAME,null,registro);
        close();
    }

    public void updateDetailRecipe(String id, ContentValues contentValues){
        open();
        myDataBase.update(MoninContract.DetailRecipeEntry.TABLE_NAME,contentValues,MoninContract.DetailRecipeEntry.Key_Id + "=?",new String[]{id});
        close();
    }

    public void updateFavoriteInMoninRecipes(String id, boolean favorite){
        int rating = getRatingRecipe(id);
        open();
        ContentValues contentValues = new ContentValues();
        rating = favorite ? rating + 1 : rating - 1;
        contentValues.put(MoninContract.MoninEntry.Key_Rating,rating);
        myDataBase.update(MoninContract.MoninEntry.TABLE_NAME,contentValues,MoninContract.MoninEntry.Key_IdRecipe + "=?",new String[]{id});
        close();
    }

    public int getRatingRecipe(String id){
        int rating = 0;
        open();
        Cursor cursor = myDataBase.rawQuery("SELECT " + MoninContract.MoninEntry.Key_IdRecipe + "," + MoninContract.MoninEntry.Key_Rating  + " FROM " +
                MoninContract.MoninEntry.TABLE_NAME + " WHERE " + MoninContract.MoninEntry.Key_IdRecipe + "=?",new String[]{id});
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            rating = cursor.getInt(cursor.getColumnIndex(MoninContract.MoninEntry.Key_Rating));
        }
        close();
        return rating;
    }

    private String convertDirectionsToString(List<Direction> directions) {
        String aux = "";
        for (Direction direction : directions){
            int sequence = direction.getSequence();
            String dir = direction.getDirection().equalsIgnoreCase("") || direction.getDirection().equalsIgnoreCase(" ") ? "null" : direction.getDirection();
            aux = aux + sequence + "|" + dir + "@";
        }
        int index = aux.lastIndexOf("@");
        if(index > 0){
            aux = aux.substring(0,index);
        }
        return aux;
    }

    private String convertIngredientsToString(List<Ingredient> ingredients) {
        String aux = "";
        if (ingredients.size() > 0) {
            for (Ingredient ingredient : ingredients) {
                String fraction = ingredient.getFraction().equalsIgnoreCase("") || ingredient.getFraction().equalsIgnoreCase(" ") ? "null" : ingredient.getFraction();
                String ingred = ingredient.getIngredient().equalsIgnoreCase("") || ingredient.getIngredient().equalsIgnoreCase(" ") ? "null" : ingredient.getIngredient();
                String measure = ingredient.getMeasure().equalsIgnoreCase("") || ingredient.getMeasure().equalsIgnoreCase(" ") ? "null" : ingredient.getMeasure();
                String measureID = ingredient.getMeasureId().equalsIgnoreCase("") || ingredient.getMeasureId().equalsIgnoreCase(" ") ? "null" : ingredient.getMeasureId();
                String fractionID = ingredient.getFractionID().equalsIgnoreCase("") || ingredient.getFractionID().equalsIgnoreCase(" ") ? "null" : ingredient.getFractionID();
                String id = ingredient.getId().equalsIgnoreCase("") || ingredient.getId().equalsIgnoreCase(" ") ? "null" : ingredient.getId();
                aux = aux + ingredient.getAmount() + "|" + fraction + "|" + ingred + "|" + measure + "|" + measureID + "|" + fractionID + "|" + id + "@";
            }
            int index = aux.lastIndexOf("@");
            aux = aux.substring(0, index);
            return aux;
        }else {
            return "";
        }
    }

    public SQLiteDatabase getInstanceDataBase(){
        if (myDataBase == null || !myDataBase.isOpen()) this.open();
        return myDataBase;
    }

    private byte[] getImageInBytes(String imagePath, boolean isFromBitmap){
        if (imagePath == null || imagePath.length() == 0){
            return null;
        }
        Bitmap bitmap;
        if (isFromBitmap){
           bitmap = BitmapFactory.decodeFile(imagePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
            byte[] array = stream.toByteArray();
            return array;
        }else {
            try {
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                int index = imagePath.lastIndexOf("/");
                String path = imagePath.substring(index);
                File imageFile = new File(storageDir,path);
                //bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                bitmap = PictureTools.decodeSampledBitmapFromUri(imageFile.getAbsolutePath(),1280,720);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                byte[] array = stream.toByteArray();
                return array;
            } catch (Exception e){
                bitmap = null;
                return null;
            }

        }
    }

    /*public void newEntryAnswers(String residuo_name, String company, float weight, boolean bien_recolectado, boolean otros_residuos, boolean residuos_peligrosos, boolean liquidos, String photo, String thumbnail, String sucursal){
        open();
        ContentValues registro = new ContentValues();
        registro.put(Key_ImageFlag, residuo_name);
        registro.put(Key_Company, company);
        registro.put(Key_ImageRecipe, weight);
        registro.put(Key_IsMoninRecipe, Reporte.regresoBooleano(bien_recolectado));
        registro.put(Key_OtherTrash, Reporte.regresoBooleano(otros_residuos));
        registro.put(Key_DangerousTrash, Reporte.regresoBooleano(residuos_peligrosos));
        registro.put(Key_Liquidos, Reporte.regresoBooleano(liquidos));
        registro.put(Key_Photo, photo);
        registro.put(Key_Thumbnail,thumbnail);
        registro.put(Key_Sucursal, sucursal);
        myDataBase.insert(nTAnswers, null, registro);
        close();
    }

    /**
     * This method is used to make a new entry for movies table
     * @param company String that represents the id of the movie.
     * @param sucursal String that represents the title of the movie.
     * @param time String that represents the description of the movie.
     * @param date String that represents the popularity value of the movie.
     */
  /*  public void newEntryRegistry(String company, String sucursal, String time, String date, long recolectionTime, String signPath, String supervisor, String position){
        open();
        ContentValues registro = new ContentValues();
        registro.put(Key_Company, company);
        registro.put(Key_Sucursal, sucursal);
        registro.put(Key_Time, time);
        registro.put(Key_Date, date);
        registro.put(Key_Sign,signPath);
        registro.put(Key_Supervisor, supervisor);
        registro.put(Key_Position, position);
        registro.put(Key_TimeRecolection, recolectionTime);
        myDataBase.insert(nTRegistry, null, registro);
        close();
    }

    public Cursor getUserData(){
        Cursor cursor = querySQL("SELECT * FROM " + nTUserInfo,null);
        return cursor;
    }

    /**
     * This method is used to retrieve data from movies table and you can sorted the data by sortBy
     * parameter.
     * @return Cursor that retrieves the data from the db of the query
     */
   /* public Cursor getAllCompanies(){
        open();
        String[] columns = new String[]{Key_Company,Key_Sucursal,Key_Date,Key_Time,Key_TimeRecolection,Key_Sign,Key_Supervisor,Key_Position};
        Cursor cursor = myDataBase.query(nTRegistry,columns,null,null,null,null,null);
        return cursor;
    }

    public Cursor getAllAnswersfrom(String sucursal){
        Cursor cursor = querySQL("SELECT * FROM " + nTAnswers + " WHERE " + Key_Sucursal + " = ? ORDER BY " + Key_Location + " ASC",new String[]{sucursal});
        return cursor;
    }


    public Cursor querySQL(String sql, String[] selectionArgs){
        Cursor regreso = null;
        open();
        regreso = myDataBase.rawQuery(sql,selectionArgs);
        return regreso;
    }

    /**
     * This method says if the table selected is empty
     * @param tableName String that represents the table that will be check.
     * @param primaryKey String that represents the primary field of the table selected.
     * @return a boolean values that represents if the table is empty.
     */
    public boolean isEmpty(String tableName, String primaryKey){
        try {
            open();
            Cursor c = myDataBase.query(tableName,new String[]{primaryKey},null,null,null,null,null);
            if(c.moveToFirst()){
                return false;
            }else {
                return true;
            }
        }catch (Exception e){
            return true;
        }finally {
            close();
        }
    }

    public boolean isEmpty(String tableName, String primaryKey, String selection, String argSelection){
        try {
            open();
            String sqliteStatement = "SELECT * FROM " + tableName + " WHERE " + selection + "= ?";
            Cursor cursor = myDataBase.rawQuery(sqliteStatement,new String[]{argSelection});
            if (cursor!=null && cursor.moveToFirst()){
                if (cursor.getCount() > 0){
                    close();
                    return false;
                }else {
                    return true;
                }
            }else {
                close();
                return true;
            }
        }catch (Exception e){
            close();
            return true;
        }
    }

    /**
     * This method is used to delete all data from the table selected
     * @param table String that represents the name of the table that will be erased
     */
    public void DeleteDataFromTable(String table){
        open();
        myDataBase.execSQL("DROP TABLE IF EXISTS " + table);
        if(table == MoninContract.MoninEntry.TABLE_NAME){
            myDataBase.execSQL(TMoninInfo);
        }else if(table == MoninContract.PeopleEntry.TABLE_NAME){
            myDataBase.execSQL(TPeopleCommunity);
        }else if(table == MoninContract.GalleryEntry.TABLE_NAME){
            myDataBase.execSQL(TSlideShows);
        }
        close();
    }

    public enum BOOLEAN{
        TRUE(1),FALSE(0);

        public final int value;
        BOOLEAN(int val){
            value = val;
        }
    }
}
