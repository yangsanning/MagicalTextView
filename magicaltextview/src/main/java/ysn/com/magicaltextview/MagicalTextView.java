package ysn.com.magicaltextview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yangsanning
 * @ClassName MagicalTextView
 * @Description 一句话概括作用
 * @Date 2019/4/22
 * @History 2019/4/22 author: description:
 */
public class MagicalTextView extends View {

    private int paddingLeft, paddingRight, paddingTop, paddingBottom;

    private TextPaint boldTextPaint, defaultTextPaint, detailsTextPaint;
    private String defaultText, detailsText;
    private int boldTextColor, defaultTextColor, detailsTextColor;
    private int boldTextSize, defaultTextSize, detailsTextSize;
    private int detailsMarginLeft;
    private int boldEndIndex;

    /**
     * 行宽
     */
    private int rowWidth;
    /**
     * 最大显示行数
     */
    private int maxLine;
    /**
     * 强制最大高度
     */
    private boolean isForceMaxHeight;
    /**
     * isForceMaxHeight =true 的情况下，记录虚拟的开始行
     */
    private int virtualStartLine;

    private Bitmap detailsImage;
    private int detailsImageWidth;
    private int detailsImageHeight;
    private int detailsImageMarginLeft;

    private int viewWidth, viewHeight;

    private char[] textCharArray;
    private List<String> lineTextList = new ArrayList<>();
    private Rect[] textRectArray = null;
    private String endTagText = "...";
    private int endTagTextWidth, detailsTextWidth, lineTextWidth;

    private boolean isRangeDown;
    private OnDetailsClickListener onDetailsClickListener;

    public MagicalTextView(Context context) {
        this(context, null);
    }

