package com.codename1.impl.android;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.MotionEvent;
import com.codename1.media.Media;
import com.codename1.ui.geom.Dimension;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.codename1.ui.BrowserComponent;

import com.codename1.ui.Component;
import com.codename1.ui.Display;
import com.codename1.ui.Font;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.PeerComponent;
import com.codename1.ui.events.ActionEvent;
import com.codename1.impl.CodenameOneImplementation;
import com.codename1.impl.VirtualKeyboardInterface;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Vector;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;
import com.codename1.io.BufferedInputStream;
import com.codename1.io.BufferedOutputStream;
import com.codename1.io.ConnectionRequest;
import com.codename1.location.LocationManager;
import com.codename1.messaging.Message;
import com.codename1.ui.Form;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Rectangle;
import com.codename1.ui.layouts.BorderLayout;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

public class AndroidImplementation extends CodenameOneImplementation implements IntentResultListener {

    /**
     * make sure these important keys have a negative value when passed
     * to Codename One or they might be interpreted as characters.
     */
    static final int DROID_IMPL_KEY_LEFT = -23446;
    static final int DROID_IMPL_KEY_RIGHT = -23447;
    static final int DROID_IMPL_KEY_UP = -23448;
    static final int DROID_IMPL_KEY_DOWN = -23449;
    static final int DROID_IMPL_KEY_FIRE = -23450;
    static final int DROID_IMPL_KEY_MENU = -23451;
    static final int DROID_IMPL_KEY_BACK = -23452;
    static final int DROID_IMPL_KEY_BACKSPACE = -23453;
    static final int DROID_IMPL_KEY_CLEAR = -23454;
    static final int DROID_IMPL_KEY_SEARCH = -23455;
    static final int DROID_IMPL_KEY_CALL = -23456;
    static final int DROID_IMPL_KEY_VOLUME_UP = -23457;
    static final int DROID_IMPL_KEY_VOLUME_DOWN = -23458;
    static final int DROID_IMPL_KEY_MUTE = -23459;
    static int[] leftSK = new int[]{DROID_IMPL_KEY_MENU};
    static AndroidView myView = null;
    private Paint defaultFont;
    private final char[] tmpchar = new char[1];
    private final RectF tmprectF = new RectF();
    private final Rect tmprect = new Rect();
    private final Path tmppath = new Path();
    protected int defaultFontHeight;
    private int lastSizeChangeW = -1;
    private int lastSizeChangeH = -1;
    private final char[] tmpDrawChar = new char[1];
    private Vibrator v = null;
    private boolean vibrateInitialized = false;
    private int displayWidth;
    private int displayHeight;
    Activity activity;
    RelativeLayout relativeLayout;
    final Vector nativePeers = new Vector();
    int lastDirectionalKeyEventReceivedByWrapper;

    private Uri imageUri;
    private ActionListener intentResponse;
    
    
    @Override
    public void init(Object m) {
        this.activity = (Activity) m;

        try {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {
            Log.d("Codename One", "No idea why this throws a Runtime Error", e);
        }
        
        if (m instanceof CodenameOneActivity) {
            ((CodenameOneActivity) m).setIntentResultListener(this);
        }
        
        int hardwareAcceleration = 16777216;
        activity.getWindow().setFlags(hardwareAcceleration, hardwareAcceleration);
        /**
         * translate our default font height depending on the screen density.
         * this is required for new high resolution devices.  otherwise everything
         * looks awfully small.
         *
         * we use our default font height value of 16 and go from there.  i thought
         * about using new Paint().getTextSize() for this value but if some new
         * version of android suddenly returns values already tranlated to the screen
         * then we might end up with too large fonts.  the documentation is not very
         * precise on that.
         */
        final int defaultFontPixelHeight = 16;
        this.defaultFontHeight = this.translatePixelForDPI(defaultFontPixelHeight);


        this.defaultFont = (Paint) ((Object[]) this.createFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM))[0];
        Display.getInstance().setTransitionYield(-1);
        initSurface();
        /**
         * devices are extemely sensitive so dragging should start
         * a little later than suggested by default implementation.
         */
        this.setDragStartPercentage(6);
        VirtualKeyboardInterface vkb = new AndroidKeyboard(this);
        Display.getInstance().registerVirtualKeyboard(vkb);
        Display.getInstance().setDefaultVirtualKeyboard(vkb);

        saveTextEditingState();
        
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (nativePeers.size() > 0) {
            AndroidPeer[] peers = new AndroidPeer[nativePeers.size()];
            for (int i = 0; i < leftSK.length; i++) {
                peers[i] = (AndroidPeer) nativePeers.elementAt(i);
            }
            for (int i = 0; i < peers.length; i++) {
                peers[i].release();
            }
        }
    }

