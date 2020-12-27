package com.github.barteksc.sample.View;

import android.app.Dialog;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.github.barteksc.sample.PDFUtil.PublicValues;
import com.github.barteksc.sample.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FragmentBottomSheetDialogFull extends BottomSheetDialogFragment implements View.OnClickListener {

    private BottomSheetBehavior mBehavior;
    private AppCompatButton appCompatButtonDelete, appCompatButtonUpdate;
    private String currAnnotationID;
    private float currX;
    private float currY;
    private BottomSheetCallback bottomSheetCallback;
    private PointF pointF;

    public interface BottomSheetCallback {
        void onDeleteAnnotationClicked(String annotationId);

        void onUpdateAnnotationClicked(String annotationId, PointF pointF);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.fragment_bottom_sheet_dialog_full, null);

        dialog.setContentView(view);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);

        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    // View is expended
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    // View is collapsed
                }

                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    dismiss();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        appCompatButtonDelete = view.findViewById(R.id.appCompatButton_delete);
        appCompatButtonDelete.setOnClickListener(this);
        appCompatButtonUpdate = view.findViewById(R.id.appCompatButton_update);
        appCompatButtonUpdate.setOnClickListener(this);

        parseInputBundle(getArguments());

        return dialog;
    }

    private void parseInputBundle(Bundle arguments) {
        if (arguments == null)
            return;

        if (!arguments.containsKey(PublicValues.KEY_ANNOTATION_ID))
            return;

        currAnnotationID = arguments.getString(PublicValues.KEY_ANNOTATION_ID);

        if (arguments.containsKey(PublicValues.KEY_X))
            currX = arguments.getFloat(PublicValues.KEY_X);

        if (arguments.containsKey(PublicValues.KEY_Y))
            currY = arguments.getFloat(PublicValues.KEY_Y);

    }

    @Override
    public void onStart() {
        super.onStart();
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.appCompatButton_delete) {
            if (bottomSheetCallback != null)
                bottomSheetCallback.onDeleteAnnotationClicked(currAnnotationID);
            dismiss();
        } else if (v.getId() == R.id.appCompatButton_update) {
            if (bottomSheetCallback != null)
                bottomSheetCallback.onUpdateAnnotationClicked(currAnnotationID, new PointF(currX, currY));
            dismiss();
        }
    }

    public void setBottomSheetCallback(BottomSheetCallback bottomSheetCallback) {
        this.bottomSheetCallback = bottomSheetCallback;
    }
}