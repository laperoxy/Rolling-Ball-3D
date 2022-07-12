package com.example.projekat_2.arena;

import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

public class Obstacle extends Cylinder {

    public Obstacle(double radius, double height, Material material, Translate position) {
        super(radius, height);
        super.setMaterial(material);
        super.getTransforms().addAll(position);
    }

    public boolean changeMat(PhongMaterial newMat) {
        super.setMaterial(newMat);
        return true;
    }

    public boolean handleCollision(Sphere ball) {
        Bounds ballBounds = ball.getBoundsInParent();

        double ballX = ballBounds.getCenterX();
        double ballZ = ballBounds.getCenterZ();

        Bounds obstacleBounds = super.getBoundsInParent();
        double obsX = obstacleBounds.getCenterX();
        double obsZ = obstacleBounds.getCenterZ();
        double obstacleRadius = super.getRadius();

        double dx = obsX - ballX;
        double dz = obsZ - ballZ;

        double distance = dx * dx + dz * dz;


        boolean hitObstacle = obstacleBounds.intersects(ballBounds);

        Ball myBall = (Ball) ball;
        double x = myBall.getSpeed().getX();
        double y = myBall.getSpeed().getZ();
        if (hitObstacle) {
            myBall.setSpeed(new Point3D(-x, 0, -y));
        }


        return hitObstacle;
    }

}
