/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.sample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.link.LinkHandler;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnLongPressListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.model.LinkTapEvent;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.barteksc.pdfviewer.util.UriUtils;
import com.github.barteksc.sample.PDFUtil.PDFDrawer;
import com.github.barteksc.sample.PDFUtil.PDFException;
import com.github.barteksc.sample.PDFUtil.PublicValues;
import com.github.barteksc.sample.View.FragmentBottomSheetDialogFull;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.benjinus.pdfium.Bookmark;
import org.benjinus.pdfium.Meta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.options)
public class PDFViewActivity
        extends AppCompatActivity
        implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener, OnTapListener, OnLongPressListener, LinkHandler {

    private static final String TAG = PDFViewActivity.class.getSimpleName() + "11";

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "sample.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private PDFView.Configurator configurator = null;

    @ViewById
    PDFView pdfView;

    @ViewById
    RelativeLayout relativeLayout;

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    String pdfFileName;

    @OptionsItem(R.id.pickFile)
    void pickFile() {

        int permissionCheck = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    @OptionsItem(R.id.deleteAnnotation)
    void deleteAnnotation() {

        try {
            String filePath = UriUtils.getPathFromUri(PDFViewActivity.this, uri);

            PDFDrawer.removeAllAnnotation(filePath, pdfView.getCurrentPage());

            configurator.refresh(pdfView.getCurrentPage()); // refresh view

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    @AfterViews
    void afterViews() {
        pdfView.setBackgroundColor(Color.LTGRAY);
        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
        setTitle(pdfFileName);

        pickFile();
    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
    }

    private void displayFromUri(Uri uri) {

        pdfFileName = getFileName(uri);

        configurator = pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .enableSwipe(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .onTap(this)
                .onLongPress(this)
                .linkHandler(this);

        configurator.load();

    }

    @Override
    public boolean onTap(MotionEvent e) {

        // here we have a tap
        Log.i(TAG, "onTap --> X: " + e.getX() + " | Y: " + e.getY());
        Log.i(TAG, "--------------------------------------------------");

        // check zoom and scale
        Log.i(TAG, "zoom --> " + pdfView.getZoom() + " | scale " + pdfView.getScaleX() + " , " + pdfView.getScaleY());
        Log.i(TAG, "--------------------------------------------------");

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

        showLoading();

        // here we have a long click
        Log.i(TAG, "onLongPress --> X: " + e.getX() + " | Y: " + e.getY());
        Log.i(TAG, "--------------------------------------------------");

        PointF pointF = pdfView.convertScreenPintsToPdfCoordinates(e);
        Log.i(TAG, "PointF --> X: " + pointF.x + " | Y: " + pointF.y);
        Log.i(TAG, "--------------------------------------------------");

        try {

            String filePath = UriUtils.getPathFromUri(PDFViewActivity.this, uri);

            PDFDrawer.addAnnotatedBoxImageWithLocation(getApplicationContext(), filePath, pointF, pdfView.getCurrentPage());

            configurator.refresh(pdfView.getCurrentPage()); // refresh view

            // openCommentSheet();

        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (PDFException pdfException) {
            pdfException.printStackTrace();
        }

    }

    @OnActivityResult(REQUEST_CODE)
    public void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void loadComplete(int nbPages) {
        Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

        hideLoading();
    }

    public void printBookmarksTree(List<Bookmark> tree, String sep) {
        for (Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    /**
     * Listener for response to user permission request
     *
     * @param requestCode  Check that permission request code matches
     * @param permissions  Permissions that requested
     * @param grantResults Whether permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }

    @Override
    public void handleLinkEvent(LinkTapEvent event) {
        openCommentSheet(event.getLink().getUri(), event);
    }

    private void openCommentSheet(String annotationId, LinkTapEvent event) {
        // create ID bundle
        Bundle bundle = new Bundle();
        bundle.putString(PublicValues.KEY_ANNOTATION_ID, annotationId);

        if (event != null) {

            float baseX = event.getDocumentX();
            float baseY = event.getDocumentY();

            PointF pointF = pdfView.convertScreenPintsToPdfCoordinates(baseX, baseY); // convert to pdf

            bundle.putFloat(PublicValues.KEY_X, pointF.x);
            bundle.putFloat(PublicValues.KEY_Y, pointF.y);
        }

        // generate bottom sheet
        FragmentBottomSheetDialogFull fragment = new FragmentBottomSheetDialogFull();
        fragment.setArguments(bundle);
        fragment.setBottomSheetCallback(new FragmentBottomSheetDialogFull.BottomSheetCallback() {
            @Override
            public void onDeleteAnnotationClicked(String hashId) {
                Log.i(TAG, "onDeleteAnnotationClicked: " + hashId);
                try {
                    String filePath = UriUtils.getPathFromUri(PDFViewActivity.this, uri);
                    PDFDrawer.removeAnnotationWithCode(filePath, pdfView.getCurrentPage(), hashId);
                    configurator.refresh(pdfView.getCurrentPage()); // refresh view
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUpdateAnnotationClicked(String annotationId, PointF pointF) {
                Log.i(TAG, "onUpdateAnnotationClicked: " + annotationId);
                try {
                    String filePath = UriUtils.getPathFromUri(PDFViewActivity.this, uri);
                    PDFDrawer.removeAndUpdateAnnotationWithCode(PDFViewActivity.this, filePath, pdfView.getCurrentPage(), annotationId, pointF);
                    configurator.refresh(pdfView.getCurrentPage()); // refresh view
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        fragment.show(getSupportFragmentManager(), fragment.getTag());
    }

    private void showLoading() {
        relativeLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                relativeLayout.animate()
                        .translationY(0)
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                relativeLayout.setVisibility(View.GONE);
                            }
                        });
            }

        }.start();
    }

}
