package com.sj0404.nimbusdeck;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Custom lightweight decoration and icons; no external UI dependency required. */
final class DecorDrawables {
    private DecorDrawables() {
    }

    static final class AmbientBackground extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float width = bounds.width();
            float height = bounds.height();
            paint.setShader(new LinearGradient(0, 0, width, height,
                    new int[]{Color.rgb(8, 16, 35), Color.rgb(5, 10, 24), Color.rgb(3, 6, 17)},
                    new float[]{0f, 0.48f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(bounds, paint);

            paint.setShader(new RadialGradient(width * 0.91f, height * 0.08f,
                    Math.max(width, height) * 0.52f,
                    new int[]{Color.argb(82, 55, 73, 189), Color.argb(0, 26, 43, 120)},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(bounds, paint);

            paint.setShader(new RadialGradient(width * 0.04f, height * 0.52f,
                    Math.max(width, height) * 0.43f,
                    new int[]{Color.argb(48, 0, 172, 219), Color.argb(0, 0, 83, 144)},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(bounds, paint);
            paint.setShader(null);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }

    static final class HeroBackground extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path clip = new Path();
        private final RectF rect = new RectF();

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            rect.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
            float radius = Math.min(bounds.width(), bounds.height()) * 0.075f;
            radius = Math.max(26f, Math.min(radius, 46f));
            clip.reset();
            clip.addRoundRect(rect, radius, radius, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(clip);

            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(bounds.left, bounds.top, bounds.right, bounds.bottom,
                    new int[]{Color.rgb(15, 49, 91), Color.rgb(25, 34, 82), Color.rgb(10, 22, 52)},
                    new float[]{0f, 0.52f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(rect, paint);

            float glowRadius = Math.max(bounds.width() * 0.5f, bounds.height() * 1.1f);
            paint.setShader(new RadialGradient(bounds.right * 0.91f, bounds.top + bounds.height() * 0.12f,
                    glowRadius,
                    new int[]{Color.argb(130, 77, 73, 255), Color.argb(0, 47, 47, 160)},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(rect, paint);

            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(Color.argb(30, 125, 225, 255));
            float spacing = Math.max(42f, bounds.width() / 13f);
            for (float x = bounds.left - bounds.height(); x < bounds.right; x += spacing) {
                canvas.drawLine(x, bounds.bottom, x + bounds.height(), bounds.top, paint);
            }

            paint.setStrokeWidth(Math.max(10f, bounds.width() * 0.018f));
            paint.setColor(Color.argb(26, 83, 220, 255));
            float cx = bounds.right - bounds.width() * 0.08f;
            float cy = bounds.top + bounds.height() * 0.22f;
            for (int index = 0; index < 3; index++) {
                float ring = bounds.width() * (0.14f + index * 0.09f);
                canvas.drawCircle(cx, cy, ring, paint);
            }
            canvas.restoreToCount(save);

            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(Color.argb(90, 107, 206, 255));
            canvas.drawRoundRect(rect.left + 1f, rect.top + 1f, rect.right - 1f, rect.bottom - 1f,
                    radius, radius, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    static final class LineIcon extends Drawable {
        static final int REFRESH = 0;
        static final int NETWORK = 1;
        static final int APP = 2;
        static final int ACCOUNT = 3;
        static final int SHIELD = 4;

        private final int type;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF oval = new RectF();

        LineIcon(int type, int color) {
            this.type = type;
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.1f);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float size = Math.min(bounds.width(), bounds.height());
            float scale = size / 24f;
            float left = bounds.centerX() - size / 2f;
            float top = bounds.centerY() - size / 2f;
            int save = canvas.save();
            canvas.translate(left, top);
            canvas.scale(scale, scale);
            paint.setStrokeWidth(1.85f);
            paint.setStyle(Paint.Style.STROKE);
            path.reset();
            switch (type) {
                case REFRESH:
                    drawRefresh(canvas);
                    break;
                case NETWORK:
                    drawNetwork(canvas);
                    break;
                case APP:
                    drawApp(canvas);
                    break;
                case ACCOUNT:
                    drawAccount(canvas);
                    break;
                case SHIELD:
                    drawShield(canvas);
                    break;
                default:
                    break;
            }
            canvas.restoreToCount(save);
        }

        private void drawRefresh(Canvas canvas) {
            oval.set(4.2f, 4.2f, 19.8f, 19.8f);
            canvas.drawArc(oval, 205f, 245f, false, paint);
            path.moveTo(4.1f, 7.1f);
            path.lineTo(4.3f, 11.1f);
            path.lineTo(8.2f, 10.4f);
            canvas.drawPath(path, paint);
        }

        private void drawNetwork(Canvas canvas) {
            oval.set(3f, 6f, 21f, 24f);
            canvas.drawArc(oval, 215f, 110f, false, paint);
            oval.set(6.5f, 9.5f, 17.5f, 20.5f);
            canvas.drawArc(oval, 215f, 110f, false, paint);
            oval.set(10.8f, 14.2f, 13.2f, 16.6f);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawOval(oval, paint);
        }

        private void drawApp(Canvas canvas) {
            canvas.drawRoundRect(4f, 4f, 20f, 20f, 3.4f, 3.4f, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(7.2f, 7.2f, 10.5f, 10.5f, 0.8f, 0.8f, paint);
            canvas.drawRoundRect(13.5f, 7.2f, 16.8f, 10.5f, 0.8f, 0.8f, paint);
            canvas.drawRoundRect(7.2f, 13.5f, 10.5f, 16.8f, 0.8f, 0.8f, paint);
            canvas.drawRoundRect(13.5f, 13.5f, 16.8f, 16.8f, 0.8f, 0.8f, paint);
        }

        private void drawAccount(Canvas canvas) {
            canvas.drawCircle(12f, 8.2f, 3.3f, paint);
            path.moveTo(5.4f, 19.2f);
            path.cubicTo(6.2f, 15.4f, 8.4f, 13.5f, 12f, 13.5f);
            path.cubicTo(15.6f, 13.5f, 17.8f, 15.4f, 18.6f, 19.2f);
            canvas.drawPath(path, paint);
        }

        private void drawShield(Canvas canvas) {
            path.moveTo(12f, 3.5f);
            path.cubicTo(14.5f, 5.1f, 16.9f, 5.7f, 19f, 6.2f);
            path.lineTo(19f, 11.3f);
            path.cubicTo(19f, 15.9f, 16.1f, 19f, 12f, 20.6f);
            path.cubicTo(7.9f, 19f, 5f, 15.9f, 5f, 11.3f);
            path.lineTo(5f, 6.2f);
            path.cubicTo(7.1f, 5.7f, 9.5f, 5.1f, 12f, 3.5f);
            canvas.drawPath(path, paint);
            path.reset();
            path.moveTo(8.8f, 11.8f);
            path.lineTo(11.1f, 14f);
            path.lineTo(15.5f, 9.6f);
            canvas.drawPath(path, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return 24;
        }

        @Override
        public int getIntrinsicHeight() {
            return 24;
        }
    }
}
