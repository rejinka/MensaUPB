package de.ironjan.mensaupb.adapters;

import android.database.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import de.ironjan.mensaupb.*;
import de.ironjan.mensaupb.library.stw.*;

/**
 * Extension of MenuListItemViewBinder to show explained allergens too
 */
public class MenuDetailViewBinder implements android.support.v4.widget.SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (view == null || cursor == null || columnIndex < 0) {
            return false;
        }

        if (!(view instanceof TextView)) {
            return false;
        }


        switch (view.getId()) {
            case R.id.textPrice:
                bindPrice((TextView) view, cursor, columnIndex);
                return true;
            case R.id.textPricePer100g:
                bindPricePer100g((TextView) view, cursor, columnIndex);
                return true;
            case R.id.textBadges:
                bindBadges((TextView) view, cursor, columnIndex);
                return true;
            default:
                ((TextView) view).setText(cursor.getString(columnIndex));
                return true;
        }
    }

    private void bindPrice(TextView view, Cursor cursor, int columnIndex) {
        Double price = cursor.getDouble(columnIndex);
        if (price != 0) {
            // it can be 0 when syncing and not yet set
            String priceAsString = String.format(Locale.GERMAN, "%.2f €", price);
            view.setText(priceAsString);
        }
    }

    private void bindPricePer100g(TextView view, Cursor cursor, int columnIndex) {
        double price = cursor.getDouble(columnIndex - 1);
        if (price == 0) {
            return;
        }

        String string = cursor.getString(columnIndex);
        if (PriceType.WEIGHT.toString().equals(string)) {
            view.setText("/100g");
        } else {
            view.setText("");
        }
    }

    private void bindBadges(TextView textView, Cursor cursor, int columnIndex) {
        String string = cursor.getString(columnIndex);
        textView.setText(string);
    }
}
