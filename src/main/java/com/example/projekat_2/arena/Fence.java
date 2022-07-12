package com.example.projekat_2.arena;

import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

public class Fence extends Box {
    private String ident;

    public Fence(double width, double height, double depth, Material material, Translate position, String id) {
        super(width, height, depth);
        super.setMaterial(material);
        super.getTransforms().addAll(position);
        this.ident = id;
    }

    public boolean handleCollision(Sphere ball) {

        Bounds ballBounds = ball.getBoundsInParent();
        Bounds fenceBounds = super.getBoundsInParent();

        if (!ballBounds.intersects(fenceBounds))
            return false;

        Ball myBall = (Ball) ball;

        if (ident.equals("up/down"))
            myBall.setSpeed(new Point3D(myBall.getSpeed().getX(), 0, -myBall.getSpeed().getZ()));
        else
            myBall.setSpeed(new Point3D(-myBall.getSpeed().getX(), 0, myBall.getSpeed().getZ()));
        return true;
    }

}
