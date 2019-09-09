package ru.slybeaver.slycalendarview;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.slybeaver.slycalendarview.listeners.DateSelectListener;
import ru.slybeaver.slycalendarview.listeners.GridChangeListener;

/**
 * Created by psinetron on 29/11/2018.
 * http://slybeaver.ru
 */
public class GridAdapter extends ArrayAdapter {

    private SlyCalendarData calendarData;
    private int shiftMonth;
    private ArrayList<Date> monthlyDates = new ArrayList<>();
    private DateSelectListener listener;
    private LayoutInflater inflater;
    private GridChangeListener gridListener;


    public GridAdapter(
            @NonNull Context context,
            @NonNull SlyCalendarData calendarData,
            int shiftMonth,
            @Nullable DateSelectListener listener,
            GridChangeListener gridListener
    ) {
        super(context, R.layout.slycalendar_single_cell);
        this.calendarData = calendarData;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
        this.shiftMonth = shiftMonth;
        this.gridListener = gridListener;
        init();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(monthlyDates.get(position));

        Calendar calendarStart = createCalendarWith(calendarData.getSelectedStartDate());
        Calendar calendarEnd = createCalendarWith(calendarData.getSelectedEndDate());


        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.slycalendar_single_cell, parent, false);
        }

        ((TextView) view.findViewById(R.id.txtDate)).setText(String.valueOf(dateCal.get(Calendar.DAY_OF_MONTH)));
        view.findViewById(R.id.cellView).setBackgroundResource(R.color.slycalendar_defBackgroundColor);


        view.findViewById(R.id.cellView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.setTime(monthlyDates.get(position));
                selectedDate.set(Calendar.HOUR, 0);
                selectedDate.set(Calendar.MINUTE, 0);
                selectedDate.set(Calendar.SECOND, 0);
                selectedDate.set(Calendar.MILLISECOND, 0);
                if (listener != null) listener.dateSelect(selectedDate.getTime());
                notifyDataSetChanged();
                gridListener.gridChanged();
            }
        });

        view.findViewById(R.id.cellView).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) listener.dateLongSelect(monthlyDates.get(position));
                notifyDataSetChanged();
                gridListener.gridChanged();
                return true;
            }
        });


        view.findViewById(R.id.cellView).setBackgroundColor(calendarData.getBackgroundColor());

        if (calendarStart != null && calendarEnd != null) {
            if (dateCal.get(Calendar.DAY_OF_YEAR) == calendarStart.get(Calendar.DAY_OF_YEAR) &&
                    dateCal.get(Calendar.YEAR) == calendarStart.get(Calendar.YEAR)) {

                setUpShapeBackgroundOn(view, R.drawable.slycalendar_start_day);

            } else if (dateCal.getTimeInMillis() > calendarStart.getTimeInMillis()
                    && dateCal.getTimeInMillis() < calendarEnd.getTimeInMillis()) {

                setUpShapeBackgroundOn(view, R.drawable.slycalendar_middle_day);

                if (isMonday(position)) {

                    setUpShapeBackgroundOn(view, R.drawable.slycalendar_start_day);
                }
                if (nextDayIsMonday(position)) {

                    setUpShapeBackgroundOn(view, R.drawable.slycalendar_end_day);
                }


            } else if (dateCal.get(Calendar.DAY_OF_YEAR) == calendarEnd.get(Calendar.DAY_OF_YEAR)
                    && dateCal.get(Calendar.YEAR) == calendarEnd.get(Calendar.YEAR)) {

                setUpShapeBackgroundOn(view, R.drawable.slycalendar_end_day);
            }
        }


        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(calendarData.getShowDate());
        currentDate.add(Calendar.MONTH, shiftMonth);

        view.findViewById(R.id.frameSelected).setBackgroundResource(0);
        ((TextView) view.findViewById(R.id.txtDate)).setTextColor(calendarData.getTextColor());
        if (calendarStart != null && dateCal.get(Calendar.DAY_OF_YEAR) == calendarStart.get(Calendar.DAY_OF_YEAR) && currentDate.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH) && dateCal.get(Calendar.YEAR) == calendarStart.get(Calendar.YEAR)) {
            LayerDrawable shape = (LayerDrawable) ContextCompat.getDrawable(getContext(), R.drawable.slycalendar_selected_day);
            assert shape != null;
            ((GradientDrawable) shape.findDrawableByLayerId(R.id.selectedDateShapeItem)).setColor(calendarData.getSelectedColor());
            view.findViewById(R.id.frameSelected).setBackground(shape);
            ((TextView) view.findViewById(R.id.txtDate)).setTextColor(calendarData.getSelectedTextColor());
            ((TextView) view.findViewById(R.id.txtDate)).setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }

        if (calendarEnd != null && dateCal.get(Calendar.DAY_OF_YEAR) == calendarEnd.get(Calendar.DAY_OF_YEAR) && currentDate.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH) && dateCal.get(Calendar.YEAR) == calendarEnd.get(Calendar.YEAR)) {
            LayerDrawable shape = (LayerDrawable) ContextCompat.getDrawable(getContext(), R.drawable.slycalendar_selected_day);
            assert shape != null;
            ((GradientDrawable) shape.findDrawableByLayerId(R.id.selectedDateShapeItem)).setColor(calendarData.getSelectedColor());
            view.findViewById(R.id.frameSelected).setBackground(shape);
            ((TextView) view.findViewById(R.id.txtDate)).setTextColor(calendarData.getSelectedTextColor());
        }

        if (currentDate.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)) {
            (view.findViewById(R.id.txtDate)).setAlpha(1);
        } else {
            (view.findViewById(R.id.txtDate)).setAlpha(.2f);
        }

        return view;
    }

    private boolean isMonday(int position) {
        return position % 7 == 0;
    }

    private boolean nextDayIsMonday(int position) {
        return (position + 1) % 7 == 0;
    }

    private Calendar createCalendarWith(Date selectedStartDate) {
        Calendar calendarStart = null;
        if (selectedStartDate != null) {
            calendarStart = Calendar.getInstance();
            calendarStart.setTime(selectedStartDate);
            calendarStart.set(Calendar.HOUR, 0);
            calendarStart.set(Calendar.MINUTE, 0);
            calendarStart.set(Calendar.SECOND, 0);
            calendarStart.set(Calendar.MILLISECOND, 0);
        }
        return calendarStart;
    }

    private void setUpShapeBackgroundOn(View view, @DrawableRes int drawableResource) {
        LayerDrawable shape = (LayerDrawable) ContextCompat.getDrawable(getContext(), drawableResource);
        assert shape != null;
        ((GradientDrawable) shape.findDrawableByLayerId(R.id.dateShapeItem)).setColor(calendarData.getSelectedColor());
        (shape.findDrawableByLayerId(R.id.dateShapeItem)).setAlpha(20);
        view.findViewById(R.id.cellView).setBackground(shape);

    }

    @Override
    public int getCount() {
        return monthlyDates.size();
    }

    @Nullable
    @Override
    public Date getItem(int position) {
        return monthlyDates.get(position);
    }

    private void init() {
        monthlyDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(calendarData.getShowDate());
        calendar.add(Calendar.MONTH, shiftMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfTheMonth = calendarData.isFirstMonday() ? calendar.get(Calendar.DAY_OF_WEEK) - 2 : calendar.get(Calendar.DAY_OF_WEEK) - 1;
        calendar.add(Calendar.DAY_OF_MONTH, -firstDayOfTheMonth);
        int MAX_CALENDAR_COLUMN = 42;
        while (monthlyDates.size() < MAX_CALENDAR_COLUMN) {
            monthlyDates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }


}