    public int translatePixelForDPI(int pixel) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, pixel,
                activity.getResources().getDisplayMetrics());
    }

    @Override
    public int getDeviceDensity() {
        if (isTablet()) {
            return Display.DENSITY_MEDIUM;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                return Display.DENSITY_LOW;
            case DisplayMetrics.DENSITY_HIGH:
                return Display.DENSITY_HIGH;
            case DisplayMetrics.DENSITY_XHIGH:
                return Display.DENSITY_VERY_HIGH;
            default:
                return Display.DENSITY_MEDIUM;
        }
    }

    public void deinitialize() {
        myView = null;
    }

    /**
     * init view. a lot of back and forth between this thread and the UI thread.
     */
    private void initSurface() {

        relativeLayout = new RelativeLayout(activity);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT));
        relativeLayout.setFocusable(false);

        //FrameLayout v = new FrameLayout(activity);

        myView = new AndroidView(activity, AndroidImplementation.this);
        myView.setVisibility(View.VISIBLE);

        relativeLayout.addView(myView);
        myView.setVisibility(View.VISIBLE);
        activity.setContentView(relativeLayout);

        myView.requestFocus();
    }

    @Override
    public void confirmControlView() {
        myView.setVisibility(View.VISIBLE);
    }

    public void hideNotifyPublic() {
        super.hideNotify();
        //make sure the inline edit is cleaned
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // Must be called from the UI thread
//                InPlaceEditView.endEdit();
//            }
//        });
        saveTextEditingState();
    }

    public void showNotifyPublic() {
        super.showNotify();
    }

    @Override
    public boolean isMinimized() {
        return myView == null || myView.getVisibility() != View.VISIBLE;
    }

    @Override
    public boolean minimizeApplication() {
        activity.runOnUiThread(new Runnable() {

            public void run() {
                if (myView != null) {
                    myView.setVisibility(View.INVISIBLE);
                }
            }
        });
        return true;
    }

    @Override
    public void restoreMinimizedApplication() {
        activity.runOnUiThread(new Runnable() {

            public void run() {
                if (myView != null) {
                    myView.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    public void editString(final Component cmp, int maxSize, final int constraint, String text, int keyCode) {
        if (keyCode > 0 && getKeyboardType() == Display.KEYBOARD_TYPE_QWERTY) {
            text += (char) keyCode;
        }
        Display display = Display.getInstance();
        String userInput = InPlaceEditView.edit(this, cmp, constraint);
        display.onEditingComplete(cmp, userInput);
    }

    protected boolean editInProgress() {
        return InPlaceEditView.isEditing();
    }

    @Override
    public void saveTextEditingState() {
        final boolean[] flag = new boolean[]{false};

        // InPlaceEditView.endEdit must be called from the UI thread.
        // We must wait for this call to be over, otherwise Codename One's painting
        // of the next form will be garbled.
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // Must be called from the UI thread
                InPlaceEditView.endEdit();

                synchronized (flag) {
                    flag[0] = true;
                    flag.notify();
                }
            }
        });

        if (!flag[0]) {
            // Wait (if necessary) for the asynchronous runOnUiThread to do its work
            synchronized (flag) {

                try {
                    flag.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    protected void setLastSizeChangedWH(int w, int h) {
        this.lastSizeChangeW = w;
        this.lastSizeChangeH = h;
    }

    @Override
    public boolean handleEDTException(final Throwable err) {

        final boolean[] messageComplete = new boolean[]{false};

        Log.e("Codename One", "Err on EDT", err);

        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                UIManager m = UIManager.getInstance();
                final FrameLayout frameLayout = new FrameLayout(
                        activity);
                final TextView textView = new TextView(
                        activity);
                textView.setGravity(Gravity.CENTER);
                frameLayout.addView(textView, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.FILL_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT));
                textView.setText("An internal application error occurred: " + err.toString());
                AlertDialog.Builder bob = new AlertDialog.Builder(
                        activity);
                bob.setView(frameLayout);
                bob.setTitle("");
                bob.setPositiveButton(m.localize("ok", "OK"),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface d, int which) {
                                d.dismiss();
                                synchronized (messageComplete) {
                                    messageComplete[0] = true;
                                    messageComplete.notify();
                                }
                            }
                        });
                AlertDialog editDialog = bob.create();
                editDialog.show();
            }
        });

        synchronized (messageComplete) {
            if (messageComplete[0]) {
                return true;
            }
            try {
                messageComplete.wait();
            } catch (Exception ignored) {
                ;
            }
        }
        return true;
    }

    //@Override
    public InputStream getResourceAsStream(Class cls, String resource) {
        try {
            if (resource.startsWith("/")) {
                resource = resource.substring(1);
            }
            return activity.getAssets().open(resource);
        } catch (IOException ex) {
            Log.i("Codename One", "Failed to load resource: " + resource);
            return null;
        }
    }

    @Override
    protected void pointerPressed(final int x, final int y) {
        super.pointerPressed(x, y);
    }

    @Override
    protected void pointerPressed(final int[] x, final int[] y) {
        super.pointerPressed(x, y);
    }

    @Override
    protected void pointerReleased(final int x, final int y) {
        super.pointerReleased(x, y);
    }

    @Override
    protected void pointerReleased(final int[] x, final int[] y) {
        super.pointerReleased(x, y);
    }

    @Override
    protected void pointerDragged(int x, int y) {
        super.pointerDragged(x, y);
    }

    @Override
    protected void pointerDragged(int[] x, int[] y) {
        super.pointerDragged(x, y);
    }

    @Override
    protected int getDragAutoActivationThreshold() {
        return 1000000;
    }

    @Override
    public void flushGraphics() {
        if (myView != null) {
            myView.flushGraphics();
        }

    }

    @Override
    public void flushGraphics(int x, int y, int width, int height) {
        this.tmprect.set(x, y, x + width, y + height);
        if (myView != null) {
            myView.flushGraphics(this.tmprect);
        }
    }

    @Override
    public int charWidth(Object nativeFont, char ch) {
        this.tmpchar[0] = ch;
        float w = (nativeFont == null ? this.defaultFont
                : (Paint) ((Object[]) nativeFont)[0]).measureText(this.tmpchar, 0, 1);
        if (w - (int) w > 0) {
            return (int) (w + 1);
        }
        return (int) w;
    }

    @Override
    public int charsWidth(Object nativeFont, char[] ch, int offset, int length) {
        float w = (nativeFont == null ? this.defaultFont
                : (Paint) ((Object[]) nativeFont)[0]).measureText(ch, offset, length);
        if (w - (int) w > 0) {
            return (int) (w + 1);
        }
        return (int) w;
    }

    @Override
    public int stringWidth(Object nativeFont, String str) {
        float w = (nativeFont == null ? this.defaultFont
                : (Paint) ((Object[]) nativeFont)[0]).measureText(str);
        if (w - (int) w > 0) {
            return (int) (w + 1);
        }
        return (int) w;
    }

    @Override
    public void setNativeFont(Object graphics, Object font) {
        if (font == null) {
            font = this.defaultFont;
        }
        if (font instanceof Object[]) {
            ((AndroidGraphics) graphics).setFont((Paint) ((Object[]) font)[0]);
        } else {
            ((AndroidGraphics) graphics).setFont((Paint) font);
        }
    }

    @Override
    public int getHeight(Object nativeFont) {
        Paint font = (nativeFont == null ? this.defaultFont
                : (Paint) ((Object[]) nativeFont)[0]);
        return font.getFontMetricsInt(font.getFontMetricsInt());
    }

    public int getFace(Object nativeFont) {
        if (nativeFont == null) {
            return Font.FACE_SYSTEM;
        }
        int[] i = (int[]) ((Object[]) nativeFont)[1];
        return i[0];
    }

    public int getStyle(Object nativeFont) {
        if (nativeFont == null) {
            return Font.STYLE_PLAIN;
        }
        int[] i = (int[]) ((Object[]) nativeFont)[1];
        return i[1];
    }

    @Override
    public int getSize(Object nativeFont) {
        if (nativeFont == null) {
            return Font.SIZE_MEDIUM;
        }
        int[] i = (int[]) ((Object[]) nativeFont)[1];
        return i[2];
    }

    @Override
    public Object createFont(int face, int style, int size) {
        Paint font = new TextPaint();
        font.setAntiAlias(true);
        Typeface typeface = null;
        switch (face) {
            case Font.FACE_MONOSPACE:
                typeface = Typeface.MONOSPACE;
                break;
            default:
                typeface = Typeface.DEFAULT;
                break;
        }

        int fontstyle = Typeface.NORMAL;
        if ((style & Font.STYLE_BOLD) != 0) {
            fontstyle |= Typeface.BOLD;
        }
        if ((style & Font.STYLE_ITALIC) != 0) {
            fontstyle |= Typeface.ITALIC;
        }


        int height = this.defaultFontHeight;
        int diff = height / 3;

        switch (size) {
            case Font.SIZE_SMALL:
                height -= diff;
                break;
            case Font.SIZE_LARGE:
                height += diff;
                break;
        }

        font.setTypeface(Typeface.create(typeface, fontstyle));
        font.setUnderlineText((style & Font.STYLE_UNDERLINED) != 0);
        font.setTextSize(height);
        return new Object[]{font, new int[]{face, style, size}};

    }

    /**
     * Loads a native font based on a lookup for a font name and attributes. Font lookup
     * values can be separated by commas and thus allow fallback if the primary font
     * isn't supported by the platform.
     *
     * @param lookup string describing the font
     * @return the native font object
     */
    public Object loadNativeFont(String lookup) {
        try {
            lookup = lookup.split(";")[0];
            Paint font = new TextPaint();
            font.setAntiAlias(true);
            int typeface = Typeface.NORMAL;
            String familyName = lookup.substring(0, lookup.indexOf("-"));
            String style = lookup.substring(lookup.indexOf("-") + 1, lookup.lastIndexOf("-"));
            String size = lookup.substring(lookup.lastIndexOf("-") + 1, lookup.length());

            if (style.equals("bolditalic")) {
                typeface = Typeface.BOLD_ITALIC;
            } else if (style.equals("italic")) {
                typeface = Typeface.ITALIC;
            } else if (style.equals("bold")) {
                typeface = Typeface.BOLD;
            }
            font.setTypeface(Typeface.create(familyName, typeface));
            font.setTextSize(Integer.parseInt(size));
            return new Object[]{font, new int[]{0, 0, 0}};
        } catch (Exception err) {
            return null;
        }
    }

    /**
     * Indicates whether loading a font by a string is supported by the platform
     *
     * @return true if the platform supports font lookup
     */
    public boolean isLookupFontSupported() {
        return true;
    }

    @Override
    public boolean isAntiAliasedTextSupported() {
        return true;
    }

    @Override
    public void setAntiAliasedText(Object graphics, boolean a) {
        ((AndroidGraphics) graphics).getFont().setAntiAlias(a);
    }

    @Override
    public Object getDefaultFont() {
        TextPaint paint = new TextPaint();
        paint.set(this.defaultFont);
        return new Object[]{paint, new int[]{Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM}};
    }

    @Override
    public Object getNativeGraphics() {
        return this.myView.getGraphics();
    }

    @Override
    public Object getNativeGraphics(Object image) {
        return new AndroidGraphics(this, new Canvas((Bitmap) image));
    }

    @Override
    public void getRGB(Object nativeImage, int[] arr, int offset, int x, int y,
            int width, int height) {
        ((Bitmap) nativeImage).getPixels(arr, offset, width, x, y, width,
                height);
    }

    @Override
    public Object createImage(String path) throws IOException {
        InputStream in = this.getResourceAsStream(null, path);
        if (in == null) {
            throw new IOException("Resource not found. " + path);
        }
        try {
            return this.createImage(in);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                    ;
                }
            }
        }
    }

    @Override
    public Object createImage(InputStream i) throws IOException {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        try {
            BitmapFactory.Options.class.getField("inPurgeable").set(opts, true);
        } catch (Exception e) {
            // inPurgeable not supported
            // http://www.droidnova.com/2d-sprite-animation-in-android-addendum,505.html
        }
        return BitmapFactory.decodeStream(i, null, opts);
    }

    @Override
    public void releaseImage(Object image) {
        Bitmap i = (Bitmap) image;
        i.recycle();
    }

    
    @Override
    public Object createImage(byte[] bytes, int offset, int len) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        try {
            BitmapFactory.Options.class.getField("inPurgeable").set(opts, true);
        } catch (Exception e) {
            // inPurgeable not supported
            // http://www.droidnova.com/2d-sprite-animation-in-android-addendum,505.html
        }
        return BitmapFactory.decodeByteArray(bytes, offset, len, opts);
    }

    @Override
    public Object createImage(int[] rgb, int width, int height) {
        return Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
    }

    @Override
    public boolean isAlphaMutableImageSupported() {
        return true;
    }

    @Override
    public Object scale(Object nativeImage, int width, int height) {
        return Bitmap.createScaledBitmap((Bitmap) nativeImage, width, height,
                false);
    }

