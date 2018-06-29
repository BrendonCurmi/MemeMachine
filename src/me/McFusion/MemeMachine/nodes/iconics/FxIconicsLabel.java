package me.McFusion.MemeMachine.nodes.iconics;

import com.pepperonas.fxiconics.FxIconics;
import com.pepperonas.fxiconics.awf.FxFontAwesome;
import com.pepperonas.fxiconics.base.BuilderControl;
import com.pepperonas.fxiconics.cmd.FxFontCommunity;
import com.pepperonas.fxiconics.gmd.FxFontGoogleMaterial;
import com.pepperonas.fxiconics.met.FxFontMeteoconcs;
import com.pepperonas.fxiconics.oct.FxFontOcticons;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Class to override {@link com.pepperonas.fxiconics.FxIconicsLabel} due to
 * it automatically colouring the text.
 */
public class FxIconicsLabel extends Label {

    public FxIconicsLabel(BuilderControl builder) {
        Font font = null;
        if (builder.collection instanceof FxFontGoogleMaterial) font = FxIconics.getGoogleMaterialFont(builder.size);
        if (builder.collection instanceof FxFontCommunity) font = FxIconics.getCommunityMaterialFont(builder.size);
        if (builder.collection instanceof FxFontAwesome) font = FxIconics.getAwesomeFont(builder.size);
        if (builder.collection instanceof FxFontOcticons) font = FxIconics.getOcticonsFont(builder.size);
        if (builder.collection instanceof FxFontMeteoconcs) font = FxIconics.getMeteoconsFont(builder.size);

        this.setText(builder.icon);
        this.setFont(font);
        if (builder.text != null && !builder.text.isEmpty()) {
            Text text = new Text(builder.text);
            text.setStyle("-fx-font-size: " + builder.textSize);
            this.setText(builder.icon);
            this.setGraphic(text);
            this.setContentDisplay(builder.contentDisplay);
        }
        //this.setStyle("-fx-text-fill: " + ColorConverter.toRgbString(builder.color) + ""); To stop labels from defaulting to black
    }

    public static class Builder extends BuilderControl {
        public Builder(FxFontGoogleMaterial.Icons icon) {
            super(icon);
        }

        public Builder(FxFontCommunity.Icons icon) {
            super(icon);
        }

        public Builder(FxFontAwesome.Icons icon) {
            super(icon);
        }

        public Builder(FxFontOcticons.Icons icon) {
            super(icon);
        }

        public Builder(FxFontMeteoconcs.Icons icon) {
            super(icon);
        }

        public FxIconicsLabel build() {
            return new FxIconicsLabel(this);
        }
    }
}
