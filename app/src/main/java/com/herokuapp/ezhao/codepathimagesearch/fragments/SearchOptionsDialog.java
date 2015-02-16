package com.herokuapp.ezhao.codepathimagesearch.fragments;

import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.herokuapp.ezhao.codepathimagesearch.R;
// ...

public class SearchOptionsDialog extends DialogFragment {
    private String optionSize;
    private String optionColor;
    private String optionType;
    private String optionSite;

    Spinner spSizePicker;
    Spinner spColorPicker;
    Spinner spTypePicker;
    EditText etSiteQuery;

    public interface SearchOptionsListener {
        void onSearchOptionsEdited(String optionSize, String optionColor, String optionType, String optionSite);
    }

    public SearchOptionsDialog() {
        // Empty constructor required for DialogFragment
    }

    public void setOptions(String optionSize, String optionColor, String optionType, String optionSite) {
        this.optionSize = optionSize;
        this.optionColor = optionColor;
        this.optionType = optionType;
        this.optionSite = optionSite;
    }

    public static SearchOptionsDialog newInstance(String optionSize, String optionColor, String optionType, String optionSite) {
        SearchOptionsDialog frag = new SearchOptionsDialog();
        frag.setOptions(optionSize, optionColor, optionType, optionSite);
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_search_options, container);

        // Set title for dialog
        getDialog().setTitle(getResources().getString(R.string.title_activity_search_options));

        // Set current parameters
        spSizePicker = (Spinner) view.findViewById(R.id.spSizePicker);
        setSpinnerToValue(spSizePicker, optionSize);
        spColorPicker = (Spinner) view.findViewById(R.id.spColorPicker);
        setSpinnerToValue(spColorPicker, optionColor);
        spTypePicker = (Spinner) view.findViewById(R.id.spTypePicker);
        setSpinnerToValue(spTypePicker, optionType);
        etSiteQuery = (EditText) view.findViewById(R.id.etSiteQuery);
        if (!optionSite.isEmpty()) {
            etSiteQuery.append(optionSite);
        }

        // Set up button listeners
        Button btnSave = (Button) view.findViewById(R.id.btnSave);
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchOptionsListener listener = (SearchOptionsListener) getActivity();
                optionSize = spSizePicker.getSelectedItem().toString();
                optionColor = spColorPicker.getSelectedItem().toString();
                optionType = spTypePicker.getSelectedItem().toString();
                optionSite = etSiteQuery.getText().toString();
                listener.onSearchOptionsEdited(optionSize, optionColor, optionType, optionSite);
                Log.i("EMILY", "Clicked save: " + optionSize + " " + optionColor + " " + optionType + " " + optionSite);
                dismiss();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("EMILY", "Clicked cancel");
                dismiss();
            }
        });

        return view;
    }

    // Helper function with the spinners
    private void setSpinnerToValue(Spinner spinner, String value) {
        int index = 0;
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                index = i;
                break; // terminate loop
            }
        }
        spinner.setSelection(index);
    }

}
