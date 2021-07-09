package com.mrkitchen.digitalsignature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mrkitchen.digitalsignature.Document.PDSPageViewer;
import com.mrkitchen.digitalsignature.Document.PDSSaveAsPDFAsyncTask;
import com.mrkitchen.digitalsignature.Document.PDSViewPager;
import com.mrkitchen.digitalsignature.PDF.PDSPDFDocument;
import com.mrkitchen.digitalsignature.PDSModel.PDSElement;
import com.mrkitchen.digitalsignature.Signature.SignatureActivity;
import com.mrkitchen.digitalsignature.Signature.SignatureUtils;
import com.mrkitchen.digitalsignature.ImageViewer.PDSPageAdapter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.KeyStore;
import java.security.Security;
import java.util.ArrayList;


public class PDFViewerActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private static final int SIGNATURE_Request_CODE = 43;
    private static final int IMAGE_REQUEST_CODE = 45;
    private static final int DIGITAL_ID_REQUEST_CODE = 44;
    Uri pdfData = null;
    private PDSViewPager mViewPager;
    PDSPageAdapter imageAdapter;
    private Context mContext = null;
    private final boolean mFirstTap = true;
    private int mVisibleWindowHt = 0;
    private PDSPDFDocument mDocument = null;
    private Uri mDigitalID = null;
    public String mDigitalIDPassword = null;
    private Menu mMenu = null;
    private final UIElementsHandler mUIElemsHandler = new UIElementsHandler(this);
    AlertDialog passwordAlertDialog;
    AlertDialog signatureOptionDialog;
    public KeyStore keyStore = null;
    public String aliases = null;
    public boolean isSigned = false;
    public ProgressBar savingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_signature);
        this.mContext = getApplicationContext();
        mViewPager = findViewById(R.id.viewpager);
        savingProgress = findViewById(R.id.savingProgress);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String message = intent.getStringExtra("ActivityAction");
        if (message.equals("FileSearch")) {
            performFileSearch();
        } else if (message.equals("PDFOpen")) {
            ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra("PDFOpen");
            if (imageUris != null) {
                for (int i = 0; i < imageUris.size(); i++) {
                    Uri imageUri = imageUris.get(i);
                    openPDFViewer((imageUri));
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == READ_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (result != null) {
                    pdfData = result.getData();
                    openPDFViewer(pdfData);
                }
            } else {
                finish();
            }
        }
        if (requestCode == SIGNATURE_Request_CODE && resultCode == Activity.RESULT_OK) {
            String returnValue = result.getStringExtra("FileName");
            File fi = new File(returnValue);
            this.addElement(PDSElement.PDSElementType.PDSElementTypeSignature, fi, (float) SignatureUtils.getSignatureWidth((int) getResources().getDimension(R.dimen.sign_field_default_height), fi, getApplicationContext()), getResources().getDimension(R.dimen.sign_field_default_height));

        }
        if (requestCode == DIGITAL_ID_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (result != null) {
                    mDigitalID = result.getData();
                    GetPassword();
                }
            } else {
                Toast.makeText(PDFViewerActivity.this, "Digital certificate is not added with Signature", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (result != null) {
                Uri imageData = result.getData();
                Bitmap bitmap = null;
                try {
                    InputStream input = this.getContentResolver().openInputStream(imageData);
                    bitmap = BitmapFactory.decodeStream(input);
                    input.close();
                    if (bitmap != null)
                        this.addElement(PDSElement.PDSElementType.PDSElementTypeImage, bitmap, getResources().getDimension(R.dimen.sign_field_default_height), getResources().getDimension(R.dimen.sign_field_default_height));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.document_menu, menu);
        this.mMenu = menu;
        MenuItem saveItem = mMenu.findItem(R.id.action_save);
        saveItem.getIcon().setAlpha(130);
        MenuItem signItem = mMenu.findItem(R.id.action_sign);
        signItem.getIcon().setAlpha(255);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_sign) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.signature_option_dialog, null);
            dialogBuilder.setView(dialogView);

            Button signature = dialogView.findViewById(R.id.fromCollection);

            signature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), SignatureActivity.class);
                    startActivityForResult(intent, SIGNATURE_Request_CODE);
                    signatureOptionDialog.dismiss();
                }
            });


            Button image = dialogView.findViewById(R.id.fromImage);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("image/jpeg");
                    String[] mimetypes = {"image/jpeg", "image/png"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    startActivityForResult(intent, IMAGE_REQUEST_CODE);
                    signatureOptionDialog.dismiss();
                }
            });

            signatureOptionDialog = dialogBuilder.create();
            signatureOptionDialog.show();
            return true;
        }

        if (id == R.id.action_save) {
            savePDFDocument();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isSigned) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Save Document")
                    .setMessage("Want to save your changes to PDF document?")

                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            savePDFDocument();
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNeutralButton("Cancel",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        } else {
            finish();
        }
    }

    private void openPDFViewer(Uri pdfData) {
        try {
            PDSPDFDocument document = new PDSPDFDocument(this, pdfData);
            document.open();
            this.mDocument = document;
            imageAdapter = new PDSPageAdapter(getSupportFragmentManager(), document);
            updatePageNumber(1);
            mViewPager.setAdapter(imageAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(PDFViewerActivity.this, "Cannot open PDF, either PDF is corrupted or password protected", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/jpeg");
        String[] mimetypes = {"application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private int computeVisibleWindowHtForNonFullScreenMode() {
        return findViewById(R.id.docviewer).getHeight();
    }

    public int getVisibleWindowHeight() {
        if (this.mVisibleWindowHt == 0) {
            this.mVisibleWindowHt = computeVisibleWindowHtForNonFullScreenMode();
        }
        return this.mVisibleWindowHt;
    }

    public PDSPDFDocument getDocument() {
        return this.mDocument;
    }

    public void GetPassword() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.password_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText password = dialogView.findViewById(R.id.passwordText);
        Button submit = dialogView.findViewById(R.id.passwordSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.length() == 0) {
                    Toast.makeText(PDFViewerActivity.this, "Password can't be blank", Toast.LENGTH_LONG).show();
                } else {
                    mDigitalIDPassword = password.getText().toString();
                    BouncyCastleProvider provider = new BouncyCastleProvider();
                    Security.addProvider(provider);
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(mDigitalID);
                        keyStore = KeyStore.getInstance("pkcs12", provider.getName());
                        keyStore.load(inputStream, mDigitalIDPassword.toCharArray());
                        aliases = keyStore.aliases().nextElement();
                        passwordAlertDialog.dismiss();
                        Toast.makeText(PDFViewerActivity.this, "Digital certificate is added with Signature", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        if (e.getMessage().contains("wrong password")) {
                            Toast.makeText(PDFViewerActivity.this, "Password is incorrect or certificate is corrupted", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(PDFViewerActivity.this, "Something went wrong while adding Digital certificate", Toast.LENGTH_LONG).show();
                            passwordAlertDialog.dismiss();
                        }
                        e.printStackTrace();
                    }
                }
            }
        });

        passwordAlertDialog = dialogBuilder.create();
        passwordAlertDialog.show();
    }

    public void invokeMenuButton(boolean disableButtonFlag) {
        MenuItem saveItem = mMenu.findItem(R.id.action_save);
        saveItem.setEnabled(disableButtonFlag);
        MenuItem signPDF = mMenu.findItem(R.id.action_sign);
        signPDF.setEnabled(!disableButtonFlag);
        isSigned = disableButtonFlag;
        if (disableButtonFlag) {
            saveItem.getIcon().setAlpha(255);
            signPDF.getIcon().setAlpha(130);
        } else {
            saveItem.getIcon().setAlpha(130);
            signPDF.getIcon().setAlpha(255);
        }
    }

    public void addElement(PDSElement.PDSElementType fASElementType, File file, float f, float f2) {
        View focusedChild = this.mViewPager.getFocusedChild();
        if (focusedChild != null) {
            PDSPageViewer fASPageViewer = (PDSPageViewer) ((ViewGroup) focusedChild).getChildAt(0);
            if (fASPageViewer != null) {
                RectF visibleRect = fASPageViewer.getVisibleRect();
                float width = (visibleRect.left + (visibleRect.width() / 2.0f)) - (f / 2.0f);
                float height = (visibleRect.top + (visibleRect.height() / 2.0f)) - (f2 / 2.0f);
                fASPageViewer.getLastFocusedElementViewer();

                fASPageViewer.createElement(fASElementType, file, width, height, f, f2);

                if (!isSigned) {
                    AlertDialog dialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(PDFViewerActivity.this);
                    builder.setMessage("Do you want to add digital certificate with this Signature?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                    intent.setType("application/keychain_access");
                                    String[] mimetypes = {"application/x-pkcs12"};
                                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                                    startActivityForResult(intent, DIGITAL_ID_REQUEST_CODE);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create();
                    dialog.show();
                }
            }
            invokeMenuButton(true);
        }
    }


    public void addElement(PDSElement.PDSElementType fASElementType, Bitmap bitmap, float f, float f2) {
        View focusedChild = this.mViewPager.getFocusedChild();
        if (focusedChild != null && bitmap != null) {
            PDSPageViewer fASPageViewer = (PDSPageViewer) ((ViewGroup) focusedChild).getChildAt(0);
            if (fASPageViewer != null) {
                RectF visibleRect = fASPageViewer.getVisibleRect();
                float width = (visibleRect.left + (visibleRect.width() / 2.0f)) - (f / 2.0f);
                float height = (visibleRect.top + (visibleRect.height() / 2.0f)) - (f2 / 2.0f);
                fASPageViewer.getLastFocusedElementViewer();

                fASPageViewer.createElement(fASElementType, bitmap, width, height, f, f2);
                if (!isSigned) {
                    AlertDialog dialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(PDFViewerActivity.this);
                    builder.setMessage("Do you want to add digital certificate with this Signature?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                    intent.setType("application/keychain_access");
                                    String[] mimetypes = {"application/x-pkcs12"};
                                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                                    startActivityForResult(intent, DIGITAL_ID_REQUEST_CODE);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create();
                    dialog.show();
                }
            }
            invokeMenuButton(true);
        }
    }

    public void updatePageNumber(int i) {
        TextView textView = (TextView) findViewById(R.id.pageNumberTxt);
        findViewById(R.id.pageNumberOverlay).setVisibility(View.VISIBLE);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(i);
        stringBuilder.append("/");
        stringBuilder.append(this.mDocument.getNumPages());
        textView.setText(stringBuilder.toString());
        resetTimerHandlerForPageNumber(1000);
    }

    private void resetTimerHandlerForPageNumber(int i) {
        this.mUIElemsHandler.removeMessages(1);
        Message message = new Message();
        message.what = 1;
        this.mUIElemsHandler.sendMessageDelayed(message, i);
    }

    private void fadePageNumberOverlay() {
        Animation loadAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        View findViewById = findViewById(R.id.pageNumberOverlay);
        if (findViewById.getVisibility() == View.VISIBLE) {
            findViewById.startAnimation(loadAnimation);
            findViewById.setVisibility(View.INVISIBLE);
        }
    }

    private static class UIElementsHandler extends Handler {
        private final WeakReference<PDFViewerActivity> mActivity;

        public UIElementsHandler(PDFViewerActivity fASDocumentViewer) {
            this.mActivity = new WeakReference(fASDocumentViewer);
        }

        public void handleMessage(Message message) {
            PDFViewerActivity fASDocumentViewer = this.mActivity.get();
            if (fASDocumentViewer != null && message.what == 1) {
                fASDocumentViewer.fadePageNumberOverlay();
            }
            super.handleMessage(message);
        }
    }


    public void runPostExecution() {
        savingProgress.setVisibility(View.INVISIBLE);
        makeResult();

    }

    public void makeResult() {
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }

    public void savePDFDocument() {
        final Dialog dialog = new Dialog(PDFViewerActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View alertView = getLayoutInflater().inflate(R.layout.file_alert_dialog, null);
        final EditText edittext = alertView.findViewById(R.id.editText2);
        dialog.setContentView(alertView);
        dialog.setCancelable(true);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        (dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        (dialog.findViewById(R.id.bt_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = edittext.getText().toString();
                if (fileName.length() == 0) {
                    Toast.makeText(PDFViewerActivity.this, "File name should not be empty", Toast.LENGTH_LONG).show();
                } else {
                    PDSSaveAsPDFAsyncTask task = new PDSSaveAsPDFAsyncTask(PDFViewerActivity.this, fileName + ".pdf");
                    task.execute(new Void[0]);
                    dialog.dismiss();
                }
            }
        });
    }
}
