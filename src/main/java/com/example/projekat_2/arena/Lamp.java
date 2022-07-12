package com.example.projekat_2.arena;

import javafx.scene.PointLight;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;

public class Lamp extends Box {

    private boolean lightOn;

    public Lamp(double width, double height, double depth, Material material, Translate pos) {
        super(width, height, depth);
        super.setMaterial(material);
        super.getTransforms().addAll(pos);
        lightOn = false;
    }

    public void light_on() {
        lightOn = true;
        Image light_pattern = new Image("selfIllumination.png");
        PhongMaterial material = new PhongMaterial();
        material.setSelfIlluminationMap(light_pattern);
        super.setMaterial(material);
    }

    public void light_off() {
        lightOn = false;
        PhongMaterial material = new PhongMaterial(Color.GRAY);
        super.setMaterial(material);
    }

    public boolean isLightOn() {
        return lightOn;
    }
}