    public MagicalTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagicalTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MagicalTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        initPaint();
        initView();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        Resources resources = context.getResources();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MagicalTextView);

        paddingLeft = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_padding_left, 0);
        paddingRight = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_padding_right, 0);
        paddingTop = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_padding_top, 0);
        paddingBottom = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_padding_bottom, 0);

        boldTextColor = typedArray.getColor(R.styleable.MagicalTextView_mtv_bold_text_color, resources.getColor(R.color.mtv_text_color));
        boldTextSize = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_bold_text_size, 50);
        boldEndIndex = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_bold_end_index, 0);

        defaultText = typedArray.getString(R.styleable.MagicalTextView_mtv_default_text);
        defaultTextColor = typedArray.getColor(R.styleable.MagicalTextView_mtv_default_text_color, resources.getColor(R.color.mtv_text_color));
        defaultTextSize = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_default_text_size, 50);

        detailsText = typedArray.getString(R.styleable.MagicalTextView_mtv_details_text);
        detailsTextColor = typedArray.getColor(R.styleable.MagicalTextView_mtv_details_text_color, resources.getColor(R.color.mtv_details_text_color));
        detailsTextSize = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_details_text_size, 50);
        detailsMarginLeft = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_details_margin_left, 0);

        Drawable detailsImage = typedArray.getDrawable(R.styleable.MagicalTextView_mtv_details_image);
        detailsImageWidth = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_details_image_width, 20);
        detailsImageHeight = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_details_image_height, 30);
        detailsImageMarginLeft = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_details_image_margin_left, 0);
        if (detailsImage != null) {
            zoomBitmap(drawableToBitmap(detailsImage));
        }

        rowWidth = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_row_width, 7);
        maxLine = typedArray.getInteger(R.styleable.MagicalTextView_mtv_max_line, 3);
        isForceMaxHeight = typedArray.getBoolean(R.styleable.MagicalTextView_mtv_force_max_height, false);

        typedArray.recycle();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        return ImageUtils.drawableToBitmap(drawable);
    }

    private void zoomBitmap(Bitmap bitmap) {
        detailsImage = ImageUtils.zoomBitmap(bitmap, detailsImageWidth, detailsImageHeight);
    }

    private void initPaint() {
        boldTextPaint = new TextPaint();
        boldTextPaint.setColor(boldTextColor);
        boldTextPaint.setTextSize(boldTextSize);
        boldTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        boldTextPaint.setAntiAlias(true);
        boldTextPaint.setTextAlign(Paint.Align.LEFT);

        defaultTextPaint = new TextPaint();
        defaultTextPaint.setColor(defaultTextColor);
        defaultTextPaint.setTextSize(defaultTextSize);
        defaultTextPaint.setAntiAlias(true);
        defaultTextPaint.setTextAlign(Paint.Align.LEFT);

        detailsTextPaint = new TextPaint();
        detailsTextPaint.setColor(detailsTextColor);
        detailsTextPaint.setTextSize(detailsTextSize);
        detailsTextPaint.setAntiAlias(true);
        detailsTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void initView() {
        setText(defaultText);

        endTagTextWidth = 0;
        endTagText = endTagText == null ? "" : endTagText;
        for (char c : endTagText.toCharArray()) {
            endTagTextWidth += getSingleCharWidth(defaultTextPaint, c);
        }

        setDetailsText(detailsText);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        initText(MeasureSpec.getMode(heightMeasureSpec));
    }

    /**
     * 初始化文本
     */
    private void initText(int heightMode) {
        if (textCharArray == null) {
            return;
        }

        initTextList();

        int textHeight = 0;
        textRectArray = new Rect[lineTextList.size()];
        for (int i = 0, length = lineTextList.size(); i < length && i < maxLine; i++) {
            String lineText = lineTextList.get(i);
            Rect lineTextRect = new Rect();
            defaultTextPaint.getTextBounds(lineText, 0, lineText.length(), lineTextRect);
//            if (heightMode == MeasureSpec.AT_MOST) {
//                if (i == length - 1 || i == maxLine - 1) {
//                    textHeight += lineTextRect.height() + paddingBottom + paddingTop;
//                } else {
//                    textHeight += (lineTextRect.height() + rowWidth);
//                }
//            } else {
//                if (textHeight == 0) {
//                    textHeight = getMeasuredHeight();
//                }
//            }
            if (i == length - 1 || i == maxLine - 1) {
                textHeight += lineTextRect.height() + paddingBottom + paddingTop;
            } else {
                textHeight += (lineTextRect.height() + rowWidth);
            }
            textRectArray[i] = lineTextRect;
        }

        setMeasuredDimension(getMeasuredWidth(), textHeight);
    }

    private void initTextList() {
        lineTextList.clear();
        lineTextWidth = getMeasuredWidth() - paddingLeft - paddingRight;
        int currentLTextWidth = 0;
        StringBuilder textStringBuilder = new StringBuilder();
        for (int i = 0, length = textCharArray.length; i < length; i++) {
            char textChar = textCharArray[i];
            // todo: 对加粗进行处理
            currentLTextWidth += getSingleCharWidth(defaultTextPaint, textChar);
            if (currentLTextWidth > lineTextWidth) {
                lineTextList.add(textStringBuilder.toString());
                textStringBuilder.delete(0, textStringBuilder.length());
                currentLTextWidth = 0;
                i--;
            } else {
                textStringBuilder.append(textChar);
                if (i == length - 1) {
                    lineTextList.add(textStringBuilder.toString());
                }
            }
            if (lineTextList.size() >= maxLine) {
                break;
            }
        }

        if (isForceMaxHeight) {
            virtualStartLine = lineTextList.size();
            for (int i = lineTextList.size(); i < maxLine; i++) {
                lineTextList.add("哈");
            }
        }
    }

    /**
     * 得到单个char的宽度
     */
    public float getSingleCharWidth(TextPaint textPaint, char textChar) {
        float[] width = new float[1];
        textPaint.getTextWidths(new char[]{textChar}, 0, 1, width);
        return width[0];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawText(canvas);
    }

    /**
     * 绘制文本
     */
    public void drawText(Canvas canvas) {
        if (lineTextList.size() == 0) {
            return;
        }

        // 绘制第一行(处理加粗情况)
        String lineText = lineTextList.get(0);
        String startText = lineText.substring(0, boldEndIndex);
        int marginTop = getTopTextMarginTop();
        canvas.drawText(startText, paddingLeft, marginTop, boldTextPaint);

        float startX = (boldTextPaint.measureText(startText) + paddingLeft);
        canvas.drawText(lineText.substring(boldEndIndex), startX, marginTop, defaultTextPaint);

        // 绘制剩余行数
        for (int i = 1, length = lineTextList.size(); i < length; i++) {
            lineText = lineTextList.get(i);
            marginTop += (textRectArray[i].height() + rowWidth);
            defaultTextPaint.setColor((isForceMaxHeight && i >= virtualStartLine) ? Color.TRANSPARENT : defaultTextColor);
            if (maxLine == i + 1) {
                canvas.drawText(getPassText(lineText, true), paddingLeft, marginTop, defaultTextPaint);
                canvas.drawText(detailsText, getDetailsStartX(), marginTop, detailsTextPaint);
                drawDetailsBitmap(canvas);
                break;
            } else {
                canvas.drawText(lineText, paddingLeft, marginTop, defaultTextPaint);
            }
        }
    }

    private int getTopTextMarginTop() {
//        return textRectArray[0].height() / 2 + paddingTop + getFontSpace(boldEndIndex == 0 ? defaultTextPaint : boldTextPaint);
        return textRectArray[0].height() / 2 + paddingTop + getFontSpace(defaultTextPaint);
    }

    private int getFontSpace(TextPaint textPaint) {
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        return (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
    }

    private String getPassText(String text, boolean isFirst) {
        lineTextWidth = (int) defaultTextPaint.measureText(text);
        if (lineTextWidth >= getMaxLineTextWidth()) {
            return getPassText(text.substring(0, text.length() - 1), false);
        }
        return isFirst ? text : text + endTagText;
    }

    private int getMaxLineTextWidth() {
        int maxLineTextWidth = viewWidth - paddingLeft - paddingRight - endTagTextWidth - detailsMarginLeft - detailsTextWidth;
        if (detailsImage != null) {
            maxLineTextWidth -= (detailsImageWidth + detailsImageMarginLeft);
        }
        return maxLineTextWidth;
    }

    private int getDetailsStartX() {
        int x = viewWidth - paddingRight - detailsTextWidth;
        return detailsImage == null ? x : (x - detailsImageWidth - detailsImageMarginLeft);
    }

    private void drawDetailsBitmap(Canvas canvas) {
        if (detailsImage != null) {
            float bitmapLeft = viewWidth - paddingRight - detailsImageWidth;
            Rect rect = new Rect();
            detailsTextPaint.getTextBounds(detailsText, 0, detailsText.length(), rect);
            int imageHeight = detailsImage.getHeight();
            int textHeight = rect.height();
            float bitmapTop;
            if (imageHeight > textHeight) {
                bitmapTop = viewHeight - (imageHeight - (imageHeight - textHeight) / 2f);
            } else {
                bitmapTop = viewHeight - (imageHeight + (textHeight - imageHeight) / 2f);
            }
            canvas.drawBitmap(detailsImage, bitmapLeft, bitmapTop, detailsTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (TextUtils.isEmpty(detailsText)) {
            return super.onTouchEvent(event);
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_DOWN:
                    isRangeDown = isRangeDown(event);
                    break;
                case MotionEvent.ACTION_UP:
                    // 判断是否是移动到外部再抬起手指
                    if (isOutsideUp(event)) {
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        break;
                    }
                    if (isRangeDown && isRangeDown(event) && onDetailsClickListener != null) {
                        onDetailsClickListener.onDetailsClick(this);
                    } else {
                        performClick();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    /**
     * 判断是否是移动到外部再抬起手指
     */
    private boolean isOutsideUp(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        return touchX < 0 || touchX > viewWidth || touchY < 0 || touchY > viewHeight;
    }

    /**
     * 是否在详情范围内
     */
    private boolean isRangeDown(MotionEvent event) {
        int rangeY = viewHeight - paddingBottom - textRectArray[maxLine - 1].height();
        return (event.getX() > getMaxLineTextWidth()) && event.getY() > rangeY;
    }

    public MagicalTextView setMaxLine(int maxLine) {
        this.maxLine = maxLine;
        requestLayout();
        return this;
    }

    public MagicalTextView setForceMaxHeight(boolean isForceMaxHeight) {
        this.isForceMaxHeight = isForceMaxHeight;
        return this;
    }

    public MagicalTextView setDetailsText(String detailsText) {
        this.detailsText = detailsText == null ? "" : detailsText;
        this.detailsTextWidth = 0;
        for (char c : this.detailsText.toCharArray()) {
            detailsTextWidth += getSingleCharWidth(detailsTextPaint, c);
        }
        return this;
    }

    public MagicalTextView setDetailsImage(Drawable detailsDrawable) {
        return setDetailsImage(drawableToBitmap(detailsDrawable));
    }

    public MagicalTextView setDetailsImage(Bitmap detailsImage) {
        zoomBitmap(detailsImage);
        invalidate();
        return this;
    }

    /**
     * 设置文本
     */
    public MagicalTextView setText(String text, int boldEndIndex) {
        if (TextUtils.isEmpty(text)) {
            return this;
        }
        setText(text);
        this.boldEndIndex = boldEndIndex;
        return this;
    }

    /**
     * 设置文本
     */
    public MagicalTextView setText(String text) {
        if (TextUtils.isEmpty(text)) {
            return this;
        }

        textCharArray = text.toCharArray();
        requestLayout();
        return this;
    }

    public MagicalTextView setOnDetailsClickListener(OnDetailsClickListener onDetailsClickListener) {
        this.onDetailsClickListener = onDetailsClickListener;
        return this;
    }

    public interface OnDetailsClickListener {

        void onDetailsClick(MagicalTextView magicalTextView);
    }
}
