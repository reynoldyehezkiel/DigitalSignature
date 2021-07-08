package com.mrkitchen.digitalsignature.PDSModel;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.mrkitchen.digitalsignature.Document.PDSElementViewer;

import java.io.File;

public class PDSElement {
    private float mHorizontalPadding = 0.0f;
    private float mLetterSpace = 0.0f;
    private float mMaxWidth = 0.0f;
    private float mMinWidth = 0.0f;
    private RectF mRect = null;
    private float mSize = 0.0f;
    private float mStrokeWidth = 0.0f;
    private final PDSElementType mType;
    public PDSElementViewer mElementViewer;
    private File mFile = null;
    private Bitmap bitmap = null;
    private float mVerticalPadding = 0.0f;
    private String mAliases;

    public enum PDSElementType {
        PDSElementTypeImage,
        PDSElementTypeSignature
    }

    public PDSElement(PDSElementType fASElementType, File file) {
        this.mType = fASElementType;
        mFile = file;
    }

    public PDSElement(PDSElementType fASElementType, Bitmap file) {
        this.mType = fASElementType;
        bitmap = file;
    }

    public PDSElementType getType() {
        return this.mType;
    }

    public void setRect(RectF rectF) {
        this.mRect = rectF;
    }

    public RectF getRect() {
        return this.mRect;
    }

    public void setSize(float f) {
        this.mSize = f;
    }

    public float getSize() {
        return this.mSize;
    }

    public void setMaxWidth(float f) {
        this.mMaxWidth = f;
    }

    public float getMaxWidth() {
        return this.mMaxWidth;
    }

    public void setMinWidth(float f) {
        this.mMinWidth = f;
    }

    public float getMinWidth() {
        return this.mMinWidth;
    }

    public void setHorizontalPadding(float f) {
        this.mHorizontalPadding = f;
    }

    public float getHorizontalPadding() {
        return this.mHorizontalPadding;
    }

    public void setVerticalPadding(float f) {
        this.mVerticalPadding = f;
    }

    public float getVerticalPadding() {
        return this.mVerticalPadding;
    }

    public void setStrokeWidth(float f) {
        this.mStrokeWidth = f;
    }

    public float getStrokeWidth() {
        return this.mStrokeWidth;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setLetterSpace(float f) {
        this.mLetterSpace = f;
    }

    public float getLetterSpace() {
        return this.mLetterSpace;
    }

    public File getFile() {
        return mFile;
    }

    public String getAlises() {
        return mAliases;
    }

    public void setAlises(String alises) {
        this.mAliases = alises;
    }
}
