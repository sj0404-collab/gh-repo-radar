package com.sj0404.nimbusdeck;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

/** Responsive, dependency-free dashboard built from Android framework views. */
final class NimbusDashboard extends FrameLayout {
    interface Actions {
        void onPrimaryAction();
        void onLearnMore();
        void onRefresh();
        void onOpenAppSettings();
    }

    private static final int WHITE = Color.rgb(247, 250, 255);
    private static final int MUTED = Color.rgb(159, 174, 203);
    private static final int SUBTLE = Color.rgb(116, 132, 164);
    private static final int CYAN = Color.rgb(67, 229, 255);
    private static final int VIOLET = Color.rgb(138, 99, 255);
    private static final int GREEN = Color.rgb(74, 222, 160);
    private static final int RED = Color.rgb(255, 111, 126);

    private final Actions actions;
    private final boolean wide;
    private TextView heroEyebrow;
    private TextView heroTitle;
    private TextView heroBody;
    private TextView primaryButton;
    private StatusItem networkStatus;
    private StatusItem clientStatus;

    NimbusDashboard(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        this.wide = context.getResources().getConfiguration().screenWidthDp >= 700;
        setBackground(new DecorDrawables.AmbientBackground());
        setFocusable(false);
        build();
    }

    void render(LauncherState state) {
        heroEyebrow.setText(state.serviceInstalled
                ? R.string.hero_eyebrow_ready : R.string.hero_eyebrow_setup);
        heroTitle.setText(state.serviceInstalled
                ? R.string.hero_title_ready : R.string.hero_title_setup);
        heroBody.setText(state.serviceInstalled
                ? R.string.hero_body_ready : R.string.hero_body_setup);
        primaryButton.setText(state.serviceInstalled
                ? R.string.launch_service : R.string.install_service);

        networkStatus.setValue(state.online ? R.string.status_online : R.string.status_offline,
                state.online ? GREEN : RED);
        clientStatus.setValue(
                state.serviceInstalled ? R.string.status_installed : R.string.status_not_installed,
                state.serviceInstalled ? GREEN : VIOLET);
        clientStatus.root.setOnClickListener(state.serviceInstalled
                ? new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        actions.onOpenAppSettings();
                    }
                }
                : new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        actions.onPrimaryAction();
                    }
                });
    }

    private void build() {
        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setOverScrollMode(OVER_SCROLL_NEVER);
        addView(scroll, match());

        FrameLayout canvas = new FrameLayout(getContext());
        scroll.addView(canvas, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int side = wide ? dp(36) : dp(20);
        content.setPadding(side, dp(18), side, dp(32));
        int available = getResources().getDisplayMetrics().widthPixels - dp(24);
        int contentWidth = Math.min(available, dp(1120));
        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(
                contentWidth, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        canvas.addView(content, contentParams);

        content.addView(createHeader(), linearMatch(dp(68)));
        content.addView(space(18));
        content.addView(createHero(), linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT));
        content.addView(space(wide ? 38 : 30));
        content.addView(sectionLabel(R.string.section_status));
        content.addView(space(13));
        content.addView(createStatusArea(), linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT));
        content.addView(space(wide ? 28 : 18));
        content.addView(createDetailsArea(), linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT));
        content.addView(space(26));

        TextView footer = text(R.string.footer_disclaimer, 12, SUBTLE, Typeface.NORMAL);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(dp(8), dp(4), dp(8), dp(4));
        content.addView(footer, linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private View createHeader() {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        ImageView logo = new ImageView(getContext());
        logo.setImageResource(R.drawable.nimbusdeck_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setContentDescription(getResources().getString(R.string.logo_description));
        logo.setBackground(roundGradient(new int[]{Color.rgb(8, 18, 39), Color.rgb(13, 28, 57)},
                16, Color.argb(100, 79, 210, 255)));
        logo.setClipToOutline(true);
        row.addView(logo, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout names = new LinearLayout(getContext());
        names.setOrientation(LinearLayout.VERTICAL);
        names.setGravity(Gravity.CENTER_VERTICAL);
        names.setPadding(dp(13), 0, 0, 0);
        TextView appName = text(R.string.app_name, wide ? 21 : 19, WHITE, Typeface.BOLD);
        appName.setLetterSpacing(0.015f);
        names.addView(appName);
        TextView tag = text(R.string.brand_tag, 10, CYAN, Typeface.BOLD);
        tag.setLetterSpacing(0.17f);
        names.addView(tag);
        row.addView(names, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        ImageButton refresh = new ImageButton(getContext());
        refresh.setImageDrawable(new DecorDrawables.LineIcon(
                DecorDrawables.LineIcon.REFRESH, MUTED));
        refresh.setScaleType(ImageView.ScaleType.CENTER);
        refresh.setPadding(dp(14), dp(14), dp(14), dp(14));
        refresh.setBackground(focusableCard(18));
        refresh.setContentDescription(getResources().getString(R.string.refresh_description));
        refresh.setFocusable(true);
        refresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onRefresh();
            }
        });
        row.addView(refresh, new LinearLayout.LayoutParams(dp(52), dp(52)));
        return row;
    }

    private View createHero() {
        FrameLayout card = new FrameLayout(getContext());
        card.setBackground(new DecorDrawables.HeroBackground());
        card.setPadding(wide ? dp(40) : dp(25), wide ? dp(38) : dp(28),
                wide ? dp(40) : dp(25), wide ? dp(38) : dp(28));
        card.setElevation(dp(12));

        LinearLayout column = new LinearLayout(getContext());
        column.setOrientation(LinearLayout.VERTICAL);
        card.addView(column, match());

        heroEyebrow = pillText(R.string.hero_eyebrow_setup, CYAN);
        LinearLayout.LayoutParams pillParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(30));
        column.addView(heroEyebrow, pillParams);
        column.addView(space(wide ? 24 : 19));

        heroTitle = text(R.string.hero_title_setup, wide ? 42 : 31, WHITE, Typeface.BOLD);
        heroTitle.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        heroTitle.setLetterSpacing(-0.025f);
        heroTitle.setLineSpacing(0f, 0.96f);
        heroTitle.setMaxWidth(dp(700));
        column.addView(heroTitle, linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT));
        column.addView(space(13));

        heroBody = text(R.string.hero_body_setup, wide ? 17 : 15, MUTED, Typeface.NORMAL);
        heroBody.setLineSpacing(dp(3), 1f);
        heroBody.setMaxWidth(dp(660));
        column.addView(heroBody, linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT));
        column.addView(space(wide ? 30 : 24));

        primaryButton = text(R.string.install_service, 16, WHITE, Typeface.BOLD);
        primaryButton.setGravity(Gravity.CENTER);
        primaryButton.setPadding(dp(24), 0, dp(24), 0);
        primaryButton.setMinWidth(dp(238));
        primaryButton.setBackground(primaryButtonBackground());
        primaryButton.setClickable(true);
        primaryButton.setFocusable(true);
        primaryButton.setElevation(dp(8));
        primaryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onPrimaryAction();
            }
        });
        primaryButton.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                view.animate().scaleX(focused ? 1.035f : 1f)
                        .scaleY(focused ? 1.035f : 1f).setDuration(140).start();
            }
        });
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                wide ? dp(270) : ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        column.addView(primaryButton, actionParams);
        return card;
    }

    private View createStatusArea() {
        LinearLayout statusArea = new LinearLayout(getContext());
        statusArea.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        networkStatus = createStatusItem(DecorDrawables.LineIcon.NETWORK,
                R.string.status_network, R.string.status_offline, RED);
        clientStatus = createStatusItem(DecorDrawables.LineIcon.APP,
                R.string.status_client, R.string.status_not_installed, VIOLET);
        StatusItem accountStatus = createStatusItem(DecorDrawables.LineIcon.ACCOUNT,
                R.string.status_account, R.string.status_account_required, CYAN);

        addStatus(statusArea, networkStatus.root, 0);
        addStatus(statusArea, clientStatus.root, 1);
        addStatus(statusArea, accountStatus.root, 2);
        return statusArea;
    }

    private void addStatus(LinearLayout parent, View item, int index) {
        LinearLayout.LayoutParams params;
        if (wide) {
            params = new LinearLayout.LayoutParams(0, dp(132), 1f);
            if (index > 0) {
                params.setMarginStart(dp(14));
            }
        } else {
            params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(94));
            if (index > 0) {
                params.topMargin = dp(10);
            }
        }
        parent.addView(item, params);
    }

    private StatusItem createStatusItem(int iconType, int titleResource,
                                        int valueResource, int accent) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(wide ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        card.setGravity(wide ? Gravity.START : Gravity.CENTER_VERTICAL);
        int inset = wide ? dp(20) : dp(17);
        card.setPadding(inset, inset, inset, inset);
        card.setBackground(focusableCard(22));
        card.setFocusable(true);

        FrameLayout iconBubble = new FrameLayout(getContext());
        GradientDrawable bubbleBackground = new GradientDrawable();
        bubbleBackground.setShape(GradientDrawable.OVAL);
        bubbleBackground.setColor(Color.argb(28, Color.red(accent),
                Color.green(accent), Color.blue(accent)));
        bubbleBackground.setStroke(dp(1), Color.argb(65, Color.red(accent),
                Color.green(accent), Color.blue(accent)));
        iconBubble.setBackground(bubbleBackground);
        ImageView icon = new ImageView(getContext());
        icon.setImageDrawable(new DecorDrawables.LineIcon(iconType, accent));
        icon.setPadding(dp(11), dp(11), dp(11), dp(11));
        iconBubble.addView(icon, match());
        card.addView(iconBubble, new LinearLayout.LayoutParams(dp(46), dp(46)));

        LinearLayout copy = new LinearLayout(getContext());
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setGravity(Gravity.CENTER_VERTICAL);
        if (wide) {
            copy.setPadding(0, dp(10), 0, 0);
        } else {
            copy.setPadding(dp(15), 0, 0, 0);
        }
        TextView title = text(titleResource, 12, SUBTLE, Typeface.BOLD);
        title.setLetterSpacing(0.06f);
        copy.addView(title);
        TextView value = text(valueResource, wide ? 15 : 16, accent, Typeface.BOLD);
        value.setPadding(0, dp(2), 0, 0);
        copy.addView(value);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                wide ? ViewGroup.LayoutParams.MATCH_PARENT : 0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                wide ? 0f : 1f);
        card.addView(copy, copyParams);
        return new StatusItem(card, value);
    }

    private View createDetailsArea() {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        View account = createDetailCard(DecorDrawables.LineIcon.ACCOUNT, CYAN,
                R.string.account_title, R.string.account_body,
                R.string.account_action, new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        actions.onLearnMore();
                    }
                });
        View privacy = createDetailCard(DecorDrawables.LineIcon.SHIELD, VIOLET,
                R.string.privacy_title, R.string.privacy_body, 0, null);

        if (wide) {
            LinearLayout.LayoutParams first = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1.15f);
            LinearLayout.LayoutParams second = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 0.85f);
            second.setMarginStart(dp(14));
            row.addView(account, first);
            row.addView(privacy, second);
        } else {
            row.addView(account, linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams privacyParams = linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT);
            privacyParams.topMargin = dp(10);
            row.addView(privacy, privacyParams);
        }
        return row;
    }

    private View createDetailCard(int iconType, int accent, int titleResource,
                                  int bodyResource, int actionResource,
                                  OnClickListener actionListener) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(22), dp(22), dp(22), dp(22));
        card.setBackground(cardBackground(24));

        ImageView icon = new ImageView(getContext());
        icon.setImageDrawable(new DecorDrawables.LineIcon(iconType, accent));
        icon.setPadding(dp(10), dp(10), dp(10), dp(10));
        GradientDrawable bubble = new GradientDrawable();
        bubble.setShape(GradientDrawable.OVAL);
        bubble.setColor(Color.argb(28, Color.red(accent), Color.green(accent), Color.blue(accent)));
        icon.setBackground(bubble);
        card.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));
        card.addView(space(15));

        TextView title = text(titleResource, 19, WHITE, Typeface.BOLD);
        card.addView(title, linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(space(8));
        TextView body = text(bodyResource, 14, MUTED, Typeface.NORMAL);
        body.setLineSpacing(dp(3), 1f);
        card.addView(body, linearMatch(ViewGroup.LayoutParams.WRAP_CONTENT));

        if (actionResource != 0) {
            card.addView(space(16));
            TextView action = text(actionResource, 14, CYAN, Typeface.BOLD);
            action.setCompoundDrawablePadding(dp(6));
            action.setPadding(dp(1), dp(8), dp(8), dp(8));
            action.setClickable(true);
            action.setFocusable(true);
            action.setBackground(transparentFocusBackground(12));
            action.setOnClickListener(actionListener);
            card.addView(action, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        return card;
    }

    private TextView sectionLabel(int resource) {
        TextView label = text(resource, 11, SUBTLE, Typeface.BOLD);
        label.setLetterSpacing(0.17f);
        return label;
    }

    private TextView pillText(int resource, int accent) {
        TextView pill = text(resource, 10, accent, Typeface.BOLD);
        pill.setGravity(Gravity.CENTER);
        pill.setLetterSpacing(0.12f);
        pill.setPadding(dp(13), 0, dp(13), 0);
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(15));
        background.setColor(Color.argb(28, Color.red(accent), Color.green(accent), Color.blue(accent)));
        background.setStroke(dp(1), Color.argb(80, Color.red(accent), Color.green(accent), Color.blue(accent)));
        pill.setBackground(background);
        return pill;
    }

    private TextView text(int resource, float size, int color, int style) {
        TextView text = new TextView(getContext());
        text.setText(resource);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        text.setTextColor(color);
        text.setTypeface(Typeface.create("sans-serif", style));
        text.setIncludeFontPadding(false);
        return text;
    }

    private Drawable cardBackground(float radius) {
        return roundGradient(new int[]{Color.argb(245, 12, 22, 43),
                        Color.argb(245, 9, 17, 35)},
                radius, Color.argb(55, 114, 144, 195));
    }

    private Drawable focusableCard(float radius) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused},
                roundGradient(new int[]{Color.rgb(20, 43, 77), Color.rgb(15, 31, 58)},
                        radius, Color.argb(210, 67, 229, 255)));
        states.addState(new int[]{android.R.attr.state_pressed},
                roundGradient(new int[]{Color.rgb(18, 37, 67), Color.rgb(12, 27, 52)},
                        radius, Color.argb(150, 67, 229, 255)));
        states.addState(new int[]{}, cardBackground(radius));
        return states;
    }

    private Drawable primaryButtonBackground() {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},
                roundGradient(new int[]{Color.rgb(48, 153, 255), Color.rgb(101, 102, 255)},
                        18, Color.argb(220, 144, 239, 255)));
        states.addState(new int[]{android.R.attr.state_focused},
                roundGradient(new int[]{Color.rgb(54, 171, 255), Color.rgb(107, 112, 255)},
                        18, WHITE));
        states.addState(new int[]{},
                roundGradient(new int[]{Color.rgb(27, 143, 255), Color.rgb(82, 82, 241)},
                        18, Color.argb(155, 111, 221, 255)));
        return states;
    }

    private Drawable transparentFocusBackground(float radius) {
        StateListDrawable states = new StateListDrawable();
        GradientDrawable focus = new GradientDrawable();
        focus.setCornerRadius(dp(radius));
        focus.setColor(Color.argb(32, 67, 229, 255));
        focus.setStroke(dp(1), Color.argb(100, 67, 229, 255));
        states.addState(new int[]{android.R.attr.state_focused}, focus);
        GradientDrawable clear = new GradientDrawable();
        clear.setColor(Color.TRANSPARENT);
        states.addState(new int[]{}, clear);
        return states;
    }

    private GradientDrawable roundGradient(int[] colors, float radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, colors);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private Space space(int height) {
        Space space = new Space(getContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(1, dp(height)));
        return space;
    }

    private LayoutParams match() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private LinearLayout.LayoutParams linearMatch(int height) {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
    }

    private int dp(float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, getResources().getDisplayMetrics()));
    }

    private static final class StatusItem {
        final LinearLayout root;
        final TextView value;

        StatusItem(LinearLayout root, TextView value) {
            this.root = root;
            this.value = value;
        }

        void setValue(int resource, int color) {
            value.setText(resource);
            value.setTextColor(color);
        }
    }
}
