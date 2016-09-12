package com.javon.playservicesdemo.textdetection;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.javon.playservicesdemo.util.GraphicOverlay;

/**
 * @author Javon Davis
 *         Created by Javon Davis on 12/09/2016.
 */

public class TextDetectionProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> graphicOverlay;

    public TextDetectionProcessor(GraphicOverlay<OcrGraphic> graphicOverlay) {
        this.graphicOverlay = graphicOverlay;
    }

    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        graphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                Log.d("Processor", "Text detected! " + item.getValue());
            }
            OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
            graphicOverlay.add(graphic);
        }
    }
}
