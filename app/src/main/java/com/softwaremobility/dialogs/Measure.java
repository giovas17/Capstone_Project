package com.softwaremobility.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

import com.softwaremobility.listeners.MeasureListener;
import com.softwaremobility.monin.R;
import com.softwaremobility.objects.FractionOption;
import com.softwaremobility.objects.Ingredient;
import com.softwaremobility.objects.MeasureOption;

import java.util.List;

/**
 * Created by DarkGeat on 3/24/2016.
 */
public class Measure extends Dialog {

    private static final String TAG = Measure.class.getSimpleName();
    private String[] measures;
    private String[] fractions;

    public Measure(final Context context, final MeasureListener listener, final Ingredient ingredient, @Nullable final List<MeasureOption> optionsMeasure, @Nullable final List<FractionOption> optionsFractions) {
        super(context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setCanceledOnTouchOutside(true);
        setContentView(R.layout.dialog_measure_new_recipe);

        final NumberPicker numByTen = (NumberPicker)findViewById(R.id.numberByTen);
        final NumberPicker numUnits = (NumberPicker)findViewById(R.id.numberUnits);
        final NumberPicker numFractions = (NumberPicker)findViewById(R.id.numberFraction);
        final NumberPicker numMajor = (NumberPicker)findViewById(R.id.numberMajor);

        Button buttonOk = (Button)findViewById(R.id.positiveButton);
        Button buttonCancel = (Button)findViewById(R.id.negativeButton);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nonUnits = numByTen.getValue();
                int units = numUnits.getValue();
                int amount = (nonUnits * 10) + units;
                String id = ingredient.getId();
                String ingredients = ingredient.getIngredient();
                String amountValue = String.valueOf(amount);
                String fraction = fractions[numFractions.getValue()];
                String major = measures[numMajor.getValue()];
                assert optionsMeasure != null;
                String major_id = optionsMeasure.get(numMajor.getValue()).getId();
                String fraction_id = optionsFractions.get(numFractions.getValue()).getId();
                //double amount, String measure, String ingredient, String fraction, String fractionID, String measureId, String id
                listener.onMeasureSelected(new Ingredient(Double.parseDouble(amountValue), major, ingredients,fraction,fraction_id,major_id,id));
                Log.d(TAG, amountValue + ", " + fraction + ", " + major);
                dismiss();
            }
        });

        numByTen.setMaxValue(9);
        numUnits.setMaxValue(9);
        numByTen.setMinValue(0);
        numUnits.setMinValue(0);
        numByTen.setWrapSelectorWheel(true);
        numUnits.setWrapSelectorWheel(true);
        if (optionsMeasure != null && optionsMeasure.size() > 0){
            measures = new String[optionsMeasure.size()];
            for (int i = 0 ; i < optionsMeasure.size() ; i++){
                measures[i] = optionsMeasure.get(i).getMeasure();
            }
        }
        else {
            measures = context.getResources().getStringArray(R.array.fractions_array);
        }

        if (optionsFractions != null && optionsFractions.size() > 0){
            fractions = new String[optionsFractions.size()];
            for (int i = 0 ; i < optionsFractions.size() ; i++){
                fractions[i] = optionsFractions.get(i).getFraction();
            }
        }
        else {
            fractions = context.getResources().getStringArray(R.array.fractions_array);
        }

        numFractions.setDisplayedValues(fractions);
        numFractions.setWrapSelectorWheel(true);
        numFractions.setMaxValue(fractions.length - 1);
        numFractions.setMinValue(0);
        numMajor.setDisplayedValues(measures);
        numMajor.setMaxValue(measures.length - 1);
        numMajor.setMinValue(0);
        if (!String.valueOf(ingredient.getAmount()).equalsIgnoreCase("") && !ingredient.getFraction().equalsIgnoreCase("") &&
                !ingredient.getMeasure().equalsIgnoreCase("")) {
            int amount = Integer.parseInt(String.valueOf((int)ingredient.getAmount()));
            if (amount > 9) {
                int decades = amount / 10;
                numByTen.setValue(decades);
                amount = amount - decades;
            }
            numUnits.setValue(amount);
            for (int i = 0; i < fractions.length; i++) {
                if (fractions[i].equalsIgnoreCase(ingredient.getFraction())) {
                    numFractions.setValue(i);
                    break;
                }
            }
            for (int i = 0; i < measures.length; i++) {
                if (measures[i].equalsIgnoreCase(ingredient.getMeasure())) {
                    numMajor.setValue(i);
                    break;
                }
            }
        }
    }
}
