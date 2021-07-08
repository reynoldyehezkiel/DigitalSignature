package com.mrkitchen.digitalsignature.ImageViewer;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.mrkitchen.digitalsignature.Document.PDSFragment;
import com.mrkitchen.digitalsignature.PDF.PDSPDFDocument;

public class PDSPageAdapter extends FragmentStatePagerAdapter {

    private final PDSPDFDocument mDocument;

    public PDSPageAdapter(FragmentManager fragmentManager, PDSPDFDocument fASDocument) {
        super(fragmentManager);
        this.mDocument = fASDocument;
    }

    public int getCount() {
        return this.mDocument.getNumPages();
    }

    public Fragment getItem(int i) {
        return PDSFragment.newInstance(i);
    }

}
