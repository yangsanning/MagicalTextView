package ysn.com.magicaltextview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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

    private TextPaint textPaint;
    private String defaultText, detailsText;
    private int textColor, detailsTextColor;
    private int textSize;
    private int detailsMarginLeft;

    private int paddingLeft, paddingRight, paddingTop, paddingBottom;
    /**
     * 行宽
     */
    private int rowWidth;
    /**
     * 最大显示行数
     */
    private int maxLine;

    private int viewWidth, viewHeight;

    private char[] textCharArray;
    private List<String> lineTextList;
    private Rect[] textRectArray = null;
    private String endTagText = "...";
    private int endTagTextWidth, detailsTextWidth, maxLineTextWidth, lineTextWidth;

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
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MagicalTextView);
        defaultText = typedArray.getString(R.styleable.MagicalTextView_mtv_default_text);
        textColor = typedArray.getColor(R.styleable.MagicalTextView_mtv_default_text_color, Color.BLACK);
        detailsText = typedArray.getString(R.styleable.MagicalTextView_mtv_details_text);
        detailsTextColor = typedArray.getColor(R.styleable.MagicalTextView_mtv_details_text_color, Color.BLUE);
        detailsMarginLeft = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_details_margin_left, 0);
        textSize = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_text_size, 50);

        paddingLeft = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_padding_left, 0);
        paddingRight = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_padding_right, 0);
        paddingTop = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_padding_top, 0);
        paddingBottom = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_padding_bottom, 0);

        rowWidth = typedArray.getDimensionPixelSize(R.styleable.MagicalTextView_mtv_row_width, 7);
        maxLine = typedArray.getInteger(R.styleable.MagicalTextView_mtv_max_line, 3);

        typedArray.recycle();
    }

    private void initPaint() {
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void initView() {
        setText(defaultText);

        endTagTextWidth = 0;
        endTagText = endTagText == null ? "" : endTagText;
        for (char c : endTagText.toCharArray()) {
            endTagTextWidth += getSingleCharWidth(c);
        }

        detailsTextWidth = 0;
        detailsText = detailsText == null ? "" : detailsText;
        for (char c : detailsText.toCharArray()) {
            detailsTextWidth += getSingleCharWidth(c);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;

        maxLineTextWidth = viewWidth - paddingLeft - paddingRight - endTagTextWidth - detailsMarginLeft - detailsTextWidth;
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

        lineTextList = new ArrayList<>();
        lineTextWidth = getMeasuredWidth() - paddingLeft - paddingRight;
        int currentLTextWidth = 0;
        StringBuilder textStringBuilder = new StringBuilder();
        for (int i = 0, length = textCharArray.length; i < length; i++) {
            char textChar = textCharArray[i];
            currentLTextWidth += getSingleCharWidth(textChar);
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
        }

        int textHeight = 0;
        textRectArray = new Rect[lineTextList.size()];
        for (int i = 0, length = lineTextList.size(); i < length && i < maxLine; i++) {
            String lineText = lineTextList.get(i);
            Rect lineTextRect = new Rect();
            textPaint.getTextBounds(lineText, 0, lineText.length(), lineTextRect);
            if (heightMode == MeasureSpec.AT_MOST) {
                if (i == length - 1 || i == maxLine - 1) {
                    textHeight += lineTextRect.height() + paddingBottom + paddingTop;
                }else {
                    textHeight += (lineTextRect.height() + rowWidth);
                }
            } else {
                if (textHeight == 0) {
                    textHeight = getMeasuredHeight();
                }
            }
            textRectArray[i] = lineTextRect;
        }

        setMeasuredDimension(getMeasuredWidth(), textHeight);
    }

    /**
     * 得到单个char的宽度
     */
    public float getSingleCharWidth(char textChar) {
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
        if (lineTextList == null || lineTextList.size() == 0) {
            return;
        }

        textPaint.setColor(textColor);
        int marginTop = getTopTextMarginTop();
        for (int i = 0, length = lineTextList.size(); i < length; i++) {
            String lineText = lineTextList.get(i);

            if (maxLine == i + 1) {
                canvas.drawText(getPassText(lineText), paddingLeft, marginTop, textPaint);
                textPaint.setColor(detailsTextColor);
                canvas.drawText(detailsText, (viewWidth - paddingRight - detailsTextWidth), marginTop, textPaint);
                break;
            } else {
                canvas.drawText(lineText, paddingLeft, marginTop, textPaint);
                marginTop += (textRectArray[i].height() + rowWidth);
            }
        }
    }

    private int getTopTextMarginTop() {
        return textRectArray[0].height() / 2 + paddingTop + getFontSpace();
    }

    private int getFontSpace() {
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        return (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
    }

    private String getPassText(String text) {
        lineTextWidth = (int) textPaint.measureText(text);
        if (lineTextWidth >= maxLineTextWidth) {
            return getPassText(text.substring(0, text.length() - 1));
        }
        return text + endTagText;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
                    onDetailsClickListener.onDetailsClick();
                } else {
                    performClick();
                }
                break;
            default:
                break;
        }
        return true;
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
        return (event.getX() > maxLineTextWidth) && event.getY() > rangeY;
    }

    /**
     * 设置文本
     */
    public void setText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        textCharArray = text.toCharArray();
        requestLayout();
    }

    public MagicalTextView setOnDetailsClickListener(OnDetailsClickListener onDetailsClickListener) {
        this.onDetailsClickListener = onDetailsClickListener;
        return this;
    }

    interface OnDetailsClickListener {

        void onDetailsClick();
    }
}
