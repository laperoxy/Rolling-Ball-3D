package com.example.projekat_2.arena;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.paint.Material;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.ArrayList;

public class Token extends Cylinder {

    public boolean hit;

    public Token(double radius, double height, Material material, Translate position, Rotate rotate) {
        super(radius, height);

        super.setMaterial(material);

        Rotate rotateY = new Rotate();
        Translate translateZ = new Translate();

        super.getTransforms().addAll(rotate, position, rotateY, translateZ);

        Timeline timelineRotate = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotateY.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(4), new KeyValue(rotateY.angleProperty(), 360))
        );
        timelineRotate.setCycleCount(Animation.INDEFINITE);
        timelineRotate.play();

        Timeline timelineMove = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(translateZ.zProperty(), -30)),
                new KeyFrame(Duration.seconds(4), new KeyValue(translateZ.zProperty(), 20))
        );
        timelineMove.setAutoReverse(true);
        timelineMove.setCycleCount(Animation.INDEFINITE);
        timelineMove.play();
        hit = false;
    }

    public boolean handleCollision(Sphere ball) {

        Bounds ballBounds = ball.getBoundsInParent();
        Bounds tokenBounds = super.getBoundsInParent();

        boolean getToken = ballBounds.intersects(tokenBounds);

        if (getToken)
            hit = true;

        return getToken;

    }

}
