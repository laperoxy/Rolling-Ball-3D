package com.example.projekat_2.Utils;

import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

public class Try extends Circle {

    public Try(double radius, Translate position) {

        super(radius, Color.RED);
        super.getTransforms().addAll(position);

    }
    
}
