package com.expensesplitter.ui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
// Date Picker
public class DatePickerField extends JPanel {

    private final DatePicker datePicker;

    public DatePickerField(int width) {
        setLayout(new BorderLayout());
        setOpaque(false);

        DatePickerSettings settings = new DatePickerSettings();
        settings.setAllowKeyboardEditing(true);

        datePicker = new DatePicker(settings);
        datePicker.setPreferredSize(new Dimension(width, 32));

        add(datePicker, BorderLayout.CENTER);
    }

    public LocalDate getDate() {
        return datePicker.getDate();
    }

    public void setDate(LocalDate date) {
        datePicker.setDate(date);
    }

    public String getDateString() {
        LocalDate d = datePicker.getDate();
        return d != null ? d.toString() : "";
    }
}