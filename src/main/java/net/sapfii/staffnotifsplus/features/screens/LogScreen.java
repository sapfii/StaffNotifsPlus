package net.sapfii.staffnotifsplus.features.screens;

import dev.dfonline.flint.Flint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.sapfii.staffnotifsplus.features.screens.widgets.LogScroll;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LogScreen extends Screen{
    boolean clickedRefresh = false;


    private LogScroll logScroll;

    public TextFieldWidget filter;
    private String defaultFilter = "";

    public TextFieldWidget plotFilter;
    private String defaultPlotFilter = "";

    public TextFieldWidget duration;
    private String defaultDuration = "";

    public CyclingButtonWidget<String> durationUnits;
    private String defaultDurationUnit = "m";

    public TextFieldWidget secondDuration;
    private String defaultSecondDuration = "";

    public CyclingButtonWidget<String> secondDurationUnits;
    private String defaultSecondDurationUnit = "m";

    private ButtonWidget refreshButton;

    String logType;
    public LogScreen(Text title, String log) {
        super(title);
        logType = log;
    }

    public LogScreen(Text title, String log, String filter, String plotFilter, String duration, String durationUnits, String secondDuration, String secondDurationUnits) {
        super(title);
        logType = log;
        defaultFilter = filter;
        defaultPlotFilter = plotFilter;
        defaultDuration = duration;
        defaultDurationUnit = durationUnits;
        defaultSecondDuration = secondDuration;
        defaultSecondDurationUnit = secondDurationUnits;
    }

    List<Text> lines = new ArrayList<>();
    public void addMsg(Text text) {
        logScroll.addLine(text);
        addSelectableChild(logScroll);
        lines.add(text);
    }

    public void refresh() {
        this.logScroll = new LogScroll(client, width, height-90, 50, 10);
        addSelectableChild(logScroll);
        for (Text text : lines) {
            logScroll.addLine(text);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        refresh();
    }

    @Override
    protected void init() {
        logScroll = new LogScroll(
                client,
                width, height-90,
                50,
                10
        );

        filter = new TextFieldWidget(
                this.textRenderer,
                Flint.getClient().getWindow().getScaledWidth()/2+60, 25,
                50, 18,
                Text.literal("Filter")
        );
        addSelectableChild(filter);
        filter.setText(defaultFilter);

        plotFilter = new TextFieldWidget(
                this.textRenderer,
                Flint.getClient().getWindow().getScaledWidth()/2+60, 5,
                50, 18,
                Text.literal("Filter")
        );
        plotFilter.setTextPredicate(text -> text.matches("\\d*"));
        addSelectableChild(plotFilter);
        plotFilter.setText(defaultPlotFilter);


        duration = new TextFieldWidget(
                this.textRenderer,
                Flint.getClient().getWindow().getScaledWidth()/2+120, 25,
                25, 18,
                Text.literal("Duration")
        );
        duration.setTextPredicate(text -> text.matches("\\d*"));
        addSelectableChild(duration);
        duration.setText(defaultDuration);

        List<String> units = List.of("s", "m", "h", "d");
        durationUnits = CyclingButtonWidget.builder(Text::literal)
                .values(units)
                .initially(defaultDurationUnit)
                .build(
                        Flint.getClient().getWindow().getScaledWidth()/2+150, 25, 18, 18,
                        Text.empty(),
                        (button, value) -> {
                            durationUnits.setMessage(Text.literal(value));
                        }
                );
        durationUnits.setMessage(Text.literal(durationUnits.getValue()));
        addSelectableChild(durationUnits);


        secondDuration = new TextFieldWidget(
                this.textRenderer,
                Flint.getClient().getWindow().getScaledWidth()/2+173, 25,
                25, 18,
                Text.literal("Duration")
        );
        secondDuration.setTextPredicate(text -> text.matches("\\d*"));
        addSelectableChild(secondDuration);
        secondDuration.setText(defaultSecondDuration);

        secondDurationUnits = CyclingButtonWidget.builder(Text::literal)
                .values(units)
                .initially(defaultSecondDurationUnit)
                .build(
                        Flint.getClient().getWindow().getScaledWidth()/2+203, 25, 18, 18,
                        Text.empty(),
                        (button, value) -> {
                            secondDurationUnits.setMessage(Text.literal(value));
                        }
                );
        secondDurationUnits.setMessage(Text.literal(secondDurationUnits.getValue()));
        addSelectableChild(secondDurationUnits);


        refreshButton = new ButtonWidget(
                Flint.getClient().getWindow().getScaledWidth()/2+226, 25,
                18, 18,
                Text.empty(),
                button -> {
                    clickedRefresh = true;
                },
                button -> Text.empty()
        ) {
            @Override
            protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

                Identifier REFRESH_ICON = Identifier.of("staffnotifsplus", "textures/gui/refresh.png");
                super.renderWidget(context, mouseX, mouseY, delta);
                context.drawTexture(
                        RenderLayer::getGuiTextured,
                        REFRESH_ICON,
                        this.getX()+1, this.getY()+1,
                        0, 0,
                        16, 16,
                        16, 16);
            }
        };
        addDrawableChild(refreshButton);

        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (clickedRefresh) {
            String type = "mod";
            if (logType.startsWith("Admin")) {
                type = "admin";
            }
            String dur = duration.getText();
            String units = "";
            if (!duration.getText().isEmpty()) {
                units = durationUnits.getValue();
            }
            String secondDur = secondDuration.getText();
            String secondUnits = "";
            if (!secondDuration.getText().isEmpty()) {
                secondUnits = secondDurationUnits.getValue();
            }
            String command = type + " log " + dur + units + " " + secondDur + secondUnits + " " + filter.getText();
            Objects.requireNonNull(Flint.getClient().getNetworkHandler()).sendCommand(command);
            clickedRefresh = false;
        }
        super.render(context, mouseX, mouseY, delta);

        Window window = Flint.getClient().getWindow();

        filter.render(context, mouseX, mouseY, delta);
        plotFilter.render(context, mouseX, mouseY, delta);
        duration.render(context, mouseX, mouseY, delta);
        durationUnits.render(context, mouseX, mouseY, delta);
        secondDuration.render(context, mouseX, mouseY, delta);
        secondDurationUnits.render(context, mouseX, mouseY, delta);
        logScroll.render(context, mouseX, mouseY, delta);
        refreshButton.render(context, mouseX, mouseY, delta);

        int titleX = window.getScaledWidth()/2-this.textRenderer.getWidth(logType)/2;
        context.drawText(this.textRenderer, logType, titleX, 30, 0xFFFFFFFF, true);

    }
}