//    @Override
//    public Object rotate(Object image, int degrees) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(degrees);
//        return Bitmap.createBitmap((Bitmap) image, 0, 0, ((Bitmap) image).getWidth(), ((Bitmap) image).getHeight(), matrix, true);
//    }
    @Override
    public boolean isRotationDrawingSupported() {
        return false;
    }

    @Override
    protected boolean cacheLinearGradients() {
        return false;
    }

    @Override
    public boolean isNativeInputSupported() {
        return true;
    }

    @Override
    public Object createMutableImage(int width, int height, int fillColor) {
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        AndroidGraphics graphics = (AndroidGraphics) this.getNativeGraphics(bitmap);
        graphics.getCanvas().drawColor(fillColor, Mode.SRC_OVER);
        return bitmap;
    }

    @Override
    public int getImageHeight(Object i) {
        return ((Bitmap) i).getHeight();
    }

    @Override
    public int getImageWidth(Object i) {
        return ((Bitmap) i).getWidth();
    }

    @Override
    public void drawImage(Object graphics, Object img, int x, int y) {
        ((AndroidGraphics) graphics).getCanvas().drawBitmap((Bitmap) img, x, y, ((AndroidGraphics) graphics).getPaint());
    }
    
    public boolean isScaledImageDrawingSupported() {
        return true;
    }

    public void drawImage(Object graphics, Object img, int x, int y, int w, int h) {
        Bitmap b = (Bitmap) img;
        Rect src = new Rect();
        src.top = 0;
        src.bottom = b.getHeight();
        src.left = 0;
        src.right = b.getWidth();
        Rect dest = new Rect();
        dest.top = y;
        dest.bottom = y + h;
        dest.left = x;
        dest.right = x + w;
        
        ((AndroidGraphics) graphics).getCanvas().drawBitmap(b, src, dest, ((AndroidGraphics) graphics).getPaint());
    }

    @Override
    public void drawLine(Object graphics, int x1, int y1, int x2, int y2) {

        ((AndroidGraphics) graphics).getPaint().setStyle(Style.FILL);
        ((AndroidGraphics) graphics).getCanvas().drawLine(x1, y1, x2, y2,
                ((AndroidGraphics) graphics).getPaint());

    }

    @Override
    public boolean isAntiAliasingSupported() {
        return true;
    }

    @Override
    public void setAntiAliased(Object graphics, boolean a) {
        ((AndroidGraphics) graphics).getPaint().setAntiAlias(a);
    }

    @Override
    public void drawPolygon(Object graphics, int[] xPoints, int[] yPoints, int nPoints) {
        if (nPoints <= 1) {
            return;
        }
        this.tmppath.rewind();
        this.tmppath.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < nPoints; i++) {
            this.tmppath.lineTo(xPoints[i], yPoints[i]);
        }
        ((AndroidGraphics) graphics).getPaint().setStyle(Style.STROKE);
        ((AndroidGraphics) graphics).getCanvas().drawPath(this.tmppath, ((AndroidGraphics) graphics).getPaint());
    }

    @Override
    public void fillPolygon(Object graphics, int[] xPoints, int[] yPoints, int nPoints) {
        if (nPoints <= 1) {
            return;
        }
        this.tmppath.rewind();
        this.tmppath.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < nPoints; i++) {
            this.tmppath.lineTo(xPoints[i], yPoints[i]);
        }
        ((AndroidGraphics) graphics).getPaint().setStyle(Style.FILL);
        ((AndroidGraphics) graphics).getCanvas().drawPath(this.tmppath, ((AndroidGraphics) graphics).getPaint());
    }

    @Override
    public void drawRGB(Object graphics, int[] rgbData, int offset, int x,
            int y, int w, int h, boolean processAlpha) {
        //Bitmap tmp = Bitmap.createBitmap(rgbData, w, h, Bitmap.Config.ARGB_8888);
        //((AndroidGraphics) graphics).drawBitmap(tmp, x, y, null);
        ((AndroidGraphics) graphics).getCanvas().drawBitmap(rgbData, offset, w, x, y, w, h,
                processAlpha, null);

    }

    @Override
    public void drawRect(Object graphics, int x, int y, int width, int height) {

        ((AndroidGraphics) graphics).getPaint().setStyle(Style.STROKE);
        ((AndroidGraphics) graphics).getCanvas().drawRect(x, y, x + width, y + height,
                ((AndroidGraphics) graphics).getPaint());

    }

    @Override
    public void drawRoundRect(Object graphics, int x, int y, int width,
            int height, int arcWidth, int arcHeight) {

        ((AndroidGraphics) graphics).getPaint().setStyle(Style.STROKE);
        this.tmprectF.set(x, y, x + width, y + height);
        ((AndroidGraphics) graphics).getCanvas().drawRoundRect(this.tmprectF, arcWidth,
                arcHeight, ((AndroidGraphics) graphics).getPaint());

    }

    @Override
    public void drawString(Object graphics, String str, int x, int y) {
        // Uncomment this if you need to run on a device that doesn't have proper bidi support
        // like some hacked 3rd party devices, unfortunately I can't find a way to detect
        // this situation on the fly
        //str = Display.getInstance().convertBidiLogicalToVisual(str);
        ((AndroidGraphics) graphics).getCanvas().drawText(str, x, y - ((AndroidGraphics) graphics).getFont().getFontMetricsInt().ascent,
                ((AndroidGraphics) graphics).getFont());
    }

    /**
     *  the next two methods are not used yet and are part of a potential performance enhancement.
     *  see https://lwuit.dev.java.net/issues/show_bug.cgi?id=218
     *  for details.
     */
    //@Override
    public void drawChar(Object graphics, char c, int x, int y) {
        tmpDrawChar[0] = c;
        ((AndroidGraphics) graphics).getCanvas().drawText(tmpDrawChar, 0, 1, x, y - ((AndroidGraphics) graphics).getFont().getFontMetricsInt().ascent,
                ((AndroidGraphics) graphics).getFont());
    }

    //@Override
    public void drawChars(Object graphics, char[] c, int offset, int length, int x, int y) {
        ((AndroidGraphics) graphics).getCanvas().drawText(c, offset, length, x, y - ((AndroidGraphics) graphics).getFont().getFontMetricsInt().ascent,
                ((AndroidGraphics) graphics).getFont());
    }

    @Override
    public void drawArc(Object graphics, int x, int y, int width, int height,
            int startAngle, int arcAngle) {

        ((AndroidGraphics) graphics).getPaint().setStyle(Style.STROKE);
        this.tmprectF.set(x, y, x + width, y + height);
        ((AndroidGraphics) graphics).getCanvas().drawArc(this.tmprectF, startAngle,
                arcAngle, false, ((AndroidGraphics) graphics).getPaint());

    }

    @Override
    public void fillArc(Object graphics, int x, int y, int width, int height,
            int startAngle, int arcAngle) {

        ((AndroidGraphics) graphics).getPaint().setStyle(Style.FILL);
        this.tmprectF.set(x, y, x + width, y + height);
        ((AndroidGraphics) graphics).getCanvas().drawArc(this.tmprectF, startAngle,
                arcAngle, false, ((AndroidGraphics) graphics).getPaint());

    }

    @Override
    public void fillRect(Object graphics, int x, int y, int width, int height) {

        ((AndroidGraphics) graphics).getPaint().setStyle(Style.FILL);
        ((AndroidGraphics) graphics).getCanvas().drawRect(x, y, x + width, y + height,
                ((AndroidGraphics) graphics).getPaint());

    }

    @Override
    public void fillRoundRect(Object graphics, int x, int y, int width,
            int height, int arcWidth, int arcHeight) {

        ((AndroidGraphics) graphics).getPaint().setStyle(Style.FILL);
        this.tmprectF.set(x, y, x + width, y + height);
        ((AndroidGraphics) graphics).getCanvas().drawRoundRect(this.tmprectF, arcWidth,
                arcHeight, ((AndroidGraphics) graphics).getPaint());

    }

    @Override
    public int getAlpha(Object graphics) {
        return ((AndroidGraphics) graphics).getPaint().getAlpha();
    }

    @Override
    public void setAlpha(Object graphics, int alpha) {
        ((AndroidGraphics) graphics).getPaint().setAlpha(alpha);
        ((AndroidGraphics) graphics).getPaint().setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
    }

    @Override
    public boolean isAlphaGlobal() {
        return true;
    }

    @Override
    public void setColor(Object graphics, int RGB) {
        ((AndroidGraphics) graphics).setColor((getColor(graphics) & 0xff000000) | RGB);
    }

    @Override
    public int getBackKeyCode() {
        return DROID_IMPL_KEY_BACK;
    }

    @Override
    public int getBackspaceKeyCode() {
        return DROID_IMPL_KEY_BACKSPACE;
    }

    @Override
    public int getClearKeyCode() {
        return DROID_IMPL_KEY_CLEAR;
    }

    @Override
    public int getClipHeight(Object graphics) {

        ((AndroidGraphics) graphics).getCanvas().getClipBounds(this.tmprect);
        return this.tmprect.height();

    }

    @Override
    public int getClipWidth(Object graphics) {

        ((AndroidGraphics) graphics).getCanvas().getClipBounds(this.tmprect);
        return this.tmprect.width();

    }

    @Override
    public int getClipX(Object graphics) {

        ((AndroidGraphics) graphics).getCanvas().getClipBounds(this.tmprect);
        return this.tmprect.left;

    }

    @Override
    public int getClipY(Object graphics) {

        ((AndroidGraphics) graphics).getCanvas().getClipBounds(this.tmprect);
        return this.tmprect.top;

    }

    @Override
    public void setClip(Object graphics, int x, int y, int width, int height) {
        ((AndroidGraphics) graphics).getCanvas().clipRect(x, y, x + width, y + height, Region.Op.REPLACE);

    }

    @Override
    public void clipRect(Object graphics, int x, int y, int width, int height) {
        ((AndroidGraphics) graphics).getCanvas().clipRect(x, y, x + width, y + height);
    }

    @Override
    public int getColor(Object graphics) {
        return ((AndroidGraphics) graphics).getPaint().getColor();
    }

    @Override
    public int getDisplayHeight() {
        if (this.myView != null) {
            int h = this.myView.getViewHeight();
            displayHeight = h;
            return h;
        }
        return displayHeight;
    }

    @Override
    public int getDisplayWidth() {
        if (this.myView != null) {
            int w = this.myView.getViewWidth();
            displayWidth = w;
            return w;
        }
        return displayWidth;
    }

    @Override
    public int getActualDisplayHeight() {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    @Override
    public int getGameAction(int keyCode) {
        switch (keyCode) {
            case DROID_IMPL_KEY_DOWN:
                return Display.GAME_DOWN;
            case DROID_IMPL_KEY_UP:
                return Display.GAME_UP;
            case DROID_IMPL_KEY_LEFT:
                return Display.GAME_LEFT;
            case DROID_IMPL_KEY_RIGHT:
                return Display.GAME_RIGHT;
            case DROID_IMPL_KEY_FIRE:
                return Display.GAME_FIRE;
            default:
                return 0;
        }
    }

    @Override
    public int getKeyCode(int gameAction) {
        switch (gameAction) {
            case Display.GAME_DOWN:
                return DROID_IMPL_KEY_DOWN;
            case Display.GAME_UP:
                return DROID_IMPL_KEY_UP;
            case Display.GAME_LEFT:
                return DROID_IMPL_KEY_LEFT;
            case Display.GAME_RIGHT:
                return DROID_IMPL_KEY_RIGHT;
            case Display.GAME_FIRE:
                return DROID_IMPL_KEY_FIRE;
            default:
                return 0;
        }
    }

    @Override
    public int[] getSoftkeyCode(int index) {
        if (index == 0) {
            return leftSK;
        }
        return null;
    }

    @Override
    public int getSoftkeyCount() {
        /**
         * one menu button only.  we may have to stuff some code here
         * as soon as there are devices that no longer have only a single
         * menu button.
         */
        return 1;
    }

    @Override
    public void vibrate(int duration) {
        if (!this.vibrateInitialized) {
            try {
                v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            } catch (Throwable e) {
                Log.e("Codename One", "problem with virbrator(0)", e);
            } finally {
                this.vibrateInitialized = true;
            }
        }
        if (v != null) {
            try {
                v.vibrate(duration);
            } catch (Throwable e) {
                Log.e("Codename One", "problem with virbrator(1)", e);
            }
        }
    }

    @Override
    public boolean isTouchDevice() {
        Configuration c = this.myView.getResources().getConfiguration();
        return c.touchscreen != Configuration.TOUCHSCREEN_NOTOUCH;
    }

    @Override
    public boolean hasPendingPaints() {
        //if the view is not visible make sure the edt won't wait.
        if (myView != null && myView.getVisibility() != View.VISIBLE) {
            return true;
        } else {
            return super.hasPendingPaints();
        }
    }

    public void revalidate() {
        if (myView != null) {
            myView.setVisibility(View.VISIBLE);
            getCurrentForm().revalidate();
            flushGraphics();
        }

    }

    @Override
    public int getKeyboardType() {
        if (Display.getInstance().getDefaultVirtualKeyboard().isVirtualKeyboardShowing()) {
            return Display.KEYBOARD_TYPE_VIRTUAL;
        }
        /**
         * can we detect this?  but even if we could i think
         * it is best to have this fixed to qwerty.  we pass unicode
         * values to Codename One in any case.  check AndroidView.onKeyUpDown()
         * method.  and read comment below.
         */
        return Display.KEYBOARD_TYPE_QWERTY;
        /**
         * some info from the MIDP docs about keycodes:
         *
         *  "Applications receive keystroke events in which the individual keys are named within a space of key codes.
         * Every key for which events are reported to MIDP applications is assigned a key code. The key code values are
         * unique for each hardware key unless two keys are obvious synonyms for each other. MIDP defines the following
         * key codes: KEY_NUM0, KEY_NUM1, KEY_NUM2, KEY_NUM3, KEY_NUM4, KEY_NUM5, KEY_NUM6, KEY_NUM7, KEY_NUM8, KEY_NUM9,
         * KEY_STAR, and KEY_POUND. (These key codes correspond to keys on a ITU-T standard telephone keypad.) Other
         * keys may be present on the keyboard, and they will generally have key codes distinct from those list above.
         * In order to guarantee portability, applications should use only the standard key codes.
         *
         * The standard key codes values are equal to the Unicode encoding for the character that represents the key.
         * If the device includes any other keys that have an obvious correspondence to a Unicode character, their key
         * code values should equal the Unicode encoding for that character. For keys that have no corresponding Unicode
         * character, the implementation must use negative values. Zero is defined to be an invalid key code."
         *
         * Because the MIDP implementation is our reference and that implementation does not interpret the given keycodes
         * we behave alike and pass on the unicode values.
         */
    }

    /**
     * Exits the application...
     */
    public void exitApplication() {
        System.exit(0);
    }

    @Override
    public void notifyCommandBehavior(int commandBehavior) {
        if (commandBehavior == Display.COMMAND_BEHAVIOR_NATIVE) {
            if (activity instanceof CodenameOneActivity) {
                ((CodenameOneActivity) activity).enableNativeMenu(true);
            } else {
                System.err.println("activity must extend CodenameOneActivity to use "
                        + "the native menu feature");
            }
        }
    }

    /**
     * @inheritDoc
     */
    public String getProperty(String key, String defaultValue) {
        if ("OS".equals(key)) {
            return "Android";
        }
        if ("AppName".equals(key)) {
            return activity.getApplicationInfo().name;
        }
        if ("AppVersion".equals(key)) {
            try {
                PackageInfo i = activity.getPackageManager().getPackageInfo(activity.getApplicationInfo().packageName, 0);
                return i.versionName;
            } catch (NameNotFoundException ex) {
                ex.printStackTrace();
            }
            return null;
        }
        if ("Platform".equals(key)) {
            return System.getProperty("platform");
        }
        if ("User-Agent".equals(key)) {
            String ua = getUserAgent();
            return getUserAgent();
        }
        if ("IMEI".equals(key)) {
            TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        }
        if ("MSISDN".equals(key)) {
            TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getLine1Number();
        }

        //these keys/values are from the Application Resources (strings values)
        try {
            int id = activity.getResources().getIdentifier(key, "string", activity.getApplicationInfo().packageName);
            String val = activity.getResources().getString(id);
            return val;

        } catch (Exception e) {
        }
        return System.getProperty(key, defaultValue);
    }

    private String getUserAgent() {
    try {
        Constructor<WebSettings> constructor = WebSettings.class.getDeclaredConstructor(Context.class, WebView.class);
        constructor.setAccessible(true);
        try {
            WebSettings settings = constructor.newInstance(activity, null);
            return settings.getUserAgentString();
        } finally {
            constructor.setAccessible(false);
        }
    } catch (Exception e) {
        final StringBuffer ua = new StringBuffer();
        if(Thread.currentThread().getName().equalsIgnoreCase("main")){
            WebView m_webview = new WebView(activity);
            ua.append(m_webview.getSettings().getUserAgentString());
        }else{
            Thread thread = new Thread(){
                public void run(){
                    Looper.prepare();
                    WebView m_webview = new WebView(activity);
                    ua.append(m_webview.getSettings().getUserAgentString());
                    Looper.loop();
                }
            };
            thread.start();
        }
        return ua.toString();
    }
}
    /**
     * @inheritDoc
     */
    public void execute(String url) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); 
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @inheritDoc
     */
    public void playBuiltinSound(String soundIdentifier) {
        if (Display.SOUND_TYPE_BUTTON_PRESS == soundIdentifier) {
            activity.runOnUiThread(new Runnable() {

                public void run() {
                    if (myView != null) {
                        myView.playSoundEffect(AudioManager.FX_KEY_CLICK);
                    }
                }
            });
        }
    }

    /**
     * @inheritDoc
     */
    protected void playNativeBuiltinSound(Object data) {
    }

    /**
     * @inheritDoc
     */
    public boolean isBuiltinSoundAvailable(String soundIdentifier) {
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Media createMedia(String uri, boolean isVideo, Runnable onCompletion) throws IOException {

        if (uri.startsWith("file://")) {
            return createMedia(uri.substring(7), isVideo, onCompletion);
        }
        File file = null;
        if (uri.indexOf(':') < 0) {
            // use a file object to play to try and workaround this issue:
            // http://code.google.com/p/android/issues/detail?id=4124
            file = new File(uri);
        }

        Media retVal;

        if (isVideo) {
            VideoView video = new VideoView(activity);
            video.setZOrderMediaOverlay(true);
            if (file != null) {
                video.setVideoURI(Uri.fromFile(file));
            } else {
                video.setVideoURI(Uri.parse(uri));
            }
            retVal = new Video(video, activity, onCompletion);
        } else {
            MediaPlayer player;
            if (file != null) {
                FileInputStream is = new FileInputStream(file);
                player = new MediaPlayer();
                player.setDataSource(is.getFD());
                player.prepare();
            } else {
                player = MediaPlayer.create(activity, Uri.parse(uri));
            }
            retVal = new Audio(activity, player, null, onCompletion);
        }
        return retVal;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Media createMedia(InputStream stream, String mimeType, Runnable onCompletion) throws IOException {

        boolean isVideo = mimeType.contains("video");

        if (!isVideo && stream instanceof FileInputStream) {
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(((FileInputStream) stream).getFD());
            player.prepare();
            return new Audio(activity, player, stream, onCompletion);
        }

        File temp = File.createTempFile("mtmp", "dat");
        temp.deleteOnExit();
        FileOutputStream out = new FileOutputStream(temp);
        byte buf[] = new byte[256];
        int len = 0;
        while ((len = stream.read(buf, 0, buf.length)) > -1) {
            out.write(buf, 0, len);
        }
        stream.close();


        if (isVideo) {
            VideoView video = new VideoView(activity);
            video.setZOrderMediaOverlay(true);
            video.setVideoURI(Uri.fromFile(temp));
            return new Video(video, activity, onCompletion);
        } else {
            return createMedia(new FileInputStream(temp), mimeType, onCompletion);
        }

    }

    /**
     * @inheritDoc
     */
    public Object createSoftWeakRef(Object o) {
        return new SoftReference(o);
    }

    /**
     * @inheritDoc
     */
    public Object extractHardRef(Object o) {
        SoftReference w = (SoftReference) o;
        if (w != null) {
            return w.get();
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    public PeerComponent createNativePeer(Object nativeComponent) {
        if (!(nativeComponent instanceof View)) {
            throw new IllegalArgumentException(nativeComponent.getClass().getName());
        }
        return new AndroidPeer((View) nativeComponent);
    }

    private void blockNativeFocusAll(boolean block) {
        synchronized (this.nativePeers) {
            final int size = this.nativePeers.size();
            for (int i = 0; i < size; i++) {
                AndroidPeer next = (AndroidPeer) this.nativePeers.get(i);
                next.blockNativeFocus(block);
            }
        }
    }

    public void onFocusChange(View view, boolean bln) {

        if (bln) {
            /**
             * whenever the base view receives focus we automatically block possible
             * native subviews from gaining focus.
             */
            blockNativeFocusAll(true);
            if (this.lastDirectionalKeyEventReceivedByWrapper != 0) {
                /**
                 * because we also consume any key event in the OnKeyListener of the native wrappers,
                 * we have to simulate key events to make Codename One move the focus to the next component.
                 */
                if (!myView.isInTouchMode()) {
                    switch (lastDirectionalKeyEventReceivedByWrapper) {
                        case AndroidImplementation.DROID_IMPL_KEY_LEFT:
                        case AndroidImplementation.DROID_IMPL_KEY_RIGHT:
                        case AndroidImplementation.DROID_IMPL_KEY_UP:
                        case AndroidImplementation.DROID_IMPL_KEY_DOWN:
                            Display.getInstance().keyPressed(lastDirectionalKeyEventReceivedByWrapper);
                            Display.getInstance().keyReleased(lastDirectionalKeyEventReceivedByWrapper);
                            break;
                        default:
                            Log.d("Codename One", "unexpected keycode: " + lastDirectionalKeyEventReceivedByWrapper);
                            break;
                    }
                } else {
                    Log.d("Codename One", "base view gained focus but no key event to process.");
                }
                lastDirectionalKeyEventReceivedByWrapper = 0;
            }
        }

    }

    /**
     * wrapper component that capsules a native view object in a Codename One component. this
     * involves A LOT of back and forth between the Codename One EDT and the Android UI thread.
     *
     *
     * To use it you would:
     *
     * 1) create your native Android view(s). Make sure to work on the Android UI thread when constructing
     *    and modifying them.
     * 2) create a Codename One peer component by calling:
     *
     *         com.codename1.ui.PeerComponent.create(myAndroidView);
     *
     * 3) currently the view's size is not automatically calculated from the native view. so you should
     *    set the preferred size of the Codename One component manually.
     *
     *
     */
    class AndroidPeer extends PeerComponent {

        private View v;
        private AndroidRelativeLayout layoutWrapper = null;
        private Bitmap nativeBuffer;
        private Image image;
        private Rect bounds;
        private Canvas canvas;
        private Paint clear = new Paint();

        public void clear() {
            //clear the canvas
            canvas.drawRect(bounds, clear);
        }

        public AndroidPeer(View vv) {
            super(vv);
            this.v = vv;
            clear.setColor(0xAA000000);
            clear.setStyle(Style.FILL);
            v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            this.doSetVisibility(visible);
        }

        void doSetVisibility(final boolean visible) {
            activity.runOnUiThread(new Runnable() {

                public void run() {
                    v.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
                    if (visible) {
                        v.bringToFront();
                    }
                }
            });
        }

        @Override
        protected void deinitialize() {
            super.deinitialize();
            synchronized (nativePeers) {
                nativePeers.remove(this);
            }
            activity.runOnUiThread(new Runnable() {

                public void run() {
                    if (layoutWrapper != null) {
                        AndroidImplementation.this.relativeLayout.removeView(layoutWrapper);
                        AndroidImplementation.this.relativeLayout.requestLayout();
                    }
                }
            });
            Display.getInstance().invokeAndBlock(new Runnable() {

                public void run() {
                    if (layoutWrapper != null) {
                        while (layoutWrapper.getParent() != null);
                    }
                }
            });
        }

        @Override
        protected void initComponent() {
            super.initComponent();
            synchronized (nativePeers) {
                nativePeers.add(this);
            }
            activity.runOnUiThread(new Runnable() {

                public void run() {
                    if (layoutWrapper == null) {
                        /**
                         * wrap the native item in a layout that we can move around on the surface view
                         * as we like.
                         */
                        layoutWrapper = new AndroidRelativeLayout(activity, AndroidPeer.this, v);
                        v.setFocusable(AndroidPeer.this.isFocusable());
                        v.setFocusableInTouchMode(false);
                        ArrayList<View> viewList = new ArrayList<View>();
                        viewList.add(layoutWrapper);
                        v.addFocusables(viewList, View.FOCUS_DOWN);
                        v.addFocusables(viewList, View.FOCUS_UP);
                        v.addFocusables(viewList, View.FOCUS_LEFT);
                        v.addFocusables(viewList, View.FOCUS_RIGHT);
                        if (v.isFocusable() || v.isFocusableInTouchMode()) {
                            if (AndroidPeer.super.hasFocus()) {
                                AndroidImplementation.this.blockNativeFocusAll(true);
                                blockNativeFocus(false);
                                v.requestFocus();
                            } else {
                                blockNativeFocus(true);
                            }
                        } else {
                            blockNativeFocus(true);
                        }
                        layoutWrapper.setOnKeyListener(new View.OnKeyListener() {

                            public boolean onKey(View view, int i, KeyEvent ke) {
                                lastDirectionalKeyEventReceivedByWrapper = AndroidView.internalKeyCodeTranslate(ke.getKeyCode());

                                // move focus back to base view.
                                AndroidImplementation.this.myView.requestFocus();

                                /**
                                 * if the wrapper has focus, then only because the wrapped
                                 * native component just lost focus. we consume whatever key
                                 * events we receive, just to make sure no half press/release
                                 * sequence reaches the base view (and therefore Codename One).
                                 */
                                return true;
                            }
                        });
                        layoutWrapper.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                            public void onFocusChange(View view, boolean bln) {
                                Log.d("Codename One", "on focus change. " + view.toString() + " focus:" + bln + " touchmode: "
                                        + v.isInTouchMode());
                            }
                        });
                        layoutWrapper.setOnTouchListener(new View.OnTouchListener() {

                            public boolean onTouch(View v, MotionEvent me) {
                               return false;//myView.onTouchEvent(me);
                            }
                        });
                    }
                    AndroidImplementation.this.relativeLayout.addView(layoutWrapper);

                }
            });

            Display.getInstance().invokeAndBlock(new Runnable() {

                public void run() {
                    if (layoutWrapper != null) {
                        while (layoutWrapper.getParent() == null);
                    }
                }
            });
        }

        @Override
        protected void onPositionSizeChange() {

            // called by Codename One EDT to position the native component.

            activity.runOnUiThread(new Runnable() {

                public void run() {
                    if (layoutWrapper != null) {
                        if (v.getVisibility() == View.VISIBLE) {

                            RelativeLayout.LayoutParams layoutParams = layoutWrapper.createMyLayoutParams(
                                    AndroidPeer.this.getAbsoluteX(),
                                    AndroidPeer.this.getAbsoluteY(),
                                    AndroidPeer.this.getWidth(),
                                    AndroidPeer.this.getHeight());
                            layoutWrapper.setLayoutParams(layoutParams);

                            AndroidImplementation.this.relativeLayout.requestLayout();
                        }
                    }
                }
            });
        }

        void blockNativeFocus(boolean block) {
            if (layoutWrapper != null) {
                layoutWrapper.setDescendantFocusability(block
                        ? ViewGroup.FOCUS_BLOCK_DESCENDANTS : ViewGroup.FOCUS_AFTER_DESCENDANTS);
            }
        }

        @Override
        public boolean isFocusable() {
            // EDT
            if (v != null) {
                return v.isFocusableInTouchMode() || v.isFocusable();
            } else {
                return super.isFocusable();
            }
        }

        @Override
        public void setFocusable(final boolean focusable) {
            // EDT
            super.setFocusable(focusable);
            activity.runOnUiThread(new Runnable() {

                public void run() {
                    v.setFocusable(focusable);
                }
            });
        }

        @Override
        protected void focusGained() {
            Log.d("Codename One", "native focus gain");
            // EDT
            super.focusGained();
            activity.runOnUiThread(new Runnable() {

                public void run() {
                    // allow this one to gain focus
                    blockNativeFocus(false);
                    if (v.isInTouchMode()) {
                        v.requestFocusFromTouch();
                    } else {
                        v.requestFocus();
                    }
                }
            });
        }

        @Override
        protected void focusLost() {
            Log.d("Codename One", "native focus loss");
            // EDT
            super.focusLost();
            if (layoutWrapper != null) {
                activity.runOnUiThread(new Runnable() {

                    public void run() {
                        // request focus of the wrapper. that will trigger the
                        // android focus listener and move focus back to the
                        // base view.
                        layoutWrapper.requestFocus();
                    }
                });
            }
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            if (canvas != null) {
                //copy the native drawing to a different image
                synchronized (canvas) {
                    g.drawImage(image, getX(), getY());
                }
            }
        }

        public Canvas getBuffer() {

            if (nativeBuffer == null || getWidth() != nativeBuffer.getWidth()
                    || getHeight() != nativeBuffer.getHeight()) {
                this.nativeBuffer = Bitmap.createBitmap(
                        getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                image = new NativeImage(nativeBuffer);
                bounds = new Rect(0, 0, getWidth(), getHeight());
                canvas = new Canvas(nativeBuffer);
            }
            return canvas;
        }

        public void release() {
            deinitialize();
        }

        @Override
        protected Dimension calcPreferredSize() {
            int w = 1;
            int h = 1;
            Drawable d = v.getBackground();
            if (d != null) {
                w = d.getMinimumWidth();
                h = d.getMinimumHeight();
            }

            if (v instanceof TextView) {
                w = (int) android.text.Layout.getDesiredWidth(((TextView) v).getText(), ((TextView) v).getPaint());
            }
            return new Dimension(w, h);
        }
    }

    /**
     * inner class that wraps the native components.
     * this is a useful thingy to handle focus stuff and
     * buffering.
     */
    class AndroidRelativeLayout extends RelativeLayout {

        private AndroidPeer peer;

        public AndroidRelativeLayout(Context activity, AndroidPeer peer, View v) {
            super(activity);

            this.peer = peer;
            this.setLayoutParams(createMyLayoutParams(peer.getAbsoluteX(), peer.getAbsoluteY(),
                    peer.getWidth(), peer.getHeight()));
            this.addView(v, new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.FILL_PARENT));
            this.setDrawingCacheEnabled(false);
            this.setAlwaysDrawnWithCacheEnabled(false);
            this.setFocusable(true);
            this.setFocusableInTouchMode(false);
            this.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        }

        /**
         * create a layout parameter object that holds the native component's position.
         * @return
         */
        private RelativeLayout.LayoutParams createMyLayoutParams(int x, int y, int width, int height) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.width = width;
            layoutParams.height = height;
            layoutParams.leftMargin = x;
            layoutParams.topMargin = y;
            return layoutParams;
        }

        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {

            if (child instanceof SurfaceView) {
                return super.drawChild(canvas, child, drawingTime);
            }

            Canvas c = peer.getBuffer();

            boolean result;
            synchronized (c) {
                peer.clear();
                /**
                 * the EDT might draw the cache bitmap from within the flushGraphics()
                 * methods. synchronizing here to avoid half painted bitmaps or whatever
                 * might happen in the background if the EDT is reading and we are drawing.
                 */
                result = super.drawChild(c, child, drawingTime);
            }

            /**
             * now that this native component has been painted we certainly need a repaint.
             * notify the EDT.
             */
            peer.repaint();

            return result;
        }
    }
    private boolean testedNativeTheme;
    private boolean nativeThemeAvailable;

    public boolean hasNativeTheme() {
        if (!testedNativeTheme) {
            testedNativeTheme = true;
            try {
                InputStream is;
                if(android.os.Build.VERSION.SDK_INT < 14 && !isTablet()){
                    is = getResourceAsStream(getClass(), "/androidTheme.res");
                }else{
                    is = getResourceAsStream(getClass(), "/android_holo_light.res");                
                }
                nativeThemeAvailable = is != null;
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return nativeThemeAvailable;
    }

    /**
     * Installs the native theme, this is only applicable if hasNativeTheme() returned true. Notice that this method
     * might replace the DefaultLookAndFeel instance and the default transitions.
     */
    public void installNativeTheme() {
        hasNativeTheme();
        if (nativeThemeAvailable) {
            try {
                InputStream is = getResourceAsStream(getClass(), "/androidTheme.res");
                Resources r = Resources.open(is);
                UIManager.getInstance().setThemeProps(r.getTheme(r.getThemeResourceNames()[0]));
                is.close();
                Display.getInstance().setCommandBehavior(Display.COMMAND_BEHAVIOR_NATIVE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isNativeBrowserComponentSupported() {
        return true;
    }

    public PeerComponent createBrowserComponent(Object parent) {
        final WebView[] r = new WebView[1];
        final Object lock = new Object();
        synchronized (lock) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    synchronized (lock) {
                        r[0] = new WebView(activity) {

                            public boolean onKeyDown(int keyCode, KeyEvent event) {
                                switch (keyCode) {
                                    case KeyEvent.KEYCODE_BACK:
                                        Display.getInstance().keyPressed(AndroidImplementation.DROID_IMPL_KEY_BACK);
                                        return true;
                                    case KeyEvent.KEYCODE_MENU:
                                        Display.getInstance().keyPressed(AndroidImplementation.DROID_IMPL_KEY_MENU);
                                        return true;
                                }
                                return super.onKeyDown(keyCode, event);
                            }

                            public boolean onKeyUp(int keyCode, KeyEvent event) {
                                switch (keyCode) {
                                    case KeyEvent.KEYCODE_BACK:
                                        Display.getInstance().keyReleased(AndroidImplementation.DROID_IMPL_KEY_BACK);
                                        return true;
                                    case KeyEvent.KEYCODE_MENU:
                                        Display.getInstance().keyReleased(AndroidImplementation.DROID_IMPL_KEY_MENU);
                                        return true;
                                }
                                return super.onKeyUp(keyCode, event);
                            }
                        };
                        lock.notify();
                    }
                }
            });
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return new AndroidBrowserComponent(r[0], activity, parent);
    }

    public void setBrowserProperty(PeerComponent browserPeer, String key, Object value) {
        ((AndroidBrowserComponent) browserPeer).setProperty(key, value);
    }

    public String getBrowserTitle(PeerComponent browserPeer) {
        return ((AndroidBrowserComponent) browserPeer).getTitle();
    }

    public String getBrowserURL(PeerComponent browserPeer) {
        return ((AndroidBrowserComponent) browserPeer).getURL();
    }

    public void setBrowserURL(PeerComponent browserPeer, String url) {
        if (url.startsWith("jar:")) {
            super.setBrowserURL(browserPeer, url);
            return;
        }
        ((AndroidBrowserComponent) browserPeer).setURL(url);
    }

    public void browserStop(PeerComponent browserPeer) {
        ((AndroidBrowserComponent) browserPeer).stop();
    }

    
    /**
     * Reload the current page
     * @param browserPeer browser instance
     */
    public void browserReload(PeerComponent browserPeer) {
        ((AndroidBrowserComponent) browserPeer).reload();
    }

    /**
     * Indicates whether back is currently available
     * @param browserPeer browser instance
     * @return true if back should work
     */
    public boolean browserHasBack(PeerComponent browserPeer) {
        return ((AndroidBrowserComponent) browserPeer).hasBack();
    }

    public boolean browserHasForward(PeerComponent browserPeer) {
        return ((AndroidBrowserComponent) browserPeer).hasForward();
    }

    public void browserBack(PeerComponent browserPeer) {
        ((AndroidBrowserComponent) browserPeer).back();
    }

    public void browserForward(PeerComponent browserPeer) {
        ((AndroidBrowserComponent) browserPeer).forward();
    }

    public void browserClearHistory(PeerComponent browserPeer) {
        ((AndroidBrowserComponent) browserPeer).clearHistory();
    }

    public void setBrowserPage(PeerComponent browserPeer, String html, String baseUrl) {
        ((AndroidBrowserComponent) browserPeer).setPage(html, baseUrl);
    }

    public void browserExposeInJavaScript(PeerComponent browserPeer, Object o, String name) {
        ((AndroidBrowserComponent) browserPeer).exposeInJavaScript(o, name);
    }

    public boolean isAffineSupported() {
        return true;
    }

    public void resetAffine(Object nativeGraphics) {
        ((AndroidGraphics) nativeGraphics).getCanvas().restore();
        ((AndroidGraphics) nativeGraphics).getCanvas().save();
    }

    public void scale(Object nativeGraphics, float x, float y) {
        ((AndroidGraphics) nativeGraphics).getCanvas().scale(x, y);
    }

    public void rotate(Object nativeGraphics, float angle) {
        ((AndroidGraphics) nativeGraphics).getCanvas().rotate(angle);
    }

    public void rotate(Object nativeGraphics, float angle, int x, int y) {
        ((AndroidGraphics) nativeGraphics).getCanvas().rotate(angle, x, y);
    }

    public void shear(Object nativeGraphics, float x, float y) {
    }

    public boolean isTablet() {
        try {
            // Compute screen size
            DisplayMetrics dm = activity.getResources().getDisplayMetrics();
            float screenWidth = dm.widthPixels / dm.xdpi;
            float screenHeight = dm.heightPixels / dm.ydpi;
            double size = Math.sqrt(Math.pow(screenWidth, 2)
                    + Math.pow(screenHeight, 2));
            // Tablet devices should have a screen size greater than 6 inches
            return size >= 6;
        } catch (Throwable t) {
            return false;
        }
    }

    public int convertToPixels(int dipCount, boolean horizontal) {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        if (horizontal) {
            return (int) (((float) dipCount) / 25.4f * dm.xdpi);
        }
        return (int) (((float) dipCount) / 25.4f * dm.ydpi);
    }

    public boolean isPortrait() {
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_UNDEFINED
                || orientation == Configuration.ORIENTATION_SQUARE) {
            return super.isPortrait();
        }
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    class AndroidBrowserComponent extends AndroidPeer {

        private Activity act;
        private WebView web;
        private BrowserComponent parent;

        public AndroidBrowserComponent(WebView web, Activity act, Object p) {
            super(web);
            parent = (BrowserComponent) p;
            this.web = web;
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setSupportZoom(true);
            this.act = act;
            web.setWebViewClient(new WebViewClient() {

                public void onLoadResource(WebView view, String url) {
                    parent.fireWebEvent("onLoadResource", new ActionEvent(url));
                    super.onLoadResource(view, url);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    parent.fireWebEvent("onStart", new ActionEvent(url));
                    super.onPageStarted(view, url, favicon);
                }
                
                
                public void onPageFinished(WebView view, String url) {
                    parent.fireWebEvent("onLoad", new ActionEvent(url));
                    super.onPageFinished(view, url);
                }

                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    parent.fireWebEvent("onError", new ActionEvent(description, errorCode));
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    super.shouldOverrideKeyEvent(view, null);
                }

                public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                    int keyCode = event.getKeyCode();
                    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
                        return true;
                    }

                    return super.shouldOverrideKeyEvent(view, event);
                }

                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("jar:")) {
                        setURL(url);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                }
            });
        }

        public void setProperty(String key, Object value) {
            WebSettings s = web.getSettings();
            String methodName = "set" + key;
            for (Method m : s.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase(methodName) && m.getParameterTypes().length == 0) {
                    try {
                        m.invoke(s, value);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return;
                }
            }
        }

        public String getTitle() {
            return web.getTitle();
        }

        public String getURL() {
            return web.getUrl();
        }

        public void setURL(final String url) {
            act.runOnUiThread(new Runnable() {

                public void run() {
                    web.loadUrl(url);
                }
            });
        }

        public void reload() {
            web.reload();
        }

        public boolean hasBack() {
            return web.canGoBack();
        }

        public boolean hasForward() {
            return web.canGoForward();
        }

        public void back() {
            web.goBack();
        }

        public void forward() {
            web.goForward();
        }

        public void clearHistory() {
            web.clearHistory();
        }

        public void stop() {
            web.stopLoading();
        }
        
        public void setPage(String html, String baseUrl) {
            web.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null);
        }

        public void exposeInJavaScript(Object o, String name) {
            web.addJavascriptInterface(o, name);
        }
    }

    private Context getContext() {
        return activity;
    }

    /**
     * @inheritDoc
     */
    public Object connect(String url, boolean read, boolean write) throws IOException {
        URL u = new URL(url);
        URLConnection con = u.openConnection();
        if(con instanceof HttpURLConnection){
            HttpURLConnection c = (HttpURLConnection) con;
            c.setUseCaches(false);
            c.setDefaultUseCaches(false);
        }
        con.setDoInput(read);
        con.setDoOutput(write);
        return con;
    }

    /**
     * @inheritDoc
     */
    public void setHeader(Object connection, String key, String val) {
        ((URLConnection) connection).setRequestProperty(key, val);
    }

    /**
     * @inheritDoc
     */
    public OutputStream openOutputStream(Object connection) throws IOException {
        if (connection instanceof String) {
            FileOutputStream fc = new FileOutputStream((String) connection);
            BufferedOutputStream o = new BufferedOutputStream(fc, (String) connection);
            return o;
        }
        return new BufferedOutputStream(((URLConnection) connection).getOutputStream(), connection.toString());
    }

    /**
     * @inheritDoc
     */
    public OutputStream openOutputStream(Object connection, int offset) throws IOException {
        RandomAccessFile rf = new RandomAccessFile((String) connection, "rw");
        rf.seek(offset);
        FileOutputStream fc = new FileOutputStream(rf.getFD());
        BufferedOutputStream o = new BufferedOutputStream(fc, (String) connection);
        o.setConnection(rf);
        return o;
    }

    /**
     * @inheritDoc
     */
    public void cleanup(Object o) {
        try {
            super.cleanup(o);
            if (o != null) {
                if (o instanceof RandomAccessFile) {
                    ((RandomAccessFile) o).close();
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @inheritDoc
     */
    public InputStream openInputStream(Object connection) throws IOException {
        if (connection instanceof String) {
            FileInputStream fc = new FileInputStream((String) connection);
            BufferedInputStream o = new BufferedInputStream(fc, (String) connection);
            return o;
        }
        return new BufferedInputStream(((URLConnection) connection).getInputStream(), connection.toString());
    }

    /**
     * @inheritDoc
     */
    public void setPostRequest(Object connection, boolean p) {
        try {
            if (p) {
                ((HttpURLConnection) connection).setRequestMethod("POST");
            } else {
                ((HttpURLConnection) connection).setRequestMethod("GET");
            }
        } catch (IOException err) {
            // an exception here doesn't make sense
            err.printStackTrace();
        }
    }

    /**
     * @inheritDoc
     */
    public int getResponseCode(Object connection) throws IOException {
        return ((HttpURLConnection) connection).getResponseCode();
    }

    /**
     * @inheritDoc
     */
    public String getResponseMessage(Object connection) throws IOException {
        return ((HttpURLConnection) connection).getResponseMessage();
    }

    /**
     * @inheritDoc
     */
    public int getContentLength(Object connection) {
        return ((HttpURLConnection) connection).getContentLength();
    }

    /**
     * @inheritDoc
     */
    public String getHeaderField(String name, Object connection) throws IOException {
        return ((HttpURLConnection) connection).getHeaderField(name);
    }

    /**
     * @inheritDoc
     */
    public String[] getHeaderFields(String name, Object connection) throws IOException {
        HttpURLConnection c = (HttpURLConnection) connection;
        List r = new ArrayList();
        String ck = c.getHeaderFieldKey(0);
        for (int iter = 0; ck != null; iter++) {
            if (ck.equalsIgnoreCase(name)) {
                r.add(c.getHeaderField(iter));
            }
            ck = c.getHeaderFieldKey(iter);
        }

        if (r.size() == 0) {
            return null;
        }
        String[] response = new String[r.size()];
        for (int iter = 0; iter < response.length; iter++) {
            response[iter] = (String) r.get(iter);
        }
        return response;
    }

    /**
     * @inheritDoc
     */
    public void deleteStorageFile(String name) {
        getContext().deleteFile(name);
    }

    /**
     * @inheritDoc
     */
    public OutputStream createStorageOutputStream(String name) throws IOException {
        return getContext().openFileOutput(name, 0);
    }

    /**
     * @inheritDoc
     */
    public InputStream createStorageInputStream(String name) throws IOException {
        return getContext().openFileInput(name);
    }

    /**
     * @inheritDoc
     */
    public boolean storageFileExists(String name) {
        String[] fileList = getContext().fileList();
        for (int iter = 0; iter < fileList.length; iter++) {
            if (fileList[iter].equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     */
    public String[] listStorageEntries() {
        return getContext().fileList();
    }

    /**
     * @inheritDoc
     */
    public String[] listFilesystemRoots() {
        File f = Environment.getExternalStorageDirectory();
        if (f != null && f.exists()) {
            return new String[]{Environment.getRootDirectory().getAbsolutePath(), f.getAbsolutePath()};
        }
        return new String[]{Environment.getRootDirectory().getAbsolutePath()};
    }

    /**
     * @inheritDoc
     */
    public String[] listFiles(String directory) throws IOException {
        return new File(directory).list();
    }

    /**
     * @inheritDoc
     */
    public long getRootSizeBytes(String root) {
        return -1;
    }

    /**
     * @inheritDoc
     */
    public long getRootAvailableSpace(String root) {
        return -1;
    }

    /**
     * @inheritDoc
     */
    public void mkdir(String directory) {
        new File(directory).mkdirs();
    }

    /**
     * @inheritDoc
     */
    public void deleteFile(String file) {
        new File(file).delete();
    }

    /**
     * @inheritDoc
     */
    public boolean isHidden(String file) {
        return new File(file).isHidden();
    }

    /**
     * @inheritDoc
     */
    public void setHidden(String file, boolean h) {
    }

    /**
     * @inheritDoc
     */
    public long getFileLength(String file) {
        return new File(file).length();
    }

    /**
     * @inheritDoc
     */
    public boolean isDirectory(String file) {
        return new File(file).isDirectory();
    }

    /**
     * @inheritDoc
     */
    public char getFileSystemSeparator() {
        return File.separatorChar;
    }

    /**
     * @inheritDoc
     */
    public OutputStream openFileOutputStream(String file) throws IOException {
        return new FileOutputStream(file);
    }

    /**
     * @inheritDoc
     */
    public InputStream openFileInputStream(String file) throws IOException {
        return new FileInputStream(file);
    }

    /**
     * @inheritDoc
     */
    public boolean exists(String file) {
        return new File(file).exists();
    }

    /**
     * @inheritDoc
     */
    public void rename(String file, String newName) {
        new File(file).renameTo(new File(new File(file).getParentFile(), newName));
    }

    /**
     * @inheritDoc
     */
    public boolean shouldWriteUTFAsGetBytes() {
        return true;
    }

    /**
     * @inheritDoc
     */
    public void startThread(String name, Runnable r) {
        new Thread(Thread.currentThread().getThreadGroup(), r, name, 64 * 1024).start();
    }

    /**
     * @inheritDoc
     */
    public void closingOutput(OutputStream s) {
        // For some reasons the Android guys chose not doing this by default:
        // http://android-developers.blogspot.com/2010/12/saving-data-safely.html
        // this seems to be a mistake of sacrificing stability for minor performance
        // gains which will only be noticeable on a server.
        if (s != null) {
            if (s instanceof FileOutputStream) {
                try {
                    FileDescriptor fd = ((FileOutputStream) s).getFD();
                    if (fd != null) {
                        fd.sync();
                    }
                } catch (IOException ex) {
                    // this exception doesn't help us
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void printStackTraceToStream(Throwable t, Writer o) {
        PrintWriter p = new PrintWriter(o);
        t.printStackTrace(p);
    }

    /**
     * This method returns the platform Location Control
     * @return LocationControl Object
     */
    public LocationManager getLocationManager() {
        return new AndroidLocationManager(activity);
    }

    /**
     * @inheritDoc
     */
    public void sendMessage(String[] recipients, String subject, Message msg) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg.getContent());
        emailIntent.setType(msg.getMimeType());
        if(msg.getAttachment() != null && msg.getAttachment().length() > 0){
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ msg.getAttachment()));        
        }
        activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
 
    /**
     * @inheritDoc
     */
    public void dial(String phoneNumber) {        
        Intent dialer = new Intent(android.content.Intent.ACTION_DIAL, Uri.parse("tel:"+ phoneNumber));
        activity.startActivity(dialer);
    }
    
    
    /**
     * @inheritDoc
     */
    public void sendSMS(final String phoneNumber, final String message) throws IOException{
        PendingIntent deliveredPI = PendingIntent.getBroadcast(activity, 0,
        new Intent("SMS_DELIVERED"), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, deliveredPI, null);     
    }    

    /**
     * @inheritDoc
     */
    public String getPlatformName() {
        return "and";
    }

    /**
     * @inheritDoc
     */
    public String[] getPlatformOverrides() {
        if(isTablet()) {
            return new String[] {"tablet", "android", "android-tab"};
        } else {
            return new String[] {"phone", "android", "android-phone"};
        }
    }
        
    public class Video extends AndroidImplementation.AndroidPeer implements Media {

        private VideoView nativeVideo;
        private Activity activity;
        private boolean fullScreen = false;
        private Rectangle bounds;
        private boolean nativeController = true;
        private boolean nativePlayer;
        private Form curentForm;

        public Video(VideoView nativeVideo, Activity activity, final Runnable onCompletion) {
            super(nativeVideo);
            this.nativeVideo = nativeVideo;
            this.activity = activity;
            if (nativeController) {
                MediaController mc = new MediaController(activity);
                nativeVideo.setMediaController(mc);
            }
            nativeVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                    if (onCompletion != null) {
                        onCompletion.run();
                    }
                }
            });

            nativeVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (onCompletion != null) {
                        onCompletion.run();
                    }
                    return false;
                }
            });
        }

        @Override
        public void play() {
            if (nativePlayer && curentForm == null) {
                curentForm = Display.getInstance().getCurrent();
                Form f = new Form();
                f.setLayout(new BorderLayout());
                f.addComponent(BorderLayout.CENTER, getVideoComponent());
                f.show();
            }
            nativeVideo.start();
        }

        @Override
        public void pause() {
            nativeVideo.stopPlayback();
        }

        @Override
        public void cleanup() {
            nativeVideo.stopPlayback();
            nativeVideo = null;
            if (nativePlayer && curentForm != null) {
                curentForm.showBack();
                curentForm = null;
            }
        }

        @Override
        public int getTime() {
            return nativeVideo.getCurrentPosition();
        }

        @Override
        public void setTime(int time) {
            nativeVideo.seekTo(time);
        }

        @Override
        public int getDuration() {
            return nativeVideo.getDuration();
        }

        @Override
        public void setVolume(int vol) {
            // float v = ((float) vol) / 100.0F;
            AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
        }

        @Override
        public int getVolume() {
            AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            return am.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public boolean isVideo() {
            return true;
        }

        @Override
        public boolean isFullScreen() {
            return fullScreen || nativePlayer;
        }

        @Override
        public void setFullScreen(boolean fullScreen) {
            this.fullScreen = fullScreen;
            if (fullScreen) {
                bounds = new Rectangle(getBounds());
                setX(0);
                setY(0);
                setWidth(Display.getInstance().getDisplayWidth());
                setHeight(Display.getInstance().getDisplayHeight());
            } else {
                if (bounds != null) {
                    setX(bounds.getX());
                    setY(bounds.getY());
                    setWidth(bounds.getSize().getWidth());
                    setHeight(bounds.getSize().getHeight());
                }
            }
            repaint();
        }

        @Override
        public Component getVideoComponent() {
            return this;
        }

        @Override
        protected Dimension calcPreferredSize() {
            return new Dimension(nativeVideo.getWidth(), nativeVideo.getHeight());
        }

        @Override
        public void setNativePlayerMode(boolean nativePlayer) {
            this.nativePlayer = nativePlayer;
        }

        @Override
        public boolean isNativePlayerMode() {
            return nativePlayer;
        }

        @Override
        public boolean isPlaying() {
            return nativeVideo.isPlaying();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE) {
                try {
                    String path = convertImageUriToFilePath(imageUri, activity);
                    Bitmap picture = BitmapFactory.decodeFile(path);
                    File f = getOutputMediaFile(false);
                    FileOutputStream os = new FileOutputStream(f);
                    picture.compress(Bitmap.CompressFormat.JPEG, 50, os);
                    os.close();
                    picture.recycle();
                    picture = null;
                    new File(path).delete();

                    intentResponse.actionPerformed(new ActionEvent(f.getAbsolutePath()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(requestCode == CAPTURE_VIDEO || requestCode == CAPTURE_AUDIO){
                Uri data = intent.getData();
                String path = convertImageUriToFilePath(data, activity);
                intentResponse.actionPerformed(new ActionEvent(path));            
            }
        }
    }

    public void capturePhoto(int type, ActionListener response) {
        intentResponse = response;
        
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        
        String fileName = "temp.jpg";  
        ContentValues values = new ContentValues();  
        values.put(MediaStore.Images.Media.TITLE, fileName);  
        imageUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  
                        
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);

        this.activity.startActivityForResult(intent, CAPTURE_IMAGE);
    }

    @Override
    public void captureVideo(ActionListener response) {
        intentResponse = response;
        Intent intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);                                
        this.activity.startActivityForResult(intent, CAPTURE_VIDEO);
    }

    @Override
    public void captureAudio(ActionListener response) {
        intentResponse = response;
        Intent intent = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        this.activity.startActivityForResult(intent, CAPTURE_AUDIO);
    }
     
    
    
    
    class NativeImage extends Image {

        public NativeImage(Bitmap nativeImage) {
            super(nativeImage);
        }
    }
    
    
    /** Create a File for saving an image or video */
    private File getOutputMediaFile(boolean isVideo) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        activity.getComponentName();
        
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "" + activity.getTitle());
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = null;
        if (!isVideo) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else{
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } 

        return mediaFile;
    }

    private static String convertImageUriToFilePath(Uri imageUri, Activity activity) {

        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = activity.managedQuery(imageUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
